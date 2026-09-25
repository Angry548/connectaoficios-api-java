package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.servicio.ServicioCambiarEstado;
import com.connectaoficios.api.dtos.servicio.ServicioFiltroDTO;
import com.connectaoficios.api.dtos.servicio.ServicioGuardar;
import com.connectaoficios.api.dtos.servicio.ServicioModificar;
import com.connectaoficios.api.dtos.servicio.ServicioSalida;
import com.connectaoficios.api.enums.EstadoServicio;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.Categoria;
import com.connectaoficios.api.modelos.PerfilTrabajador;
import com.connectaoficios.api.modelos.Servicio;
import com.connectaoficios.api.modelos.ZonaCobertura;
import com.connectaoficios.api.repositorios.ICategoriaRepository;
import com.connectaoficios.api.repositorios.IPerfilTrabajadorRepository;
import com.connectaoficios.api.repositorios.IServicioRepository;
import com.connectaoficios.api.repositorios.IZonaCoberturaRepository;
import com.connectaoficios.api.servicios.interfaces.IServicioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ServicioService implements IServicioService {

    private final IServicioRepository servicioRepository;
    private final IPerfilTrabajadorRepository perfilTrabajadorRepository;
    private final ICategoriaRepository categoriaRepository;
    private final IZonaCoberturaRepository zonaCoberturaRepository;

    public ServicioService(
            IServicioRepository servicioRepository,
            IPerfilTrabajadorRepository perfilTrabajadorRepository,
            ICategoriaRepository categoriaRepository,
            IZonaCoberturaRepository zonaCoberturaRepository
    ) {
        this.servicioRepository = servicioRepository;
        this.perfilTrabajadorRepository = perfilTrabajadorRepository;
        this.categoriaRepository = categoriaRepository;
        this.zonaCoberturaRepository = zonaCoberturaRepository;
    }

    @Override
    @Transactional
    public ServicioSalida publicar(ServicioGuardar servicioGuardar) {

        PerfilTrabajador perfilTrabajador =
                buscarPerfilTrabajador(
                        servicioGuardar.getPerfilTrabajadorId()
                );

        Categoria categoria =
                buscarCategoriaActiva(
                        servicioGuardar.getCategoriaId()
                );

        validarTarifas(
                servicioGuardar.getTarifaMinima(),
                servicioGuardar.getTarifaMaxima()
        );

        Set<ZonaCobertura> zonasCobertura =
                buscarZonas(
                        servicioGuardar.getZonasCoberturaIds()
                );

        Servicio servicio = new Servicio();

        servicio.setPerfilTrabajador(perfilTrabajador);
        servicio.setCategoria(categoria);
        servicio.setTitulo(servicioGuardar.getTitulo().trim());
        servicio.setDescripcion(servicioGuardar.getDescripcion().trim());
        servicio.setTarifaMinima(servicioGuardar.getTarifaMinima());
        servicio.setTarifaMaxima(servicioGuardar.getTarifaMaxima());
        servicio.setEstado(EstadoServicio.ACTIVO);
        servicio.setEliminado(false);
        servicio.setZonasCobertura(zonasCobertura);

        Servicio servicioPublicado =
                servicioRepository.save(servicio);

        return convertirASalida(servicioPublicado);
    }

    @Override
    @Transactional
    public ServicioSalida modificar(
            Long id,
            ServicioModificar servicioModificar
    ) {

        Servicio servicio = buscarServicio(id);

        if (servicioModificar.getCategoriaId() != null) {
            Categoria categoria =
                    buscarCategoriaActiva(
                            servicioModificar.getCategoriaId()
                    );

            servicio.setCategoria(categoria);
        }

        if (servicioModificar.getTitulo() != null) {

            String titulo = servicioModificar.getTitulo().trim();

            if (titulo.isBlank()) {
                throw new ReglaNegocioException(
                        "El título del servicio no puede estar vacío"
                );
            }

            servicio.setTitulo(titulo);
        }

        if (servicioModificar.getDescripcion() != null) {

            String descripcion =
                    servicioModificar.getDescripcion().trim();

            if (descripcion.isBlank()) {
                throw new ReglaNegocioException(
                        "La descripción del servicio no puede estar vacía"
                );
            }

            servicio.setDescripcion(descripcion);
        }

        BigDecimal tarifaMinima =
                servicioModificar.getTarifaMinima() != null
                        ? servicioModificar.getTarifaMinima()
                        : servicio.getTarifaMinima();

        BigDecimal tarifaMaxima =
                servicioModificar.getTarifaMaxima() != null
                        ? servicioModificar.getTarifaMaxima()
                        : servicio.getTarifaMaxima();

        validarTarifas(tarifaMinima, tarifaMaxima);

        if (servicioModificar.getTarifaMinima() != null) {
            servicio.setTarifaMinima(
                    servicioModificar.getTarifaMinima()
            );
        }

        if (servicioModificar.getTarifaMaxima() != null) {
            servicio.setTarifaMaxima(
                    servicioModificar.getTarifaMaxima()
            );
        }

        if (servicioModificar.getZonasCoberturaIds() != null) {

            Set<ZonaCobertura> zonasCobertura =
                    buscarZonas(
                            servicioModificar.getZonasCoberturaIds()
                    );

            servicio.setZonasCobertura(zonasCobertura);
        }

        Servicio servicioActualizado =
                servicioRepository.save(servicio);

        return convertirASalida(servicioActualizado);
    }

    @Override
    @Transactional
    public ServicioSalida cambiarEstado(
            Long id,
            ServicioCambiarEstado servicioCambiarEstado
    ) {

        Servicio servicio = buscarServicio(id);

        servicio.setEstado(
                servicioCambiarEstado.getEstado()
        );

        Servicio servicioActualizado =
                servicioRepository.save(servicio);

        return convertirASalida(servicioActualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        Servicio servicio = buscarServicio(id);

        servicio.setEliminado(true);

        servicioRepository.save(servicio);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioSalida obtenerPorId(Long id) {

        return convertirASalida(buscarServicio(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioSalida> listarActivos() {

        return convertirLista(
                servicioRepository
                        .findAllByEliminadoFalseAndEstadoOrderByFechaCreacionDesc(
                                EstadoServicio.ACTIVO
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioSalida> listarPorCategoria(Long categoriaId) {

        return convertirLista(
                servicioRepository
                        .findAllByCategoriaIdAndEliminadoFalseOrderByFechaCreacionDesc(
                                categoriaId
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioSalida> listarPorTrabajador(
            Long perfilTrabajadorId
    ) {

        return convertirLista(
                servicioRepository
                        .findAllByPerfilTrabajadorIdAndEliminadoFalseOrderByFechaCreacionDesc(
                                perfilTrabajadorId
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioSalida> buscarPorZona(Long zonaId) {

        return convertirLista(
                servicioRepository
                        .findAllByZonaCoberturaId(zonaId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioSalida> buscarConFiltros(
            ServicioFiltroDTO filtro
    ) {

        return convertirLista(
                servicioRepository.buscarConFiltros(
                        filtro.getCategoriaId(),
                        filtro.getZonaId(),
                        filtro.getDiaSemana(),
                        filtro.getTarifaMinima(),
                        filtro.getTarifaMaxima()
                )
        );
    }

    private Servicio buscarServicio(Long id) {

        return servicioRepository
                .findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el servicio"
                ));
    }

    private PerfilTrabajador buscarPerfilTrabajador(Long id) {

        return perfilTrabajadorRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el perfil del trabajador"
                ));
    }

    private Categoria buscarCategoriaActiva(Long id) {

        Categoria categoria = categoriaRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la categoría"
                ));

        if (!Boolean.TRUE.equals(categoria.getActivo())) {
            throw new ReglaNegocioException(
                    "La categoría no está activa"
            );
        }

        return categoria;
    }

    private void validarTarifas(
            BigDecimal tarifaMinima,
            BigDecimal tarifaMaxima
    ) {

        if (tarifaMaxima != null
                && tarifaMinima.compareTo(tarifaMaxima) > 0) {

            throw new ReglaNegocioException(
                    "La tarifa mínima no puede ser mayor "
                            + "que la tarifa máxima"
            );
        }
    }

    private Set<ZonaCobertura> buscarZonas(
            Set<Long> zonasCoberturaIds
    ) {

        if (zonasCoberturaIds == null
                || zonasCoberturaIds.isEmpty()) {

            return new HashSet<>();
        }

        List<ZonaCobertura> zonasEncontradas =
                zonaCoberturaRepository
                        .findAllById(zonasCoberturaIds);

        if (zonasEncontradas.size() != zonasCoberturaIds.size()) {

            throw new RecursoNoEncontradoException(
                    "No se encontraron todas las zonas de cobertura "
                            + "seleccionadas"
            );
        }

        return new HashSet<>(zonasEncontradas);
    }

    private ServicioSalida convertirASalida(Servicio servicio) {

        ServicioSalida salida = new ServicioSalida();

        salida.setId(servicio.getId());
        salida.setPerfilTrabajadorId(
                servicio.getPerfilTrabajador().getId()
        );
        salida.setCategoriaId(
                servicio.getCategoria().getId()
        );
        salida.setTitulo(servicio.getTitulo());
        salida.setDescripcion(servicio.getDescripcion());
        salida.setTarifaMinima(servicio.getTarifaMinima());
        salida.setTarifaMaxima(servicio.getTarifaMaxima());
        salida.setEstado(servicio.getEstado());
        salida.setFechaCreacion(servicio.getFechaCreacion());
        salida.setFechaActualizacion(servicio.getFechaActualizacion());

        Set<Long> zonasIds = new java.util.LinkedHashSet<>();

        for (ZonaCobertura zona :
                servicio.getZonasCobertura()) {

            zonasIds.add(zona.getId());
        }

        salida.setZonasCoberturaIds(zonasIds);

        return salida;
    }

    private List<ServicioSalida> convertirLista(
            List<Servicio> servicios
    ) {

        List<ServicioSalida> lista = new ArrayList<>();

        for (Servicio servicio : servicios) {
            lista.add(convertirASalida(servicio));
        }

        return lista;
    }
}