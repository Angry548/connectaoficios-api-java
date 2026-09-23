package com.connectaoficios.api.dtos.servicio;

import com.connectaoficios.api.enums.EstadoServicio;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicioCambiarEstado {

    @NotNull(message = "El estado es obligatorio")
    private EstadoServicio estado;
}