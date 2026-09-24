package com.connectaoficios.api.dtos.disponibilidad;

import com.connectaoficios.api.enums.DiaSemana;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class DisponibilidadServicioGuardar {

    @NotNull(message = "El servicio es obligatorio")
    private Long servicioId;

    @NotNull(message = "El día de la semana es obligatorio")
    private DiaSemana diaSemana;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;
}