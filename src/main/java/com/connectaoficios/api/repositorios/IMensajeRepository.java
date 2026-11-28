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

    @Query("""
      select m from Mensaje m join fetch m.conversacion c
      where c.id in :ids and not exists
       (select m2.id from Mensaje m2 where m2.conversacion = c and
        (m2.fechaEnvio > m.fechaEnvio or (m2.fechaEnvio = m.fechaEnvio and m2.id > m.id)))
      """)
    List<Mensaje> ultimosPorConversacion(@Param("ids") java.util.Collection<Long> ids);

    @Query("select m.conversacion.id, count(m) from Mensaje m where m.conversacion.id in :ids and m.leido = false and m.remitenteId <> :usuario group by m.conversacion.id")
    List<Object[]> contarNoLeidosPorConversacion(@Param("ids") java.util.Collection<Long> ids, @Param("usuario") Integer usuario);

    List<Mensaje> findByConversacionIdOrderByFechaEnvioAsc(
            Long conversacionId
    );

    long countByConversacionIdAndLeidoFalse(
            Long conversacionId
    );

    @Query("""
            SELECT COUNT(m)
            FROM Mensaje m
            JOIN m.conversacion c
            JOIN c.solicitud s
            WHERE m.leido = false
              AND m.remitenteId <> :usuarioId
              AND (s.clienteId = :usuarioId OR s.trabajadorId = :usuarioId)
            """)
    long contarTotalNoLeidos(@Param("usuarioId") Integer usuarioId);

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
