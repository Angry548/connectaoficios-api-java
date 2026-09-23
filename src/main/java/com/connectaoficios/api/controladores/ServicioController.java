package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.servicio.ServicioCambiarEstado;
import com.connectaoficios.api.dtos.servicio.ServicioFiltroDTO;
import com.connectaoficios.api.dtos.servicio.ServicioGuardar;
import com.connectaoficios.api.dtos.servicio.ServicioModificar;
import com.connectaoficios.api.dtos.servicio.ServicioSalida;
import com.connectaoficios.api.servicios.interfaces.IServicioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final IServicioService servicioService;

    public ServicioController(IServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<ServicioSalida> publicar(
            @Valid @RequestBody ServicioGuardar servicioGuardar
    ) {

        ServicioSalida servicio =
                servicioService.publicar(servicioGuardar);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicio);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicioSalida>> listarActivos() {

        List<ServicioSalida> servicios =
                servicioService.listarActivos();

        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/trabajador/{perfilTrabajadorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicioSalida>> listarPorTrabajador(
            @PathVariable Long perfilTrabajadorId
    ) {

        List<ServicioSalida> servicios =
                servicioService
                        .listarPorTrabajador(perfilTrabajadorId);

        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/categoria/{categoriaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicioSalida>> listarPorCategoria(
            @PathVariable Long categoriaId
    ) {

        List<ServicioSalida> servicios =
                servicioService
                        .listarPorCategoria(categoriaId);

        return ResponseEntity.ok(servicios);
    }

    @GetMapping("/zona/{zonaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicioSalida>> buscarPorZona(
            @PathVariable Long zonaId
    ) {

        List<ServicioSalida> servicios =
                servicioService.buscarPorZona(zonaId);

        return ResponseEntity.ok(servicios);
    }

    @PostMapping("/filtros")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicioSalida>> buscarConFiltros(
            @RequestBody ServicioFiltroDTO filtro
    ) {

        List<ServicioSalida> servicios =
                servicioService.buscarConFiltros(filtro);

        return ResponseEntity.ok(servicios);
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
                servicioService.modificar(id, servicioModificar);

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

        return ResponseEntity.noContent().build();
    }
}