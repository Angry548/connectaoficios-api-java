package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.conversacion.ConversacionFiltroDTO;
import com.connectaoficios.api.dtos.conversacion.ConversacionGuardar;
import com.connectaoficios.api.dtos.conversacion.ConversacionSalida;
import com.connectaoficios.api.enums.EstadoSolicitud;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.servicios.interfaces.IConversacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/conversaciones")
public class ConversacionController {

    private final IConversacionService conversacionService;

    public ConversacionController(
            IConversacionService conversacionService
    ) {
        this.conversacionService =
                conversacionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<ConversacionSalida> guardar(
            @Valid @RequestBody ConversacionGuardar conversacionGuardar,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                obtenerUsuarioId(jwt);

        ConversacionSalida conversacion =
                conversacionService.guardar(
                        conversacionGuardar,
                        usuarioId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(conversacion);
    }

    @GetMapping("/paginadas")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<PaginaSalida<ConversacionSalida>> buscarConversaciones(
            @RequestParam(required = false) Long solicitudId,
            @RequestParam(required = false) EstadoSolicitud estadoSolicitud,
            @RequestParam(required = false) LocalDateTime fechaDesde,
            @RequestParam(required = false) LocalDateTime fechaHasta,
            @RequestParam(required = false) Boolean puedeEnviarMensajes,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                obtenerUsuarioId(jwt);

        ConversacionFiltroDTO filtro =
                new ConversacionFiltroDTO();

        filtro.setSolicitudId(
                solicitudId
        );

        filtro.setEstadoSolicitud(
                estadoSolicitud
        );

        filtro.setFechaDesde(
                fechaDesde
        );

        filtro.setFechaHasta(
                fechaHasta
        );

        filtro.setPuedeEnviarMensajes(
                puedeEnviarMensajes
        );

        PaginaSalida<ConversacionSalida> conversaciones =
                conversacionService.buscarConversaciones(
                        filtro,
                        usuarioId,
                        page,
                        size
                );

        return ResponseEntity.ok(
                conversaciones
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<ConversacionSalida> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                obtenerUsuarioId(jwt);

        ConversacionSalida conversacion =
                conversacionService.obtenerPorId(
                        id,
                        usuarioId
                );

        return ResponseEntity.ok(
                conversacion
        );
    }

    @GetMapping("/solicitud/{solicitudId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public ResponseEntity<ConversacionSalida> obtenerPorSolicitud(
            @PathVariable Long solicitudId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer usuarioId =
                obtenerUsuarioId(jwt);

        ConversacionSalida conversacion =
                conversacionService.obtenerPorSolicitud(
                        solicitudId,
                        usuarioId
                );

        return ResponseEntity.ok(
                conversacion
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