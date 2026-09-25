package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoAprobar;
import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoGuardar;
import com.connectaoficios.api.dtos.transaccionpago.TransaccionPagoSalida;
import com.connectaoficios.api.enums.EstadoTransaccion;
import com.connectaoficios.api.servicios.interfaces.ITransaccionPagoService;
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
@RequestMapping("/api/transacciones-pago")
public class TransaccionPagoController {

    private static final String CLAIM_NAME_IDENTIFIER =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier";

    private final ITransaccionPagoService transaccionPagoService;

    public TransaccionPagoController(
            ITransaccionPagoService transaccionPagoService
    ) {
        this.transaccionPagoService =
                transaccionPagoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<TransaccionPagoSalida> guardar(
            @Valid @RequestBody TransaccionPagoGuardar dto,
            JwtAuthenticationToken authentication
    ) {

        Integer trabajadorId =
                obtenerIdDeUsuario(
                        authentication.getToken()
                );

        TransaccionPagoSalida transaccion =
                transaccionPagoService.guardar(
                        dto,
                        trabajadorId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transaccion);
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<Page<TransaccionPagoSalida>>
    obtenerTodosPaginados(Pageable pageable) {

        return ResponseEntity.ok(
                transaccionPagoService
                        .obtenerTodosPaginados(pageable)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransaccionPagoSalida> obtenerPorId(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                transaccionPagoService.obtenerPorId(id)
        );
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<Page<TransaccionPagoSalida>>
    obtenerPorEstado(
            @PathVariable EstadoTransaccion estado,
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                transaccionPagoService
                        .obtenerPorEstado(
                                estado,
                                pageable
                        )
        );
    }

    @GetMapping("/promocion/{promocionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TransaccionPagoSalida>>
    obtenerPorPromocion(
            @PathVariable Long promocionId
    ) {

        return ResponseEntity.ok(
                transaccionPagoService
                        .obtenerPorPromocion(promocionId)
        );
    }

    @GetMapping("/mis-transacciones")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<List<TransaccionPagoSalida>>
    obtenerMisTransacciones(
            JwtAuthenticationToken authentication
    ) {

        Integer trabajadorId =
                obtenerIdDeUsuario(
                        authentication.getToken()
                );

        return ResponseEntity.ok(
                transaccionPagoService
                        .obtenerPorTrabajador(trabajadorId)
        );
    }

    @PutMapping("/{id}/aprobar")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<TransaccionPagoSalida> aprobar(
            @PathVariable Long id,
            @Valid @RequestBody TransaccionPagoAprobar dto
    ) {

        return ResponseEntity.ok(
                transaccionPagoService.aprobar(id, dto)
        );
    }

    @PutMapping("/{id}/rechazar")
    @PreAuthorize(
            "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
    )
    public ResponseEntity<TransaccionPagoSalida> rechazar(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                transaccionPagoService.rechazar(id)
        );
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<TransaccionPagoSalida> cancelar(
            @PathVariable Long id,
            JwtAuthenticationToken authentication
    ) {

        Integer trabajadorId =
                obtenerIdDeUsuario(
                        authentication.getToken()
                );

        return ResponseEntity.ok(
                transaccionPagoService.cancelar(
                        id,
                        trabajadorId
                )
        );
    }

    private Integer obtenerIdDeUsuario(Jwt jwt) {

        String id =
                jwt.getClaimAsString(
                        CLAIM_NAME_IDENTIFIER
                );

        if (id == null || id.isBlank()) {
            id = jwt.getSubject();
        }

        return Integer.valueOf(id);
    }
}