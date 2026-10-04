package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.reporte.ReporteFiltroDTO;
import com.connectaoficios.api.dtos.reporte.ReporteGuardar;
import com.connectaoficios.api.dtos.reporte.ReporteRechazo;
import com.connectaoficios.api.dtos.reporte.ReporteResolucion;
import com.connectaoficios.api.dtos.reporte.ReporteSalida;
import com.connectaoficios.api.enums.EstadoReporte;
import com.connectaoficios.api.enums.TipoReporte;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.servicios.interfaces.IReporteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final String CLAIM_NAME_IDENTIFIER =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier";

    private final IReporteService reporteService;

    public ReporteController(
            IReporteService reporteService
    ) {
        this.reporteService = reporteService;
    }

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('CLIENTE', 'TRABAJADOR')"
    )
    public ResponseEntity<ReporteSalida> guardar(
            @Valid @RequestBody ReporteGuardar dto,
            JwtAuthenticationToken authentication
    ) {

        Integer usuarioId =
                obtenerIdDeUsuario(
                        authentication.getToken()
                );

        dto.setUsuarioReportanteId(
                usuarioId
        );

        ReporteSalida reporte =
                reporteService.guardar(
                        dto
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reporte);
    }

    @GetMapping("/mis-reportes")
    @PreAuthorize(
            "hasAnyRole('CLIENTE', 'TRABAJADOR')"
    )
    public ResponseEntity<PaginaSalida<ReporteSalida>> obtenerMisReportes(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) TipoReporte tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            JwtAuthenticationToken authentication
    ) {

        Integer usuarioId =
                obtenerIdDeUsuario(
                        authentication.getToken()
                );

        ReporteFiltroDTO filtro =
                new ReporteFiltroDTO();

        filtro.setTexto(
                texto
        );

        filtro.setEstado(
                estado
        );

        filtro.setTipo(
                tipo
        );

        filtro.setUsuarioReportanteId(
                usuarioId
        );

        return ResponseEntity.ok(
                reporteService.buscarConFiltros(
                        filtro,
                        page,
                        size
                )
        );
    }

    @GetMapping("/mis-reportes/{id}")
    @PreAuthorize(
            "hasAnyRole('CLIENTE', 'TRABAJADOR')"
    )
    public ResponseEntity<ReporteSalida> obtenerMiReportePorId(
            @PathVariable Long id,
            JwtAuthenticationToken authentication
    ) {

        Integer usuarioId =
                obtenerIdDeUsuario(
                        authentication.getToken()
                );

        ReporteSalida reporte =
                reporteService.obtenerPorId(
                        id
                );

        if (!usuarioId.equals(
                reporte.getUsuarioReportanteId()
        )) {
            throw new ReglaNegocioException(
                    "No puede consultar un reporte perteneciente a otro usuario"
            );
        }

        return ResponseEntity.ok(
                reporte
        );
    }

    @GetMapping("/admin")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<PaginaSalida<ReporteSalida>> obtenerReportesAdmin(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) TipoReporte tipo,
            @RequestParam(required = false) Integer usuarioReportanteId,
            @RequestParam(required = false) Integer usuarioReportadoId,
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) Integer administradorId,
            @RequestParam(required = false) LocalDateTime fechaDesde,
            @RequestParam(required = false) LocalDateTime fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        ReporteFiltroDTO filtro =
                new ReporteFiltroDTO();

        filtro.setTexto(
                texto
        );

        filtro.setEstado(
                estado
        );

        filtro.setTipo(
                tipo
        );

        filtro.setUsuarioReportanteId(
                usuarioReportanteId
        );

        filtro.setUsuarioReportadoId(
                usuarioReportadoId
        );

        filtro.setServicioId(
                servicioId
        );

        filtro.setAdministradorId(
                administradorId
        );

        filtro.setFechaDesde(
                fechaDesde
        );

        filtro.setFechaHasta(
                fechaHasta
        );

        return ResponseEntity.ok(
                reporteService.buscarConFiltros(
                        filtro,
                        page,
                        size
                )
        );
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<ReporteSalida> obtenerReporteAdmin(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                reporteService.obtenerPorId(
                        id
                )
        );
    }

    @PutMapping("/admin/{id}/iniciar-revision")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<ReporteSalida> iniciarRevision(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                reporteService.iniciarRevision(
                        id
                )
        );
    }

    @PutMapping("/admin/{id}/resolver")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<ReporteSalida> resolver(
            @PathVariable Long id,
            @Valid @RequestBody ReporteResolucion dto,
            JwtAuthenticationToken authentication
    ) {

        Integer administradorId =
                obtenerIdDeUsuario(
                        authentication.getToken()
                );

        return ResponseEntity.ok(
                reporteService.resolver(
                        id,
                        dto,
                        administradorId
                )
        );
    }

    @PutMapping("/admin/{id}/rechazar")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<ReporteSalida> rechazar(
            @PathVariable Long id,
            @Valid @RequestBody ReporteRechazo dto,
            JwtAuthenticationToken authentication
    ) {

        Integer administradorId =
                obtenerIdDeUsuario(
                        authentication.getToken()
                );

        return ResponseEntity.ok(
                reporteService.rechazar(
                        id,
                        dto,
                        administradorId
                )
        );
    }

    private Integer obtenerIdDeUsuario(
            Jwt jwt
    ) {

        if (jwt == null) {
            throw new ReglaNegocioException(
                    "No se pudo identificar al usuario autenticado"
            );
        }

        String id =
                jwt.getClaimAsString(
                        CLAIM_NAME_IDENTIFIER
                );

        if (id == null || id.isBlank()) {
            id = jwt.getSubject();
        }

        if (id == null || id.isBlank()) {
            throw new ReglaNegocioException(
                    "No se pudo identificar al usuario autenticado"
            );
        }

        try {
            return Integer.valueOf(
                    id
            );
        } catch (NumberFormatException exception) {
            throw new ReglaNegocioException(
                    "El identificador del usuario autenticado no es válido"
            );
        }
    }
}