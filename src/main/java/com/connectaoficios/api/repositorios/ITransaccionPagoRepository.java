package com.connectaoficios.api.repositorios;

import com.connectaoficios.api.enums.EstadoTransaccion;
import com.connectaoficios.api.modelos.TransaccionPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ITransaccionPagoRepository extends JpaRepository<TransaccionPago, Long> {

    Page<TransaccionPago> findByEstado(EstadoTransaccion estado, Pageable pageable);

    List<TransaccionPago> findByTrabajadorId(Integer trabajadorId);

    List<TransaccionPago> findByPromocion_Id(Long promocionId);

    boolean existsByPromocion_IdAndEstadoIn(Long promocionId, List<EstadoTransaccion> estados);
}