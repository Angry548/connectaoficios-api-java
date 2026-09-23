package com.connectaoficios.api.servicios.interfaces;


import com.connectaoficios.api.dtos.solicitud.SolicitudServicioCancelar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioGuardar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioRespuesta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ISolicitudServicioService {

    List<SolicitudServicioRespuesta> obtenerTodas();

    SolicitudServicioRespuesta obtenerPorId(Long id);

    Page<SolicitudServicioRespuesta> obtenerTodasPaginadas(Pageable pageable);

    List<SolicitudServicioRespuesta> obtenerPorCliente(Integer clienteId);

    List<SolicitudServicioRespuesta> obtenerPorTrabajador(Integer trabajadorId);

    SolicitudServicioRespuesta guardar(SolicitudServicioGuardar solicitudGuardar);

    void eliminar(Long id);

    SolicitudServicioRespuesta aceptar(Long id);

    SolicitudServicioRespuesta rechazar(Long id);

    SolicitudServicioRespuesta iniciar(Long id);

    SolicitudServicioRespuesta completar(Long id);

    SolicitudServicioRespuesta cancelar(Long id, SolicitudServicioCancelar solicitudCancelar);

    boolean esParticipante(Long id, Integer usuarioId);
}
