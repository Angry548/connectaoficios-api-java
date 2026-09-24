package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.resena.ResenaGuardar;
import com.connectaoficios.api.dtos.resena.ResenaSalida;
import com.connectaoficios.api.servicios.interfaces.IResenaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    private final IResenaService resenaService;

    public ResenaController(
            IResenaService resenaService
    ) {
        this.resenaService = resenaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ResenaSalida> guardar(
            @Valid @RequestBody ResenaGuardar dto,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer clienteId =
                Integer.valueOf(jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        resenaService.guardar(
                                dto,
                                clienteId
                        )
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResenaSalida> obtenerPorId(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                resenaService.obtenerPorId(id)
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
}