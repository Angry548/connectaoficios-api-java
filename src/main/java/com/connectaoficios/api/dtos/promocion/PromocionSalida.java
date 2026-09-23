package com.connectaoficios.api.dtos.promocion;

import com.connectaoficios.api.enums.EstadoPromocion;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PromocionSalida {

    private Long id;

    private Long servicioId;

    private Long planId;

    private String planNombre;

    private Integer trabajadorId;

    private EstadoPromocion estado;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private LocalDateTime fechaCreacion;

    private Boolean vigente;
}
