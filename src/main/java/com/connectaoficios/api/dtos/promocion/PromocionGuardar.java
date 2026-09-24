package com.connectaoficios.api.dtos.promocion;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromocionGuardar {

    @NotNull(message = "El servicio es obligatorio")
    private Long servicioId;

    @NotNull(message = "El plan de promoción es obligatorio")
    private Long planId;
}