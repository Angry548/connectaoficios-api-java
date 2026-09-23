package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.mensaje.MensajeGuardar;
import com.connectaoficios.api.dtos.mensaje.MensajeSalida;
import com.connectaoficios.api.servicios.interfaces.IMensajeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    private final IMensajeService mensajeService;

    public MensajeController(
            IMensajeService mensajeService
    ) {
        this.mensajeService = mensajeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<MensajeSalida> guardar(
            @Valid @RequestBody MensajeGuardar mensajeGuardar,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer remitenteId =
                Integer.valueOf(jwt.getSubject());

        MensajeSalida mensaje =
                mensajeService.guardar(
                        mensajeGuardar,
                        remitenteId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mensaje);
    }

    @GetMapping("/conversacion/{conversacionId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<List<MensajeSalida>> obtenerPorConversacion(
            @PathVariable Long conversacionId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                Integer.valueOf(jwt.getSubject());

        List<MensajeSalida> mensajes =
                mensajeService.obtenerPorConversacion(
                        conversacionId,
                        usuarioId
                );

        return ResponseEntity.ok(mensajes);
    }

    @PutMapping("/{mensajeId}/leido")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<Void> marcarComoLeido(
            @PathVariable Long mensajeId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                Integer.valueOf(jwt.getSubject());

        mensajeService.marcarComoLeido(
                mensajeId,
                usuarioId
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conversacion/{conversacionId}/no-leidos")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<Long> contarNoLeidos(
            @PathVariable Long conversacionId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                Integer.valueOf(jwt.getSubject());

        long cantidad =
                mensajeService.contarNoLeidos(
                        conversacionId,
                        usuarioId
                );

        return ResponseEntity.ok(cantidad);
    }
}