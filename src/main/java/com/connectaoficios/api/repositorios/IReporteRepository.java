package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.EstadoReporte;
import com.connectaoficios.api.enums.TipoReporte;
import com.connectaoficios.api.modelos.Reporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IReporteRepository extends JpaRepository<Reporte, Long> {

    Page<Reporte> findByEstado(EstadoReporte estado, Pageable pageable);

    List<Reporte> findByEstado(EstadoReporte estado);

    Page<Reporte> findByTipo(TipoReporte tipo, Pageable pageable);

    List<Reporte> findByUsuarioReportanteId(Integer usuarioReportanteId);

    List<Reporte> findByUsuarioReportadoId(Integer usuarioReportadoId);

    List<Reporte> findByServicio_Id(Long servicioId);

    boolean existsByUsuarioReportanteIdAndServicio_IdAndEstadoIn(
            Integer usuarioReportanteId,
            Long servicioId,
            List<EstadoReporte> estados
    );
}