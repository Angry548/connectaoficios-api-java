package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioCancelar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioFiltroDTO;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioGuardar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioRespuesta;

import java.util.List;

public interface ISolicitudServicioService {

    List<SolicitudServicioRespuesta> obtenerTodas();

    SolicitudServicioRespuesta obtenerPorId(
            Long id
    );

    PaginaSalida<SolicitudServicioRespuesta> buscarConFiltros(
            SolicitudServicioFiltroDTO filtro,
            int pagina,
            int tamanio
    );

    List<SolicitudServicioRespuesta> obtenerPorCliente(
            Integer clienteId
    );

    List<SolicitudServicioRespuesta> obtenerPorTrabajador(
            Integer trabajadorId
    );

    SolicitudServicioRespuesta guardar(
            SolicitudServicioGuardar solicitudGuardar
    );

    void eliminar(Long id);

    SolicitudServicioRespuesta aceptar(
            Long id
    );

    SolicitudServicioRespuesta rechazar(
            Long id
    );

    SolicitudServicioRespuesta iniciar(
            Long id
    );

    SolicitudServicioRespuesta completar(
            Long id
    );

    SolicitudServicioRespuesta cancelar(
            Long id,
            SolicitudServicioCancelar solicitudCancelar
    );

    boolean esParticipante(
            Long id,
            Integer usuarioId
    );
}