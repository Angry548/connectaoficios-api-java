package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.resena.ResenaGuardar;
import com.connectaoficios.api.dtos.resena.ResenaSalida;

import java.util.List;

public interface IResenaService {

    ResenaSalida guardar(
            ResenaGuardar resenaGuardar,
            Integer clienteId
    );

    ResenaSalida obtenerPorId(Long id);

    List<ResenaSalida> obtenerPorPerfilTrabajador(
            Long perfilTrabajadorId
    );
}
