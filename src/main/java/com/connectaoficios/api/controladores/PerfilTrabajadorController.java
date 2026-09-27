package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorBusquedaSalida;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorFiltroDTO;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorGuardar;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorModificar;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorSalida;
import com.connectaoficios.api.servicios.interfaces.IPerfilTrabajadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfiles-trabajador")
public class PerfilTrabajadorController {

    private final IPerfilTrabajadorService perfilService;

    public PerfilTrabajadorController(
            IPerfilTrabajadorService perfilService
    ) {
        this.perfilService = perfilService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<PerfilTrabajadorSalida> guardar(
            @Valid @RequestBody PerfilTrabajadorGuardar dto,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer trabajadorId =
                Integer.valueOf(
                        jwt.getSubject()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        perfilService.guardar(
                                dto,
                                trabajadorId
                        )
                );
    }

    @GetMapping
    public ResponseEntity<PaginaSalida<PerfilTrabajadorSalida>> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long zonaPrincipalId,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false)
            Integer porcentajeCompletitudMinimo,
            @RequestParam(required = false)
            Integer porcentajeCompletitudMaximo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        PerfilTrabajadorFiltroDTO filtro =
                new PerfilTrabajadorFiltroDTO();

        filtro.setTexto(texto);
        filtro.setZonaPrincipalId(
                zonaPrincipalId
        );
        filtro.setDepartamento(
                departamento
        );
        filtro.setMunicipio(
                municipio
        );
        filtro.setPorcentajeCompletitudMinimo(
                porcentajeCompletitudMinimo
        );
        filtro.setPorcentajeCompletitudMaximo(
                porcentajeCompletitudMaximo
        );

        return ResponseEntity.ok(
                perfilService.buscarConFiltros(
                        filtro,
                        page,
                        size
                )
        );
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<PerfilTrabajadorBusquedaSalida>> buscar(
            @RequestParam String texto,
            @RequestParam(defaultValue = "10") int limit
    ) {

        return ResponseEntity.ok(
                perfilService.buscarParaAutocomplete(
                        texto,
                        limit
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfilTrabajadorSalida> obtenerPorId(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                perfilService.obtenerPorId(id)
        );
    }

    @GetMapping("/trabajador/{trabajadorId}")
    public ResponseEntity<PerfilTrabajadorSalida> obtenerPorTrabajador(
            @PathVariable Integer trabajadorId
    ) {

        return ResponseEntity.ok(
                perfilService.obtenerPorTrabajadorId(
                        trabajadorId
                )
        );
    }

    @PutMapping("/mi-perfil")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<PerfilTrabajadorSalida> modificar(
            @Valid @RequestBody PerfilTrabajadorModificar dto,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Integer trabajadorId =
                Integer.valueOf(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                perfilService.modificar(
                        trabajadorId,
                        dto
                )
        );
    }
}