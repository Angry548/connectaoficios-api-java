package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.reporte.ReporteGuardar;
import com.connectaoficios.api.dtos.reporte.ReporteRechazo;
import com.connectaoficios.api.dtos.reporte.ReporteResolucion;
import com.connectaoficios.api.dtos.reporte.ReporteSalida;
import com.connectaoficios.api.enums.EstadoReporte;
import com.connectaoficios.api.enums.TipoReporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IReporteService {

    List<ReporteSalida> obtenerTodos();

    Page<ReporteSalida> obtenerTodosPaginados(Pageable pageable);

    ReporteSalida obtenerPorId(Long id);

    Page<ReporteSalida> obtenerPorEstado(EstadoReporte estado, Pageable pageable);

    Page<ReporteSalida> obtenerPorTipo(TipoReporte tipo, Pageable pageable);

    List<ReporteSalida> obtenerPorUsuarioReportante(Integer usuarioReportanteId);

    List<ReporteSalida> obtenerPorUsuarioReportado(Integer usuarioReportadoId);

    ReporteSalida guardar(ReporteGuardar dto);

    ReporteSalida iniciarRevision(Long id);

    ReporteSalida resolver(Long id, ReporteResolucion dto, Integer administradorId);

    ReporteSalida rechazar(Long id, ReporteRechazo dto, Integer administradorId);
}