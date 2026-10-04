package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.categoria.CategoriaBusquedaSalida;
import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.servicio.ServicioAdminFiltroDTO;
import com.connectaoficios.api.dtos.servicio.ServicioAdminSalida;
import com.connectaoficios.api.dtos.servicio.ServicioCambiarEstado;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.modelos.Categoria;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.modelos.ZonaCobertura;
import com.connectaoficios.api.repositorios.ICategoriaRepository;
import com.connectaoficios.api.repositorios.IServicioRepository;
import com.connectaoficios.api.servicios.interfaces.IServicioAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ServicioAdminService
        implements IServicioAdminService {

    private static final int TAMANIO_MAXIMO_PAGINA = 100;

    private final IServicioRepository servicioRepository;

    private final ICategoriaRepository categoriaRepository;

    public ServicioAdminService(
            IServicioRepository servicioRepository,
            ICategoriaRepository categoriaRepository
    ) {
        this.servicioRepository =
                servicioRepository;

        this.categoriaRepository =
                categoriaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<ServicioAdminSalida> buscar(
            ServicioAdminFiltroDTO filtro,
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

        Page<ServicioAdminSalida> resultado =
                servicioRepository
                        .buscarParaAdministracion(
                                texto,
                                filtro.getTrabajadorId(),
                                filtro.getCategoriaId(),
                                filtro.getEstado(),
                                pageable
                        )
                        .map(this::convertirASalida);

        return PaginaSalida.desde(
                resultado
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioAdminSalida obtenerPorId(
            Long id
    ) {
        Servicio servicio =
                buscarServicio(id);

        return convertirASalida(
                servicio
        );
    }

    @Override
    @Transactional
    public ServicioAdminSalida cambiarEstado(
            Long id,
            ServicioCambiarEstado request
    ) {
        Servicio servicio =
                buscarServicio(id);

        servicio.setEstado(
                request.getEstado()
        );

        Servicio actualizado =
                servicioRepository.save(
                        servicio
                );

        return convertirASalida(
                actualizado
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaBusquedaSalida> listarCategorias() {
        List<Categoria> categorias =
                categoriaRepository
                        .findAllByOrderByNombreAsc();

        List<CategoriaBusquedaSalida> salida =
                new ArrayList<>();

        for (Categoria categoria : categorias) {
            salida.add(
                    new CategoriaBusquedaSalida(
                            categoria.getId(),
                            categoria.getNombre()
                    )
            );
        }

        return salida;
    }

    private Servicio buscarServicio(
            Long id
    ) {
        return servicioRepository
                .findByIdAndEliminadoFalse(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró el servicio"
                        )
                );
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
                        Sort.Direction.DESC,
                        "fechaCreacion"
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

    private ServicioAdminSalida convertirASalida(
            Servicio servicio
    ) {
        ServicioAdminSalida salida =
                new ServicioAdminSalida();

        salida.setId(
                servicio.getId()
        );

        salida.setPerfilTrabajadorId(
                servicio
                        .getPerfilTrabajador()
                        .getId()
        );

        salida.setTrabajadorId(
                servicio
                        .getPerfilTrabajador()
                        .getTrabajadorId()
        );

        salida.setCategoriaId(
                servicio
                        .getCategoria()
                        .getId()
        );

        salida.setCategoriaNombre(
                servicio
                        .getCategoria()
                        .getNombre()
        );

        salida.setTitulo(
                servicio.getTitulo()
        );

        salida.setDescripcion(
                servicio.getDescripcion()
        );

        salida.setTarifaMinima(
                servicio.getTarifaMinima()
        );

        salida.setTarifaMaxima(
                servicio.getTarifaMaxima()
        );

        salida.setEstado(
                servicio.getEstado()
        );

        salida.setFechaCreacion(
                servicio.getFechaCreacion()
        );

        salida.setFechaActualizacion(
                servicio.getFechaActualizacion()
        );

        Set<Long> zonas =
                new LinkedHashSet<>();

        for (ZonaCobertura zona :
                servicio.getZonasCobertura()) {
            zonas.add(
                    zona.getId()
            );
        }

        salida.setZonasCoberturaIds(
                zonas
        );

        return salida;
    }
}