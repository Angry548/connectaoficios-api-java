package com.connectaoficios.api.dtos.solicitud;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
public class SolicitudServicioRespuesta {

    private Long idSolicitud;
    private Long servicioId;
    private Integer clienteId;
    private Integer trabajadorId;
    private LocalDate fechaPropuesta;
    private LocalTime horaAproximada;
    private String direccionServicio;
    private String descripcionTrabajo;
    private String estado;
    private String motivoCancelacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
