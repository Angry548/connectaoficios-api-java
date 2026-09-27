package com.connectaoficios.api.dtos.reporte;

import com.connectaoficios.api.enums.EstadoReporte;
import com.connectaoficios.api.enums.TipoReporte;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReporteFiltroDTO {

    private String texto;

    private EstadoReporte estado;

    private TipoReporte tipo;

    private Integer usuarioReportanteId;

    private Integer usuarioReportadoId;

    private Long servicioId;

    private Integer administradorId;

    private LocalDateTime fechaDesde;

    private LocalDateTime fechaHasta;
}