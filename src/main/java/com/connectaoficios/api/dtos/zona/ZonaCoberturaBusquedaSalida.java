package com.connectaoficios.api.dtos.zona;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZonaCoberturaBusquedaSalida {

    private Long id;

    private String departamento;

    private String municipio;

    private String localidad;
}