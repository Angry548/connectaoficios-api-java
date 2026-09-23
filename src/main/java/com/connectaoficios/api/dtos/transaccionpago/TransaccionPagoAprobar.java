package com.connectaoficios.api.dtos.transaccionpago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransaccionPagoAprobar {

    @NotBlank(message = "La referencia externa es obligatoria")
    @Size(max = 255, message = "La referencia externa no puede superar los 255 caracteres")
    private String referenciaExterna;
}