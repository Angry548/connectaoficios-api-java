package com.connectaoficios.api.dtos.transaccionpago;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransaccionPagoGuardar {

    @NotNull(message = "La promoción es obligatoria")
    private Long promocionId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotBlank(message = "La moneda es obligatoria")
    @Size(max = 10, message = "La moneda no puede superar los 10 caracteres")
    private String moneda;
}