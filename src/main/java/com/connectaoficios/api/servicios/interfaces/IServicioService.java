package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.servicio.ServicioCambiarEstado;
import com.connectaoficios.api.dtos.servicio.ServicioFiltroDTO;
import com.connectaoficios.api.dtos.servicio.ServicioGuardar;
import com.connectaoficios.api.dtos.servicio.ServicioModificar;
import com.connectaoficios.api.dtos.servicio.ServicioSalida;

import java.util.List;

public interface IServicioService {

    ServicioSalida publicar(ServicioGuardar servicioGuardar);

    ServicioSalida modificar(
            Long id,
            ServicioModificar servicioModificar
    );

    ServicioSalida cambiarEstado(
            Long id,
            ServicioCambiarEstado servicioCambiarEstado
    );

    void eliminar(Long id);

    ServicioSalida obtenerPorId(Long id);

    List<ServicioSalida> listarActivos();

    List<ServicioSalida> listarPorCategoria(Long categoriaId);

    List<ServicioSalida> listarPorTrabajador(Long perfilTrabajadorId);

    List<ServicioSalida> buscarPorZona(Long zonaId);

    List<ServicioSalida> buscarConFiltros(ServicioFiltroDTO filtro);
}