package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IMensajeRepository
        extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByConversacionIdOrderByFechaEnvioAsc(
            Long conversacionId
    );

    long countByConversacionIdAndLeidoFalse(
            Long conversacionId
    );

    long countByConversacionIdAndLeidoFalseAndRemitenteIdNot(
            Long conversacionId,
            Integer remitenteId
    );

    @Query(
            value = """
                    SELECT m
                    FROM Mensaje m
                    WHERE m.conversacion.id = :conversacionId
                      AND (
                            :remitenteId IS NULL
                            OR m.remitenteId = :remitenteId
                      )
                      AND (
                            :leido IS NULL
                            OR m.leido = :leido
                      )
                      AND (
                            :texto IS NULL
                            OR LOWER(m.contenido) LIKE LOWER(CONCAT('%', :texto, '%'))
                      )
                      AND (
                            :fechaDesde IS NULL
                            OR m.fechaEnvio >= :fechaDesde
                      )
                      AND (
                            :fechaHasta IS NULL
                            OR m.fechaEnvio <= :fechaHasta
                      )
                    """,
            countQuery = """
                    SELECT COUNT(m)
                    FROM Mensaje m
                    WHERE m.conversacion.id = :conversacionId
                      AND (
                            :remitenteId IS NULL
                            OR m.remitenteId = :remitenteId
                      )
                      AND (
                            :leido IS NULL
                            OR m.leido = :leido
                      )
                      AND (
                            :texto IS NULL
                            OR LOWER(m.contenido) LIKE LOWER(CONCAT('%', :texto, '%'))
                      )
                      AND (
                            :fechaDesde IS NULL
                            OR m.fechaEnvio >= :fechaDesde
                      )
                      AND (
                            :fechaHasta IS NULL
                            OR m.fechaEnvio <= :fechaHasta
                      )
                    """
    )
    Page<Mensaje> buscarConFiltros(
            @Param("conversacionId") Long conversacionId,
            @Param("remitenteId") Integer remitenteId,
            @Param("leido") Boolean leido,
            @Param("texto") String texto,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            Pageable pageable
    );
}