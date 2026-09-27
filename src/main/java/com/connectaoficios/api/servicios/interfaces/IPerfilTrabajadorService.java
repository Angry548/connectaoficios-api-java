package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorBusquedaSalida;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorFiltroDTO;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorGuardar;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorModificar;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorSalida;

import java.util.List;

public interface IPerfilTrabajadorService {

    PerfilTrabajadorSalida guardar(
            PerfilTrabajadorGuardar perfilGuardar,
            Integer trabajadorId
    );

    PerfilTrabajadorSalida obtenerPorId(
            Long id
    );

    PerfilTrabajadorSalida obtenerPorTrabajadorId(
            Integer trabajadorId
    );

    PerfilTrabajadorSalida modificar(
            Integer trabajadorId,
            PerfilTrabajadorModificar perfilModificar
    );

    PaginaSalida<PerfilTrabajadorSalida> listarPaginado(
            int pagina,
            int tamanio
    );

    PaginaSalida<PerfilTrabajadorSalida> buscarConFiltros(
            PerfilTrabajadorFiltroDTO filtro,
            int pagina,
            int tamanio
    );

    List<PerfilTrabajadorBusquedaSalida> buscarParaAutocomplete(
            String texto,
            int limite
    );
}