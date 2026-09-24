package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IResenaRepository
        extends JpaRepository<Resena, Long> {

    boolean existsBySolicitudId(Long solicitudId);

    List<Resena> findByPerfilTrabajadorIdOrderByFechaDesc(
            Long perfilTrabajadorId
    );

    List<Resena> findByPerfilTrabajadorId(
            Long perfilTrabajadorId
    );
}
