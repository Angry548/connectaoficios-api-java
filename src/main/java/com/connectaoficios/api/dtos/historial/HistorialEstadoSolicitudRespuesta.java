package com.connectaoficios.api.dtos.historial;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class HistorialEstadoSolicitudRespuesta {

    private Long idHistorial;
    private Long solicitudId;
    private String estadoAnterior;
    private String estadoNuevo;
    private Integer cambiadoPorId;
    private String motivo;
    private LocalDateTime fechaCambio;
}