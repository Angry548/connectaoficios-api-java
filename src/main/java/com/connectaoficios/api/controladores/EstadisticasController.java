package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.estadistica.EstadisticasSalida;
import com.connectaoficios.api.servicios.interfaces.IEstadisticasService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/estadisticas")
public class EstadisticasController {

    private final IEstadisticasService estadisticasService;

    public EstadisticasController(
            IEstadisticasService estadisticasService
    ) {
        this.estadisticasService =
                estadisticasService;
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<EstadisticasSalida> obtenerEstadisticas(
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime fechaDesde,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime fechaHasta
    ) {

        return ResponseEntity.ok(
                estadisticasService.obtenerEstadisticas(
                        fechaDesde,
                        fechaHasta
                )
        );
    }
}