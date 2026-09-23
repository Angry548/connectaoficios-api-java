package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorGuardar;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorModificar;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorSalida;

public interface IPerfilTrabajadorService {

    PerfilTrabajadorSalida guardar(
            PerfilTrabajadorGuardar perfilGuardar,
            Integer trabajadorId
    );

    PerfilTrabajadorSalida obtenerPorId(Long id);

    PerfilTrabajadorSalida obtenerPorTrabajadorId(
            Integer trabajadorId
    );

    PerfilTrabajadorSalida modificar(
            Integer trabajadorId,
            PerfilTrabajadorModificar perfilModificar
    );
}
