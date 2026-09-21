package com.connectaoficios.api.dtos.reputacion;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReputacionTrabajadorGuardar {

    @NotNull(message = "El perfil del trabajador es obligatorio")
    private Long perfilTrabajadorId;
}