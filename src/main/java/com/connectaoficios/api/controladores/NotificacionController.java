package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.notificacion.NotificacionRespuesta;
import com.connectaoficios.api.servicios.interfaces.INotificacionService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final INotificacionService notificacionService;

    public NotificacionController(
            INotificacionService notificacionService
    ) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('CLIENTE','TRABAJADOR','ADMINISTRADOR','ADMINISTRADOR_PRINCIPAL')"
    )
    public ResponseEntity<Page<NotificacionRespuesta>> listarMisNotificaciones(
            @RequestParam(required = false) Boolean leida,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Integer usuarioId = obtenerUsuarioId(jwt);

        return ResponseEntity.ok(
                notificacionService.listarPorUsuario(
                        usuarioId,
                        leida,
                        page,
                        size
                )
        );
    }

    @GetMapping("/no-leidas/count")
    @PreAuthorize(
            "hasAnyRole('CLIENTE','TRABAJADOR','ADMINISTRADOR','ADMINISTRADOR_PRINCIPAL')"
    )
    public ResponseEntity<Map<String, Long>> contarNoLeidas(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Integer usuarioId = obtenerUsuarioId(jwt);

        long cantidad =
                notificacionService.contarNoLeidas(
                        usuarioId
                );

        return ResponseEntity.ok(
                Map.of(
                        "cantidad",
                        cantidad
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('CLIENTE','TRABAJADOR','ADMINISTRADOR','ADMINISTRADOR_PRINCIPAL')"
    )
    public ResponseEntity<NotificacionRespuesta> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Integer usuarioId = obtenerUsuarioId(jwt);

        return ResponseEntity.ok(
                notificacionService.obtenerPorId(
                        id,
                        usuarioId
                )
        );
    }

    @PatchMapping("/{id}/leer")
    @PreAuthorize(
            "hasAnyRole('CLIENTE','TRABAJADOR','ADMINISTRADOR','ADMINISTRADOR_PRINCIPAL')"
    )
    public ResponseEntity<NotificacionRespuesta> marcarComoLeida(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Integer usuarioId = obtenerUsuarioId(jwt);

        return ResponseEntity.ok(
                notificacionService.marcarComoLeida(
                        id,
                        usuarioId
                )
        );
    }

    @PatchMapping("/{id}/no-leida")
    @PreAuthorize(
            "hasAnyRole('CLIENTE','TRABAJADOR','ADMINISTRADOR','ADMINISTRADOR_PRINCIPAL')"
    )
    public ResponseEntity<NotificacionRespuesta> marcarComoNoLeida(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Integer usuarioId = obtenerUsuarioId(jwt);

        return ResponseEntity.ok(
                notificacionService.marcarComoNoLeida(
                        id,
                        usuarioId
                )
        );
    }

    @PatchMapping("/leer-todas")
    @PreAuthorize(
            "hasAnyRole('CLIENTE','TRABAJADOR','ADMINISTRADOR','ADMINISTRADOR_PRINCIPAL')"
    )
    public ResponseEntity<Map<String, Object>> marcarTodasComoLeidas(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Integer usuarioId = obtenerUsuarioId(jwt);

        int cantidad =
                notificacionService.marcarTodasComoLeidas(
                        usuarioId
                );

        return ResponseEntity.ok(
                Map.of(
                        "mensaje",
                        "Notificaciones actualizadas correctamente.",
                        "cantidad",
                        cantidad
                )
        );
    }

    private Integer obtenerUsuarioId(
            Jwt jwt
    ) {
        if (
                jwt == null ||
                        jwt.getSubject() == null ||
                        jwt.getSubject().isBlank()
        ) {
            throw new RuntimeException(
                    "No se pudo identificar al usuario autenticado."
            );
        }

        try {
            return Integer.valueOf(
                    jwt.getSubject()
            );
        } catch (NumberFormatException ex) {
            throw new RuntimeException(
                    "El identificador del usuario autenticado no es válido."
            );
        }
    }
}