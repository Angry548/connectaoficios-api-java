package com.connectaoficios.api.dtos.historial;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistorialEstadoSolicitudGuardar {

    @NotNull(message = "El ID de la solicitud es obligatorio")
    private Long solicitudId;

    private String estadoAnterior;

    @NotBlank(message = "El estado nuevo es obligatorio")
    private String estadoNuevo;

    @NotNull(message = "El ID del usuario que realiza el cambio es obligatorio")
    private Integer cambiadoPorId;

    private String motivo;
}