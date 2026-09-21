package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorGuardar;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorModificar;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorSalida;
import com.connectaoficios.api.servicios.interfaces.IReputacionTrabajadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reputaciones")
public class ReputacionTrabajadorController {

    private final IReputacionTrabajadorService reputacionTrabajadorService;

    public ReputacionTrabajadorController(
            IReputacionTrabajadorService reputacionTrabajadorService
    ) {
        this.reputacionTrabajadorService = reputacionTrabajadorService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<ReputacionTrabajadorSalida> guardar(
            @Valid @RequestBody ReputacionTrabajadorGuardar reputacionGuardar
    ) {

        ReputacionTrabajadorSalida reputacion =
                reputacionTrabajadorService.guardar(reputacionGuardar);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reputacion);
    }

    @GetMapping("/trabajador/{perfilTrabajadorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReputacionTrabajadorSalida> obtenerPorPerfilTrabajador(
            @PathVariable Long perfilTrabajadorId
    ) {

        ReputacionTrabajadorSalida reputacion =
                reputacionTrabajadorService
                        .obtenerPorPerfilTrabajador(perfilTrabajadorId);

        return ResponseEntity.ok(reputacion);
    }

    @GetMapping("/ranking")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReputacionTrabajadorSalida>> obtenerRanking() {

        List<ReputacionTrabajadorSalida> ranking =
                reputacionTrabajadorService.obtenerRanking();

        return ResponseEntity.ok(ranking);
    }

    @PutMapping("/trabajador/{perfilTrabajadorId}")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<ReputacionTrabajadorSalida> modificar(
            @PathVariable Long perfilTrabajadorId,
            @Valid @RequestBody ReputacionTrabajadorModificar reputacionModificar
    ) {

        ReputacionTrabajadorSalida reputacion =
                reputacionTrabajadorService.modificar(
                        perfilTrabajadorId,
                        reputacionModificar
                );

        return ResponseEntity.ok(reputacion);
    }

    @PutMapping("/trabajador/{perfilTrabajadorId}/recalcular")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<ReputacionTrabajadorSalida> recalcular(
            @PathVariable Long perfilTrabajadorId
    ) {

        ReputacionTrabajadorSalida reputacion =
                reputacionTrabajadorService
                        .recalcular(perfilTrabajadorId);

        return ResponseEntity.ok(reputacion);
    }
}




