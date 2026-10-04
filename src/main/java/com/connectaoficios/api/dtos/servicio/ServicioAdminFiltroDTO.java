package com.connectaoficios.api.dtos.servicio;

import com.connectaoficios.api.enums.EstadoServicio;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicioAdminFiltroDTO {

    private String texto;

    private Integer trabajadorId;

    private Long categoriaId;

    private EstadoServicio estado;
}