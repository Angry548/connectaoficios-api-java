package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.solicitud.SolicitudServicioCancelar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioGuardar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioRespuesta;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.SolicitudServicio;
import com.connectaoficios.api.repositorios.ISolicitudServicioRepository;
import com.connectaoficios.api.servicios.interfaces.ISolicitudServicioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitudServicioService implements ISolicitudServicioService {

    private final ISolicitudServicioRepository solicitudRepository;

    public SolicitudServicioService(ISolicitudServicioRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioRespuesta> obtenerTodas() {
        return solicitudRepository.findAll()
                .stream()
                .map(this::convertirARespuesta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudServicioRespuesta obtenerPorId(Long id) {
        SolicitudServicio solicitud = buscarPorId(id);
        return convertirARespuesta(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SolicitudServicioRespuesta> obtenerTodasPaginadas(Pageable pageable) {
        return solicitudRepository.findAll(pageable)
                .map(this::convertirARespuesta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioRespuesta> obtenerPorCliente(Integer clienteId) {
        return solicitudRepository.findByClienteId(clienteId)
                .stream()
                .map(this::convertirARespuesta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudServicioRespuesta> obtenerPorTrabajador(Integer trabajadorId) {
        return solicitudRepository.findByTrabajadorId(trabajadorId)
                .stream()
                .map(this::convertirARespuesta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SolicitudServicioRespuesta guardar(SolicitudServicioGuardar dto) {
        SolicitudServicio solicitud = new SolicitudServicio();

        solicitud.setClienteId(dto.getClienteId());
        solicitud.setTrabajadorId(dto.getTrabajadorId());
        solicitud.setFechaPropuesta(dto.getFechaPropuesta());
        solicitud.setHoraAproximada(dto.getHoraAproximada());
        solicitud.setDireccionServicio(dto.getDireccionServicio());
        solicitud.setDescripcionTrabajo(dto.getDescripcionTrabajo());

        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaCreacion(LocalDateTime.now());
        solicitud.setFechaActualizacion(LocalDateTime.now());

        SolicitudServicio guardada = solicitudRepository.save(solicitud);
        return convertirARespuesta(guardada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        SolicitudServicio solicitud = buscarPorId(id);
        solicitudRepository.delete(solicitud);
    }

    @Override
    @Transactional
    public SolicitudServicioRespuesta aceptar(Long id) {
        SolicitudServicio solicitud = buscarPorId(id);
        validarTransicionEstado(solicitud, EstadoSolicitud.ACEPTADA);

        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        solicitud.setFechaActualizacion(LocalDateTime.now());

        return convertirARespuesta(solicitudRepository.save(solicitud));
    }

    @Override
    @Transactional
    public SolicitudServicioRespuesta rechazar(Long id) {
        SolicitudServicio solicitud = buscarPorId(id);
        validarTransicionEstado(solicitud, EstadoSolicitud.RECHAZADA);

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setFechaActualizacion(LocalDateTime.now());

        return convertirARespuesta(solicitudRepository.save(solicitud));
    }

    @Override
    @Transactional
    public SolicitudServicioRespuesta iniciar(Long id) {
        SolicitudServicio solicitud = buscarPorId(id);
        validarTransicionEstado(solicitud, EstadoSolicitud.EN_PROCESO);

        solicitud.setEstado(EstadoSolicitud.EN_PROCESO);
        solicitud.setFechaActualizacion(LocalDateTime.now());

        return convertirARespuesta(solicitudRepository.save(solicitud));
    }

    @Override
    @Transactional
    public SolicitudServicioRespuesta completar(Long id) {
        SolicitudServicio solicitud = buscarPorId(id);
        validarTransicionEstado(solicitud, EstadoSolicitud.COMPLETADA);

        solicitud.setEstado(EstadoSolicitud.COMPLETADA);
        solicitud.setFechaActualizacion(LocalDateTime.now());

        return convertirARespuesta(solicitudRepository.save(solicitud));
    }

    @Override
    @Transactional
    public SolicitudServicioRespuesta cancelar(Long id, SolicitudServicioCancelar dto) {
        SolicitudServicio solicitud = buscarPorId(id);
        validarTransicionEstado(solicitud, EstadoSolicitud.CANCELADA);

        solicitud.setEstado(EstadoSolicitud.CANCELADA);
        solicitud.setMotivoCancelacion(dto.getMotivoCancelacion());
        solicitud.setFechaActualizacion(LocalDateTime.now());

        return convertirARespuesta(solicitudRepository.save(solicitud));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esParticipante(Long id, Integer usuarioId) {
        SolicitudServicio solicitud = buscarPorId(id);
        return usuarioId.equals(solicitud.getClienteId()) || usuarioId.equals(solicitud.getTrabajadorId());
    }

    // --- Métodos Privados Auxiliares ---

    private SolicitudServicio buscarPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la solicitud de servicio con ID: " + id));
    }

    private void validarTransicionEstado(SolicitudServicio solicitud, EstadoSolicitud nuevoEstado) {
        if (!solicitud.puedeCambiarA(nuevoEstado)) {
            throw new ReglaNegocioException(
                    String.format("No se puede cambiar el estado de la solicitud de %s a %s",
                            solicitud.getEstado(), nuevoEstado)
            );
        }
    }

    private SolicitudServicioRespuesta convertirARespuesta(SolicitudServicio entidad) {
        SolicitudServicioRespuesta respuesta = new SolicitudServicioRespuesta();

        respuesta.setIdSolicitud(entidad.getIdSolicitud());
        if (entidad.getServicio() != null) {
            respuesta.setServicioId(entidad.getServicio().getId());
        }
        respuesta.setClienteId(entidad.getClienteId());
        respuesta.setTrabajadorId(entidad.getTrabajadorId());
        respuesta.setFechaPropuesta(entidad.getFechaPropuesta());
        respuesta.setHoraAproximada(entidad.getHoraAproximada());
        respuesta.setDireccionServicio(entidad.getDireccionServicio());
        respuesta.setDescripcionTrabajo(entidad.getDescripcionTrabajo());
        respuesta.setEstado(entidad.getEstado() != null ? entidad.getEstado().name() : null);
        respuesta.setMotivoCancelacion(entidad.getMotivoCancelacion());
        respuesta.setFechaCreacion(entidad.getFechaCreacion());
        respuesta.setFechaActualizacion(entidad.getFechaActualizacion());

        return respuesta;
    }
}
