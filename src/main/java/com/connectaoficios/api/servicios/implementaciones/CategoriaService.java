package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.categoria.CategoriaGuardar;
import com.connectaoficios.api.dtos.categoria.CategoriaModificar;
import com.connectaoficios.api.dtos.categoria.CategoriaSalida;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Categoria;
import com.connectaoficios.api.repositorios.ICategoriaRepository;
import com.connectaoficios.api.servicios.interfaces.ICategoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoriaService implements ICategoriaService {

    private final ICategoriaRepository categoriaRepository;

    public CategoriaService(ICategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public CategoriaSalida guardar(CategoriaGuardar categoriaGuardar) {

        String nombre = categoriaGuardar.getNombre().trim();

        validarNombreUnico(nombre, null);

        Categoria categoria = new Categoria();

        categoria.setNombre(nombre);
        categoria.setDescripcion(categoriaGuardar.getDescripcion());
        categoria.setActivo(true);

        Categoria categoriaGuardada = categoriaRepository.save(categoria);

        return convertirASalida(categoriaGuardada);
    }

    @Override
    @Transactional
    public CategoriaSalida modificar(
            Long id,
            CategoriaModificar categoriaModificar
    ) {

        Categoria categoria = buscarCategoria(id);

        if (categoriaModificar.getNombre() != null) {

            String nombre = categoriaModificar.getNombre().trim();

            if (nombre.isBlank()) {
                throw new ReglaNegocioException(
                        "El nombre de la categoría no puede estar vacío"
                );
            }

            validarNombreUnico(nombre, id);
            categoria.setNombre(nombre);
        }

        if (categoriaModificar.getDescripcion() != null) {
            categoria.setDescripcion(
                    categoriaModificar.getDescripcion().trim()
            );
        }

        Categoria categoriaActualizada =
                categoriaRepository.save(categoria);

        return convertirASalida(categoriaActualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        Categoria categoria = buscarCategoria(id);

        categoria.setActivo(false);

        categoriaRepository.save(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaSalida obtenerPorId(Long id) {

        return convertirASalida(buscarCategoria(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaSalida> listar() {

        List<Categoria> categorias =
                categoriaRepository.findAllByOrderByNombreAsc();

        return convertirLista(categorias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaSalida> listarActivas() {

        List<Categoria> categorias =
                categoriaRepository.findAllByActivoTrueOrderByNombreAsc();

        return convertirLista(categorias);
    }

    private Categoria buscarCategoria(Long id) {

        return categoriaRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la categoría"
                ));
    }

    private void validarNombreUnico(String nombre, Long idExcluido) {

        boolean existe;

        if (idExcluido == null) {
            existe = categoriaRepository
                    .existsByNombreIgnoreCase(nombre);
        } else {
            existe = categoriaRepository
                    .existsByNombreIgnoreCaseAndIdNot(nombre, idExcluido);
        }

        if (existe) {
            throw new ReglaNegocioException(
                    "Ya existe una categoría con ese nombre"
            );
        }
    }

    private CategoriaSalida convertirASalida(Categoria categoria) {

        CategoriaSalida salida = new CategoriaSalida();

        salida.setId(categoria.getId());
        salida.setNombre(categoria.getNombre());
        salida.setDescripcion(categoria.getDescripcion());
        salida.setActivo(categoria.getActivo());

        return salida;
    }

    private List<CategoriaSalida> convertirLista(
            List<Categoria> categorias
    ) {

        List<CategoriaSalida> lista = new ArrayList<>();

        for (Categoria categoria : categorias) {
            lista.add(convertirASalida(categoria));
        }

        return lista;
    }
}