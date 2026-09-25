package com.connectaoficios.api.dtos.disponibilidad;

import com.connectaoficios.api.enums.DiaSemana;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class DisponibilidadServicioModificar {

    private DiaSemana diaSemana;

    private LocalTime horaInicio;

    private LocalTime horaFin;
}