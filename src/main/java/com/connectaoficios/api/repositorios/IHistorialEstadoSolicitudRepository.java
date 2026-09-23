package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.HistorialEstadoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IHistorialEstadoSolicitudRepository
        extends JpaRepository<HistorialEstadoSolicitud, Long> {

    // Obtener todo el historial de cambios de una solicitud específica ordenado cronológicamente
    List<HistorialEstadoSolicitud> findBySolicitud_IdSolicitudOrderByFechaCambioDesc(Long solicitudId);

    Page<HistorialEstadoSolicitud> findBySolicitud_IdSolicitud(Long solicitudId, Pageable pageable);

    List<HistorialEstadoSolicitud> findByCambiadoPorId(Integer cambiadoPorId);
}
