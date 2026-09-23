package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.EstadoPromocion;
import com.connectaoficios.api.modelos.Promocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPromocionRepository extends JpaRepository<Promocion, Long> {

    Page<Promocion> findByEstado(EstadoPromocion estado, Pageable pageable);

    List<Promocion> findByTrabajadorId(Integer trabajadorId);

    List<Promocion> findByServicio_Id(Long servicioId);

    boolean existsByServicio_IdAndEstadoIn(Long servicioId, List<EstadoPromocion> estados);
}
