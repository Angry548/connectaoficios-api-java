package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.EstadoPromocion;
import com.connectaoficios.api.modelos.Promocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IPromocionRepository
        extends JpaRepository<Promocion, Long> {

    Page<Promocion> findByEstado(
            EstadoPromocion estado,
            Pageable pageable
    );

    List<Promocion> findByTrabajadorId(
            Integer trabajadorId
    );

    List<Promocion> findByServicio_Id(
            Long servicioId
    );

    boolean existsByServicio_IdAndEstadoIn(
            Long servicioId,
            List<EstadoPromocion> estados
    );

    @Query(
            value = """
                    SELECT p
                    FROM Promocion p
                    WHERE
                        (:servicioId IS NULL OR p.servicio.id = :servicioId)
                        AND (:planId IS NULL OR p.plan.id = :planId)
                        AND (:trabajadorId IS NULL OR p.trabajadorId = :trabajadorId)
                        AND (:estado IS NULL OR p.estado = :estado)
                        AND (
                            :fechaCreacionDesde IS NULL
                            OR p.fechaCreacion >= :fechaCreacionDesde
                        )
                        AND (
                            :fechaCreacionHasta IS NULL
                            OR p.fechaCreacion <= :fechaCreacionHasta
                        )
                        AND (
                            :fechaInicioDesde IS NULL
                            OR p.fechaInicio >= :fechaInicioDesde
                        )
                        AND (
                            :fechaInicioHasta IS NULL
                            OR p.fechaInicio <= :fechaInicioHasta
                        )
                        AND (
                            :fechaFinDesde IS NULL
                            OR p.fechaFin >= :fechaFinDesde
                        )
                        AND (
                            :fechaFinHasta IS NULL
                            OR p.fechaFin <= :fechaFinHasta
                        )
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Promocion p
                    WHERE
                        (:servicioId IS NULL OR p.servicio.id = :servicioId)
                        AND (:planId IS NULL OR p.plan.id = :planId)
                        AND (:trabajadorId IS NULL OR p.trabajadorId = :trabajadorId)
                        AND (:estado IS NULL OR p.estado = :estado)
                        AND (
                            :fechaCreacionDesde IS NULL
                            OR p.fechaCreacion >= :fechaCreacionDesde
                        )
                        AND (
                            :fechaCreacionHasta IS NULL
                            OR p.fechaCreacion <= :fechaCreacionHasta
                        )
                        AND (
                            :fechaInicioDesde IS NULL
                            OR p.fechaInicio >= :fechaInicioDesde
                        )
                        AND (
                            :fechaInicioHasta IS NULL
                            OR p.fechaInicio <= :fechaInicioHasta
                        )
                        AND (
                            :fechaFinDesde IS NULL
                            OR p.fechaFin >= :fechaFinDesde
                        )
                        AND (
                            :fechaFinHasta IS NULL
                            OR p.fechaFin <= :fechaFinHasta
                        )
                    """
    )
    Page<Promocion> buscarConFiltros(
            @Param("servicioId") Long servicioId,
            @Param("planId") Long planId,
            @Param("trabajadorId") Integer trabajadorId,
            @Param("estado") EstadoPromocion estado,
            @Param("fechaCreacionDesde") LocalDateTime fechaCreacionDesde,
            @Param("fechaCreacionHasta") LocalDateTime fechaCreacionHasta,
            @Param("fechaInicioDesde") LocalDateTime fechaInicioDesde,
            @Param("fechaInicioHasta") LocalDateTime fechaInicioHasta,
            @Param("fechaFinDesde") LocalDateTime fechaFinDesde,
            @Param("fechaFinHasta") LocalDateTime fechaFinHasta,
            Pageable pageable
    );
}