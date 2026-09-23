package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.conversacion.ConversacionGuardar;
import com.connectaoficios.api.dtos.conversacion.ConversacionSalida;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Conversacion;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.IConversacionRepository;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversacionServiceTest {

    @Mock
    private IConversacionRepository conversacionRepository;

    @Mock
    private ISolicitudServicioRepository solicitudServicioRepository;

    private ConversacionService conversacionService;

    private SolicitudServicio solicitud;

    @BeforeEach
    void setUp() {

        conversacionService =
                new ConversacionService(
                        conversacionRepository,
                        solicitudServicioRepository
                );

        solicitud = new SolicitudServicio();
        solicitud.setId(1L);
        solicitud.setClienteId(10);
        solicitud.setTrabajadorId(20);
        solicitud.setEstado(
                EstadoSolicitud.ACEPTADA
        );
    }

    @Test
    void guardar_debeCrearConversacionCuandoSolicitudEsAceptada() {

        ConversacionGuardar dto =
                new ConversacionGuardar();

        dto.setSolicitudId(1L);

        when(
                solicitudServicioRepository.findById(1L)
        ).thenReturn(Optional.of(solicitud));

        when(
                conversacionRepository.existsBySolicitudId(1L)
        ).thenReturn(false);

        when(
                conversacionRepository.save(
                        any(Conversacion.class)
                )
        ).thenAnswer(invocation -> {

            Conversacion conversacion =
                    invocation.getArgument(0);

            conversacion.setId(1L);

            return conversacion;
        });

        ConversacionSalida resultado =
                conversacionService.guardar(
                        dto,
                        10
                );

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getSolicitudId());
        assertEquals(10, resultado.getClienteId());
        assertEquals(20, resultado.getTrabajadorId());
        assertTrue(resultado.getPuedeEnviarMensajes());

        verify(
                conversacionRepository,
                times(1)
        ).save(any(Conversacion.class));
    }

    @Test
    void guardar_debePermitirTrabajadorParticipante() {

        ConversacionGuardar dto =
                new ConversacionGuardar();

        dto.setSolicitudId(1L);

        when(
                solicitudServicioRepository.findById(1L)
        ).thenReturn(Optional.of(solicitud));

        when(
                conversacionRepository.existsBySolicitudId(1L)
        ).thenReturn(false);

        when(
                conversacionRepository.save(
                        any(Conversacion.class)
                )
        ).thenAnswer(invocation -> {

            Conversacion conversacion =
                    invocation.getArgument(0);

            conversacion.setId(1L);

            return conversacion;
        });

        ConversacionSalida resultado =
                conversacionService.guardar(
                        dto,
                        20
                );

        assertNotNull(resultado);
        assertTrue(resultado.getPuedeEnviarMensajes());
    }

    @Test
    void guardar_debeRechazarUsuarioQueNoEsParticipante() {

        ConversacionGuardar dto =
                new ConversacionGuardar();

        dto.setSolicitudId(1L);

        when(
                solicitudServicioRepository.findById(1L)
        ).thenReturn(Optional.of(solicitud));

        assertThrows(
                ReglaNegocioException.class,
                () -> conversacionService.guardar(
                        dto,
                        99
                )
        );

        verify(
                conversacionRepository,
                never()
        ).save(any());
    }

    @Test
    void guardar_debeRechazarSolicitudPendiente() {

        solicitud.setEstado(
                EstadoSolicitud.PENDIENTE
        );

        ConversacionGuardar dto =
                new ConversacionGuardar();

        dto.setSolicitudId(1L);

        when(
                solicitudServicioRepository.findById(1L)
        ).thenReturn(Optional.of(solicitud));

        assertThrows(
                ReglaNegocioException.class,
                () -> conversacionService.guardar(
                        dto,
                        10
                )
        );

        verify(
                conversacionRepository,
                never()
        ).save(any());
    }

    @Test
    void guardar_debeRechazarConversacionDuplicada() {

        ConversacionGuardar dto =
                new ConversacionGuardar();

        dto.setSolicitudId(1L);

        when(
                solicitudServicioRepository.findById(1L)
        ).thenReturn(Optional.of(solicitud));

        when(
                conversacionRepository.existsBySolicitudId(1L)
        ).thenReturn(true);

        assertThrows(
                ReglaNegocioException.class,
                () -> conversacionService.guardar(
                        dto,
                        10
                )
        );

        verify(
                conversacionRepository,
                never()
        ).save(any());
    }

    @Test
    void obtenerPorSolicitud_debePermitirConsultarHistorialCompletado() {

        solicitud.setEstado(
                EstadoSolicitud.COMPLETADA
        );

        Conversacion conversacion =
                new Conversacion();

        conversacion.setId(1L);
        conversacion.setSolicitud(solicitud);

        when(
                conversacionRepository.findBySolicitudId(1L)
        ).thenReturn(Optional.of(conversacion));

        ConversacionSalida resultado =
                conversacionService.obtenerPorSolicitud(
                        1L,
                        10
                );

        assertNotNull(resultado);
        assertFalse(
                resultado.getPuedeEnviarMensajes()
        );
    }
}