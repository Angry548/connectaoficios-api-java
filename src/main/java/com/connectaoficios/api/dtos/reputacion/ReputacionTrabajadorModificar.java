package com.connectaoficios.api.dtos.reputacion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReputacionTrabajadorModificar {

    @NotNull(message = "La cantidad de servicios completados es obligatoria")
    @Min(value = 0, message = "Los servicios completados no pueden ser negativos")
    private Integer serviciosCompletados;
}