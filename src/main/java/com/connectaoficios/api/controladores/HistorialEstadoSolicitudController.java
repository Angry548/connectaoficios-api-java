package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.historial.HistorialEstadoSolicitudGuardar;
import com.connectaoficios.api.dtos.historial.HistorialEstadoSolicitudRespuesta;
import com.connectaoficios.api.servicios.interfaces.IHistorialEstadoSolicitudService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historial-solicitudes")
public class HistorialEstadoSolicitudController {

    private final IHistorialEstadoSolicitudService historialService;

    public HistorialEstadoSolicitudController(IHistorialEstadoSolicitudService historialService) {
        this.historialService = historialService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR', 'ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<HistorialEstadoSolicitudRespuesta> guardar(
            @Valid @RequestBody HistorialEstadoSolicitudGuardar historialGuardar
    ) {
        HistorialEstadoSolicitudRespuesta historial = historialService.guardar(historialGuardar);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(historial);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<List<HistorialEstadoSolicitudRespuesta>> obtenerTodos() {
        List<HistorialEstadoSolicitudRespuesta> historial = historialService.obtenerTodos();
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/paginados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<Page<HistorialEstadoSolicitudRespuesta>> obtenerTodosPaginados(Pageable pageable) {
        Page<HistorialEstadoSolicitudRespuesta> historial = historialService.obtenerTodosPaginados(pageable);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HistorialEstadoSolicitudRespuesta> obtenerPorId(@PathVariable Long id) {
        HistorialEstadoSolicitudRespuesta historial = historialService.obtenerPorId(id);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/solicitud/{solicitudId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HistorialEstadoSolicitudRespuesta>> obtenerPorSolicitud(@PathVariable Long solicitudId) {
        List<HistorialEstadoSolicitudRespuesta> historial = historialService.obtenerPorSolicitud(solicitudId);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/solicitud/{solicitudId}/paginados")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<HistorialEstadoSolicitudRespuesta>> obtenerPorSolicitudPaginado(
            @PathVariable Long solicitudId,
            Pageable pageable
    ) {
        Page<HistorialEstadoSolicitudRespuesta> historial = historialService.obtenerPorSolicitudPaginado(solicitudId, pageable);
        return ResponseEntity.ok(historial);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        historialService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
