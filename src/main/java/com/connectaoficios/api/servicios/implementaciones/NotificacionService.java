package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.notificacion.NotificacionGuardar;
import com.connectaoficios.api.dtos.notificacion.NotificacionRespuesta;
import com.connectaoficios.api.enums.TipoNotificacion;
import com.connectaoficios.api.modelos.Notificacion;
import com.connectaoficios.api.repositorios.INotificacionRepository;
import com.connectaoficios.api.servicios.interfaces.INotificacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class NotificacionService implements INotificacionService {

    private final INotificacionRepository notificacionRepository;

    public NotificacionService(
            INotificacionRepository notificacionRepository
    ) {
        this.notificacionRepository = notificacionRepository;
    }

    @Override
    @Transactional
    public NotificacionRespuesta guardar(
            NotificacionGuardar dto
    ) {
        Notificacion notificacion = new Notificacion();

        notificacion.setUsuarioDestinoId(
                dto.getUsuarioDestinoId()
        );

        notificacion.setTipo(
                convertirTipo(dto.getTipo())
        );

        notificacion.setTitulo(
                dto.getTitulo().trim()
        );

        notificacion.setMensaje(
                dto.getMensaje().trim()
        );

        notificacion.setReferenciaId(
                dto.getReferenciaId()
        );

        notificacion.setLeida(false);

        Notificacion guardada =
                notificacionRepository.save(notificacion);

        return convertirRespuesta(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificacionRespuesta obtenerPorId(
            Long id,
            Integer usuarioId
    ) {
        Notificacion notificacion =
                obtenerNotificacionDelUsuario(
                        id,
                        usuarioId
                );

        return convertirRespuesta(notificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificacionRespuesta> listarPorUsuario(
            Integer usuarioId,
            Boolean leida,
            int page,
            int size
    ) {
        int paginaSegura = Math.max(page, 0);
        int tamanoSeguro = Math.min(
                Math.max(size, 1),
                50
        );

        Pageable pageable = PageRequest.of(
                paginaSegura,
                tamanoSeguro,
                Sort.by(
                        Sort.Direction.DESC,
                        "fechaCreacion"
                )
        );

        Page<Notificacion> notificaciones;

        if (leida == null) {
            notificaciones =
                    notificacionRepository
                            .findByUsuarioDestinoId(
                                    usuarioId,
                                    pageable
                            );
        } else {
            notificaciones =
                    notificacionRepository
                            .findByUsuarioDestinoIdAndLeida(
                                    usuarioId,
                                    leida,
                                    pageable
                            );
        }

        return notificaciones.map(
                this::convertirRespuesta
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidas(
            Integer usuarioId
    ) {
        return notificacionRepository
                .countByUsuarioDestinoIdAndLeidaFalse(
                        usuarioId
                );
    }

    @Override
    @Transactional
    public NotificacionRespuesta marcarComoLeida(
            Long id,
            Integer usuarioId
    ) {
        Notificacion notificacion =
                obtenerNotificacionDelUsuario(
                        id,
                        usuarioId
                );

        if (!Boolean.TRUE.equals(
                notificacion.getLeida()
        )) {
            notificacion.setLeida(true);
            notificacion =
                    notificacionRepository.save(
                            notificacion
                    );
        }

        return convertirRespuesta(notificacion);
    }

    @Override
    @Transactional
    public NotificacionRespuesta marcarComoNoLeida(
            Long id,
            Integer usuarioId
    ) {
        Notificacion notificacion =
                obtenerNotificacionDelUsuario(
                        id,
                        usuarioId
                );

        if (!Boolean.FALSE.equals(
                notificacion.getLeida()
        )) {
            notificacion.setLeida(false);
            notificacion =
                    notificacionRepository.save(
                            notificacion
                    );
        }

        return convertirRespuesta(notificacion);
    }

    @Override
    @Transactional
    public int marcarTodasComoLeidas(
            Integer usuarioId
    ) {
        List<Notificacion> pendientes =
                notificacionRepository
                        .findByUsuarioDestinoIdAndLeida(
                                usuarioId,
                                false
                        );

        if (pendientes.isEmpty()) {
            return 0;
        }

        pendientes.forEach(
                notificacion ->
                        notificacion.setLeida(true)
        );

        notificacionRepository.saveAll(pendientes);

        return pendientes.size();
    }

    private Notificacion obtenerNotificacionDelUsuario(
            Long id,
            Integer usuarioId
    ) {
        Notificacion notificacion =
                notificacionRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "La notificación no existe."
                                )
                        );

        if (!notificacion
                .getUsuarioDestinoId()
                .equals(usuarioId)) {
            throw new RuntimeException(
                    "No tienes permiso para acceder a esta notificación."
            );
        }

        return notificacion;
    }

    private TipoNotificacion convertirTipo(
            String tipo
    ) {
        try {
            return TipoNotificacion.valueOf(
                    tipo.trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "El tipo de notificación no es válido."
            );
        }
    }

    private NotificacionRespuesta convertirRespuesta(
            Notificacion notificacion
    ) {
        NotificacionRespuesta respuesta =
                new NotificacionRespuesta();

        respuesta.setIdNotificacion(
                notificacion.getId()
        );

        respuesta.setUsuarioDestinoId(
                notificacion.getUsuarioDestinoId()
        );

        respuesta.setTipo(
                notificacion.getTipo().name()
        );

        respuesta.setTitulo(
                notificacion.getTitulo()
        );

        respuesta.setMensaje(
                notificacion.getMensaje()
        );

        respuesta.setReferenciaId(
                notificacion.getReferenciaId()
        );

        respuesta.setLeida(
                notificacion.getLeida()
        );

        respuesta.setFechaCreacion(
                notificacion.getFechaCreacion()
        );

        return respuesta;
    }
}