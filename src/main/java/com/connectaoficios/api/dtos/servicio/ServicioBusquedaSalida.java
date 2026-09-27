package com.connectaoficios.api.dtos.servicio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioBusquedaSalida {

    private Long id;

    private String titulo;

    private Long categoriaId;

    private Long perfilTrabajadorId;

    private BigDecimal tarifaMinima;

    private BigDecimal tarifaMaxima;
}