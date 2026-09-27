package com.connectaoficios.api.dtos.reputacion;

import com.connectaoficios.api.enums.InsigniaReputacion;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReputacionTrabajadorFiltroDTO {

    private Long perfilTrabajadorId;

    private InsigniaReputacion insignia;

    private BigDecimal promedioMinimo;

    private BigDecimal promedioMaximo;

    private Integer totalResenasMinimo;

    private Integer totalResenasMaximo;

    private Integer serviciosCompletadosMinimo;

    private Integer serviciosCompletadosMaximo;

    private BigDecimal puntuacionMinima;

    private BigDecimal puntuacionMaxima;
}