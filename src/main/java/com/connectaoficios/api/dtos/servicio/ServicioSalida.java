package com.connectaoficios.api.dtos.servicio;

import com.connectaoficios.api.enums.EstadoServicio;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class ServicioSalida {

    private Long id;

    private Long perfilTrabajadorId;

    private Long categoriaId;

    private String titulo;

    private String descripcion;

    private BigDecimal tarifaMinima;

    private BigDecimal tarifaMaxima;

    private EstadoServicio estado;

    private Set<Long> zonasCoberturaIds = new LinkedHashSet<>();

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;
}