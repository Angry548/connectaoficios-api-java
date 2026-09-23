package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.notificacion.NotificacionGuardar;
import com.connectaoficios.api.dtos.notificacion.NotificacionRespuesta;
import com.connectaoficios.api.servicios.interfaces.INotificacionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final INotificacionService notificacionService;

    public NotificacionController(INotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR', 'ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<NotificacionRespuesta> guardar(
            @Valid @RequestBody NotificacionGuardar notificacionGuardar
    ) {
        NotificacionRespuesta notificacion = notificacionService.guardar(notificacionGuardar);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificacion);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<List<NotificacionRespuesta>> obtenerTodas() {
        List<NotificacionRespuesta> notificaciones = notificacionService.obtenerTodas();
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/paginadas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<Page<NotificacionRespuesta>> obtenerTodasPaginadas(Pageable pageable) {
        Page<NotificacionRespuesta> notificaciones = notificacionService.obtenerTodasPaginadas(pageable);
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificacionRespuesta> obtenerPorId(@PathVariable Long id) {
        NotificacionRespuesta notificacion = notificacionService.obtenerPorId(id);
        return ResponseEntity.ok(notificacion);
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificacionRespuesta>> obtenerPorUsuario(@PathVariable Integer usuarioId) {
        List<NotificacionRespuesta> notificaciones = notificacionService.obtenerPorUsuario(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/usuario/{usuarioId}/paginadas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificacionRespuesta>> obtenerPorUsuarioPaginado(
            @PathVariable Integer usuarioId,
            Pageable pageable
    ) {
        Page<NotificacionRespuesta> notificaciones = notificacionService.obtenerPorUsuarioPaginado(usuarioId, pageable);
        return ResponseEntity.ok(notificaciones);
    }

    @PatchMapping("/{id}/marcar-leida")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificacionRespuesta> marcarComoLeida(@PathVariable Long id) {
        NotificacionRespuesta notificacion = notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok(notificacion);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        notificacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
