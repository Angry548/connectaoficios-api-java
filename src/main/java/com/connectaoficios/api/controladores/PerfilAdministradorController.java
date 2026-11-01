package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.perfil.*;
import com.connectaoficios.api.servicios.interfaces.IPerfilAdministradorService;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfiles-administrador")
public class PerfilAdministradorController {
    private final IPerfilAdministradorService perfilService;

    public PerfilAdministradorController(IPerfilAdministradorService perfilService) {
        this.perfilService = perfilService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')")
    public ResponseEntity<PerfilAdministradorSalida> guardar(
            @Valid @RequestBody PerfilAdministradorGuardar dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(perfilService.guardar(dto, obtenerAdministradorId(jwt)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfilAdministradorSalida> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(perfilService.obtenerPorId(id));
    }

    @GetMapping("/administrador/{administradorId}")
    public ResponseEntity<PerfilAdministradorSalida> obtenerPorAdministrador(@PathVariable Integer administradorId) {
        return ResponseEntity.ok(perfilService.obtenerPorAdministradorId(administradorId));
    }

    // Vista pública del perfil para usuarios autenticados, como el resto de la API.
    @GetMapping("/administrador/{administradorId}/publico")
    public ResponseEntity<PerfilAdministradorPublicoSalida> obtenerPublico(@PathVariable Integer administradorId) {
        return ResponseEntity.ok(perfilService.obtenerPerfilPublicoPorAdministradorId(administradorId));
    }

    @PutMapping("/mi-perfil")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')")
    public ResponseEntity<PerfilAdministradorSalida> modificar(
            @Valid @RequestBody PerfilAdministradorModificar dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(perfilService.modificar(obtenerAdministradorId(jwt), dto));
    }

    private Integer obtenerAdministradorId(Jwt jwt) {
        return com.connectaoficios.api.seguridad.IdentidadJwt.usuarioId(jwt);
    }
}
