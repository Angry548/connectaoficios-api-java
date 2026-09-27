package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.planpromocion.PlanPromocionFiltroDTO;
import com.connectaoficios.api.dtos.planpromocion.PlanPromocionGuardar;
import com.connectaoficios.api.dtos.planpromocion.PlanPromocionModificar;
import com.connectaoficios.api.dtos.planpromocion.PlanPromocionSalida;
import com.connectaoficios.api.servicios.interfaces.IPlanPromocionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/planes-promocion")
public class PlanPromocionController {

    private final IPlanPromocionService planPromocionService;

    public PlanPromocionController(
            IPlanPromocionService planPromocionService
    ) {
        this.planPromocionService =
                planPromocionService;
    }

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<PlanPromocionSalida> guardar(
            @Valid @RequestBody PlanPromocionGuardar dto
    ) {

        PlanPromocionSalida plan =
                planPromocionService.guardar(
                        dto
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(plan);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PlanPromocionSalida>>
    obtenerTodosPaginados(
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                planPromocionService
                        .obtenerTodosPaginados(
                                pageable
                        )
        );
    }

    @GetMapping("/paginados")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginaSalida<PlanPromocionSalida>>
    buscarConFiltros(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Integer duracionMinima,
            @RequestParam(required = false) Integer duracionMaxima,
            @RequestParam(required = false) BigDecimal precioMinimo,
            @RequestParam(required = false) BigDecimal precioMaximo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        PlanPromocionFiltroDTO filtro =
                new PlanPromocionFiltroDTO();

        filtro.setTexto(
                texto
        );

        filtro.setActivo(
                activo
        );

        filtro.setDuracionMinima(
                duracionMinima
        );

        filtro.setDuracionMaxima(
                duracionMaxima
        );

        filtro.setPrecioMinimo(
                precioMinimo
        );

        filtro.setPrecioMaximo(
                precioMaximo
        );

        return ResponseEntity.ok(
                planPromocionService
                        .buscarConFiltros(
                                filtro,
                                page,
                                size
                        )
        );
    }

    @GetMapping("/activos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PlanPromocionSalida>>
    obtenerActivos(
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                planPromocionService
                        .obtenerActivos(
                                pageable
                        )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlanPromocionSalida> obtenerPorId(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                planPromocionService
                        .obtenerPorId(id)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<PlanPromocionSalida> modificar(
            @PathVariable Long id,
            @Valid @RequestBody PlanPromocionModificar dto
    ) {

        return ResponseEntity.ok(
                planPromocionService
                        .modificar(
                                id,
                                dto
                        )
        );
    }

    @PutMapping("/{id}/activar")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<PlanPromocionSalida> activar(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                planPromocionService
                        .activar(id)
        );
    }

    @PutMapping("/{id}/desactivar")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<PlanPromocionSalida> desactivar(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                planPromocionService
                        .desactivar(id)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasRole('ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        planPromocionService.eliminar(
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}