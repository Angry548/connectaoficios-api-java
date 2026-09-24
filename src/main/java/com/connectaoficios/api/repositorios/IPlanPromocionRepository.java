package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.modelos.PlanPromocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPlanPromocionRepository extends JpaRepository<PlanPromocion, Long> {

    Page<PlanPromocion> findByActivo(Boolean activo, Pageable pageable);

    Optional<PlanPromocion> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}