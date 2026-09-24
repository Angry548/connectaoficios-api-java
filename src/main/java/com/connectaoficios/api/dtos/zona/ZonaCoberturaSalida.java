package com.connectaoficios.api.dtos.zona;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZonaCoberturaSalida {

    private Long id;

    private String departamento;

    private String municipio;

    private String localidad;

    private Boolean activo;
}