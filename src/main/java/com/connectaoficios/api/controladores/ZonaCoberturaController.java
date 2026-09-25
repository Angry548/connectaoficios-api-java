package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.zona.ZonaCoberturaGuardar;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaModificar;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaSalida;
import com.connectaoficios.api.servicios.interfaces.IZonaCoberturaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zonas-cobertura")
public class ZonaCoberturaController {

    private final IZonaCoberturaService zonaCoberturaService;

    public ZonaCoberturaController(
            IZonaCoberturaService zonaCoberturaService
    ) {
        this.zonaCoberturaService = zonaCoberturaService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')")
    public ResponseEntity<ZonaCoberturaSalida> guardar(
            @Valid @RequestBody ZonaCoberturaGuardar zonaGuardar
    ) {

        ZonaCoberturaSalida zona =
                zonaCoberturaService.guardar(zonaGuardar);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(zona);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ZonaCoberturaSalida>> listarActivas() {

        List<ZonaCoberturaSalida> zonas =
                zonaCoberturaService.listarActivas();

        return ResponseEntity.ok(zonas);
    }

    @GetMapping("/todas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')")
    public ResponseEntity<List<ZonaCoberturaSalida>> listarTodas() {

        List<ZonaCoberturaSalida> zonas =
                zonaCoberturaService.listar();

        return ResponseEntity.ok(zonas);
    }

    @GetMapping("/departamento/{departamento}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ZonaCoberturaSalida>> buscarPorDepartamento(
            @PathVariable String departamento
    ) {

        List<ZonaCoberturaSalida> zonas =
                zonaCoberturaService
                        .buscarPorDepartamento(departamento);

        return ResponseEntity.ok(zonas);
    }

    @GetMapping("/municipio/{municipio}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ZonaCoberturaSalida>> buscarPorMunicipio(
            @PathVariable String municipio
    ) {

        List<ZonaCoberturaSalida> zonas =
                zonaCoberturaService
                        .buscarPorMunicipio(municipio);

        return ResponseEntity.ok(zonas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ZonaCoberturaSalida> obtenerPorId(
            @PathVariable Long id
    ) {

        ZonaCoberturaSalida zona =
                zonaCoberturaService.obtenerPorId(id);

        return ResponseEntity.ok(zona);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')")
    public ResponseEntity<ZonaCoberturaSalida> modificar(
            @PathVariable Long id,
            @Valid @RequestBody ZonaCoberturaModificar zonaModificar
    ) {

        ZonaCoberturaSalida zona =
                zonaCoberturaService.modificar(id, zonaModificar);

        return ResponseEntity.ok(zona);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        zonaCoberturaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}