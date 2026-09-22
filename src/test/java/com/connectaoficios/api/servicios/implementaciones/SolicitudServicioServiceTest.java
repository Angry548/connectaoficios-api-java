package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.solicitud.SolicitudServicioCancelar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioGuardar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioRespuesta;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServicioServiceTest {

    @Mock
    private ISolicitudServicioRepository solicitudRepository;

    private SolicitudServicioService solicitudService;

    private SolicitudServicio solicitud;

    @BeforeEach
    void setUp() {
        solicitudService = new SolicitudServicioService(solicitudRepository);

        solicitud = new SolicitudServicio();
        solicitud.setIdSolicitud(1L);
        solicitud.setClienteId(100);
        solicitud.setTrabajadorId(200);
        solicitud.setFechaPropuesta(LocalDate.now().plusDays(1));
        solicitud.setHoraAproximada(LocalTime.of(10, 0));
        solicitud.setDireccionServicio("Calle Falsa 123");
        solicitud.setDescripcionTrabajo("Reparación de tubería");
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
    }

    @Test
    void guardar_debeCrearSolicitudNueva() {
        SolicitudServicioGuardar dto = new SolicitudServicioGuardar();
        dto.setClienteId(100);
        dto.setTrabajadorId(200);
        dto.setFechaPropuesta(LocalDate.now().plusDays(1));
        dto.setHoraAproximada(LocalTime.of(10, 0));
        dto.setDireccionServicio("Calle Falsa 123");
        dto.setDescripcionTrabajo("Reparación de tubería");

        when(solicitudRepository.save(any(SolicitudServicio.class)))
                .thenAnswer(invocation -> {
                    SolicitudServicio guardada = invocation.getArgument(0);
                    guardada.setIdSolicitud(1L);
                    return guardada;
                });

        SolicitudServicioRespuesta resultado = solicitudService.guardar(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdSolicitud());
        assertEquals(100, resultado.getClienteId());
        assertEquals(200, resultado.getTrabajadorId());
        assertEquals(EstadoSolicitud.PENDIENTE.name(), resultado.getEstado());

        verify(solicitudRepository, times(1)).save(any(SolicitudServicio.class));
    }

    @Test
    void obtenerPorId_debeRetornarSolicitud() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        SolicitudServicioRespuesta resultado = solicitudService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdSolicitud());
        assertEquals("Calle Falsa 123", resultado.getDireccionServicio());
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> solicitudService.obtenerPorId(99L));
    }

    @Test
    void aceptar_debeCambiarEstadoAAceptada() {
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudServicio.class))).thenReturn(solicitud);

        SolicitudServicioRespuesta resultado = solicitudService.aceptar(1L);

        assertNotNull(resultado);
        assertEquals(EstadoSolicitud.ACEPTADA.name(), resultado.getEstado());
        verify(solicitudRepository, times(1)).save(solicitud);
    }

    @Test
    void cancelar_debeCambiarEstadoYAsignarMotivo() {
        SolicitudServicioCancelar dto = new SolicitudServicioCancelar();
        dto.setMotivoCancelacion("El cliente no estará disponible");

        solicitud.setEstado(EstadoSolicitud.PENDIENTE);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudServicio.class))).thenReturn(solicitud);

        SolicitudServicioRespuesta resultado = solicitudService.cancelar(1L, dto);

        assertNotNull(resultado);
        assertEquals(EstadoSolicitud.CANCELADA.name(), resultado.getEstado());
        assertEquals("El cliente no estará disponible", resultado.getMotivoCancelacion());
        verify(solicitudRepository, times(1)).save(solicitud);
    }

    @Test
    void eliminar_debeBorrarSolicitudSiExiste() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        solicitudService.eliminar(1L);

        verify(solicitudRepository, times(1)).delete(solicitud);
    }

    @Test
    void esParticipante_debeRetornarTrueSiEsClienteOTrabajador() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        assertTrue(solicitudService.esParticipante(1L, 100)); // ID Cliente
        assertTrue(solicitudService.esParticipante(1L, 200)); // ID Trabajador
        assertFalse(solicitudService.esParticipante(1L, 300)); // Usuario ajeno
    }
}
