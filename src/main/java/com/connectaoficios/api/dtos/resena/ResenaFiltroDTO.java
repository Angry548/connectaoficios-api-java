package com.connectaoficios.api.dtos.resena;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ResenaFiltroDTO {

    private Long perfilTrabajadorId;

    private Long servicioId;

    private Integer clienteId;

    private Integer calificacion;

    private String texto;

    private LocalDateTime fechaDesde;

    private LocalDateTime fechaHasta;
}