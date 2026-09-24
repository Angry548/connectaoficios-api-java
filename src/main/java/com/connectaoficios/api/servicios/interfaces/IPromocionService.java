package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.promocion.PromocionGuardar;
import com.connectaoficios.api.dtos.promocion.PromocionResumenSalida;
import com.connectaoficios.api.dtos.promocion.PromocionSalida;
import com.connectaoficios.api.enums.EstadoPromocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPromocionService {

    List<PromocionSalida> obtenerTodos();

    Page<PromocionSalida> obtenerTodosPaginados(Pageable pageable);

    PromocionSalida obtenerPorId(Long id);

    List<PromocionSalida> obtenerPorTrabajador(Integer trabajadorId);

    List<PromocionSalida> obtenerPorServicio(Long servicioId);

    Page<PromocionSalida> obtenerPorEstado(EstadoPromocion estado, Pageable pageable);

    PromocionResumenSalida obtenerResumen(Long servicioId, Long planId);

    PromocionSalida guardar(PromocionGuardar dto, Integer trabajadorId);

    PromocionSalida activar(Long id);

    PromocionSalida cancelar(Long id);

    PromocionSalida finalizar(Long id);

    boolean estaVigente(Long id);
}