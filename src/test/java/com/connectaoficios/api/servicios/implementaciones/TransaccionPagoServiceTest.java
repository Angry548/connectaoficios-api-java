package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoAprobar;
import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoGuardar;
import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoSalida;
import com.connectaoficios.api.enums.EstadoPromocion;
import com.connectaoficios.api.enums.EstadoTransaccion;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PlanPromocion;
import com.connectaoficios.api.modelos.Promocion;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.modelos.TransaccionPago;
import com.connectaoficios.api.repositorios.IPromocionRepository;
import com.connectaoficios.api.repositorios.ITransaccionPagoRepository;
import com.connectaoficios.api.servicios.interfaces.IPromocionService;

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
class TransaccionPagoServiceTest {

    @Mock
    private ITransaccionPagoRepository transaccionPagoRepository;

    @Mock
    private IPromocionRepository promocionRepository;

    @Mock
    private IPromocionService promocionService;

    private TransaccionPagoService transaccionPagoService;

    private Promocion promocion;
    private TransaccionPago transaccion;

    private static final Integer TRABAJADOR_ID = 10;

    @BeforeEach
    void setUp() {

        transaccionPagoService = new TransaccionPagoService(
                transaccionPagoRepository,
                promocionRepository,
                promocionService
        );

        Servicio servicio = new Servicio();
        servicio.setId(1L);

        PlanPromocion plan = new PlanPromocion();
        plan.setId(1L);
        plan.setPrecio(new BigDecimal("5.00"));

        promocion = new Promocion();
        promocion.setId(1L);
        promocion.setServicio(servicio);
        promocion.setPlan(plan);
        promocion.setTrabajadorId(TRABAJADOR_ID);
        promocion.setEstado(EstadoPromocion.PENDIENTE);

        transaccion = new TransaccionPago();
        transaccion.setId(1L);
        transaccion.setPromocion(promocion);
        transaccion.setTrabajadorId(TRABAJADOR_ID);
        transaccion.setMonto(new BigDecimal("5.00"));
        transaccion.setMoneda("USD");
        transaccion.setEstado(EstadoTransaccion.PENDIENTE);
        transaccion.setFecha(LocalDateTime.now());
    }

    @Test
    void guardar_debeCrearTransaccionPendienteCuandoTodoEsValido() {

        TransaccionPagoGuardar dto = new TransaccionPagoGuardar();
        dto.setPromocionId(1L);
        dto.setMonto(new BigDecimal("5.00"));
        dto.setMoneda("USD");

        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));
        when(transaccionPagoRepository.existsByPromocion_IdAndEstadoIn(eq(1L), anyList()))
                .thenReturn(false);
        when(transaccionPagoRepository.save(any(TransaccionPago.class))).thenAnswer(invocation -> {
            TransaccionPago guardada = invocation.getArgument(0);
            guardada.setId(1L);
            return guardada;
        });

        TransaccionPagoSalida resultado = transaccionPagoService.guardar(dto, TRABAJADOR_ID);

        assertEquals(EstadoTransaccion.PENDIENTE, resultado.getEstado());
        assertEquals(new BigDecimal("5.00"), resultado.getMonto());
        assertEquals(1L, resultado.getServicioId());

        verify(transaccionPagoRepository, times(1)).save(any(TransaccionPago.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoPromocionNoPerteneceAlTrabajador() {

        TransaccionPagoGuardar dto = new TransaccionPagoGuardar();
        dto.setPromocionId(1L);
        dto.setMonto(new BigDecimal("5.00"));
        dto.setMoneda("USD");

        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));

        assertThrows(
                ReglaNegocioException.class,
                () -> transaccionPagoService.guardar(dto, 999)
        );

        verify(transaccionPagoRepository, never()).save(any(TransaccionPago.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoPromocionNoEstaPendiente() {

        promocion.setEstado(EstadoPromocion.ACTIVA);

        TransaccionPagoGuardar dto = new TransaccionPagoGuardar();
        dto.setPromocionId(1L);
        dto.setMonto(new BigDecimal("5.00"));
        dto.setMoneda("USD");

        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));

        assertThrows(
                ReglaNegocioException.class,
                () -> transaccionPagoService.guardar(dto, TRABAJADOR_ID)
        );

        verify(transaccionPagoRepository, never()).save(any(TransaccionPago.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoYaExisteTransaccionPendienteOAprobada() {

        TransaccionPagoGuardar dto = new TransaccionPagoGuardar();
        dto.setPromocionId(1L);
        dto.setMonto(new BigDecimal("5.00"));
        dto.setMoneda("USD");

        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));
        when(transaccionPagoRepository.existsByPromocion_IdAndEstadoIn(eq(1L), anyList()))
                .thenReturn(true);

        assertThrows(
                ReglaNegocioException.class,
                () -> transaccionPagoService.guardar(dto, TRABAJADOR_ID)
        );

        verify(transaccionPagoRepository, never()).save(any(TransaccionPago.class));
    }

    @Test
    void aprobar_debeCambiarEstadoAAprobadaYActivarLaPromocion() {

        TransaccionPagoAprobar dto = new TransaccionPagoAprobar();
        dto.setReferenciaExterna("REF-12345");

        when(transaccionPagoRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        when(transaccionPagoRepository.save(transaccion)).thenReturn(transaccion);

        TransaccionPagoSalida resultado = transaccionPagoService.aprobar(1L, dto);

        assertEquals(EstadoTransaccion.APROBADA, resultado.getEstado());
        assertEquals("REF-12345", resultado.getReferenciaExterna());

        verify(promocionService, times(1)).activar(1L);
    }

    @Test
    void aprobar_debeLanzarExcepcionCuandoNoEstaPendiente() {

        transaccion.setEstado(EstadoTransaccion.APROBADA);

        TransaccionPagoAprobar dto = new TransaccionPagoAprobar();
        dto.setReferenciaExterna("REF-12345");

        when(transaccionPagoRepository.findById(1L)).thenReturn(Optional.of(transaccion));

        assertThrows(
                ReglaNegocioException.class,
                () -> transaccionPagoService.aprobar(1L, dto)
        );

        verify(promocionService, never()).activar(any());
        verify(transaccionPagoRepository, never()).save(any(TransaccionPago.class));
    }

    @Test
    void rechazar_debeCambiarEstadoARechazadaYCancelarLaPromocion() {

        when(transaccionPagoRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        when(transaccionPagoRepository.save(transaccion)).thenReturn(transaccion);

        TransaccionPagoSalida resultado = transaccionPagoService.rechazar(1L);

        assertEquals(EstadoTransaccion.RECHAZADA, resultado.getEstado());
        verify(promocionService, times(1)).cancelar(1L);
    }

    @Test
    void rechazar_debeLanzarExcepcionCuandoNoEstaPendiente() {

        transaccion.setEstado(EstadoTransaccion.RECHAZADA);

        when(transaccionPagoRepository.findById(1L)).thenReturn(Optional.of(transaccion));

        assertThrows(
                ReglaNegocioException.class,
                () -> transaccionPagoService.rechazar(1L)
        );

        verify(promocionService, never()).cancelar(any());
    }

    @Test
    void cancelar_debeCambiarEstadoACanceladaSinTocarLaPromocion() {

        when(transaccionPagoRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        when(transaccionPagoRepository.save(transaccion)).thenReturn(transaccion);

        TransaccionPagoSalida resultado = transaccionPagoService.cancelar(1L);

        assertEquals(EstadoTransaccion.CANCELADA, resultado.getEstado());
        verifyNoInteractions(promocionService);
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {

        when(transaccionPagoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> transaccionPagoService.obtenerPorId(99L)
        );
    }
}