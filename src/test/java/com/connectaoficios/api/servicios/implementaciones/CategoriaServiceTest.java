package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.categoria.CategoriaGuardar;
import com.connectaoficios.api.dtos.categoria.CategoriaModificar;
import com.connectaoficios.api.dtos.categoria.CategoriaSalida;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Categoria;
import com.connectaoficios.api.repositorios.ICategoriaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private ICategoriaRepository categoriaRepository;

    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService(categoriaRepository);
    }

    @Test
    void guardar_debeCrearCategoriaActiva() {

        CategoriaGuardar dto = new CategoriaGuardar();
        dto.setNombre("Plomería");
        dto.setDescripcion("Trabajos de plomería");

        when(
                categoriaRepository
                        .existsByNombreIgnoreCase("Plomería")
        ).thenReturn(false);

        when(
                categoriaRepository.save(
                        any(Categoria.class)
                )
        ).thenAnswer(invocation -> {

            Categoria guardada = invocation.getArgument(0);
            guardada.setId(1L);

            return guardada;
        });

        CategoriaSalida resultado =
                categoriaService.guardar(dto);

        assertNotNull(resultado);
        assertEquals("Plomería", resultado.getNombre());
        assertEquals("Trabajos de plomería",
                resultado.getDescripcion());
        assertEquals(true, resultado.getActivo());

        verify(
                categoriaRepository,
                times(1)
        ).save(any(Categoria.class));
    }

    @Test
    void guardar_debeLanzarExcepcionCuandoNombreYaExiste() {

        CategoriaGuardar dto = new CategoriaGuardar();
        dto.setNombre("Plomería");

        when(
                categoriaRepository
                        .existsByNombreIgnoreCase("Plomería")
        ).thenReturn(true);

        assertThrows(
                ReglaNegocioException.class,
                () -> categoriaService.guardar(dto)
        );

        verify(
                categoriaRepository,
                never()
        ).save(any(Categoria.class));
    }

    @Test
    void modificar_debeActualizarCategoria() {

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Plomería");
        categoria.setActivo(true);

        CategoriaModificar dto = new CategoriaModificar();
        dto.setNombre("Plomería Residencial");
        dto.setDescripcion("Nueva descripción");

        when(
                categoriaRepository.findById(1L)
        ).thenReturn(Optional.of(categoria));

        when(
                categoriaRepository
                        .existsByNombreIgnoreCaseAndIdNot(
                                "Plomería Residencial",
                                1L
                        )
        ).thenReturn(false);

        when(
                categoriaRepository.save(categoria)
        ).thenReturn(categoria);

        CategoriaSalida resultado =
                categoriaService.modificar(1L, dto);

        assertEquals(
                "Plomería Residencial",
                resultado.getNombre()
        );
        assertEquals(
                "Nueva descripción",
                resultado.getDescripcion()
        );
    }

    @Test
    void modificar_debeLanzarExcepcionCuandoNoExiste() {

        CategoriaModificar dto = new CategoriaModificar();
        dto.setNombre("Plomería");

        when(
                categoriaRepository.findById(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> categoriaService.modificar(99L, dto)
        );
    }

    @Test
    void eliminar_debeDesactivarCategoria() {

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setActivo(true);

        when(
                categoriaRepository.findById(1L)
        ).thenReturn(Optional.of(categoria));

        when(
                categoriaRepository.save(categoria)
        ).thenReturn(categoria);

        categoriaService.eliminar(1L);

        assertEquals(false, categoria.getActivo());

        verify(
                categoriaRepository,
                times(1)
        ).save(categoria);
    }

    @Test
    void listarActivas_debeRetornarSoloActivas() {

        Categoria activa = new Categoria();
        activa.setId(1L);
        activa.setNombre("Plomería");
        activa.setActivo(true);

        when(
                categoriaRepository
                        .findAllByActivoTrueOrderByNombreAsc()
        ).thenReturn(List.of(activa));

        List<CategoriaSalida> resultado =
                categoriaService.listarActivas();

        assertEquals(1, resultado.size());
        assertEquals("Plomería", resultado.get(0).getNombre());
    }
}