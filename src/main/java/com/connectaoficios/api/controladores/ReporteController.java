package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.reporte.ReporteGuardar;
import com.connectaoficios.api.dtos.reporte.ReporteRechazo;
import com.connectaoficios.api.dtos.reporte.ReporteResolucion;
import com.connectaoficios.api.dtos.reporte.ReporteSalida;
import com.connectaoficios.api.enums.EstadoReporte;
import com.connectaoficios.api.enums.TipoReporte;
import com.connectaoficios.api.servicios.interfaces.IReporteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final String CLAIM_NAME_IDENTIFIER =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier";

    private final IReporteService reporteService;

    public ReporteController(IReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReporteSalida> guardar(
            @Valid @RequestBody ReporteGuardar reporteGuardar
    ) {
        ReporteSalida reporte = reporteService.guardar(reporteGuardar);
        return ResponseEntity.status(HttpStatus.CREATED).body(reporte);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<Page<ReporteSalida>> obtenerTodosPaginados(Pageable pageable) {
        Page<ReporteSalida> reportes = reporteService.obtenerTodosPaginados(pageable);
        return ResponseEntity.ok(reportes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<ReporteSalida> obtenerPorId(@PathVariable Long id) {
        ReporteSalida reporte = reporteService.obtenerPorId(id);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<Page<ReporteSalida>> obtenerPorEstado(
            @PathVariable EstadoReporte estado,
            Pageable pageable
    ) {
        Page<ReporteSalida> reportes = reporteService.obtenerPorEstado(estado, pageable);
        return ResponseEntity.ok(reportes);
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<Page<ReporteSalida>> obtenerPorTipo(
            @PathVariable TipoReporte tipo,
            Pageable pageable
    ) {
        Page<ReporteSalida> reportes = reporteService.obtenerPorTipo(tipo, pageable);
        return ResponseEntity.ok(reportes);
    }

    @GetMapping("/reportante/{usuarioReportanteId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<List<ReporteSalida>> obtenerPorUsuarioReportante(
            @PathVariable Integer usuarioReportanteId
    ) {
        List<ReporteSalida> reportes = reporteService.obtenerPorUsuarioReportante(usuarioReportanteId);
        return ResponseEntity.ok(reportes);
    }

    @GetMapping("/reportado/{usuarioReportadoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<List<ReporteSalida>> obtenerPorUsuarioReportado(
            @PathVariable Integer usuarioReportadoId
    ) {
        List<ReporteSalida> reportes = reporteService.obtenerPorUsuarioReportado(usuarioReportadoId);
        return ResponseEntity.ok(reportes);
    }

    @PutMapping("/{id}/iniciar-revision")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<ReporteSalida> iniciarRevision(@PathVariable Long id) {
        ReporteSalida reporte = reporteService.iniciarRevision(id);
        return ResponseEntity.ok(reporte);
    }

    @PutMapping("/{id}/resolver")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<ReporteSalida> resolver(
            @PathVariable Long id,
            @Valid @RequestBody ReporteResolucion reporteResolucion,
            JwtAuthenticationToken authentication
    ) {
        Integer administradorId = obtenerIdDeAdministrador(authentication.getToken());
        ReporteSalida reporte = reporteService.resolver(id, reporteResolucion, administradorId);
        return ResponseEntity.ok(reporte);
    }

    @PutMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<ReporteSalida> rechazar(
            @PathVariable Long id,
            @Valid @RequestBody ReporteRechazo reporteRechazo,
            JwtAuthenticationToken authentication
    ) {
        Integer administradorId = obtenerIdDeAdministrador(authentication.getToken());
        ReporteSalida reporte = reporteService.rechazar(id, reporteRechazo, administradorId);
        return ResponseEntity.ok(reporte);
    }

    private Integer obtenerIdDeAdministrador(Jwt jwt) {
        String id = jwt.getClaimAsString(CLAIM_NAME_IDENTIFIER);
        if (id == null || id.isBlank()) {
            id = jwt.getSubject();
        }
        return Integer.valueOf(id);
    }
}