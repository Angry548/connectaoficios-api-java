package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.mensaje.MensajeGuardar;
import com.connectaoficios.api.dtos.mensaje.MensajeSalida;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Conversacion;
import com.connectaoficios.api.modelos.Mensaje;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.IConversacionRepository;
import com.connectaoficios.api.repositorios.IMensajeRepository;
import com.connectaoficios.api.servicios.interfaces.IChatTiempoRealService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensajeServiceTest {

    @Mock
    private IMensajeRepository mensajeRepository;

    @Mock
    private IConversacionRepository conversacionRepository;

    @Mock
    private IChatTiempoRealService chatTiempoRealService;

    private MensajeService mensajeService;

    private SolicitudServicio solicitud;
    private Conversacion conversacion;

    @BeforeEach
    void setUp() {

        mensajeService =
                new MensajeService(
                        mensajeRepository,
                        conversacionRepository,
                        chatTiempoRealService
                );

        solicitud = new SolicitudServicio();
        solicitud.setId(1L);
        solicitud.setClienteId(10);
        solicitud.setTrabajadorId(20);
        solicitud.setEstado(
                EstadoSolicitud.ACEPTADA
        );

        conversacion = new Conversacion();
        conversacion.setId(1L);
        conversacion.setSolicitud(solicitud);
    }

    @Test
    void guardar_debeGuardarYPublicarMensaje() {

        MensajeGuardar dto =
                new MensajeGuardar();

        dto.setConversacionId(1L);
        dto.setContenido("Hola, ¿cómo está?");

        when(
                conversacionRepository.findById(1L)
        ).thenReturn(Optional.of(conversacion));

        when(
                mensajeRepository.save(any(Mensaje.class))
        ).thenAnswer(invocation -> {

            Mensaje mensaje =
                    invocation.getArgument(0);

            mensaje.setId(1L);

            return mensaje;
        });

        MensajeSalida resultado =
                mensajeService.guardar(
                        dto,
                        10
                );

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getConversacionId());
        assertEquals(10, resultado.getRemitenteId());
        assertEquals(
                "Hola, ¿cómo está?",
                resultado.getContenido()
        );
        assertFalse(resultado.getLeido());

        verify(
                mensajeRepository,
                times(1)
        ).save(any(Mensaje.class));

        verify(
                chatTiempoRealService,
                times(1)
        ).publicarMensaje(any(MensajeSalida.class));
    }

    @Test
    void guardar_debeRechazarUsuarioNoParticipante() {

        MensajeGuardar dto =
                new MensajeGuardar();

        dto.setConversacionId(1L);
        dto.setContenido("Mensaje");

        when(
                conversacionRepository.findById(1L)
        ).thenReturn(Optional.of(conversacion));

        assertThrows(
                ReglaNegocioException.class,
                () -> mensajeService.guardar(
                        dto,
                        99
                )
        );

        verify(
                mensajeRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                chatTiempoRealService
        );
    }

    @Test
    void guardar_debeBloquearEnvioCuandoSolicitudCompletada() {

        solicitud.setEstado(
                EstadoSolicitud.COMPLETADA
        );

        MensajeGuardar dto =
                new MensajeGuardar();

        dto.setConversacionId(1L);
        dto.setContenido("Mensaje");

        when(
                conversacionRepository.findById(1L)
        ).thenReturn(Optional.of(conversacion));

        assertThrows(
                ReglaNegocioException.class,
                () -> mensajeService.guardar(
                        dto,
                        10
                )
        );

        verify(
                mensajeRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                chatTiempoRealService
        );
    }

    @Test
    void obtenerPorConversacion_debeRetornarHistorialCronologico() {

        Mensaje mensajeUno =
                crearMensaje(
                        1L,
                        10,
                        "Primer mensaje"
                );

        Mensaje mensajeDos =
                crearMensaje(
                        2L,
                        20,
                        "Segundo mensaje"
                );

        when(
                conversacionRepository.findById(1L)
        ).thenReturn(Optional.of(conversacion));

        when(
                mensajeRepository
                        .findByConversacionIdOrderByFechaEnvioAsc(1L)
        ).thenReturn(
                List.of(
                        mensajeUno,
                        mensajeDos
                )
        );

        List<MensajeSalida> resultado =
                mensajeService.obtenerPorConversacion(
                        1L,
                        10
                );

        assertEquals(2, resultado.size());

        assertEquals(
                "Primer mensaje",
                resultado.get(0).getContenido()
        );

        assertEquals(
                "Segundo mensaje",
                resultado.get(1).getContenido()
        );
    }

    @Test
    void obtenerPorConversacion_debePermitirHistorialCuandoEstaCompletada() {

        solicitud.setEstado(
                EstadoSolicitud.COMPLETADA
        );

        when(
                conversacionRepository.findById(1L)
        ).thenReturn(Optional.of(conversacion));

        when(
                mensajeRepository
                        .findByConversacionIdOrderByFechaEnvioAsc(1L)
        ).thenReturn(List.of());

        List<MensajeSalida> resultado =
                mensajeService.obtenerPorConversacion(
                        1L,
                        10
                );

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void marcarComoLeido_debeMarcarMensajeRecibido() {

        Mensaje mensaje =
                crearMensaje(
                        1L,
                        20,
                        "Mensaje del trabajador"
                );

        when(
                mensajeRepository.findById(1L)
        ).thenReturn(Optional.of(mensaje));

        when(
                mensajeRepository.save(mensaje)
        ).thenReturn(mensaje);

        mensajeService.marcarComoLeido(
                1L,
                10
        );

        assertTrue(mensaje.getLeido());

        verify(
                mensajeRepository,
                times(1)
        ).save(mensaje);
    }

    @Test
    void marcarComoLeido_noDebePermitirMarcarMensajePropio() {

        Mensaje mensaje =
                crearMensaje(
                        1L,
                        10,
                        "Mi propio mensaje"
                );

        when(
                mensajeRepository.findById(1L)
        ).thenReturn(Optional.of(mensaje));

        assertThrows(
                ReglaNegocioException.class,
                () -> mensajeService.marcarComoLeido(
                        1L,
                        10
                )
        );

        verify(
                mensajeRepository,
                never()
        ).save(any());
    }

    private Mensaje crearMensaje(
            Long id,
            Integer remitenteId,
            String contenido
    ) {

        Mensaje mensaje =
                new Mensaje();

        mensaje.setId(id);
        mensaje.setConversacion(conversacion);
        mensaje.setRemitenteId(remitenteId);
        mensaje.setContenido(contenido);
        mensaje.setLeido(false);

        return mensaje;
    }
}