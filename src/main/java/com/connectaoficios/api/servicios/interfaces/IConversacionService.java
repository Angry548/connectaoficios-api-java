package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.conversacion.ConversacionGuardar;
import com.connectaoficios.api.dtos.conversacion.ConversacionSalida;

public interface IConversacionService {

    ConversacionSalida guardar(
            ConversacionGuardar conversacionGuardar,
            Integer usuarioId
    );

    ConversacionSalida obtenerPorId(
            Long id,
            Integer usuarioId
    );

    ConversacionSalida obtenerPorSolicitud(
            Long solicitudId,
            Integer usuarioId
    );
}