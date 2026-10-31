package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.perfil.PerfilClienteGuardar;
import com.connectaoficios.api.dtos.perfil.PerfilClienteModificar;
import com.connectaoficios.api.dtos.perfil.PerfilClientePublicoSalida;
import com.connectaoficios.api.dtos.perfil.PerfilClienteSalida;

public interface IPerfilClienteService {

    PerfilClienteSalida guardar(
            PerfilClienteGuardar perfilGuardar,
            Integer clienteId
    );

    PerfilClienteSalida obtenerPorId(
            Long id
    );

    PerfilClienteSalida obtenerPorClienteId(
            Integer clienteId
    );

    PerfilClientePublicoSalida obtenerPerfilPublicoPorClienteId(
            Integer clienteId
    );

    PerfilClienteSalida modificar(
            Integer clienteId,
            PerfilClienteModificar perfilModificar
    );
}
