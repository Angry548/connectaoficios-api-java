package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.planpromocion.PlanPromocionGuardar;
import com.connectaoficios.api.dtos.planpromocion.PlanPromocionModificar;
import com.connectaoficios.api.dtos.planpromocion.PlanPromocionSalida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface IPlanPromocionService {

    List<PlanPromocionSalida> obtenerTodos();

    Page<PlanPromocionSalida> obtenerTodosPaginados(Pageable pageable);

    Page<PlanPromocionSalida> obtenerActivos(Pageable pageable);

    PlanPromocionSalida obtenerPorId(Long id);

    PlanPromocionSalida guardar(PlanPromocionGuardar dto);

    PlanPromocionSalida modificar(Long id, PlanPromocionModificar dto);

    PlanPromocionSalida activar(Long id);

    PlanPromocionSalida desactivar(Long id);

    void eliminar(Long id);

    LocalDateTime calcularFechaFin(Long id, LocalDateTime fechaInicio);
}