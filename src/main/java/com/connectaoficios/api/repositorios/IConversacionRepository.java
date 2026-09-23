package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IConversacionRepository
        extends JpaRepository<Conversacion, Long> {

    Optional<Conversacion> findBySolicitudId(Long solicitudId);

    boolean existsBySolicitudId(Long solicitudId);
}