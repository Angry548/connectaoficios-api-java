package com.connectaoficios.api.dtos.conversacion;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversacionGuardar {

    @NotNull(message = "La solicitud de servicio es obligatoria")
    private Long solicitudId;
}