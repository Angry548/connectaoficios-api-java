package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoAprobar;
import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoGuardar;
import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoSalida;
import com.connectaoficios.api.enums.EstadoTransaccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ITransaccionPagoService {

    List<TransaccionPagoSalida> obtenerTodos();

    Page<TransaccionPagoSalida> obtenerTodosPaginados(Pageable pageable);

    TransaccionPagoSalida obtenerPorId(Long id);

    List<TransaccionPagoSalida> obtenerPorTrabajador(Integer trabajadorId);

    List<TransaccionPagoSalida> obtenerPorPromocion(Long promocionId);

    Page<TransaccionPagoSalida> obtenerPorEstado(
            EstadoTransaccion estado,
            Pageable pageable
    );

    TransaccionPagoSalida guardar(
            TransaccionPagoGuardar dto,
            Integer trabajadorId
    );

    TransaccionPagoSalida aprobar(
            Long id,
            TransaccionPagoAprobar dto
    );

    TransaccionPagoSalida rechazar(Long id);

    TransaccionPagoSalida cancelar(
            Long id,
            Integer trabajadorId
    );
}