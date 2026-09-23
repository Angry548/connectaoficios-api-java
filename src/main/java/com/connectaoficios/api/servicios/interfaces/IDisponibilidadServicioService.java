package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.disponibilidad.DisponibilidadServicioGuardar;
import com.connectaoficios.api.dtos.disponibilidad.DisponibilidadServicioModificar;
import com.connectaoficios.api.dtos.disponibilidad.DisponibilidadServicioSalida;
import com.connectaoficios.api.enums.DiaSemana;

import java.util.List;

public interface IDisponibilidadServicioService {

    DisponibilidadServicioSalida guardar(
            DisponibilidadServicioGuardar disponibilidadGuardar
    );

    DisponibilidadServicioSalida modificar(
            Long id,
            DisponibilidadServicioModificar disponibilidadModificar
    );

    void eliminar(Long id);

    DisponibilidadServicioSalida obtenerPorId(Long id);

    List<DisponibilidadServicioSalida> listarPorServicio(Long servicioId);

    List<DisponibilidadServicioSalida> listarActivasPorServicio(Long servicioId);

    List<DisponibilidadServicioSalida> buscarPorDia(DiaSemana diaSemana);
}