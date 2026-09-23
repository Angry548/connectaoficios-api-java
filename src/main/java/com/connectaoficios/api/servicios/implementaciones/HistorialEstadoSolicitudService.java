package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.historial.HistorialEstadoSolicitudGuardar;
import com.connectaoficios.api.dtos.historial.HistorialEstadoSolicitudRespuesta;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.modelos.HistorialEstadoSolicitud;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.IHistorialEstadoSolicitudRepository;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;
import com.connectaoficios.api.servicios.interfaces.IHistorialEstadoSolicitudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistorialEstadoSolicitudService implements IHistorialEstadoSolicitudService {

    private final IHistorialEstadoSolicitudRepository historialRepository;
    private final ISolicitudServicioRepository solicitudRepository;

    public HistorialEstadoSolicitudService(
            IHistorialEstadoSolicitudRepository historialRepository,
            ISolicitudServicioRepository solicitudRepository
    ) {
        this.historialRepository = historialRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialEstadoSolicitudRespuesta> obtenerTodos() {
        return historialRepository.findAll()
                .stream()
                .map(this::convertirARespuesta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialEstadoSolicitudRespuesta obtenerPorId(Long id) {
        HistorialEstadoSolicitud historial = buscarPorId(id);
        return convertirARespuesta(historial);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialEstadoSolicitudRespuesta> obtenerTodosPaginados(Pageable pageable) {
        return historialRepository.findAll(pageable)
                .map(this::convertirARespuesta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialEstadoSolicitudRespuesta> obtenerPorSolicitud(Long solicitudId) {
        validarExisteSolicitud(solicitudId);
        return historialRepository.findBySolicitud_IdSolicitudOrderByFechaCambioDesc(solicitudId)
                .stream()
                .map(this::convertirARespuesta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialEstadoSolicitudRespuesta> obtenerPorSolicitudPaginado(Long solicitudId, Pageable pageable) {
        validarExisteSolicitud(solicitudId);
        return historialRepository.findBySolicitud_IdSolicitud(solicitudId, pageable)
                .map(this::convertirARespuesta);
    }

    @Override
    @Transactional
    public HistorialEstadoSolicitudRespuesta guardar(HistorialEstadoSolicitudGuardar dto) {
        SolicitudServicio solicitud = solicitudRepository.findById(dto.getSolicitudId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la solicitud de servicio con ID: " + dto.getSolicitudId()
                ));

        HistorialEstadoSolicitud historial = new HistorialEstadoSolicitud();
        historial.setSolicitud(solicitud);

        if (dto.getEstadoAnterior() != null) {
            historial.setEstadoAnterior(EstadoSolicitud.valueOf(dto.getEstadoAnterior().toUpperCase()));
        }

        historial.setEstadoNuevo(EstadoSolicitud.valueOf(dto.getEstadoNuevo().toUpperCase()));
        historial.setCambiadoPorId(dto.getCambiadoPorId());
        historial.setMotivo(dto.getMotivo());
        historial.setFechaCambio(LocalDateTime.now());

        HistorialEstadoSolicitud guardado = historialRepository.save(historial);
        return convertirARespuesta(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        HistorialEstadoSolicitud historial = buscarPorId(id);
        historialRepository.delete(historial);
    }

    // --- Métodos Privados Auxiliares ---

    private HistorialEstadoSolicitud buscarPorId(Long id) {
        return historialRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el registro de historial con ID: " + id
                ));
    }

    private void validarExisteSolicitud(Long solicitudId) {
        if (!solicitudRepository.existsById(solicitudId)) {
            throw new RecursoNoEncontradoException("No existe la solicitud con ID: " + solicitudId);
        }
    }

    private HistorialEstadoSolicitudRespuesta convertirARespuesta(HistorialEstadoSolicitud entidad) {
        HistorialEstadoSolicitudRespuesta respuesta = new HistorialEstadoSolicitudRespuesta();

        respuesta.setIdHistorial(entidad.getIdHistorial());
        if (entidad.getSolicitud() != null) {
            respuesta.setSolicitudId(entidad.getSolicitud().getIdSolicitud());
        }
        respuesta.setEstadoAnterior(entidad.getEstadoAnterior() != null ? entidad.getEstadoAnterior().name() : null);
        respuesta.setEstadoNuevo(entidad.getEstadoNuevo() != null ? entidad.getEstadoNuevo().name() : null);
        respuesta.setCambiadoPorId(entidad.getCambiadoPorId());
        respuesta.setMotivo(entidad.getMotivo());
        respuesta.setFechaCambio(entidad.getFechaCambio());

        return respuesta;
    }
}
