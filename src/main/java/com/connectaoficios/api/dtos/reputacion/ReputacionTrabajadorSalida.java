package com.connectaoficios.api.dtos.reputacion;

import com.connectaoficios.api.enums.InsigniaReputacion;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReputacionTrabajadorSalida {

    private Long id;

    private Long perfilTrabajadorId;

    private BigDecimal promedioCalificacion;

    private Integer totalResenas;

    private Integer serviciosCompletados;

    private BigDecimal puntuacionRanking;

    private Integer posicionRanking;

    private InsigniaReputacion insignia;

    private LocalDateTime fechaActualizacion;
}