package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.categoria.CategoriaGuardar;
import com.connectaoficios.api.dtos.categoria.CategoriaModificar;
import com.connectaoficios.api.dtos.categoria.CategoriaSalida;
import com.connectaoficios.api.servicios.interfaces.ICategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final ICategoriaService categoriaService;

    public CategoriaController(ICategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<CategoriaSalida> guardar(
            @Valid @RequestBody CategoriaGuardar categoriaGuardar
    ) {

        CategoriaSalida categoria =
                categoriaService.guardar(categoriaGuardar);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoria);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategoriaSalida>> listarActivas() {

        List<CategoriaSalida> categorias =
                categoriaService.listarActivas();

        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/todas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<List<CategoriaSalida>> listarTodas() {

        List<CategoriaSalida> categorias =
                categoriaService.listar();

        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CategoriaSalida> obtenerPorId(
            @PathVariable Long id
    ) {

        CategoriaSalida categoria =
                categoriaService.obtenerPorId(id);

        return ResponseEntity.ok(categoria);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<CategoriaSalida> modificar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaModificar categoriaModificar
    ) {

        CategoriaSalida categoria =
                categoriaService.modificar(id, categoriaModificar);

        return ResponseEntity.ok(categoria);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADOR PRINCIPAL')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        categoriaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}