package com.connectaoficios.api.dtos.mensaje;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MensajeFiltroDTO {

    private Long conversacionId;

    private Integer remitenteId;

    private Boolean leido;

    private String texto;

    private LocalDateTime fechaDesde;

    private LocalDateTime fechaHasta;
}