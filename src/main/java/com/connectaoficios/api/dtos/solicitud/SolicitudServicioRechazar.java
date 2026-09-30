package com.connectaoficios.api.dtos.solicitud;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudServicioRechazar {

    @NotBlank(message = "El motivo del rechazo es obligatorio")
    @Size(
            max = 500,
            message = "El motivo del rechazo no puede superar los 500 caracteres"
    )
    private String motivoRechazo;
}