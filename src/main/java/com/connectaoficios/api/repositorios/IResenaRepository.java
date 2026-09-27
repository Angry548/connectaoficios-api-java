package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.Resena;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IResenaRepository
        extends JpaRepository<Resena, Long> {

    boolean existsBySolicitudId(Long solicitudId);

    List<Resena> findByPerfilTrabajadorIdOrderByFechaDesc(
            Long perfilTrabajadorId
    );

    List<Resena> findByPerfilTrabajadorId(
            Long perfilTrabajadorId
    );

    @Query(
            value = """
                    SELECT r
                    FROM Resena r
                    WHERE
                        (
                            :perfilTrabajadorId IS NULL
                            OR r.perfilTrabajador.id = :perfilTrabajadorId
                        )
                        AND (
                            :servicioId IS NULL
                            OR r.servicio.id = :servicioId
                        )
                        AND (
                            :clienteId IS NULL
                            OR r.clienteId = :clienteId
                        )
                        AND (
                            :calificacion IS NULL
                            OR r.calificacion = :calificacion
                        )
                        AND (
                            :texto IS NULL
                            OR LOWER(r.comentario) LIKE LOWER(CONCAT('%', :texto, '%'))
                        )
                        AND (
                            :fechaDesde IS NULL
                            OR r.fecha >= :fechaDesde
                        )
                        AND (
                            :fechaHasta IS NULL
                            OR r.fecha <= :fechaHasta
                        )
                    """,
            countQuery = """
                    SELECT COUNT(r)
                    FROM Resena r
                    WHERE
                        (
                            :perfilTrabajadorId IS NULL
                            OR r.perfilTrabajador.id = :perfilTrabajadorId
                        )
                        AND (
                            :servicioId IS NULL
                            OR r.servicio.id = :servicioId
                        )
                        AND (
                            :clienteId IS NULL
                            OR r.clienteId = :clienteId
                        )
                        AND (
                            :calificacion IS NULL
                            OR r.calificacion = :calificacion
                        )
                        AND (
                            :texto IS NULL
                            OR LOWER(r.comentario) LIKE LOWER(CONCAT('%', :texto, '%'))
                        )
                        AND (
                            :fechaDesde IS NULL
                            OR r.fecha >= :fechaDesde
                        )
                        AND (
                            :fechaHasta IS NULL
                            OR r.fecha <= :fechaHasta
                        )
                    """
    )
    Page<Resena> buscarConFiltros(
            @Param("perfilTrabajadorId") Long perfilTrabajadorId,
            @Param("servicioId") Long servicioId,
            @Param("clienteId") Integer clienteId,
            @Param("calificacion") Integer calificacion,
            @Param("texto") String texto,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            Pageable pageable
    );
}