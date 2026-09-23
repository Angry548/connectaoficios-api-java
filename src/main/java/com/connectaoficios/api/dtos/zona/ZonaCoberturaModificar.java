package com.connectaoficios.api.dtos.zona;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZonaCoberturaModificar {

    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 100, message = "El departamento no puede superar los 100 caracteres")
    private String departamento;

    @NotBlank(message = "El municipio es obligatorio")
    @Size(max = 100, message = "El municipio no puede superar los 100 caracteres")
    private String municipio;

    @Size(max = 150, message = "La localidad no puede superar los 150 caracteres")
    private String localidad;
}