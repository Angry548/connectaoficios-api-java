package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioRechazar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioCancelar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioFiltroDTO;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioGuardar;
import com.connectaoficios.api.dtos.solicitud.SolicitudServicioRespuesta;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.servicios.interfaces.ISolicitudServicioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudServicioController {

    private final ISolicitudServicioService solicitudServicioService;

    public SolicitudServicioController(
            ISolicitudServicioService solicitudServicioService
    ) {
        this.solicitudServicioService = solicitudServicioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<SolicitudServicioRespuesta> guardar(
            @Valid @RequestBody SolicitudServicioGuardar solicitudGuardar
    ) {
        SolicitudServicioRespuesta solicitud =
                solicitudServicioService.guardar(
                        solicitudGuardar
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(solicitud);
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<List<SolicitudServicioRespuesta>> obtenerTodas() {
        return ResponseEntity.ok(
                solicitudServicioService.obtenerTodas()
        );
    }

    @GetMapping("/paginadas")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<PaginaSalida<SolicitudServicioRespuesta>> obtenerPaginadas(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) Integer trabajadorId,
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        SolicitudServicioFiltroDTO filtro =
                new SolicitudServicioFiltroDTO();

        filtro.setTexto(texto);
        filtro.setServicioId(servicioId);
        filtro.setClienteId(clienteId);
        filtro.setTrabajadorId(trabajadorId);
        filtro.setEstado(estado);
        filtro.setFechaDesde(fechaDesde);
        filtro.setFechaHasta(fechaHasta);

        return ResponseEntity.ok(
                solicitudServicioService.buscarConFiltros(
                        filtro,
                        page,
                        size
                )
        );
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize(
            "hasAnyRole('CLIENTE', 'ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<List<SolicitudServicioRespuesta>> obtenerPorCliente(
            @PathVariable Integer clienteId
    ) {
        return ResponseEntity.ok(
                solicitudServicioService.obtenerPorCliente(
                        clienteId
                )
        );
    }

    @GetMapping("/cliente/{clienteId}/paginadas")
    @PreAuthorize(
            "hasAnyRole('CLIENTE', 'ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<PaginaSalida<SolicitudServicioRespuesta>> obtenerPorClientePaginadas(
            @PathVariable Integer clienteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                solicitudServicioService.obtenerPorClientePaginadas(
                        clienteId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/trabajador/{trabajadorId}")
    @PreAuthorize(
            "hasAnyRole('TRABAJADOR', 'ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<List<SolicitudServicioRespuesta>> obtenerPorTrabajador(
            @PathVariable Integer trabajadorId
    ) {
        return ResponseEntity.ok(
                solicitudServicioService.obtenerPorTrabajador(
                        trabajadorId
                )
        );
    }

    @GetMapping("/trabajador/{trabajadorId}/paginadas")
    @PreAuthorize(
            "hasAnyRole('TRABAJADOR', 'ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<PaginaSalida<SolicitudServicioRespuesta>> obtenerPorTrabajadorPaginadas(
            @PathVariable Integer trabajadorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                solicitudServicioService.obtenerPorTrabajadorPaginadas(
                        trabajadorId,
                        page,
                        size
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudServicioRespuesta> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                solicitudServicioService.obtenerPorId(
                        id
                )
        );
    }

    @PatchMapping("/{id}/aceptar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<SolicitudServicioRespuesta> aceptar(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                solicitudServicioService.aceptar(
                        id
                )
        );
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<SolicitudServicioRespuesta> rechazar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudServicioRechazar solicitudRechazar
    ) {
        return ResponseEntity.ok(
                solicitudServicioService.rechazar(
                        id,
                        solicitudRechazar
                )
        );
    }

    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<SolicitudServicioRespuesta> iniciar(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                solicitudServicioService.iniciar(
                        id
                )
        );
    }

    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<SolicitudServicioRespuesta> completar(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                solicitudServicioService.completar(
                        id
                )
        );
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SolicitudServicioRespuesta> cancelar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudServicioCancelar solicitudCancelar
    ) {
        return ResponseEntity.ok(
                solicitudServicioService.cancelar(
                        id,
                        solicitudCancelar
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        solicitudServicioService.eliminar(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}