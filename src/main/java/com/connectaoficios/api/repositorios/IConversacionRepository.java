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

    @Query("select c from SolicitudServicio s join s.conversacion c where s.id = :solicitudId")
    Optional<Conversacion> findBySolicitudId(@Param("solicitudId") Long solicitudId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conversacion c where c.clienteId = :cliente and c.trabajadorId = :trabajador and c.servicio.id = :servicio")
    Optional<Conversacion> buscarConBloqueo(@Param("cliente") Integer cliente, @Param("trabajador") Integer trabajador, @Param("servicio") Long servicio);

    boolean existsBySolicitudId(Long solicitudId);

    @Query(value = """
            select c from Conversacion c join fetch c.solicitud anchor join fetch anchor.servicio
            where (c.clienteId = :usuarioId or c.trabajadorId = :usuarioId)
              and ((:solicitudId is null and :estadoSolicitud is null) or exists
                   (select s.id from SolicitudServicio s where s.conversacion = c
                    and (:solicitudId is null or s.id = :solicitudId)
                    and (:estadoSolicitud is null or s.estado = :estadoSolicitud)))
              and (:fechaDesde is null or c.fechaCreacion >= :fechaDesde)
              and (:fechaHasta is null or c.fechaCreacion <= :fechaHasta)
              and (:puedeEnviarMensajes is null or :puedeEnviarMensajes = true)
            """, countQuery = """
            select count(c) from Conversacion c
            where (c.clienteId = :usuarioId or c.trabajadorId = :usuarioId)
              and ((:solicitudId is null and :estadoSolicitud is null) or exists
                   (select s.id from SolicitudServicio s where s.conversacion = c
                    and (:solicitudId is null or s.id = :solicitudId)
                    and (:estadoSolicitud is null or s.estado = :estadoSolicitud)))
              and (:fechaDesde is null or c.fechaCreacion >= :fechaDesde)
              and (:fechaHasta is null or c.fechaCreacion <= :fechaHasta)
              and (:puedeEnviarMensajes is null or :puedeEnviarMensajes = true)
            """)
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
