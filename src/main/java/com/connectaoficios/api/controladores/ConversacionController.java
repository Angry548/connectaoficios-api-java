package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.conversacion.ConversacionGuardar;
import com.connectaoficios.api.dtos.conversacion.ConversacionSalida;
import com.connectaoficios.api.servicios.interfaces.IConversacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversaciones")
public class ConversacionController {

    private final IConversacionService conversacionService;

    public ConversacionController(
            IConversacionService conversacionService
    ) {
        this.conversacionService = conversacionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<ConversacionSalida> guardar(
            @Valid @RequestBody ConversacionGuardar conversacionGuardar,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                Integer.valueOf(jwt.getSubject());

        ConversacionSalida conversacion =
                conversacionService.guardar(
                        conversacionGuardar,
                        usuarioId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(conversacion);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<ConversacionSalida> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                Integer.valueOf(jwt.getSubject());

        ConversacionSalida conversacion =
                conversacionService.obtenerPorId(
                        id,
                        usuarioId
                );

        return ResponseEntity.ok(conversacion);
    }

    @GetMapping("/solicitud/{solicitudId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<ConversacionSalida> obtenerPorSolicitud(
            @PathVariable Long solicitudId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                Integer.valueOf(jwt.getSubject());

        ConversacionSalida conversacion =
                conversacionService.obtenerPorSolicitud(
                        solicitudId,
                        usuarioId
                );

        return ResponseEntity.ok(conversacion);
    }
}