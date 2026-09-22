package com.connectaoficios.api.dtos.solicitud;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class SolicitudServicioGuardar {

    @NotNull(message = "El ID del servicio es obligatorio")
    private Long servicioId;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Integer clienteId;

    @NotNull(message = "El ID del trabajador es obligatorio")
    private Integer trabajadorId;

    @NotNull(message = "La fecha propuesta es obligatoria")
    @FutureOrPresent(message = "La fecha propuesta debe ser hoy o una fecha futura")
    private LocalDate fechaPropuesta;

    @NotNull(message = "La hora aproximada es obligatoria")
    private LocalTime horaAproximada;

    @NotBlank(message = "La dirección del servicio es obligatoria")
    private String direccionServicio;

    @NotBlank(message = "La descripción del trabajo es obligatoria")
    private String descripcionTrabajo;
}

