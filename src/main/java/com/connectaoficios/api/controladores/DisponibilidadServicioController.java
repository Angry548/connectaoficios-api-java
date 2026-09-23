package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.disponibilidad.DisponibilidadServicioGuardar;
import com.connectaoficios.api.dtos.disponibilidad.DisponibilidadServicioModificar;
import com.connectaoficios.api.dtos.disponibilidad.DisponibilidadServicioSalida;
import com.connectaoficios.api.enums.DiaSemana;
import com.connectaoficios.api.servicios.interfaces.IDisponibilidadServicioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disponibilidades")
public class DisponibilidadServicioController {

    private final IDisponibilidadServicioService disponibilidadService;

    public DisponibilidadServicioController(
            IDisponibilidadServicioService disponibilidadService
    ) {
        this.disponibilidadService = disponibilidadService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<DisponibilidadServicioSalida> guardar(
            @Valid @RequestBody
            DisponibilidadServicioGuardar disponibilidadGuardar
    ) {

        DisponibilidadServicioSalida disponibilidad =
                disponibilidadService.guardar(disponibilidadGuardar);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(disponibilidad);
    }

    @GetMapping("/servicio/{servicioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DisponibilidadServicioSalida>>
    listarPorServicio(
            @PathVariable Long servicioId
    ) {

        List<DisponibilidadServicioSalida> disponibilidades =
                disponibilidadService
                        .listarPorServicio(servicioId);

        return ResponseEntity.ok(disponibilidades);
    }

    @GetMapping("/servicio/{servicioId}/activas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DisponibilidadServicioSalida>>
    listarActivasPorServicio(
            @PathVariable Long servicioId
    ) {

        List<DisponibilidadServicioSalida> disponibilidades =
                disponibilidadService
                        .listarActivasPorServicio(servicioId);

        return ResponseEntity.ok(disponibilidades);
    }

    @GetMapping("/dia/{diaSemana}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DisponibilidadServicioSalida>>
    buscarPorDia(
            @PathVariable DiaSemana diaSemana
    ) {

        List<DisponibilidadServicioSalida> disponibilidades =
                disponibilidadService.buscarPorDia(diaSemana);

        return ResponseEntity.ok(disponibilidades);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DisponibilidadServicioSalida> obtenerPorId(
            @PathVariable Long id
    ) {

        DisponibilidadServicioSalida disponibilidad =
                disponibilidadService.obtenerPorId(id);

        return ResponseEntity.ok(disponibilidad);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<DisponibilidadServicioSalida> modificar(
            @PathVariable Long id,
            @Valid @RequestBody
            DisponibilidadServicioModificar disponibilidadModificar
    ) {

        DisponibilidadServicioSalida disponibilidad =
                disponibilidadService.modificar(
                        id,
                        disponibilidadModificar
                );

        return ResponseEntity.ok(disponibilidad);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        disponibilidadService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}