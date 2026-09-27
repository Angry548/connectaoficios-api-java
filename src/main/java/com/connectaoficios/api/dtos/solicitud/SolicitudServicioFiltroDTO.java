package com.connectaoficios.api.dtos.solicitud;

import com.connectaoficios.api.enums.EstadoSolicitud;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SolicitudServicioFiltroDTO {

    private String texto;

    private Long servicioId;

    private Integer clienteId;

    private Integer trabajadorId;

    private EstadoSolicitud estado;

    private LocalDate fechaDesde;

    private LocalDate fechaHasta;
}