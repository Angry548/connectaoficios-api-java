package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.resena.ResenaFiltroDTO;
import com.connectaoficios.api.dtos.resena.ResenaGuardar;
import com.connectaoficios.api.dtos.resena.ResenaSalida;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.servicios.interfaces.IResenaService;
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
@RequestMapping("/api/resenas")
public class ResenaController {

    private final IResenaService resenaService;

    public ResenaController(
            IResenaService resenaService
    ) {
        this.resenaService =
                resenaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ResenaSalida> guardar(
            @Valid @RequestBody ResenaGuardar dto,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer clienteId =
                obtenerUsuarioId(
                        jwt
                );

        ResenaSalida resena =
                resenaService.guardar(
                        dto,
                        clienteId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resena);
    }

    @GetMapping("/paginadas")
    public ResponseEntity<PaginaSalida<ResenaSalida>> buscarConFiltros(
            @RequestParam(required = false) Long perfilTrabajadorId,
            @RequestParam(required = false) Long servicioId,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) Integer calificacion,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) LocalDateTime fechaDesde,
            @RequestParam(required = false) LocalDateTime fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        ResenaFiltroDTO filtro =
                new ResenaFiltroDTO();

        filtro.setPerfilTrabajadorId(
                perfilTrabajadorId
        );

        filtro.setServicioId(
                servicioId
        );

        filtro.setClienteId(
                clienteId
        );

        filtro.setCalificacion(
                calificacion
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

        PaginaSalida<ResenaSalida> resenas =
                resenaService.buscarConFiltros(
                        filtro,
                        page,
                        size
                );

        return ResponseEntity.ok(
                resenas
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResenaSalida> obtenerPorId(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                resenaService.obtenerPorId(
                        id
                )
        );
    }

    @GetMapping("/trabajador/{perfilTrabajadorId}")
    public ResponseEntity<List<ResenaSalida>> obtenerPorTrabajador(
            @PathVariable Long perfilTrabajadorId
    ) {

        return ResponseEntity.ok(
                resenaService.obtenerPorPerfilTrabajador(
                        perfilTrabajadorId
                )
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