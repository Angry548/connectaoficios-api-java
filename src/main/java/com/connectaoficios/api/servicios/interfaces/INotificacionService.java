package com.connectaoficios.api.servicios.interfaces;


import com.connectaoficios.api.dtos.notificacion.NotificacionGuardar;
import com.connectaoficios.api.dtos.notificacion.NotificacionRespuesta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface INotificacionService {

    List<NotificacionRespuesta> obtenerTodas();

    NotificacionRespuesta obtenerPorId(Long id);

    Page<NotificacionRespuesta> obtenerTodasPaginadas(Pageable pageable);

    List<NotificacionRespuesta> obtenerPorUsuario(Integer usuarioId);

    Page<NotificacionRespuesta> obtenerPorUsuarioPaginado(Integer usuarioId, Pageable pageable);

    NotificacionRespuesta guardar(NotificacionGuardar notificacionGuardar);

    NotificacionRespuesta marcarComoLeida(Long id);

    void eliminar(Long id);
}
