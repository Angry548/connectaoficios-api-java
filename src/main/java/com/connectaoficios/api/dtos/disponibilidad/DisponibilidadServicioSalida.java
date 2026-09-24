package com.connectaoficios.api.dtos.disponibilidad;

import com.connectaoficios.api.enums.DiaSemana;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class DisponibilidadServicioSalida {

    private Long id;

    private Long servicioId;

    private DiaSemana diaSemana;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private Boolean activo;
}