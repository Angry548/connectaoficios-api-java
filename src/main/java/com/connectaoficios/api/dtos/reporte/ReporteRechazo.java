package com.connectaoficios.api.dtos.reporte;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReporteRechazo {

    @NotBlank(message = "La resolución es obligatoria")
    @Size(max = 2000, message = "La resolución no puede superar los 2000 caracteres")
    private String resolucion;
}
