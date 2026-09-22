package com.connectaoficios.api.dtos.reporte;

import com.connectaoficios.api.enums.EstadoReporte;
import com.connectaoficios.api.enums.TipoReporte;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReporteSalida {

    private Long id;

    private Integer usuarioReportanteId;

    private Integer usuarioReportadoId;

    private Long servicioId;

    private TipoReporte tipo;

    private String motivo;

    private String descripcion;

    private EstadoReporte estado;

    private String resolucion;

    private String accionTomada;

    private Integer administradorId;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaResolucion;
}