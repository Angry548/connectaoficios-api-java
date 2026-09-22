package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.solicitud.SolicitudServicioCancelar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioGuardar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioRespuesta;
import com.connectaoficios.api.servicios.interfaces.ISolicitudServicioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudServicioController {

    private final ISolicitudServicioService solicitudServicioService;

    public SolicitudServicioController(ISolicitudServicioService solicitudServicioService) {
        this.solicitudServicioService = solicitudServicioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<SolicitudServicioRespuesta> guardar(
            @Valid @RequestBody SolicitudServicioGuardar solicitudGuardar
    ) {
        SolicitudServicioRespuesta solicitud = solicitudServicioService.guardar(solicitudGuardar);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(solicitud);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<List<SolicitudServicioRespuesta>> obtenerTodas() {
        List<SolicitudServicioRespuesta> solicitudes = solicitudServicioService.obtenerTodas();
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/paginadas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<Page<SolicitudServicioRespuesta>> obtenerTodasPaginadas(Pageable pageable) {
        Page<SolicitudServicioRespuesta> solicitudes = solicitudServicioService.obtenerTodasPaginadas(pageable);
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudServicioRespuesta> obtenerPorId(@PathVariable Long id) {
        SolicitudServicioRespuesta solicitud = solicitudServicioService.obtenerPorId(id);
        return ResponseEntity.ok(solicitud);
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<List<SolicitudServicioRespuesta>> obtenerPorCliente(@PathVariable Integer clienteId) {
        List<SolicitudServicioRespuesta> solicitudes = solicitudServicioService.obtenerPorCliente(clienteId);
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/trabajador/{trabajadorId}")
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<List<SolicitudServicioRespuesta>> obtenerPorTrabajador(@PathVariable Integer trabajadorId) {
        List<SolicitudServicioRespuesta> solicitudes = solicitudServicioService.obtenerPorTrabajador(trabajadorId);
        return ResponseEntity.ok(solicitudes);
    }

    @PatchMapping("/{id}/aceptar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<SolicitudServicioRespuesta> aceptar(@PathVariable Long id) {
        SolicitudServicioRespuesta solicitud = solicitudServicioService.aceptar(id);
        return ResponseEntity.ok(solicitud);
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<SolicitudServicioRespuesta> rechazar(@PathVariable Long id) {
        SolicitudServicioRespuesta solicitud = solicitudServicioService.rechazar(id);
        return ResponseEntity.ok(solicitud);
    }

    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<SolicitudServicioRespuesta> iniciar(@PathVariable Long id) {
        SolicitudServicioRespuesta solicitud = solicitudServicioService.iniciar(id);
        return ResponseEntity.ok(solicitud);
    }

    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<SolicitudServicioRespuesta> completar(@PathVariable Long id) {
        SolicitudServicioRespuesta solicitud = solicitudServicioService.completar(id);
        return ResponseEntity.ok(solicitud);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudServicioRespuesta> cancelar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudServicioCancelar solicitudCancelar
    ) {
        SolicitudServicioRespuesta solicitud = solicitudServicioService.cancelar(id, solicitudCancelar);
        return ResponseEntity.ok(solicitud);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        solicitudServicioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
