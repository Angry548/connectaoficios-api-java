package com.connectaoficios.api.dtos.planpromocion;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PlanPromocionFiltroDTO {

    private String texto;

    private Boolean activo;

    private Integer duracionMinima;

    private Integer duracionMaxima;

    private BigDecimal precioMinimo;

    private BigDecimal precioMaximo;
}