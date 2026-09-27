package com.connectaoficios.api.dtos.conversacion;

import com.connectaoficios.api.enums.EstadoSolicitud;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ConversacionFiltroDTO {

    private Long solicitudId;

    private EstadoSolicitud estadoSolicitud;

    private LocalDateTime fechaDesde;

    private LocalDateTime fechaHasta;

    private Boolean puedeEnviarMensajes;
}