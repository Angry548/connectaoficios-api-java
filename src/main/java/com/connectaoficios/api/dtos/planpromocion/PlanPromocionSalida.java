package com.connectaoficios.api.dtos.planpromocion;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PlanPromocionSalida {

    private Long id;

    private String nombre;

    private String descripcion;

    private Integer duracionDias;

    private BigDecimal precio;

    private Boolean activo;
}
