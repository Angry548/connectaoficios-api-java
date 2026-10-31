package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.PerfilAdministrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPerfilAdministradorRepository
        extends JpaRepository<PerfilAdministrador, Long> {

    List<PerfilAdministrador> findByAdministradorIdIn(
            Collection<Integer> administradorIds
    );

    Optional<PerfilAdministrador> findByAdministradorId(
            Integer administradorId
    );

    boolean existsByAdministradorId(
            Integer administradorId
    );
}
