package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorFiltroDTO;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorGuardar;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorModificar;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorSalida;

import java.util.List;

public interface IReputacionTrabajadorService {

    ReputacionTrabajadorSalida guardar(
            ReputacionTrabajadorGuardar reputacionGuardar
    );

    ReputacionTrabajadorSalida obtenerPorPerfilTrabajador(
            Long perfilTrabajadorId
    );

    List<ReputacionTrabajadorSalida> obtenerRanking();

    PaginaSalida<ReputacionTrabajadorSalida> buscarConFiltros(
            ReputacionTrabajadorFiltroDTO filtro,
            int pagina,
            int tamanio
    );

    ReputacionTrabajadorSalida modificar(
            Long perfilTrabajadorId,
            ReputacionTrabajadorModificar reputacionModificar
    );

    ReputacionTrabajadorSalida recalcular(
            Long perfilTrabajadorId
    );
}