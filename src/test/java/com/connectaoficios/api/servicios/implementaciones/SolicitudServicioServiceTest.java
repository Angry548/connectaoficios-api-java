package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.notificacion.NotificacionGuardar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioCancelar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioGuardar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioRechazar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioRespuesta;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.IServicioRepository;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;
import com.connectaoficios.api.servicios.interfaces.INotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServicioServiceTest {

    @Mock
    private ISolicitudServicioRepository solicitudRepository;

    @Mock
    private IServicioRepository servicioRepository;

    @Mock
    private INotificacionService notificacionService;

    private SolicitudServicioService solicitudService;

    private SolicitudServicio solicitud;
    private Servicio servicio;

    @BeforeEach
    void setUp() {

        solicitudService = new SolicitudServicioService(
                solicitudRepository,
                servicioRepository,
                notificacionService
        );

        servicio = new Servicio();
        servicio.setId(1L);
        servicio.setTitulo(
                "Servicio de fontanería residencial"
        );

        solicitud = new SolicitudServicio();
        solicitud.setId(1L);
        solicitud.setServicio(servicio);
        solicitud.setClienteId(100);
        solicitud.setTrabajadorId(200);
        solicitud.setFechaPropuesta(
                LocalDate.now().plusDays(1)
        );
        solicitud.setHoraAproximada(
                LocalTime.of(10, 0)
        );
        solicitud.setDireccion(
                "Calle Falsa 123"
        );
        solicitud.setDescripcionTrabajo(
                "Reparación de tubería"
        );
        solicitud.setEstado(
                EstadoSolicitud.PENDIENTE
        );
    }

    @Test
    void guardar_debeCrearSolicitudNueva() {

        SolicitudServicioGuardar dto =
                new SolicitudServicioGuardar();

        dto.setServicioId(1L);
        dto.setClienteId(100);
        dto.setTrabajadorId(200);
        dto.setFechaPropuesta(
                LocalDate.now().plusDays(1)
        );
        dto.setHoraAproximada(
                LocalTime.of(10, 0)
        );
        dto.setDireccionServicio(
                "Calle Falsa 123"
        );
        dto.setDescripcionTrabajo(
                "Reparación de tubería"
        );

        when(
                servicioRepository
                        .findByIdAndEliminadoFalse(1L)
        ).thenReturn(
                Optional.of(servicio)
        );

        when(
                solicitudRepository
                        .save(any(SolicitudServicio.class))
        ).thenAnswer(invocation -> {

            SolicitudServicio guardada =
                    invocation.getArgument(0);

            guardada.setId(1L);

            return guardada;
        });

        SolicitudServicioRespuesta resultado =
                solicitudService.guardar(dto);

        assertNotNull(resultado);

        assertEquals(
                1L,
                resultado.getIdSolicitud()
        );

        assertEquals(
                1L,
                resultado.getServicioId()
        );

        assertEquals(
                "Servicio de fontanería residencial",
                resultado.getServicioTitulo()
        );

        assertEquals(
                100,
                resultado.getClienteId()
        );

        assertEquals(
                200,
                resultado.getTrabajadorId()
        );

        assertEquals(
                EstadoSolicitud.PENDIENTE.name(),
                resultado.getEstado()
        );

        verify(
                servicioRepository,
                times(1)
        ).findByIdAndEliminadoFalse(1L);

        verify(
                solicitudRepository,
                times(1)
        ).save(any(SolicitudServicio.class));

        ArgumentCaptor<NotificacionGuardar> captor =
                ArgumentCaptor.forClass(
                        NotificacionGuardar.class
                );

        verify(
                notificacionService,
                times(1)
        ).guardar(
                captor.capture()
        );

        NotificacionGuardar notificacion =
                captor.getValue();

        assertEquals(
                200,
                notificacion.getUsuarioDestinoId()
        );

        assertEquals(
                "NUEVA_SOLICITUD",
                notificacion.getTipo()
        );

        assertEquals(
                "Nueva solicitud de servicio",
                notificacion.getTitulo()
        );

        assertEquals(
                "Has recibido una nueva solicitud para el servicio \"Servicio de fontanería residencial\".",
                notificacion.getMensaje()
        );

        assertEquals(
                1L,
                notificacion.getReferenciaId()
        );
    }

    @Test
    void obtenerPorId_debeRetornarSolicitud() {

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        SolicitudServicioRespuesta resultado =
                solicitudService.obtenerPorId(1L);

        assertNotNull(resultado);

        assertEquals(
                1L,
                resultado.getIdSolicitud()
        );

        assertEquals(
                1L,
                resultado.getServicioId()
        );

        assertEquals(
                "Servicio de fontanería residencial",
                resultado.getServicioTitulo()
        );

        assertEquals(
                "Calle Falsa 123",
                resultado.getDireccionServicio()
        );
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {

        when(
                solicitudRepository.findById(99L)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                RecursoNoEncontradoException.class,
                () ->
                        solicitudService
                                .obtenerPorId(99L)
        );

        verify(
                solicitudRepository,
                times(1)
        ).findById(99L);
    }

    @Test
    void aceptar_debeCambiarEstadoAAceptada() {

        solicitud.setEstado(
                EstadoSolicitud.PENDIENTE
        );

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        when(
                solicitudRepository
                        .save(any(SolicitudServicio.class))
        ).thenReturn(
                solicitud
        );

        SolicitudServicioRespuesta resultado =
                solicitudService.aceptar(1L);

        assertNotNull(resultado);

        assertEquals(
                EstadoSolicitud.ACEPTADA.name(),
                resultado.getEstado()
        );

        verify(
                solicitudRepository,
                times(1)
        ).save(solicitud);

        ArgumentCaptor<NotificacionGuardar> captor =
                ArgumentCaptor.forClass(
                        NotificacionGuardar.class
                );

        verify(
                notificacionService,
                times(1)
        ).guardar(
                captor.capture()
        );

        NotificacionGuardar notificacion =
                captor.getValue();

        assertEquals(
                100,
                notificacion.getUsuarioDestinoId()
        );

        assertEquals(
                "CAMBIO_ESTADO_SOLICITUD",
                notificacion.getTipo()
        );

        assertEquals(
                "Solicitud aceptada",
                notificacion.getTitulo()
        );

        assertEquals(
                "El trabajador aceptó tu solicitud de servicio.",
                notificacion.getMensaje()
        );

        assertEquals(
                1L,
                notificacion.getReferenciaId()
        );
    }

    @Test
    void rechazar_debeCambiarEstadoARechazadaYGuardarMotivo() {

        SolicitudServicioRechazar dto =
                new SolicitudServicioRechazar();

        dto.setMotivoRechazo(
                "No tengo disponibilidad para la fecha solicitada"
        );

        solicitud.setEstado(
                EstadoSolicitud.PENDIENTE
        );

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        when(
                solicitudRepository
                        .save(any(SolicitudServicio.class))
        ).thenReturn(
                solicitud
        );

        SolicitudServicioRespuesta resultado =
                solicitudService.rechazar(
                        1L,
                        dto
                );

        assertNotNull(resultado);

        assertEquals(
                EstadoSolicitud.RECHAZADA.name(),
                resultado.getEstado()
        );

        assertEquals(
                "No tengo disponibilidad para la fecha solicitada",
                resultado.getMotivoRechazo()
        );

        assertEquals(
                "No tengo disponibilidad para la fecha solicitada",
                solicitud.getMotivoRechazo()
        );

        verify(
                solicitudRepository,
                times(1)
        ).save(solicitud);

        ArgumentCaptor<NotificacionGuardar> captor =
                ArgumentCaptor.forClass(
                        NotificacionGuardar.class
                );

        verify(
                notificacionService,
                times(1)
        ).guardar(
                captor.capture()
        );

        NotificacionGuardar notificacion =
                captor.getValue();

        assertEquals(
                100,
                notificacion.getUsuarioDestinoId()
        );

        assertEquals(
                "CAMBIO_ESTADO_SOLICITUD",
                notificacion.getTipo()
        );

        assertEquals(
                "Solicitud rechazada",
                notificacion.getTitulo()
        );

        assertEquals(
                "El trabajador rechazó tu solicitud de servicio.",
                notificacion.getMensaje()
        );

        assertEquals(
                1L,
                notificacion.getReferenciaId()
        );
    }

    @Test
    void iniciar_debeCambiarEstadoAEnProceso() {

        solicitud.setEstado(
                EstadoSolicitud.ACEPTADA
        );

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        when(
                solicitudRepository
                        .save(any(SolicitudServicio.class))
        ).thenReturn(
                solicitud
        );

        SolicitudServicioRespuesta resultado =
                solicitudService.iniciar(1L);

        assertNotNull(resultado);

        assertEquals(
                EstadoSolicitud.EN_PROCESO.name(),
                resultado.getEstado()
        );

        verify(
                solicitudRepository,
                times(1)
        ).save(solicitud);

        ArgumentCaptor<NotificacionGuardar> captor =
                ArgumentCaptor.forClass(
                        NotificacionGuardar.class
                );

        verify(
                notificacionService,
                times(1)
        ).guardar(
                captor.capture()
        );

        NotificacionGuardar notificacion =
                captor.getValue();

        assertEquals(
                100,
                notificacion.getUsuarioDestinoId()
        );

        assertEquals(
                "CAMBIO_ESTADO_SOLICITUD",
                notificacion.getTipo()
        );

        assertEquals(
                "Servicio iniciado",
                notificacion.getTitulo()
        );

        assertEquals(
                "El trabajador inició el servicio solicitado.",
                notificacion.getMensaje()
        );

        assertEquals(
                1L,
                notificacion.getReferenciaId()
        );
    }

    @Test
    void completar_debeCambiarEstadoACompletada() {

        solicitud.setEstado(
                EstadoSolicitud.EN_PROCESO
        );

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        when(
                solicitudRepository
                        .save(any(SolicitudServicio.class))
        ).thenReturn(
                solicitud
        );

        SolicitudServicioRespuesta resultado =
                solicitudService.completar(1L);

        assertNotNull(resultado);

        assertEquals(
                EstadoSolicitud.COMPLETADA.name(),
                resultado.getEstado()
        );

        verify(
                solicitudRepository,
                times(1)
        ).save(solicitud);

        ArgumentCaptor<NotificacionGuardar> captor =
                ArgumentCaptor.forClass(
                        NotificacionGuardar.class
                );

        verify(
                notificacionService,
                times(1)
        ).guardar(
                captor.capture()
        );

        NotificacionGuardar notificacion =
                captor.getValue();

        assertEquals(
                100,
                notificacion.getUsuarioDestinoId()
        );

        assertEquals(
                "CAMBIO_ESTADO_SOLICITUD",
                notificacion.getTipo()
        );

        assertEquals(
                "Servicio completado",
                notificacion.getTitulo()
        );

        assertEquals(
                "El trabajador marcó tu servicio como completado.",
                notificacion.getMensaje()
        );

        assertEquals(
                1L,
                notificacion.getReferenciaId()
        );
    }

    @Test
    void cancelar_debeCambiarEstadoYAsignarMotivo() {

        SolicitudServicioCancelar dto =
                new SolicitudServicioCancelar();

        dto.setMotivoCancelacion(
                "El cliente no estará disponible"
        );

        solicitud.setEstado(
                EstadoSolicitud.PENDIENTE
        );

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        when(
                solicitudRepository
                        .save(any(SolicitudServicio.class))
        ).thenReturn(
                solicitud
        );

        SolicitudServicioRespuesta resultado =
                solicitudService.cancelar(
                        1L,
                        dto
                );

        assertNotNull(resultado);

        assertEquals(
                EstadoSolicitud.CANCELADA.name(),
                resultado.getEstado()
        );

        assertEquals(
                "El cliente no estará disponible",
                resultado.getMotivoCancelacion()
        );

        verify(
                solicitudRepository,
                times(1)
        ).save(solicitud);

        ArgumentCaptor<NotificacionGuardar> captor =
                ArgumentCaptor.forClass(
                        NotificacionGuardar.class
                );

        verify(
                notificacionService,
                times(1)
        ).guardar(
                captor.capture()
        );

        NotificacionGuardar notificacion =
                captor.getValue();

        assertEquals(
                200,
                notificacion.getUsuarioDestinoId()
        );

        assertEquals(
                "CAMBIO_ESTADO_SOLICITUD",
                notificacion.getTipo()
        );

        assertEquals(
                "Solicitud cancelada",
                notificacion.getTitulo()
        );

        assertEquals(
                "La solicitud del servicio \"Servicio de fontanería residencial\" fue cancelada.",
                notificacion.getMensaje()
        );

        assertEquals(
                1L,
                notificacion.getReferenciaId()
        );
    }

    @Test
    void rechazar_debeLanzarExcepcion_cuandoSolicitudNoEstaPendiente() {

        SolicitudServicioRechazar dto =
                new SolicitudServicioRechazar();

        dto.setMotivoRechazo(
                "No tengo disponibilidad"
        );

        solicitud.setEstado(
                EstadoSolicitud.ACEPTADA
        );

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        assertThrows(
                ReglaNegocioException.class,
                () ->
                        solicitudService.rechazar(
                                1L,
                                dto
                        )
        );

        verify(
                solicitudRepository,
                never()
        ).save(
                any(SolicitudServicio.class)
        );

        verify(
                notificacionService,
                never()
        ).guardar(
                any(NotificacionGuardar.class)
        );
    }

    @Test
    void iniciar_debeLanzarExcepcion_cuandoSolicitudEstaPendiente() {

        solicitud.setEstado(
                EstadoSolicitud.PENDIENTE
        );

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        assertThrows(
                ReglaNegocioException.class,
                () ->
                        solicitudService.iniciar(1L)
        );

        verify(
                solicitudRepository,
                never()
        ).save(
                any(SolicitudServicio.class)
        );

        verify(
                notificacionService,
                never()
        ).guardar(
                any(NotificacionGuardar.class)
        );
    }

    @Test
    void completar_debeLanzarExcepcion_cuandoSolicitudEstaPendiente() {

        solicitud.setEstado(
                EstadoSolicitud.PENDIENTE
        );

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        assertThrows(
                ReglaNegocioException.class,
                () ->
                        solicitudService.completar(1L)
        );

        verify(
                solicitudRepository,
                never()
        ).save(
                any(SolicitudServicio.class)
        );

        verify(
                notificacionService,
                never()
        ).guardar(
                any(NotificacionGuardar.class)
        );
    }

    @Test
    void aceptar_debeLanzarExcepcion_cuandoSolicitudYaEstaCompletada() {

        solicitud.setEstado(
                EstadoSolicitud.COMPLETADA
        );

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        assertThrows(
                ReglaNegocioException.class,
                () ->
                        solicitudService.aceptar(1L)
        );

        verify(
                solicitudRepository,
                never()
        ).save(
                any(SolicitudServicio.class)
        );

        verify(
                notificacionService,
                never()
        ).guardar(
                any(NotificacionGuardar.class)
        );
    }

    @Test
    void cancelar_debeLanzarExcepcion_cuandoSolicitudYaEstaRechazada() {

        SolicitudServicioCancelar dto =
                new SolicitudServicioCancelar();

        dto.setMotivoCancelacion(
                "Intento de cancelación invalido"
        );

        solicitud.setEstado(
                EstadoSolicitud.RECHAZADA
        );

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        assertThrows(
                ReglaNegocioException.class,
                () ->
                        solicitudService.cancelar(
                                1L,
                                dto
                        )
        );

        verify(
                solicitudRepository,
                never()
        ).save(
                any(SolicitudServicio.class)
        );

        verify(
                notificacionService,
                never()
        ).guardar(
                any(NotificacionGuardar.class)
        );
    }

    @Test
    void eliminar_debeBorrarSolicitudSiExiste() {

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        solicitudService.eliminar(1L);

        verify(
                solicitudRepository,
                times(1)
        ).delete(solicitud);
    }

    @Test
    void esParticipante_debeRetornarTrueSiEsClienteOTrabajador() {

        when(
                solicitudRepository.findById(1L)
        ).thenReturn(
                Optional.of(solicitud)
        );

        assertTrue(
                solicitudService.esParticipante(
                        1L,
                        100
                )
        );

        assertTrue(
                solicitudService.esParticipante(
                        1L,
                        200
                )
        );

        assertFalse(
                solicitudService.esParticipante(
                        1L,
                        300
                )
        );
    }
}