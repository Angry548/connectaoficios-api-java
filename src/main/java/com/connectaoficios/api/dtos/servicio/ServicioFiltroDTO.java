package com.connectaoficios.api.dtos.servicio;

import com.connectaoficios.api.enums.DiaSemana;
import com.connectaoficios.api.enums.EstadoServicio;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ServicioFiltroDTO {

    private String texto;

    private Long perfilTrabajadorId;

    private Long categoriaId;

    private Long zonaId;

    private DiaSemana diaSemana;

    private EstadoServicio estado;

    private BigDecimal tarifaMinima;

    private BigDecimal tarifaMaxima;
}