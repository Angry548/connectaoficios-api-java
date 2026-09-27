package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.categoria.CategoriaBusquedaSalida;
import com.connectaoficios.api.dtos.categoria.CategoriaFiltroDTO;
import com.connectaoficios.api.dtos.categoria.CategoriaGuardar;
import com.connectaoficios.api.dtos.categoria.CategoriaModificar;
import com.connectaoficios.api.dtos.categoria.CategoriaSalida;
import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Categoria;
import com.connectaoficios.api.repositorios.ICategoriaRepository;
import com.connectaoficios.api.servicios.interfaces.ICategoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoriaService implements ICategoriaService {

    private static final int TAMANIO_MAXIMO_PAGINA = 100;
    private static final int LIMITE_MAXIMO_BUSQUEDA = 20;

    private final ICategoriaRepository categoriaRepository;

    public CategoriaService(
            ICategoriaRepository categoriaRepository
    ) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public CategoriaSalida guardar(
            CategoriaGuardar categoriaGuardar
    ) {

        String nombre =
                categoriaGuardar
                        .getNombre()
                        .trim();

        validarNombreUnico(
                nombre,
                null
        );

        Categoria categoria =
                new Categoria();

        categoria.setNombre(nombre);

        if (categoriaGuardar.getDescripcion() != null) {
            categoria.setDescripcion(
                    categoriaGuardar
                            .getDescripcion()
                            .trim()
            );
        }

        categoria.setActivo(true);

        Categoria categoriaGuardada =
                categoriaRepository.save(
                        categoria
                );

        return convertirASalida(
                categoriaGuardada
        );
    }

    @Override
    @Transactional
    public CategoriaSalida modificar(
            Long id,
            CategoriaModificar categoriaModificar
    ) {

        Categoria categoria =
                buscarCategoria(id);

        if (categoriaModificar.getNombre() != null) {

            String nombre =
                    categoriaModificar
                            .getNombre()
                            .trim();

            if (nombre.isBlank()) {

                throw new ReglaNegocioException(
                        "El nombre de la categoría no puede estar vacío"
                );
            }

            validarNombreUnico(
                    nombre,
                    id
            );

            categoria.setNombre(nombre);
        }

        if (categoriaModificar.getDescripcion() != null) {

            categoria.setDescripcion(
                    categoriaModificar
                            .getDescripcion()
                            .trim()
            );
        }

        Categoria categoriaActualizada =
                categoriaRepository.save(
                        categoria
                );

        return convertirASalida(
                categoriaActualizada
        );
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        Categoria categoria =
                buscarCategoria(id);

        categoria.setActivo(false);

        categoriaRepository.save(
                categoria
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaSalida obtenerPorId(
            Long id
    ) {

        return convertirASalida(
                buscarCategoria(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaSalida> listar() {

        List<Categoria> categorias =
                categoriaRepository
                        .findAllByOrderByNombreAsc();

        return convertirLista(
                categorias
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaSalida> listarActivas() {

        List<Categoria> categorias =
                categoriaRepository
                        .findAllByActivoTrueOrderByNombreAsc();

        return convertirLista(
                categorias
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<CategoriaSalida> buscarConFiltros(
            CategoriaFiltroDTO filtro,
            int pagina,
            int tamanio
    ) {

        String texto =
                normalizarTexto(
                        filtro.getTexto()
                );

        Pageable pageable =
                crearPageable(
                        pagina,
                        tamanio
                );

        Page<CategoriaSalida> resultado =
                categoriaRepository
                        .buscarConFiltros(
                                texto,
                                filtro.getActivo(),
                                pageable
                        )
                        .map(this::convertirASalida);

        return PaginaSalida.desde(
                resultado
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaBusquedaSalida> buscarParaAutocomplete(
            String texto,
            int limite
    ) {

        String textoNormalizado =
                normalizarTexto(texto);

        if (textoNormalizado == null
                || textoNormalizado.length() < 2) {

            throw new ReglaNegocioException(
                    "Debe ingresar al menos 2 caracteres para buscar"
            );
        }

        int limiteSeguro =
                Math.max(
                        1,
                        Math.min(
                                limite,
                                LIMITE_MAXIMO_BUSQUEDA
                        )
                );

        Pageable pageable =
                PageRequest.of(
                        0,
                        limiteSeguro
                );

        List<Categoria> categorias =
                categoriaRepository
                        .buscarParaAutocomplete(
                                textoNormalizado,
                                pageable
                        );

        List<CategoriaBusquedaSalida> salida =
                new ArrayList<>();

        for (Categoria categoria : categorias) {

            salida.add(
                    convertirABusquedaSalida(
                            categoria
                    )
            );
        }

        return salida;
    }

    private Pageable crearPageable(
            int pagina,
            int tamanio
    ) {

        int paginaSegura =
                Math.max(
                        pagina,
                        0
                );

        int tamanioSeguro =
                Math.max(
                        1,
                        Math.min(
                                tamanio,
                                TAMANIO_MAXIMO_PAGINA
                        )
                );

        return PageRequest.of(
                paginaSegura,
                tamanioSeguro,
                Sort.by(
                        Sort.Direction.ASC,
                        "nombre"
                )
        );
    }

    private String normalizarTexto(
            String texto
    ) {

        if (texto == null) {
            return null;
        }

        String textoLimpio =
                texto.trim();

        return textoLimpio.isBlank()
                ? null
                : textoLimpio;
    }

    private Categoria buscarCategoria(
            Long id
    ) {

        return categoriaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró la categoría"
                        )
                );
    }

    private void validarNombreUnico(
            String nombre,
            Long idExcluido
    ) {

        boolean existe;

        if (idExcluido == null) {

            existe =
                    categoriaRepository
                            .existsByNombreIgnoreCase(
                                    nombre
                            );

        } else {

            existe =
                    categoriaRepository
                            .existsByNombreIgnoreCaseAndIdNot(
                                    nombre,
                                    idExcluido
                            );
        }

        if (existe) {

            throw new ReglaNegocioException(
                    "Ya existe una categoría con ese nombre"
            );
        }
    }

    private CategoriaSalida convertirASalida(
            Categoria categoria
    ) {

        CategoriaSalida salida =
                new CategoriaSalida();

        salida.setId(
                categoria.getId()
        );

        salida.setNombre(
                categoria.getNombre()
        );

        salida.setDescripcion(
                categoria.getDescripcion()
        );

        salida.setActivo(
                categoria.getActivo()
        );

        return salida;
    }

    private List<CategoriaSalida> convertirLista(
            List<Categoria> categorias
    ) {

        List<CategoriaSalida> lista =
                new ArrayList<>();

        for (Categoria categoria : categorias) {

            lista.add(
                    convertirASalida(
                            categoria
                    )
            );
        }

        return lista;
    }

    private CategoriaBusquedaSalida convertirABusquedaSalida(
            Categoria categoria
    ) {

        return new CategoriaBusquedaSalida(
                categoria.getId(),
                categoria.getNombre()
        );
    }
}