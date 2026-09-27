package com.connectaoficios.api.dtos.perfil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilTrabajadorBusquedaSalida {

    private Long id;

    private Integer trabajadorId;

    private String oficioPrincipal;

    private Long zonaPrincipalId;

    private String departamento;

    private String municipio;

    private String localidad;
}