package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.perfil.PerfilAdministradorGuardar;
import com.connectaoficios.api.dtos.perfil.PerfilAdministradorModificar;
import com.connectaoficios.api.dtos.perfil.PerfilAdministradorPublicoSalida;
import com.connectaoficios.api.dtos.perfil.PerfilAdministradorSalida;

public interface IPerfilAdministradorService {

    PerfilAdministradorSalida guardar(
            PerfilAdministradorGuardar perfilGuardar,
            Integer administradorId
    );

    PerfilAdministradorSalida obtenerPorId(
            Long id
    );

    PerfilAdministradorSalida obtenerPorAdministradorId(
            Integer administradorId
    );

    PerfilAdministradorPublicoSalida obtenerPerfilPublicoPorAdministradorId(
            Integer administradorId
    );

    PerfilAdministradorSalida modificar(
            Integer administradorId,
            PerfilAdministradorModificar perfilModificar
    );
}
