package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.modelos.SolicitudServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ISolicitudServicioRepository
        extends JpaRepository<SolicitudServicio, Long> {

    List<SolicitudServicio> findByClienteId(
            Integer clienteId
    );

    List<SolicitudServicio> findByTrabajadorId(
            Integer trabajadorId
    );

    List<SolicitudServicio> findByEstado(
            EstadoSolicitud estado
    );

    long countByTrabajadorIdAndEstado(
            Integer trabajadorId,
            EstadoSolicitud estado
    );

    Page<SolicitudServicio> findByClienteId(
            Integer clienteId,
            Pageable pageable
    );

    Page<SolicitudServicio> findByTrabajadorId(
            Integer trabajadorId,
            Pageable pageable
    );

    boolean existsByServicioIdAndClienteId(
            Long servicioId,
            Integer clienteId
    );

    @Query(
            value = """
                    SELECT s
                    FROM SolicitudServicio s
                    WHERE
                        (
                            :texto IS NULL
                            OR LOWER(s.direccion)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(s.descripcionTrabajo)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(s.motivoCancelacion)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                        )
                        AND (
                            :servicioId IS NULL
                            OR s.servicio.id = :servicioId
                        )
                        AND (
                            :clienteId IS NULL
                            OR s.clienteId = :clienteId
                        )
                        AND (
                            :trabajadorId IS NULL
                            OR s.trabajadorId = :trabajadorId
                        )
                        AND (
                            :estado IS NULL
                            OR s.estado = :estado
                        )
                        AND (
                            :fechaDesde IS NULL
                            OR s.fechaPropuesta >= :fechaDesde
                        )
                        AND (
                            :fechaHasta IS NULL
                            OR s.fechaPropuesta <= :fechaHasta
                        )
                    """,
            countQuery = """
                    SELECT COUNT(s)
                    FROM SolicitudServicio s
                    WHERE
                        (
                            :texto IS NULL
                            OR LOWER(s.direccion)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(s.descripcionTrabajo)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                            OR LOWER(s.motivoCancelacion)
                                LIKE LOWER(CONCAT('%', :texto, '%'))
                        )
                        AND (
                            :servicioId IS NULL
                            OR s.servicio.id = :servicioId
                        )
                        AND (
                            :clienteId IS NULL
                            OR s.clienteId = :clienteId
                        )
                        AND (
                            :trabajadorId IS NULL
                            OR s.trabajadorId = :trabajadorId
                        )
                        AND (
                            :estado IS NULL
                            OR s.estado = :estado
                        )
                        AND (
                            :fechaDesde IS NULL
                            OR s.fechaPropuesta >= :fechaDesde
                        )
                        AND (
                            :fechaHasta IS NULL
                            OR s.fechaPropuesta <= :fechaHasta
                        )
                    """
    )
    Page<SolicitudServicio> buscarConFiltros(
            @Param("texto") String texto,
            @Param("servicioId") Long servicioId,
            @Param("clienteId") Integer clienteId,
            @Param("trabajadorId") Integer trabajadorId,
            @Param("estado") EstadoSolicitud estado,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable
    );
}