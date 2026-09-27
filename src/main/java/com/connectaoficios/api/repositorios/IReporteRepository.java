package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.EstadoReporte;
import com.connectaoficios.api.enums.TipoReporte;
import com.connectaoficios.api.modelos.Reporte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IReporteRepository
        extends JpaRepository<Reporte, Long> {

    Page<Reporte> findByEstado(
            EstadoReporte estado,
            Pageable pageable
    );

    List<Reporte> findByEstado(
            EstadoReporte estado
    );

    Page<Reporte> findByTipo(
            TipoReporte tipo,
            Pageable pageable
    );

    List<Reporte> findByUsuarioReportanteId(
            Integer usuarioReportanteId
    );

    List<Reporte> findByUsuarioReportadoId(
            Integer usuarioReportadoId
    );

    List<Reporte> findByServicio_Id(
            Long servicioId
    );

    boolean existsByUsuarioReportanteIdAndServicio_IdAndEstadoIn(
            Integer usuarioReportanteId,
            Long servicioId,
            List<EstadoReporte> estados
    );

    @Query(
            value = """
                    SELECT r
                    FROM Reporte r
                    WHERE
                        (
                            :texto IS NULL
                            OR LOWER(r.motivo) LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(r.resolucion) LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(r.accionTomada) LIKE LOWER(CONCAT('%', :texto, '%'))
                        )
                        AND (
                            :estado IS NULL
                            OR r.estado = :estado
                        )
                        AND (
                            :tipo IS NULL
                            OR r.tipo = :tipo
                        )
                        AND (
                            :usuarioReportanteId IS NULL
                            OR r.usuarioReportanteId = :usuarioReportanteId
                        )
                        AND (
                            :usuarioReportadoId IS NULL
                            OR r.usuarioReportadoId = :usuarioReportadoId
                        )
                        AND (
                            :servicioId IS NULL
                            OR r.servicio.id = :servicioId
                        )
                        AND (
                            :administradorId IS NULL
                            OR r.administradorId = :administradorId
                        )
                        AND (
                            :fechaDesde IS NULL
                            OR r.fechaCreacion >= :fechaDesde
                        )
                        AND (
                            :fechaHasta IS NULL
                            OR r.fechaCreacion <= :fechaHasta
                        )
                    """,
            countQuery = """
                    SELECT COUNT(r)
                    FROM Reporte r
                    WHERE
                        (
                            :texto IS NULL
                            OR LOWER(r.motivo) LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(r.resolucion) LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(r.accionTomada) LIKE LOWER(CONCAT('%', :texto, '%'))
                        )
                        AND (
                            :estado IS NULL
                            OR r.estado = :estado
                        )
                        AND (
                            :tipo IS NULL
                            OR r.tipo = :tipo
                        )
                        AND (
                            :usuarioReportanteId IS NULL
                            OR r.usuarioReportanteId = :usuarioReportanteId
                        )
                        AND (
                            :usuarioReportadoId IS NULL
                            OR r.usuarioReportadoId = :usuarioReportadoId
                        )
                        AND (
                            :servicioId IS NULL
                            OR r.servicio.id = :servicioId
                        )
                        AND (
                            :administradorId IS NULL
                            OR r.administradorId = :administradorId
                        )
                        AND (
                            :fechaDesde IS NULL
                            OR r.fechaCreacion >= :fechaDesde
                        )
                        AND (
                            :fechaHasta IS NULL
                            OR r.fechaCreacion <= :fechaHasta
                        )
                    """
    )
    Page<Reporte> buscarConFiltros(
            @Param("texto") String texto,
            @Param("estado") EstadoReporte estado,
            @Param("tipo") TipoReporte tipo,
            @Param("usuarioReportanteId") Integer usuarioReportanteId,
            @Param("usuarioReportadoId") Integer usuarioReportadoId,
            @Param("servicioId") Long servicioId,
            @Param("administradorId") Integer administradorId,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            Pageable pageable
    );
}