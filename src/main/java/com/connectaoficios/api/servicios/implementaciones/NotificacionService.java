package com.connectaoficios.api.servicios.implementaciones;


import com.connectaoficios.api.dtos.notificacion.NotificacionGuardar;
import com.connectaoficios.api.dtos.notificacion.NotificacionRespuesta;
import com.connectaoficios.api.enums.TipoNotificacion;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.modelos.Notificacion;
import com.connectaoficios.api.repositorios.INotificacionRepository;
import com.connectaoficios.api.servicios.interfaces.INotificacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService implements INotificacionService {

    private final INotificacionRepository notificacionRepository;

    public NotificacionService(INotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionRespuesta> obtenerTodas() {
        return notificacionRepository.findAll()
                .stream()
                .map(this::convertirARespuesta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificacionRespuesta obtenerPorId(Long id) {
        Notificacion notificacion = buscarPorId(id);
        return convertirARespuesta(notificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificacionRespuesta> obtenerTodasPaginadas(Pageable pageable) {
        return notificacionRepository.findAll(pageable)
                .map(this::convertirARespuesta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionRespuesta> obtenerPorUsuario(Integer usuarioId) {
        return notificacionRepository.findByUsuarioDestinoId(usuarioId)
                .stream()
                .map(this::convertirARespuesta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificacionRespuesta> obtenerPorUsuarioPaginado(Integer usuarioId, Pageable pageable) {
        return notificacionRepository.findByUsuarioDestinoId(usuarioId, pageable)
                .map(this::convertirARespuesta);
    }

    @Override
    @Transactional
    public NotificacionRespuesta guardar(NotificacionGuardar dto) {
        Notificacion notificacion = new Notificacion();

        notificacion.setUsuarioDestinoId(dto.getUsuarioDestinoId());
        notificacion.setTipo(TipoNotificacion.valueOf(dto.getTipo().toUpperCase()));
        notificacion.setTitulo(dto.getTitulo());
        notificacion.setMensaje(dto.getMensaje());
        notificacion.setReferenciaId(dto.getReferenciaId());

        // Valores por defecto al crear
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());

        Notificacion guardada = notificacionRepository.save(notificacion);
        return convertirARespuesta(guardada);
    }

    @Override
    @Transactional
    public NotificacionRespuesta marcarComoLeida(Long id) {
        Notificacion notificacion = buscarPorId(id);

        notificacion.setLeida(true);

        return convertirARespuesta(notificacionRepository.save(notificacion));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Notificacion notificacion = buscarPorId(id);
        notificacionRepository.delete(notificacion);
    }

    // --- Métodos Privados Auxiliares ---

    private Notificacion buscarPorId(Long id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la notificación con ID: " + id));
    }

    private NotificacionRespuesta convertirARespuesta(Notificacion entidad) {
        NotificacionRespuesta respuesta = new NotificacionRespuesta();

        respuesta.setIdNotificacion(entidad.getIdNotificacion());
        respuesta.setUsuarioDestinoId(entidad.getUsuarioDestinoId());
        respuesta.setTipo(entidad.getTipo() != null ? entidad.getTipo().name() : null);
        respuesta.setTitulo(entidad.getTitulo());
        respuesta.setMensaje(entidad.getMensaje());
        respuesta.setReferenciaId(entidad.getReferenciaId());
        respuesta.setLeida(entidad.getLeida());
        respuesta.setFechaCreacion(entidad.getFechaCreacion());

        return respuesta;
    }
}
