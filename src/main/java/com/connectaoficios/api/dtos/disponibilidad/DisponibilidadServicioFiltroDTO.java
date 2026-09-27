package com.connectaoficios.api.dtos.disponibilidad;

import com.connectaoficios.api.enums.DiaSemana;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class DisponibilidadServicioFiltroDTO {

    private Long servicioId;

    private DiaSemana diaSemana;

    private Boolean activo;

    private LocalTime horaDesde;

    private LocalTime horaHasta;
}