package com.connectaoficios.api.dtos.reporte;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReporteResolucion {

    @NotBlank(message = "La resolución es obligatoria")
    @Size(max = 2000, message = "La resolución no puede superar los 2000 caracteres")
    private String resolucion;

    @NotBlank(message = "La acción tomada es obligatoria")
    @Size(max = 255, message = "La acción tomada no puede superar los 255 caracteres")
    private String accionTomada;
}