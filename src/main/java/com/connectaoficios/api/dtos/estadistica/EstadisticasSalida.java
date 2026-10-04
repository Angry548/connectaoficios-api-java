package com.connectaoficios.api.dtos.estadistica;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadisticasSalida {

    private long totalServicios;

    private long totalSolicitudes;

    private long totalSolicitudesCompletadas;

    private long totalReportes;

    private long solicitudesPendientes;

    private long solicitudesAceptadas;

    private long solicitudesEnProceso;

    private long solicitudesRechazadas;

    private long solicitudesCanceladas;

    private long reportesPendientes;

    private long reportesEnRevision;

    private long reportesResueltos;

    private long reportesRechazados;
}