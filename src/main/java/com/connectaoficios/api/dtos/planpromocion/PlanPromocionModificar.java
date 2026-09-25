package com.connectaoficios.api.dtos.planpromocion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PlanPromocionModificar {

    @Size(
            max = 100,
            message = "El nombre no puede superar los 100 caracteres"
    )
    private String nombre;

    @Size(
            max = 500,
            message = "La descripción no puede superar los 500 caracteres"
    )
    private String descripcion;

    @Min(
            value = 1,
            message = "La duración debe ser de al menos 1 día"
    )
    private Integer duracionDias;

    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "El precio debe ser mayor a 0"
    )
    private BigDecimal precio;
}