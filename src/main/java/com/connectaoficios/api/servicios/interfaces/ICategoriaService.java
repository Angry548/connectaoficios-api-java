package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.categoria.CategoriaBusquedaSalida;
import com.connectaoficios.api.dtos.categoria.CategoriaFiltroDTO;
import com.connectaoficios.api.dtos.categoria.CategoriaGuardar;
import com.connectaoficios.api.dtos.categoria.CategoriaModificar;
import com.connectaoficios.api.dtos.categoria.CategoriaSalida;
import com.connectaoficios.api.dtos.comun.PaginaSalida;

import java.util.List;

public interface ICategoriaService {

    CategoriaSalida guardar(
            CategoriaGuardar categoriaGuardar
    );

    CategoriaSalida modificar(
            Long id,
            CategoriaModificar categoriaModificar
    );

    void eliminar(Long id);

    CategoriaSalida obtenerPorId(Long id);

    List<CategoriaSalida> listar();

    List<CategoriaSalida> listarActivas();

    PaginaSalida<CategoriaSalida> buscarConFiltros(
            CategoriaFiltroDTO filtro,
            int pagina,
            int tamanio
    );

    List<CategoriaBusquedaSalida> buscarParaAutocomplete(
            String texto,
            int limite
    );
}