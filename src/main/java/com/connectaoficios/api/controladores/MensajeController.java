package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.mensaje.MensajeFiltroDTO;
import com.connectaoficios.api.dtos.mensaje.MensajeGuardar;
import com.connectaoficios.api.dtos.mensaje.MensajeSalida;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.servicios.interfaces.IMensajeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    private final IMensajeService mensajeService;

    public MensajeController(
            IMensajeService mensajeService
    ) {
        this.mensajeService =
                mensajeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<MensajeSalida> guardar(
            @Valid @RequestBody MensajeGuardar mensajeGuardar,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer remitenteId =
                obtenerUsuarioId(
                        jwt
                );

        MensajeSalida mensaje =
                mensajeService.guardar(
                        mensajeGuardar,
                        remitenteId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mensaje);
    }

    @GetMapping("/conversacion/{conversacionId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<List<MensajeSalida>> obtenerPorConversacion(
            @PathVariable Long conversacionId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                obtenerUsuarioId(
                        jwt
                );

        List<MensajeSalida> mensajes =
                mensajeService.obtenerPorConversacion(
                        conversacionId,
                        usuarioId
                );

        return ResponseEntity.ok(
                mensajes
        );
    }

    @GetMapping("/conversacion/{conversacionId}/paginados")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<PaginaSalida<MensajeSalida>> buscarConFiltros(
            @PathVariable Long conversacionId,
            @RequestParam(required = false) Integer remitenteId,
            @RequestParam(required = false) Boolean leido,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) LocalDateTime fechaDesde,
            @RequestParam(required = false) LocalDateTime fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                obtenerUsuarioId(
                        jwt
                );

        MensajeFiltroDTO filtro =
                new MensajeFiltroDTO();

        filtro.setConversacionId(
                conversacionId
        );

        filtro.setRemitenteId(
                remitenteId
        );

        filtro.setLeido(
                leido
        );

        filtro.setTexto(
                texto
        );

        filtro.setFechaDesde(
                fechaDesde
        );

        filtro.setFechaHasta(
                fechaHasta
        );

        PaginaSalida<MensajeSalida> mensajes =
                mensajeService.buscarConFiltros(
                        filtro,
                        usuarioId,
                        page,
                        size
                );

        return ResponseEntity.ok(
                mensajes
        );
    }

    @PutMapping("/{mensajeId}/leido")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<Void> marcarComoLeido(
            @PathVariable Long mensajeId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                obtenerUsuarioId(
                        jwt
                );

        mensajeService.marcarComoLeido(
                mensajeId,
                usuarioId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/conversacion/{conversacionId}/no-leidos")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<Long> contarNoLeidos(
            @PathVariable Long conversacionId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                obtenerUsuarioId(
                        jwt
                );

        long cantidad =
                mensajeService.contarNoLeidos(
                        conversacionId,
                        usuarioId
                );

        return ResponseEntity.ok(
                cantidad
        );
    }

    private Integer obtenerUsuarioId(
            Jwt jwt
    ) {

        if (jwt == null
                || jwt.getSubject() == null
                || jwt.getSubject().isBlank()) {

            throw new ReglaNegocioException(
                    "No se pudo identificar al usuario autenticado"
            );
        }

        try {

            return Integer.valueOf(
                    jwt.getSubject()
            );

        } catch (NumberFormatException exception) {

            throw new ReglaNegocioException(
                    "El identificador del usuario autenticado no es válido"
            );
        }
    }
}