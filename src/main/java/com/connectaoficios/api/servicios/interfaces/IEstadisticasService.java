package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.estadistica.EstadisticasSalida;

import java.time.LocalDateTime;

public interface IEstadisticasService {

    EstadisticasSalida obtenerEstadisticas(
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    );
}