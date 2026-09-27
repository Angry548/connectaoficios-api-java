package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.modelos.Conversacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IConversacionRepository
        extends JpaRepository<Conversacion, Long> {

    Optional<Conversacion> findBySolicitudId(Long solicitudId);

    boolean existsBySolicitudId(Long solicitudId);

    @Query(
            value = """
                    SELECT c
                    FROM Conversacion c
                    JOIN c.solicitud s
                    WHERE
                        (
                            s.clienteId = :usuarioId
                            OR s.trabajadorId = :usuarioId
                        )
                        AND (
                            :solicitudId IS NULL
                            OR s.id = :solicitudId
                        )
                        AND (
                            :estadoSolicitud IS NULL
                            OR s.estado = :estadoSolicitud
                        )
                        AND (
                            :fechaDesde IS NULL
                            OR c.fechaCreacion >= :fechaDesde
                        )
                        AND (
                            :fechaHasta IS NULL
                            OR c.fechaCreacion <= :fechaHasta
                        )
                        AND (
                            :puedeEnviarMensajes IS NULL
                            OR (
                                :puedeEnviarMensajes = true
                                AND (
                                    s.estado = com.connectaoficios.api.enums.EstadoSolicitud.ACEPTADA
                                    OR s.estado = com.connectaoficios.api.enums.EstadoSolicitud.EN_PROCESO
                                )
                            )
                            OR (
                                :puedeEnviarMensajes = false
                                AND s.estado <> com.connectaoficios.api.enums.EstadoSolicitud.ACEPTADA
                                AND s.estado <> com.connectaoficios.api.enums.EstadoSolicitud.EN_PROCESO
                            )
                        )
                    """,
            countQuery = """
                    SELECT COUNT(c)
                    FROM Conversacion c
                    JOIN c.solicitud s
                    WHERE
                        (
                            s.clienteId = :usuarioId
                            OR s.trabajadorId = :usuarioId
                        )
                        AND (
                            :solicitudId IS NULL
                            OR s.id = :solicitudId
                        )
                        AND (
                            :estadoSolicitud IS NULL
                            OR s.estado = :estadoSolicitud
                        )
                        AND (
                            :fechaDesde IS NULL
                            OR c.fechaCreacion >= :fechaDesde
                        )
                        AND (
                            :fechaHasta IS NULL
                            OR c.fechaCreacion <= :fechaHasta
                        )
                        AND (
                            :puedeEnviarMensajes IS NULL
                            OR (
                                :puedeEnviarMensajes = true
                                AND (
                                    s.estado = com.connectaoficios.api.enums.EstadoSolicitud.ACEPTADA
                                    OR s.estado = com.connectaoficios.api.enums.EstadoSolicitud.EN_PROCESO
                                )
                            )
                            OR (
                                :puedeEnviarMensajes = false
                                AND s.estado <> com.connectaoficios.api.enums.EstadoSolicitud.ACEPTADA
                                AND s.estado <> com.connectaoficios.api.enums.EstadoSolicitud.EN_PROCESO
                            )
                        )
                    """
    )
    Page<Conversacion> buscarConversacionesUsuario(
            @Param("usuarioId") Integer usuarioId,
            @Param("solicitudId") Long solicitudId,
            @Param("estadoSolicitud") EstadoSolicitud estadoSolicitud,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("puedeEnviarMensajes") Boolean puedeEnviarMensajes,
            Pageable pageable
    );
}