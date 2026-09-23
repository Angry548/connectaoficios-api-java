package com.connectaoficios.api.dtos.mensaje;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MensajeGuardar {

    @NotNull(message = "La conversación es obligatoria")
    private Long conversacionId;

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(
            max = 2000,
            message = "El mensaje no puede superar los 2000 caracteres"
    )
    private String contenido;
}