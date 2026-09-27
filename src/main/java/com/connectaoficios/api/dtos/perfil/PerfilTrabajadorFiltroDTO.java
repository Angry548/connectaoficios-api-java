package com.connectaoficios.api.dtos.perfil;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilTrabajadorFiltroDTO {

    private String texto;

    private Long zonaPrincipalId;

    private String departamento;

    private String municipio;

    private Integer porcentajeCompletitudMinimo;

    private Integer porcentajeCompletitudMaximo;
}