package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.planpromocion.PlanPromocionGuardar;
import com.connectaoficios.api.dtos.planpromocion.PlanPromocionModificar;
import com.connectaoficios.api.dtos.planpromocion.PlanPromocionSalida;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PlanPromocion;
import com.connectaoficios.api.repositorios.IPlanPromocionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanPromocionServiceTest {

    @Mock
    private IPlanPromocionRepository planPromocionRepository;

    private PlanPromocionService planPromocionService;

    private PlanPromocion plan;

    @BeforeEach
    void setUp() {

        planPromocionService = new PlanPromocionService(planPromocionRepository);

        plan = new PlanPromocion();
        plan.setId(1L);
        plan.setNombre("Plan Básico");
        plan.setDescripcion("Promoción por 7 días");
        plan.setDuracionDias(7);
        plan.setPrecio(new BigDecimal("5.00"));
        plan.setActivo(true);
    }

    @Test
    void guardar_debeCrearPlanCuandoNombreNoExiste() {

        PlanPromocionGuardar dto = new PlanPromocionGuardar();
        dto.setNombre("Plan Básico");
        dto.setDescripcion("Promoción por 7 días");
        dto.setDuracionDias(7);
        dto.setPrecio(new BigDecimal("5.00"));

        when(planPromocionRepository.existsByNombreIgnoreCase("Plan Básico")).thenReturn(false);
        when(planPromocionRepository.save(any(PlanPromocion.class))).thenReturn(plan);

        PlanPromocionSalida resultado = planPromocionService.guardar(dto);

        assertNotNull(resultado);
        assertEquals("Plan Básico", resultado.getNombre());
        assertTrue(resultado.getActivo());

        verify(planPromocionRepository, times(1)).save(any(PlanPromocion.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoNombreYaExiste() {

        PlanPromocionGuardar dto = new PlanPromocionGuardar();
        dto.setNombre("Plan Básico");
        dto.setDuracionDias(7);
        dto.setPrecio(new BigDecimal("5.00"));

        when(planPromocionRepository.existsByNombreIgnoreCase("Plan Básico")).thenReturn(true);

        assertThrows(
                ReglaNegocioException.class,
                () -> planPromocionService.guardar(dto)
        );

        verify(planPromocionRepository, never()).save(any(PlanPromocion.class));
    }

    @Test
    void modificar_debeActualizarPlanCuandoNombreNoEstaDuplicado() {

        PlanPromocionModificar dto = new PlanPromocionModificar();
        dto.setNombre("Plan Básico Editado");
        dto.setDuracionDias(10);
        dto.setPrecio(new BigDecimal("8.00"));

        when(planPromocionRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planPromocionRepository.existsByNombreIgnoreCaseAndIdNot("Plan Básico Editado", 1L))
                .thenReturn(false);
        when(planPromocionRepository.save(plan)).thenReturn(plan);

        PlanPromocionSalida resultado = planPromocionService.modificar(1L, dto);

        assertEquals(10, resultado.getDuracionDias());
        verify(planPromocionRepository, times(1)).save(plan);
    }

    @Test
    void modificar_debeLanzarExcepcionCuandoNombreDuplicadoEnOtroPlan() {

        PlanPromocionModificar dto = new PlanPromocionModificar();
        dto.setNombre("Plan Premium");
        dto.setDuracionDias(10);
        dto.setPrecio(new BigDecimal("8.00"));

        when(planPromocionRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planPromocionRepository.existsByNombreIgnoreCaseAndIdNot("Plan Premium", 1L))
                .thenReturn(true);

        assertThrows(
                ReglaNegocioException.class,
                () -> planPromocionService.modificar(1L, dto)
        );

        verify(planPromocionRepository, never()).save(any(PlanPromocion.class));
    }

    @Test
    void desactivar_debeCambiarActivoAFalse() {

        when(planPromocionRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planPromocionRepository.save(plan)).thenReturn(plan);

        PlanPromocionSalida resultado = planPromocionService.desactivar(1L);

        assertFalse(resultado.getActivo());
        verify(planPromocionRepository, times(1)).save(plan);
    }

    @Test
    void activar_debeCambiarActivoATrue() {

        plan.setActivo(false);

        when(planPromocionRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planPromocionRepository.save(plan)).thenReturn(plan);

        PlanPromocionSalida resultado = planPromocionService.activar(1L);

        assertTrue(resultado.getActivo());
    }

    @Test
    void calcularFechaFin_debeSumarLaDuracionEnDiasALaFechaInicio() {

        when(planPromocionRepository.findById(1L)).thenReturn(Optional.of(plan));

        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 0, 0);
        LocalDateTime resultado = planPromocionService.calcularFechaFin(1L, inicio);

        assertEquals(LocalDateTime.of(2026, 9, 29, 0, 0), resultado);
    }

    @Test
    void calcularFechaFin_debeLanzarExcepcionCuandoFechaInicioEsNula() {

        when(planPromocionRepository.findById(1L)).thenReturn(Optional.of(plan));

        assertThrows(
                ReglaNegocioException.class,
                () -> planPromocionService.calcularFechaFin(1L, null)
        );
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {

        when(planPromocionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> planPromocionService.obtenerPorId(99L)
        );
    }

    @Test
    void eliminar_debeBorrarPlanCuandoExiste() {

        when(planPromocionRepository.findById(1L)).thenReturn(Optional.of(plan));

        planPromocionService.eliminar(1L);

        verify(planPromocionRepository, times(1)).delete(plan);
    }
}
