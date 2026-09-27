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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final String CLAIM_NAME_IDENTIFIER =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier";

    private final IReporteService reporteService;

    public ReporteController(
            IReporteService reporteService
    ) {
        this.reporteService =
                reporteService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReporteSalida> guardar(
            @Valid @RequestBody ReporteGuardar reporteGuardar,
            JwtAuthenticationToken authentication
    ) {

        Integer usuarioAutenticadoId =
                obtenerIdDeUsuario(
                        authentication.getToken()
                );

        reporteGuardar.setUsuarioReportanteId(
                usuarioAutenticadoId
        );

        ReporteSalida reporte =
                reporteService.guardar(
                        reporteGuardar
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reporte);
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<Page<ReporteSalida>> obtenerTodosPaginados(
            Pageable pageable
    ) {

        Page<ReporteSalida> reportes =
                reporteService.obtenerTodosPaginados(
                        pageable
                );

        return ResponseEntity.ok(
                reportes
        );
    }

    @GetMapping("/paginados")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<PaginaSalida<ReporteSalida>> buscarConFiltros(
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

        PaginaSalida<ReporteSalida> reportes =
                reporteService.buscarConFiltros(
                        filtro,
                        page,
                        size
                );

        return ResponseEntity.ok(
                reportes
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<ReporteSalida> obtenerPorId(
            @PathVariable Long id
    ) {

        ReporteSalida reporte =
                reporteService.obtenerPorId(
                        id
                );

        return ResponseEntity.ok(
                reporte
        );
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<Page<ReporteSalida>> obtenerPorEstado(
            @PathVariable EstadoReporte estado,
            Pageable pageable
    ) {

        Page<ReporteSalida> reportes =
                reporteService.obtenerPorEstado(
                        estado,
                        pageable
                );

        return ResponseEntity.ok(
                reportes
        );
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<Page<ReporteSalida>> obtenerPorTipo(
            @PathVariable TipoReporte tipo,
            Pageable pageable
    ) {

        Page<ReporteSalida> reportes =
                reporteService.obtenerPorTipo(
                        tipo,
                        pageable
                );

        return ResponseEntity.ok(
                reportes
        );
    }

    @GetMapping("/reportante/{usuarioReportanteId}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<List<ReporteSalida>> obtenerPorUsuarioReportante(
            @PathVariable Integer usuarioReportanteId
    ) {

        List<ReporteSalida> reportes =
                reporteService.obtenerPorUsuarioReportante(
                        usuarioReportanteId
                );

        return ResponseEntity.ok(
                reportes
        );
    }

    @GetMapping("/reportado/{usuarioReportadoId}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<List<ReporteSalida>> obtenerPorUsuarioReportado(
            @PathVariable Integer usuarioReportadoId
    ) {

        List<ReporteSalida> reportes =
                reporteService.obtenerPorUsuarioReportado(
                        usuarioReportadoId
                );

        return ResponseEntity.ok(
                reportes
        );
    }

    @PutMapping("/{id}/iniciar-revision")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<ReporteSalida> iniciarRevision(
            @PathVariable Long id
    ) {

        ReporteSalida reporte =
                reporteService.iniciarRevision(
                        id
                );

        return ResponseEntity.ok(
                reporte
        );
    }

    @PutMapping("/{id}/resolver")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<ReporteSalida> resolver(
            @PathVariable Long id,
            @Valid @RequestBody ReporteResolucion reporteResolucion,
            JwtAuthenticationToken authentication
    ) {

        Integer administradorId =
                obtenerIdDeUsuario(
                        authentication.getToken()
                );

        ReporteSalida reporte =
                reporteService.resolver(
                        id,
                        reporteResolucion,
                        administradorId
                );

        return ResponseEntity.ok(
                reporte
        );
    }

    @PutMapping("/{id}/rechazar")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<ReporteSalida> rechazar(
            @PathVariable Long id,
            @Valid @RequestBody ReporteRechazo reporteRechazo,
            JwtAuthenticationToken authentication
    ) {

        Integer administradorId =
                obtenerIdDeUsuario(
                        authentication.getToken()
                );

        ReporteSalida reporte =
                reporteService.rechazar(
                        id,
                        reporteRechazo,
                        administradorId
                );

        return ResponseEntity.ok(
                reporte
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
            return Integer.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new ReglaNegocioException(
                    "El identificador del usuario autenticado no es válido"
            );
        }
    }
}