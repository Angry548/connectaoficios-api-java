package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.categoria.CategoriaBusquedaSalida;
import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.servicio.ServicioAdminFiltroDTO;
import com.connectaoficios.api.dtos.servicio.ServicioAdminSalida;
import com.connectaoficios.api.dtos.servicio.ServicioCambiarEstado;
import com.connectaoficios.api.enums.EstadoServicio;
import com.connectaoficios.api.servicios.interfaces.IServicioAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/servicios")
@PreAuthorize(
        "hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')"
)
public class ServicioAdminController {

    private final IServicioAdminService servicioAdminService;

    public ServicioAdminController(
            IServicioAdminService servicioAdminService
    ) {
        this.servicioAdminService =
                servicioAdminService;
    }

    @GetMapping
    public ResponseEntity<
            PaginaSalida<ServicioAdminSalida>
            > listar(
            @RequestParam(required = false)
            String texto,

            @RequestParam(required = false)
            Integer trabajadorId,

            @RequestParam(required = false)
            Long categoriaId,

            @RequestParam(required = false)
            EstadoServicio estado,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {
        ServicioAdminFiltroDTO filtro =
                new ServicioAdminFiltroDTO();

        filtro.setTexto(
                texto
        );

        filtro.setTrabajadorId(
                trabajadorId
        );

        filtro.setCategoriaId(
                categoriaId
        );

        filtro.setEstado(
                estado
        );

        return ResponseEntity.ok(
                servicioAdminService.buscar(
                        filtro,
                        page,
                        size
                )
        );
    }

    @GetMapping("/categorias")
    public ResponseEntity<
            List<CategoriaBusquedaSalida>
            > listarCategorias() {
        return ResponseEntity.ok(
                servicioAdminService
                        .listarCategorias()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioAdminSalida> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                servicioAdminService
                        .obtenerPorId(id)
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ServicioAdminSalida> cambiarEstado(
            @PathVariable Long id,
            @Valid
            @RequestBody
            ServicioCambiarEstado request
    ) {
        return ResponseEntity.ok(
                servicioAdminService
                        .cambiarEstado(
                                id,
                                request
                        )
        );
    }
}