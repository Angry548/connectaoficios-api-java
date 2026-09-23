package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.promocion.PromocionGuardar;
import com.connectaoficios.api.dtos.promocion.PromocionResumenSalida;
import com.connectaoficios.api.dtos.promocion.PromocionSalida;
import com.connectaoficios.api.enums.EstadoPromocion;
import com.connectaoficios.api.servicios.interfaces.IPromocionService;
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
@RequestMapping("/api/promociones")
public class PromocionController {

    private static final String CLAIM_NAME_IDENTIFIER =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier";

    private final IPromocionService promocionService;

    public PromocionController(IPromocionService promocionService) {
        this.promocionService = promocionService;
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<PromocionResumenSalida> obtenerResumen(
            @RequestParam Long servicioId,
            @RequestParam Long planId
    ) {
        return ResponseEntity.ok(promocionService.obtenerResumen(servicioId, planId));
    }

    @PostMapping
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<PromocionSalida> guardar(
            @Valid @RequestBody PromocionGuardar dto,
            JwtAuthenticationToken authentication
    ) {
        Integer trabajadorId = obtenerIdDeUsuario(authentication.getToken());
        PromocionSalida promocion = promocionService.guardar(dto, trabajadorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(promocion);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')")
    public ResponseEntity<Page<PromocionSalida>> obtenerTodosPaginados(Pageable pageable) {
        return ResponseEntity.ok(promocionService.obtenerTodosPaginados(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PromocionSalida> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(promocionService.obtenerPorId(id));
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')")
    public ResponseEntity<Page<PromocionSalida>> obtenerPorEstado(
            @PathVariable EstadoPromocion estado,
            Pageable pageable
    ) {
        return ResponseEntity.ok(promocionService.obtenerPorEstado(estado, pageable));
    }

    @GetMapping("/servicio/{servicioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PromocionSalida>> obtenerPorServicio(@PathVariable Long servicioId) {
        return ResponseEntity.ok(promocionService.obtenerPorServicio(servicioId));
    }

    @GetMapping("/mis-promociones")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<List<PromocionSalida>> obtenerMisPromociones(
            JwtAuthenticationToken authentication
    ) {
        Integer trabajadorId = obtenerIdDeUsuario(authentication.getToken());
        return ResponseEntity.ok(promocionService.obtenerPorTrabajador(trabajadorId));
    }

    @GetMapping("/{id}/vigente")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> estaVigente(@PathVariable Long id) {
        return ResponseEntity.ok(promocionService.estaVigente(id));
    }

    @PutMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')")
    public ResponseEntity<PromocionSalida> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(promocionService.finalizar(id));
    }

    private Integer obtenerIdDeUsuario(Jwt jwt) {
        String id = jwt.getClaimAsString(CLAIM_NAME_IDENTIFIER);
        if (id == null || id.isBlank()) {
            id = jwt.getSubject();
        }
        return Integer.valueOf(id);
    }
}