package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}