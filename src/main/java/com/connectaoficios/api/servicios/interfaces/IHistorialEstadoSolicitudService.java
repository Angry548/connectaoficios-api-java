package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.historial.HistorialEstadoSolicitudGuardar;
import com.connectaoficios.api.dtos.historial.HistorialEstadoSolicitudRespuesta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IHistorialEstadoSolicitudService {

    List<HistorialEstadoSolicitudRespuesta> obtenerTodos();

    HistorialEstadoSolicitudRespuesta obtenerPorId(Long id);

    Page<HistorialEstadoSolicitudRespuesta> obtenerTodosPaginados(Pageable pageable);

    List<HistorialEstadoSolicitudRespuesta> obtenerPorSolicitud(Long solicitudId);

    Page<HistorialEstadoSolicitudRespuesta> obtenerPorSolicitudPaginado(Long solicitudId, Pageable pageable);

    HistorialEstadoSolicitudRespuesta guardar(HistorialEstadoSolicitudGuardar historialGuardar);

    void eliminar(Long id);
}