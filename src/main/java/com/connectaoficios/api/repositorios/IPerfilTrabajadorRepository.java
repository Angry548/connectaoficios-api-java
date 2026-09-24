package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.PerfilTrabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPerfilTrabajadorRepository
        extends JpaRepository<PerfilTrabajador, Long> {

    Optional<PerfilTrabajador> findByTrabajadorId(Integer trabajadorId);

    boolean existsByTrabajadorId(Integer trabajadorId);
}
