package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.servicio.ServicioBusquedaSalida;
import com.connectaoficios.api.dtos.servicio.ServicioCambiarEstado;
import com.connectaoficios.api.dtos.servicio.ServicioFiltroDTO;
import com.connectaoficios.api.dtos.servicio.ServicioGuardar;
import com.connectaoficios.api.dtos.servicio.ServicioModificar;
import com.connectaoficios.api.dtos.servicio.ServicioSalida;
import com.connectaoficios.api.enums.DiaSemana;
import com.connectaoficios.api.enums.EstadoServicio;
import com.connectaoficios.api.servicios.interfaces.IServicioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final IServicioService servicioService;

    public ServicioController(
            IServicioService servicioService
    ) {
        this.servicioService = servicioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<ServicioSalida> publicar(
            @Valid @RequestBody ServicioGuardar servicioGuardar
    ) {

        ServicioSalida servicio =
                servicioService.publicar(
                        servicioGuardar
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicio);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginaSalida<ServicioSalida>> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long perfilTrabajadorId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) DiaSemana diaSemana,
            @RequestParam(required = false) EstadoServicio estado,
            @RequestParam(required = false) BigDecimal tarifaMinima,
            @RequestParam(required = false) BigDecimal tarifaMaxima,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        ServicioFiltroDTO filtro =
                new ServicioFiltroDTO();

        filtro.setTexto(texto);
        filtro.setPerfilTrabajadorId(perfilTrabajadorId);
        filtro.setCategoriaId(categoriaId);
        filtro.setZonaId(zonaId);
        filtro.setDiaSemana(diaSemana);
        filtro.setEstado(
                estado != null
                        ? estado
                        : EstadoServicio.ACTIVO
        );
        filtro.setTarifaMinima(tarifaMinima);
        filtro.setTarifaMaxima(tarifaMaxima);

        return ResponseEntity.ok(
                servicioService
                        .buscarConFiltrosPaginado(
                                filtro,
                                page,
                                size
                        )
        );
    }

    @GetMapping("/buscar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicioBusquedaSalida>> buscar(
            @RequestParam String texto,
            @RequestParam(defaultValue = "10") int limit
    ) {

        return ResponseEntity.ok(
                servicioService
                        .buscarParaAutocomplete(
                                texto,
                                limit
                        )
        );
    }

    @GetMapping("/trabajador/{perfilTrabajadorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginaSalida<ServicioSalida>> listarPorTrabajador(
            @PathVariable Long perfilTrabajadorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
                servicioService
                        .listarPorTrabajadorPaginado(
                                perfilTrabajadorId,
                                page,
                                size
                        )
        );
    }

    @GetMapping("/categoria/{categoriaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginaSalida<ServicioSalida>> listarPorCategoria(
            @PathVariable Long categoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
                servicioService
                        .listarPorCategoriaPaginado(
                                categoriaId,
                                page,
                                size
                        )
        );
    }

    @GetMapping("/zona/{zonaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginaSalida<ServicioSalida>> buscarPorZona(
            @PathVariable Long zonaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
                servicioService
                        .buscarPorZonaPaginado(
                                zonaId,
                                page,
                                size
                        )
        );
    }

    @PostMapping("/filtros")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicioSalida>> buscarConFiltros(
            @RequestBody ServicioFiltroDTO filtro
    ) {

        return ResponseEntity.ok(
                servicioService
                        .buscarConFiltros(filtro)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServicioSalida> obtenerPorId(
            @PathVariable Long id
    ) {

        ServicioSalida servicio =
                servicioService.obtenerPorId(id);

        return ResponseEntity.ok(servicio);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<ServicioSalida> modificar(
            @PathVariable Long id,
            @Valid @RequestBody ServicioModificar servicioModificar
    ) {

        ServicioSalida servicio =
                servicioService.modificar(
                        id,
                        servicioModificar
                );

        return ResponseEntity.ok(servicio);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<ServicioSalida> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ServicioCambiarEstado servicioCambiarEstado
    ) {

        ServicioSalida servicio =
                servicioService.cambiarEstado(
                        id,
                        servicioCambiarEstado
                );

        return ResponseEntity.ok(servicio);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        servicioService.eliminar(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}