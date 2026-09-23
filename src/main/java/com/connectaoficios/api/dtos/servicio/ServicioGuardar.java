package com.connectaoficios.api.dtos.servicio;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ServicioGuardar {

    @NotNull(message = "El perfil del trabajador es obligatorio")
    private Long perfilTrabajadorId;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede superar los 150 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;

    @NotNull(message = "La tarifa mínima es obligatoria")
    @DecimalMin(value = "0.0", message = "La tarifa mínima no puede ser negativa")
    private BigDecimal tarifaMinima;

    @DecimalMin(value = "0.0", message = "La tarifa máxima no puede ser negativa")
    private BigDecimal tarifaMaxima;

    private Set<Long> zonasCoberturaIds = new HashSet<>();
}