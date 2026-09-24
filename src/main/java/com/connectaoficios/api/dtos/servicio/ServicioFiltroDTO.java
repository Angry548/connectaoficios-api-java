package com.connectaoficios.api.dtos.servicio;

import com.connectaoficios.api.enums.DiaSemana;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ServicioFiltroDTO {

    private Long categoriaId;

    private Long zonaId;

    private DiaSemana diaSemana;

    private BigDecimal tarifaMinima;

    private BigDecimal tarifaMaxima;
}