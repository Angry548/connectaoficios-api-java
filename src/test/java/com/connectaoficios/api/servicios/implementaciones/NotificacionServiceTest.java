package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.notificacion.NotificacionGuardar;
import com.connectaoficios.api.dtos.notificacion.NotificacionRespuesta;
import com.connectaoficios.api.enums.TipoNotificacion;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.modelos.Notificacion;
import com.connectaoficios.api.repositorios.INotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private INotificacionRepository notificacionRepository;

    private NotificacionService notificacionService;

    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        notificacionService = new NotificacionService(notificacionRepository);

        notificacion = new Notificacion();
        notificacion.setIdNotificacion(1L);
        notificacion.setUsuarioDestinoId(100);
        notificacion.setTipo(TipoNotificacion.SOLICITUD);
        notificacion.setTitulo("Nueva Solicitud");
        notificacion.setMensaje("Has recibido una nueva solicitud de servicio.");
        notificacion.setReferenciaId(50L);
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void guardar_debeCrearNotificacionNueva() {
        NotificacionGuardar dto = new NotificacionGuardar();
        dto.setUsuarioDestinoId(100);
        dto.setTipo("SOLICITUD");
        dto.setTitulo("Nueva Solicitud");
        dto.setMensaje("Has recibido una nueva solicitud de servicio.");
        dto.setReferenciaId(50L);

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> {
                    Notificacion guardada = invocation.getArgument(0);
                    guardada.setIdNotificacion(1L);
                    return guardada;
                });

        NotificacionRespuesta resultado = notificacionService.guardar(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdNotificacion());
        assertEquals(100, resultado.getUsuarioDestinoId());
        assertEquals("SOLICITUD", resultado.getTipo());
        assertFalse(resultado.getLeida());

        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    // --- PRUEBAS ESPECÍFICAS DE TIPOS DE NOTIFICACIÓN ---

    @Test
    void guardar_debeConvertirTipoEnMinusculasAEnumCorrecto() {
        NotificacionGuardar dto = new NotificacionGuardar();
        dto.setUsuarioDestinoId(100);
        dto.setTipo("solicitud"); // En minúsculas
        dto.setTitulo("Solicitud Aceptada");
        dto.setMensaje("Tu solicitud fue aceptada.");

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificacionRespuesta resultado = notificacionService.guardar(dto);

        assertNotNull(resultado);
        assertEquals("SOLICITUD", resultado.getTipo());
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    void guardar_debeLanzarExcepcion_cuandoTipoNotificacionEsInvalido() {
        NotificacionGuardar dto = new NotificacionGuardar();
        dto.setUsuarioDestinoId(100);
        dto.setTipo("TIPO_INEXISTENTE");
        dto.setTitulo("Prueba");
        dto.setMensaje("Mensaje de prueba");

        assertThrows(IllegalArgumentException.class, () -> notificacionService.guardar(dto));

        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    // --- PRUEBAS DE CONSULTAS Y OPERACIONES ---

    @Test
    void obtenerPorId_debeRetornarNotificacion() {
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        NotificacionRespuesta resultado = notificacionService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdNotificacion());
        assertEquals("Nueva Solicitud", resultado.getTitulo());
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> notificacionService.obtenerPorId(99L));
    }

    @Test
    void obtenerPorUsuario_debeRetornarListaDeNotificaciones() {
        when(notificacionRepository.findByUsuarioDestinoId(100))
                .thenReturn(List.of(notificacion));

        List<NotificacionRespuesta> resultado = notificacionService.obtenerPorUsuario(100);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(100, resultado.get(0).getUsuarioDestinoId());
    }

    @Test
    void obtenerPorUsuarioPaginado_debeRetornarPaginaDeNotificaciones() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notificacion> paginaNotificaciones = new PageImpl<>(List.of(notificacion));

        when(notificacionRepository.findByUsuarioDestinoId(100, pageable))
                .thenReturn(paginaNotificaciones);

        Page<NotificacionRespuesta> resultado = notificacionService.obtenerPorUsuarioPaginado(100, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(100, resultado.getContent().get(0).getUsuarioDestinoId());
    }

    @Test
    void marcarComoLeida_debeActualizarEstadoALeida() {
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacion);

        NotificacionRespuesta resultado = notificacionService.marcarComoLeida(1L);

        assertNotNull(resultado);
        assertTrue(resultado.getLeida());
        verify(notificacionRepository, times(1)).save(notificacion);
    }

    @Test
    void eliminar_debeBorrarNotificacionSiExiste() {
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        notificacionService.eliminar(1L);

        verify(notificacionRepository, times(1)).delete(notificacion);
    }

    @Test
    void eliminar_debeLanzarExcepcionCuandoNoExiste() {
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> notificacionService.eliminar(99L));
        verify(notificacionRepository, never()).delete(any(Notificacion.class));
    }
}