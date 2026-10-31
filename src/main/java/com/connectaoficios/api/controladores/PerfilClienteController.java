package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.perfil.*;
import com.connectaoficios.api.servicios.interfaces.IPerfilClienteService;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfiles-cliente")
public class PerfilClienteController {
    private final IPerfilClienteService perfilService;

    public PerfilClienteController(IPerfilClienteService perfilService) {
        this.perfilService = perfilService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PerfilClienteSalida> guardar(
            @Valid @RequestBody PerfilClienteGuardar dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(perfilService.guardar(dto, obtenerClienteId(jwt)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfilClienteSalida> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(perfilService.obtenerPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<PerfilClienteSalida> obtenerPorCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(perfilService.obtenerPorClienteId(clienteId));
    }

    // Vista pública del perfil para usuarios autenticados, como el resto de la API.
    @GetMapping("/cliente/{clienteId}/publico")
    public ResponseEntity<PerfilClientePublicoSalida> obtenerPublico(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(perfilService.obtenerPerfilPublicoPorClienteId(clienteId));
    }

    @PutMapping("/mi-perfil")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PerfilClienteSalida> modificar(
            @Valid @RequestBody PerfilClienteModificar dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(perfilService.modificar(obtenerClienteId(jwt), dto));
    }

    private Integer obtenerClienteId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new ReglaNegocioException("No se pudo identificar al usuario autenticado");
        }
        try {
            int clienteId = Integer.parseInt(jwt.getSubject());
            if (clienteId > 0) {
                return clienteId;
            }
        } catch (NumberFormatException exception) {
            throw new ReglaNegocioException("El identificador del usuario autenticado no es válido");
        }
        throw new ReglaNegocioException("El identificador del usuario autenticado no es válido");
    }
}
