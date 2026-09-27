package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorBusquedaSalida;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorFiltroDTO;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorGuardar;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorModificar;
import com.connectaoficios.api.dtos.perfil.PerfilTrabajadorSalida;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PerfilTrabajador;
import com.connectaoficios.api.modelos.ZonaCobertura;
import com.connectaoficios.api.repositorios.IPerfilTrabajadorRepository;
import com.connectaoficios.api.repositorios.IZonaCoberturaRepository;
import com.connectaoficios.api.servicios.interfaces.IPerfilTrabajadorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PerfilTrabajadorService
        implements IPerfilTrabajadorService {

    private static final int TAMANIO_MAXIMO_PAGINA = 100;
    private static final int LIMITE_MAXIMO_BUSQUEDA = 20;

    private final IPerfilTrabajadorRepository perfilRepository;
    private final IZonaCoberturaRepository zonaRepository;

    public PerfilTrabajadorService(
            IPerfilTrabajadorRepository perfilRepository,
            IZonaCoberturaRepository zonaRepository
    ) {
        this.perfilRepository = perfilRepository;
        this.zonaRepository = zonaRepository;
    }

    @Override
    @Transactional
    public PerfilTrabajadorSalida guardar(
            PerfilTrabajadorGuardar dto,
            Integer trabajadorId
    ) {

        if (perfilRepository.existsByTrabajadorId(trabajadorId)) {
            throw new ReglaNegocioException(
                    "El trabajador ya posee un perfil"
            );
        }

        ZonaCobertura zona = buscarZona(
                dto.getZonaPrincipalId()
        );

        PerfilTrabajador perfil = new PerfilTrabajador();

        perfil.setTrabajadorId(trabajadorId);
        perfil.setOficioPrincipal(
                dto.getOficioPrincipal()
        );
        perfil.setDescripcionProfesional(
                dto.getDescripcionProfesional()
        );
        perfil.setExperienciaLaboral(
                dto.getExperienciaLaboral()
        );
        perfil.setFotoUrl(
                dto.getFotoUrl()
        );
        perfil.setZonaPrincipal(zona);

        perfil.setPorcentajeCompletitud(
                calcularPorcentaje(perfil)
        );

        return convertirASalida(
                perfilRepository.save(perfil)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilTrabajadorSalida obtenerPorId(Long id) {

        PerfilTrabajador perfil =
                perfilRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró el perfil del trabajador"
                                )
                        );

        return convertirASalida(perfil);
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilTrabajadorSalida obtenerPorTrabajadorId(
            Integer trabajadorId
    ) {

        PerfilTrabajador perfil =
                perfilRepository
                        .findByTrabajadorId(trabajadorId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró el perfil del trabajador"
                                )
                        );

        return convertirASalida(perfil);
    }

    @Override
    @Transactional
    public PerfilTrabajadorSalida modificar(
            Integer trabajadorId,
            PerfilTrabajadorModificar dto
    ) {

        PerfilTrabajador perfil =
                perfilRepository
                        .findByTrabajadorId(trabajadorId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "No se encontró el perfil del trabajador"
                                )
                        );

        if (dto.getOficioPrincipal() != null) {
            perfil.setOficioPrincipal(
                    dto.getOficioPrincipal()
            );
        }

        if (dto.getDescripcionProfesional() != null) {
            perfil.setDescripcionProfesional(
                    dto.getDescripcionProfesional()
            );
        }

        if (dto.getExperienciaLaboral() != null) {
            perfil.setExperienciaLaboral(
                    dto.getExperienciaLaboral()
            );
        }

        if (dto.getFotoUrl() != null) {
            perfil.setFotoUrl(
                    dto.getFotoUrl()
            );
        }

        if (dto.getZonaPrincipalId() != null) {

            perfil.setZonaPrincipal(
                    buscarZona(
                            dto.getZonaPrincipalId()
                    )
            );
        }

        perfil.setPorcentajeCompletitud(
                calcularPorcentaje(perfil)
        );

        return convertirASalida(
                perfilRepository.save(perfil)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<PerfilTrabajadorSalida> listarPaginado(
            int pagina,
            int tamanio
    ) {

        Pageable pageable =
                crearPageable(
                        pagina,
                        tamanio
                );

        Page<PerfilTrabajadorSalida> resultado =
                perfilRepository
                        .findAll(pageable)
                        .map(this::convertirASalida);

        return PaginaSalida.desde(resultado);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<PerfilTrabajadorSalida> buscarConFiltros(
            PerfilTrabajadorFiltroDTO filtro,
            int pagina,
            int tamanio
    ) {

        validarPorcentajes(filtro);

        String texto =
                normalizarTexto(
                        filtro.getTexto()
                );

        String departamento =
                normalizarTexto(
                        filtro.getDepartamento()
                );

        String municipio =
                normalizarTexto(
                        filtro.getMunicipio()
                );

        Pageable pageable =
                crearPageable(
                        pagina,
                        tamanio
                );

        Page<PerfilTrabajadorSalida> resultado =
                perfilRepository
                        .buscarConFiltros(
                                texto,
                                filtro.getZonaPrincipalId(),
                                departamento,
                                municipio,
                                filtro.getPorcentajeCompletitudMinimo(),
                                filtro.getPorcentajeCompletitudMaximo(),
                                pageable
                        )
                        .map(this::convertirASalida);

        return PaginaSalida.desde(resultado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfilTrabajadorBusquedaSalida> buscarParaAutocomplete(
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

        List<PerfilTrabajador> perfiles =
                perfilRepository
                        .buscarParaAutocomplete(
                                textoNormalizado,
                                pageable
                        );

        List<PerfilTrabajadorBusquedaSalida> salida =
                new ArrayList<>();

        for (PerfilTrabajador perfil : perfiles) {

            salida.add(
                    convertirABusquedaSalida(perfil)
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
                        Sort.Direction.DESC,
                        "fechaCreacion"
                )
        );
    }

    private void validarPorcentajes(
            PerfilTrabajadorFiltroDTO filtro
    ) {

        Integer minimo =
                filtro.getPorcentajeCompletitudMinimo();

        Integer maximo =
                filtro.getPorcentajeCompletitudMaximo();

        if (minimo != null
                && (minimo < 0 || minimo > 100)) {

            throw new ReglaNegocioException(
                    "El porcentaje mínimo debe estar entre 0 y 100"
            );
        }

        if (maximo != null
                && (maximo < 0 || maximo > 100)) {

            throw new ReglaNegocioException(
                    "El porcentaje máximo debe estar entre 0 y 100"
            );
        }

        if (minimo != null
                && maximo != null
                && minimo > maximo) {

            throw new ReglaNegocioException(
                    "El porcentaje mínimo no puede ser mayor "
                            + "que el porcentaje máximo"
            );
        }
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

    private ZonaCobertura buscarZona(
            Long zonaId
    ) {

        return zonaRepository
                .findById(zonaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró la zona de cobertura"
                        )
                );
    }

    private Integer calcularPorcentaje(
            PerfilTrabajador perfil
    ) {

        int completados = 0;
        int total = 5;

        if (tieneTexto(
                perfil.getOficioPrincipal()
        )) {
            completados++;
        }

        if (tieneTexto(
                perfil.getDescripcionProfesional()
        )) {
            completados++;
        }

        if (tieneTexto(
                perfil.getExperienciaLaboral()
        )) {
            completados++;
        }

        if (tieneTexto(
                perfil.getFotoUrl()
        )) {
            completados++;
        }

        if (perfil.getZonaPrincipal() != null) {
            completados++;
        }

        return (completados * 100) / total;
    }

    private boolean tieneTexto(
            String valor
    ) {

        return valor != null
                && !valor.isBlank();
    }

    private PerfilTrabajadorSalida convertirASalida(
            PerfilTrabajador perfil
    ) {

        PerfilTrabajadorSalida salida =
                new PerfilTrabajadorSalida();

        salida.setId(
                perfil.getId()
        );

        salida.setTrabajadorId(
                perfil.getTrabajadorId()
        );

        salida.setOficioPrincipal(
                perfil.getOficioPrincipal()
        );

        salida.setDescripcionProfesional(
                perfil.getDescripcionProfesional()
        );

        salida.setExperienciaLaboral(
                perfil.getExperienciaLaboral()
        );

        salida.setFotoUrl(
                perfil.getFotoUrl()
        );

        salida.setPorcentajeCompletitud(
                perfil.getPorcentajeCompletitud()
        );

        salida.setFechaCreacion(
                perfil.getFechaCreacion()
        );

        salida.setFechaActualizacion(
                perfil.getFechaActualizacion()
        );

        if (perfil.getZonaPrincipal() != null) {

            ZonaCobertura zona =
                    perfil.getZonaPrincipal();

            salida.setZonaPrincipalId(
                    zona.getId()
            );

            salida.setDepartamento(
                    zona.getDepartamento()
            );

            salida.setMunicipio(
                    zona.getMunicipio()
            );

            salida.setLocalidad(
                    zona.getLocalidad()
            );
        }

        return salida;
    }

    private PerfilTrabajadorBusquedaSalida convertirABusquedaSalida(
            PerfilTrabajador perfil
    ) {

        Long zonaPrincipalId = null;
        String departamento = null;
        String municipio = null;
        String localidad = null;

        if (perfil.getZonaPrincipal() != null) {

            ZonaCobertura zona =
                    perfil.getZonaPrincipal();

            zonaPrincipalId =
                    zona.getId();

            departamento =
                    zona.getDepartamento();

            municipio =
                    zona.getMunicipio();

            localidad =
                    zona.getLocalidad();
        }

        return new PerfilTrabajadorBusquedaSalida(
                perfil.getId(),
                perfil.getTrabajadorId(),
                perfil.getOficioPrincipal(),
                zonaPrincipalId,
                departamento,
                municipio,
                localidad
        );
    }
}