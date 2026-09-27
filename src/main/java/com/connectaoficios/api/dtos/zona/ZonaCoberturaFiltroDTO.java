package com.connectaoficios.api.dtos.zona;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZonaCoberturaFiltroDTO {

    private String texto;

    private String departamento;

    private String municipio;

    private String localidad;

    private Boolean activo;
}