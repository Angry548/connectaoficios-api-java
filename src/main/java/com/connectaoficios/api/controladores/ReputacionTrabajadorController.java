package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorFiltroDTO;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorGuardar;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorModificar;
import com.connectaoficios.api.dtos.reputacion.ReputacionTrabajadorSalida;
import com.connectaoficios.api.enums.InsigniaReputacion;
import com.connectaoficios.api.servicios.interfaces.IReputacionTrabajadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/reputaciones")
public class ReputacionTrabajadorController {

    private final IReputacionTrabajadorService reputacionTrabajadorService;

    public ReputacionTrabajadorController(
            IReputacionTrabajadorService reputacionTrabajadorService
    ) {
        this.reputacionTrabajadorService =
                reputacionTrabajadorService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<ReputacionTrabajadorSalida> guardar(
            @Valid @RequestBody ReputacionTrabajadorGuardar reputacionGuardar
    ) {

        ReputacionTrabajadorSalida reputacion =
                reputacionTrabajadorService.guardar(
                        reputacionGuardar
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reputacion);
    }

    @GetMapping("/trabajador/{perfilTrabajadorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReputacionTrabajadorSalida>
    obtenerPorPerfilTrabajador(
            @PathVariable Long perfilTrabajadorId
    ) {

        ReputacionTrabajadorSalida reputacion =
                reputacionTrabajadorService
                        .obtenerPorPerfilTrabajador(
                                perfilTrabajadorId
                        );

        return ResponseEntity.ok(
                reputacion
        );
    }

    @GetMapping("/ranking")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReputacionTrabajadorSalida>>
    obtenerRanking() {

        List<ReputacionTrabajadorSalida> ranking =
                reputacionTrabajadorService
                        .obtenerRanking();

        return ResponseEntity.ok(
                ranking
        );
    }

    @GetMapping("/paginadas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginaSalida<ReputacionTrabajadorSalida>>
    buscarConFiltros(
            @RequestParam(required = false) Long perfilTrabajadorId,
            @RequestParam(required = false) InsigniaReputacion insignia,
            @RequestParam(required = false) BigDecimal promedioMinimo,
            @RequestParam(required = false) BigDecimal promedioMaximo,
            @RequestParam(required = false) Integer totalResenasMinimo,
            @RequestParam(required = false) Integer totalResenasMaximo,
            @RequestParam(required = false) Integer serviciosCompletadosMinimo,
            @RequestParam(required = false) Integer serviciosCompletadosMaximo,
            @RequestParam(required = false) BigDecimal puntuacionMinima,
            @RequestParam(required = false) BigDecimal puntuacionMaxima,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        ReputacionTrabajadorFiltroDTO filtro =
                new ReputacionTrabajadorFiltroDTO();

        filtro.setPerfilTrabajadorId(
                perfilTrabajadorId
        );

        filtro.setInsignia(
                insignia
        );

        filtro.setPromedioMinimo(
                promedioMinimo
        );

        filtro.setPromedioMaximo(
                promedioMaximo
        );

        filtro.setTotalResenasMinimo(
                totalResenasMinimo
        );

        filtro.setTotalResenasMaximo(
                totalResenasMaximo
        );

        filtro.setServiciosCompletadosMinimo(
                serviciosCompletadosMinimo
        );

        filtro.setServiciosCompletadosMaximo(
                serviciosCompletadosMaximo
        );

        filtro.setPuntuacionMinima(
                puntuacionMinima
        );

        filtro.setPuntuacionMaxima(
                puntuacionMaxima
        );

        return ResponseEntity.ok(
                reputacionTrabajadorService
                        .buscarConFiltros(
                                filtro,
                                page,
                                size
                        )
        );
    }

    @PutMapping("/trabajador/{perfilTrabajadorId}")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<ReputacionTrabajadorSalida> modificar(
            @PathVariable Long perfilTrabajadorId,
            @Valid @RequestBody ReputacionTrabajadorModificar reputacionModificar
    ) {

        ReputacionTrabajadorSalida reputacion =
                reputacionTrabajadorService
                        .modificar(
                                perfilTrabajadorId,
                                reputacionModificar
                        );

        return ResponseEntity.ok(
                reputacion
        );
    }

    @PutMapping("/trabajador/{perfilTrabajadorId}/recalcular")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<ReputacionTrabajadorSalida> recalcular(
            @PathVariable Long perfilTrabajadorId
    ) {

        ReputacionTrabajadorSalida reputacion =
                reputacionTrabajadorService
                        .recalcular(
                                perfilTrabajadorId
                        );

        return ResponseEntity.ok(
                reputacion
        );
    }
}