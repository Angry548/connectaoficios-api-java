package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.promocion.PromocionGuardar;
import com.connectaoficios.api.dtos.promocion.PromocionResumenSalida;
import com.connectaoficios.api.dtos.promocion.PromocionSalida;
import com.connectaoficios.api.enums.EstadoPromocion;
import com.connectaoficios.api.enums.EstadoServicio;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PerfilTrabajador;
import com.connectaoficios.api.modelos.PlanPromocion;
import com.connectaoficios.api.modelos.Promocion;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.repositorios.IPlanPromocionRepository;
import com.connectaoficios.api.repositorios.IPromocionRepository;
import com.connectaoficios.api.repositorios.IServicioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromocionServiceTest {

    @Mock
    private IPromocionRepository promocionRepository;

    @Mock
    private IPlanPromocionRepository planPromocionRepository;

    @Mock
    private IServicioRepository servicioRepository;

    private PromocionService promocionService;

    private Servicio servicio;
    private PlanPromocion plan;
    private Promocion promocion;
    private PerfilTrabajador perfil;

    private static final Integer TRABAJADOR_ID = 10;

    @BeforeEach
    void setUp() {

        promocionService = new PromocionService(
                promocionRepository,
                planPromocionRepository,
                servicioRepository
        );

        perfil = new PerfilTrabajador();
        perfil.setId(1L);
        perfil.setTrabajadorId(TRABAJADOR_ID);

        servicio = new Servicio();
        servicio.setId(1L);
        servicio.setTitulo("Reparación eléctrica");
        servicio.setEstado(EstadoServicio.ACTIVO);
        servicio.setPerfilTrabajador(perfil);

        plan = new PlanPromocion();
        plan.setId(1L);
        plan.setNombre("Plan Básico");
        plan.setDuracionDias(7);
        plan.setPrecio(new BigDecimal("5.00"));
        plan.setActivo(true);

        promocion = new Promocion();
        promocion.setId(1L);
        promocion.setServicio(servicio);
        promocion.setPlan(plan);
        promocion.setTrabajadorId(TRABAJADOR_ID);
        promocion.setEstado(EstadoPromocion.PENDIENTE);
        promocion.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void obtenerResumen_debeRetornarServicioPlanYCostoTotal() {

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(planPromocionRepository.findById(1L)).thenReturn(Optional.of(plan));

        PromocionResumenSalida resumen = promocionService.obtenerResumen(1L, 1L);

        assertEquals("Reparación eléctrica", resumen.getServicioTitulo());
        assertEquals("Plan Básico", resumen.getPlanNombre());
        assertEquals(7, resumen.getDuracionDias());
        assertEquals(new BigDecimal("5.00"), resumen.getCostoTotal());
    }

    @Test
    void guardar_debeCrearPromocionPendienteCuandoTodoEsValido() {

        PromocionGuardar dto = new PromocionGuardar();
        dto.setServicioId(1L);
        dto.setPlanId(1L);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(planPromocionRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(promocionRepository.existsByServicio_IdAndEstadoIn(eq(1L), anyList())).thenReturn(false);
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(invocation -> {
            Promocion guardada = invocation.getArgument(0);
            guardada.setId(1L);
            return guardada;
        });

        PromocionSalida resultado = promocionService.guardar(dto, TRABAJADOR_ID);

        assertEquals(EstadoPromocion.PENDIENTE, resultado.getEstado());
        assertEquals(TRABAJADOR_ID, resultado.getTrabajadorId());
        verify(promocionRepository, times(1)).save(any(Promocion.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoServicioNoPerteneceAlTrabajador() {

        PromocionGuardar dto = new PromocionGuardar();
        dto.setServicioId(1L);
        dto.setPlanId(1L);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        assertThrows(
                ReglaNegocioException.class,
                () -> promocionService.guardar(dto, 999)
        );

        verify(promocionRepository, never()).save(any(Promocion.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoServicioNoEstaActivo() {

        servicio.setEstado(EstadoServicio.INACTIVO);

        PromocionGuardar dto = new PromocionGuardar();
        dto.setServicioId(1L);
        dto.setPlanId(1L);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        assertThrows(
                ReglaNegocioException.class,
                () -> promocionService.guardar(dto, TRABAJADOR_ID)
        );

        verify(promocionRepository, never()).save(any(Promocion.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoYaExistePromocionActivaOPendiente() {

        PromocionGuardar dto = new PromocionGuardar();
        dto.setServicioId(1L);
        dto.setPlanId(1L);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(planPromocionRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(promocionRepository.existsByServicio_IdAndEstadoIn(eq(1L), anyList())).thenReturn(true);

        assertThrows(
                ReglaNegocioException.class,
                () -> promocionService.guardar(dto, TRABAJADOR_ID)
        );

        verify(promocionRepository, never()).save(any(Promocion.class));
    }

    @Test
    void activar_debeEstablecerFechasYCambiarEstadoAActiva() {

        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));
        when(promocionRepository.save(promocion)).thenReturn(promocion);

        PromocionSalida resultado = promocionService.activar(1L);

        assertEquals(EstadoPromocion.ACTIVA, resultado.getEstado());
        assertNotNull(resultado.getFechaInicio());
        assertNotNull(resultado.getFechaFin());
        assertTrue(resultado.getFechaFin().isAfter(resultado.getFechaInicio()));
    }

    @Test
    void activar_debeLanzarExcepcionCuandoNoEstaPendiente() {

        promocion.setEstado(EstadoPromocion.ACTIVA);

        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));

        assertThrows(
                ReglaNegocioException.class,
                () -> promocionService.activar(1L)
        );

        verify(promocionRepository, never()).save(any(Promocion.class));
    }

    @Test
    void cancelar_debeCambiarEstadoACancelada() {

        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));
        when(promocionRepository.save(promocion)).thenReturn(promocion);

        PromocionSalida resultado = promocionService.cancelar(1L);

        assertEquals(EstadoPromocion.CANCELADA, resultado.getEstado());
    }

    @Test
    void cancelar_debeLanzarExcepcionCuandoYaEstaFinalizada() {

        promocion.setEstado(EstadoPromocion.FINALIZADA);

        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));

        assertThrows(
                ReglaNegocioException.class,
                () -> promocionService.cancelar(1L)
        );
    }

    @Test
    void finalizar_debeCambiarEstadoAFinalizadaCuandoEstaActiva() {

        promocion.setEstado(EstadoPromocion.ACTIVA);

        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));
        when(promocionRepository.save(promocion)).thenReturn(promocion);

        PromocionSalida resultado = promocionService.finalizar(1L);

        assertEquals(EstadoPromocion.FINALIZADA, resultado.getEstado());
    }

    @Test
    void finalizar_debeLanzarExcepcionCuandoNoEstaActiva() {

        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));

        assertThrows(
                ReglaNegocioException.class,
                () -> promocionService.finalizar(1L)
        );
    }

    @Test
    void estaVigente_debeRetornarFalseCuandoFechaFinYaPaso() {

        promocion.setEstado(EstadoPromocion.ACTIVA);
        promocion.setFechaFin(LocalDateTime.now().minusDays(1));

        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));

        assertFalse(promocionService.estaVigente(1L));
    }

    @Test
    void estaVigente_debeRetornarTrueCuandoEstaActivaYFechaFinNoHaPasado() {

        promocion.setEstado(EstadoPromocion.ACTIVA);
        promocion.setFechaFin(LocalDateTime.now().plusDays(3));

        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));

        assertTrue(promocionService.estaVigente(1L));
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {

        when(promocionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> promocionService.obtenerPorId(99L)
        );
    }
}