package com.connectaoficios.api.dtos.promocion;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PromocionResumenSalida {

    private Long servicioId;

    private String servicioTitulo;

    private Long planId;

    private String planNombre;

    private Integer duracionDias;

    private BigDecimal costoTotal;
}