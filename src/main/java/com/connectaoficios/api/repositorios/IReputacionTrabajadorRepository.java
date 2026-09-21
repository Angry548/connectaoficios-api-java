package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.ReputacionTrabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IReputacionTrabajadorRepository
        extends JpaRepository<ReputacionTrabajador, Long> {

    Optional<ReputacionTrabajador> findByPerfilTrabajadorId(Long perfilTrabajadorId);

    boolean existsByPerfilTrabajadorId(Long perfilTrabajadorId);

    List<ReputacionTrabajador> findAllByOrderByPuntuacionRankingDesc();
}

