package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.modelos.SolicitudServicio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISolicitudServicioRepository
        extends JpaRepository<SolicitudServicio, Long> {

    List<SolicitudServicio> findByClienteId(
            Integer clienteId
    );

    List<SolicitudServicio> findByTrabajadorId(
            Integer trabajadorId
    );

    List<SolicitudServicio> findByEstado(
            EstadoSolicitud estado
    );

    long countByTrabajadorIdAndEstado(
            Integer trabajadorId,
            EstadoSolicitud estado
    );

    Page<SolicitudServicio> findByClienteId(
            Integer clienteId,
            Pageable pageable
    );

    Page<SolicitudServicio> findByTrabajadorId(
            Integer trabajadorId,
            Pageable pageable
    );

    boolean existsByServicioIdAndClienteId(
            Long servicioId,
            Integer clienteId
    );
}