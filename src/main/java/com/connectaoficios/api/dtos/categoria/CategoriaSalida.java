package com.connectaoficios.api.dtos.categoria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaSalida {

    private Long id;

    private String nombre;

    private String descripcion;

    private Boolean activo;
}