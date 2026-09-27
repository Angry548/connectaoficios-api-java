package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.EstadoTransaccion;
import com.connectaoficios.api.modelos.TransaccionPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ITransaccionPagoRepository
        extends JpaRepository<TransaccionPago, Long> {

    Page<TransaccionPago> findByEstado(
            EstadoTransaccion estado,
            Pageable pageable
    );

    List<TransaccionPago> findByTrabajadorId(
            Integer trabajadorId
    );

    List<TransaccionPago> findByPromocion_Id(
            Long promocionId
    );

    boolean existsByPromocion_IdAndEstadoIn(
            Long promocionId,
            List<EstadoTransaccion> estados
    );

    @Query(
            value = """
                    SELECT t
                    FROM TransaccionPago t
                    WHERE
                        (:promocionId IS NULL OR t.promocion.id = :promocionId)
                        AND (:servicioId IS NULL OR t.promocion.servicio.id = :servicioId)
                        AND (:trabajadorId IS NULL OR t.trabajadorId = :trabajadorId)
                        AND (:estado IS NULL OR t.estado = :estado)
                        AND (
                            :moneda IS NULL
                            OR LOWER(t.moneda) = LOWER(:moneda)
                        )
                        AND (
                            :referenciaExterna IS NULL
                            OR LOWER(t.referenciaExterna) LIKE LOWER(CONCAT('%', :referenciaExterna, '%'))
                        )
                        AND (
                            :montoMinimo IS NULL
                            OR t.monto >= :montoMinimo
                        )
                        AND (
                            :montoMaximo IS NULL
                            OR t.monto <= :montoMaximo
                        )
                        AND (
                            :fechaDesde IS NULL
                            OR t.fecha >= :fechaDesde
                        )
                        AND (
                            :fechaHasta IS NULL
                            OR t.fecha <= :fechaHasta
                        )
                    """,
            countQuery = """
                    SELECT COUNT(t)
                    FROM TransaccionPago t
                    WHERE
                        (:promocionId IS NULL OR t.promocion.id = :promocionId)
                        AND (:servicioId IS NULL OR t.promocion.servicio.id = :servicioId)
                        AND (:trabajadorId IS NULL OR t.trabajadorId = :trabajadorId)
                        AND (:estado IS NULL OR t.estado = :estado)
                        AND (
                            :moneda IS NULL
                            OR LOWER(t.moneda) = LOWER(:moneda)
                        )
                        AND (
                            :referenciaExterna IS NULL
                            OR LOWER(t.referenciaExterna) LIKE LOWER(CONCAT('%', :referenciaExterna, '%'))
                        )
                        AND (
                            :montoMinimo IS NULL
                            OR t.monto >= :montoMinimo
                        )
                        AND (
                            :montoMaximo IS NULL
                            OR t.monto <= :montoMaximo
                        )
                        AND (
                            :fechaDesde IS NULL
                            OR t.fecha >= :fechaDesde
                        )
                        AND (
                            :fechaHasta IS NULL
                            OR t.fecha <= :fechaHasta
                        )
                    """
    )
    Page<TransaccionPago> buscarConFiltros(
            @Param("promocionId") Long promocionId,
            @Param("servicioId") Long servicioId,
            @Param("trabajadorId") Integer trabajadorId,
            @Param("estado") EstadoTransaccion estado,
            @Param("moneda") String moneda,
            @Param("referenciaExterna") String referenciaExterna,
            @Param("montoMinimo") BigDecimal montoMinimo,
            @Param("montoMaximo") BigDecimal montoMaximo,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            Pageable pageable
    );
}