package com.connectaoficios.api.dtos.solicitud;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudServicioCancelar {

    @NotBlank(message = "El motivo de cancelación es obligatorio")
    private String motivoCancelacion;
}
