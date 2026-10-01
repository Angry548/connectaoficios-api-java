package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.notificacion.NotificacionGuardar;
import com.connectaoficios.api.dtos.notificacion.NotificacionRespuesta;
import org.springframework.data.domain.Page;

public interface INotificacionService {

    NotificacionRespuesta guardar(
            NotificacionGuardar dto
    );

    NotificacionRespuesta obtenerPorId(
            Long id,
            Integer usuarioId
    );

    Page<NotificacionRespuesta> listarPorUsuario(
            Integer usuarioId,
            Boolean leida,
            int page,
            int size
    );

    long contarNoLeidas(
            Integer usuarioId
    );

    NotificacionRespuesta marcarComoLeida(
            Long id,
            Integer usuarioId
    );

    NotificacionRespuesta marcarComoNoLeida(
            Long id,
            Integer usuarioId
    );

    int marcarTodasComoLeidas(
            Integer usuarioId
    );
}