package com.connectaoficios.api.dtos.promocion;

import com.connectaoficios.api.enums.EstadoPromocion;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PromocionFiltroDTO {

    private Long servicioId;

    private Long planId;

    private Integer trabajadorId;

    private EstadoPromocion estado;

    private LocalDateTime fechaCreacionDesde;

    private LocalDateTime fechaCreacionHasta;

    private LocalDateTime fechaInicioDesde;

    private LocalDateTime fechaInicioHasta;

    private LocalDateTime fechaFinDesde;

    private LocalDateTime fechaFinHasta;
}