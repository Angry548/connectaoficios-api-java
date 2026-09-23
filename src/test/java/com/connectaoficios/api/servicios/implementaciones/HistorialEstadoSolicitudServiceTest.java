package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.historial.HistorialEstadoSolicitudGuardar;
import com.connectaoficios.api.dtos.historial.HistorialEstadoSolicitudRespuesta;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.modelos.HistorialEstadoSolicitud;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.IHistorialEstadoSolicitudRepository;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;
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
class HistorialEstadoSolicitudServiceTest {

    @Mock
    private IHistorialEstadoSolicitudRepository historialRepository;

    @Mock
    private ISolicitudServicioRepository solicitudRepository;

    private HistorialEstadoSolicitudService historialService;

    private SolicitudServicio solicitud;
    private HistorialEstadoSolicitud historial;

    @BeforeEach
    void setUp() {
        historialService = new HistorialEstadoSolicitudService(historialRepository, solicitudRepository);

        solicitud = new SolicitudServicio();
        solicitud.setIdSolicitud(1L);

        historial = new HistorialEstadoSolicitud();
        historial.setIdHistorial(10L);
        historial.setSolicitud(solicitud);
        historial.setEstadoAnterior(EstadoSolicitud.PENDIENTE);
        historial.setEstadoNuevo(EstadoSolicitud.ACEPTADA);
        historial.setCambiadoPorId(200);
        historial.setMotivo("Aceptado por el trabajador");
        historial.setFechaCambio(LocalDateTime.now());
    }

    @Test
    void guardar_debeCrearHistorialNuevo() {
        HistorialEstadoSolicitudGuardar dto = new HistorialEstadoSolicitudGuardar();
        dto.setSolicitudId(1L);
        dto.setEstadoAnterior("PENDIENTE");
        dto.setEstadoNuevo("ACEPTADA");
        dto.setCambiadoPorId(200);
        dto.setMotivo("Aceptado por el trabajador");

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(historialRepository.save(any(HistorialEstadoSolicitud.class)))
                .thenAnswer(invocation -> {
                    HistorialEstadoSolicitud guardado = invocation.getArgument(0);
                    guardado.setIdHistorial(10L);
                    return guardado;
                });

        HistorialEstadoSolicitudRespuesta resultado = historialService.guardar(dto);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getIdHistorial());
        assertEquals(1L, resultado.getSolicitudId());
        assertEquals(EstadoSolicitud.PENDIENTE.name(), resultado.getEstadoAnterior());
        assertEquals(EstadoSolicitud.ACEPTADA.name(), resultado.getEstadoNuevo());
        assertEquals(200, resultado.getCambiadoPorId());

        verify(historialRepository, times(1)).save(any(HistorialEstadoSolicitud.class));
    }

    @Test
    void guardar_debeLanzarExcepcion_cuandoSolicitudNoExiste() {
        HistorialEstadoSolicitudGuardar dto = new HistorialEstadoSolicitudGuardar();
        dto.setSolicitudId(99L);
        dto.setEstadoNuevo("ACEPTADA");

        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> historialService.guardar(dto));

        verify(historialRepository, never()).save(any(HistorialEstadoSolicitud.class));
    }

    @Test
    void guardar_debeConvertirEstadoEnMinusculasAEnumCorrecto() {
        HistorialEstadoSolicitudGuardar dto = new HistorialEstadoSolicitudGuardar();
        dto.setSolicitudId(1L);
        dto.setEstadoAnterior("pendiente");
        dto.setEstadoNuevo("aceptada");
        dto.setCambiadoPorId(200);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(historialRepository.save(any(HistorialEstadoSolicitud.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HistorialEstadoSolicitudRespuesta resultado = historialService.guardar(dto);

        assertNotNull(resultado);
        assertEquals("PENDIENTE", resultado.getEstadoAnterior());
        assertEquals("ACEPTADA", resultado.getEstadoNuevo());
    }

    @Test
    void obtenerPorId_debeRetornarHistorial() {
        when(historialRepository.findById(10L)).thenReturn(Optional.of(historial));

        HistorialEstadoSolicitudRespuesta resultado = historialService.obtenerPorId(10L);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getIdHistorial());
        assertEquals("Aceptado por el trabajador", resultado.getMotivo());
    }

    @Test
    void obtenerPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(historialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> historialService.obtenerPorId(99L));
    }

    @Test
    void obtenerPorSolicitud_debeRetornarListaDeHistoriales() {
        when(solicitudRepository.existsById(1L)).thenReturn(true);
        when(historialRepository.findBySolicitud_IdSolicitudOrderByFechaCambioDesc(1L))
                .thenReturn(List.of(historial));

        List<HistorialEstadoSolicitudRespuesta> resultado = historialService.obtenerPorSolicitud(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getSolicitudId());
    }

    @Test
    void obtenerPorSolicitud_debeLanzarExcepcion_cuandoSolicitudNoExiste() {
        when(solicitudRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> historialService.obtenerPorSolicitud(99L));
    }

    @Test
    void obtenerPorSolicitudPaginado_debeRetornarPaginaDeHistoriales() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<HistorialEstadoSolicitud> pagina = new PageImpl<>(List.of(historial));

        when(solicitudRepository.existsById(1L)).thenReturn(true);
        when(historialRepository.findBySolicitud_IdSolicitud(1L, pageable)).thenReturn(pagina);

        Page<HistorialEstadoSolicitudRespuesta> resultado = historialService.obtenerPorSolicitudPaginado(1L, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1L, resultado.getContent().get(0).getSolicitudId());
    }

    @Test
    void eliminar_debeBorrarHistorialSiExiste() {
        when(historialRepository.findById(10L)).thenReturn(Optional.of(historial));

        historialService.eliminar(10L);

        verify(historialRepository, times(1)).delete(historial);
    }

    @Test
    void eliminar_debeLanzarExcepcionCuandoNoExiste() {
        when(historialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> historialService.eliminar(99L));
        verify(historialRepository, never()).delete(any(HistorialEstadoSolicitud.class));
    }
}
