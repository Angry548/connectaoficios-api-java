package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.categoria.CategoriaBusquedaSalida;
import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.servicio.ServicioAdminFiltroDTO;
import com.connectaoficios.api.dtos.servicio.ServicioAdminSalida;
import com.connectaoficios.api.dtos.servicio.ServicioCambiarEstado;

import java.util.List;

public interface IServicioAdminService {

    PaginaSalida<ServicioAdminSalida> buscar(
            ServicioAdminFiltroDTO filtro,
            int pagina,
            int tamanio
    );

    ServicioAdminSalida obtenerPorId(
            Long id
    );

    ServicioAdminSalida cambiarEstado(
            Long id,
            ServicioCambiarEstado request
    );

    List<CategoriaBusquedaSalida> listarCategorias();
}