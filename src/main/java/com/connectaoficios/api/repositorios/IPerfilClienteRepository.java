package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.PerfilCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPerfilClienteRepository
        extends JpaRepository<PerfilCliente, Long> {

    List<PerfilCliente> findByClienteIdIn(
            Collection<Integer> clienteIds
    );

    Optional<PerfilCliente> findByClienteId(
            Integer clienteId
    );

    boolean existsByClienteId(
            Integer clienteId
    );
}
