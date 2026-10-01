package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.TipoNotificacion;
import com.connectaoficios.api.modelos.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioDestinoId(Integer usuarioDestinoId);

    Page<Notificacion> findByUsuarioDestinoId(
            Integer usuarioDestinoId,
            Pageable pageable
    );

    Page<Notificacion> findByUsuarioDestinoIdAndLeida(
            Integer usuarioDestinoId,
            Boolean leida,
            Pageable pageable
    );

    List<Notificacion> findByUsuarioDestinoIdAndLeida(
            Integer usuarioDestinoId,
            Boolean leida
    );

    List<Notificacion> findByTipo(TipoNotificacion tipo);

    long countByUsuarioDestinoIdAndLeidaFalse(
            Integer usuarioDestinoId
    );
}