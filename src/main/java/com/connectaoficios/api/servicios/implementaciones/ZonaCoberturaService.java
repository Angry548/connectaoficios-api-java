package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaBusquedaSalida;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaFiltroDTO;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaGuardar;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaModificar;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaSalida;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.ZonaCobertura;
import com.connectaoficios.api.repositorios.IZonaCoberturaRepository;
import com.connectaoficios.api.servicios.interfaces.IZonaCoberturaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ZonaCoberturaService implements IZonaCoberturaService {

    private static final int TAMANIO_MAXIMO_PAGINA = 100;
    private static final int LIMITE_MAXIMO_BUSQUEDA = 20;

    private final IZonaCoberturaRepository zonaRepository;

    public ZonaCoberturaService(
            IZonaCoberturaRepository zonaRepository
    ) {
        this.zonaRepository = zonaRepository;
    }

    @Override
    @Transactional
    public ZonaCoberturaSalida guardar(
            ZonaCoberturaGuardar zonaGuardar
    ) {

        String departamento =
                zonaGuardar.getDepartamento().trim();

        String municipio =
                zonaGuardar.getMunicipio().trim();

        validarTextoObligatorio(
                departamento,
                "El departamento no puede estar vacío"
        );

        validarTextoObligatorio(
                municipio,
                "El municipio no puede estar vacío"
        );

        validarDuplicado(
                departamento,
                municipio,
                null
        );

        ZonaCobertura zona =
                new ZonaCobertura();

        zona.setDepartamento(
                departamento
        );

        zona.setMunicipio(
                municipio
        );

        zona.setLocalidad(
                normalizarTexto(
                        zonaGuardar.getLocalidad()
                )
        );

        zona.setActivo(true);

        ZonaCobertura zonaGuardada =
                zonaRepository.save(
                        zona
                );

        return convertirASalida(
                zonaGuardada
        );
    }

    @Override
    @Transactional
    public ZonaCoberturaSalida modificar(
            Long id,
            ZonaCoberturaModificar zonaModificar
    ) {

        ZonaCobertura zona =
                buscarZona(id);

        String departamento =
                zona.getDepartamento();

        String municipio =
                zona.getMunicipio();

        if (zonaModificar.getDepartamento() != null) {

            departamento =
                    zonaModificar
                            .getDepartamento()
                            .trim();

            validarTextoObligatorio(
                    departamento,
                    "El departamento no puede estar vacío"
            );
        }

        if (zonaModificar.getMunicipio() != null) {

            municipio =
                    zonaModificar
                            .getMunicipio()
                            .trim();

            validarTextoObligatorio(
                    municipio,
                    "El municipio no puede estar vacío"
            );
        }

        validarDuplicado(
                departamento,
                municipio,
                id
        );

        if (zonaModificar.getDepartamento() != null) {
            zona.setDepartamento(
                    departamento
            );
        }

        if (zonaModificar.getMunicipio() != null) {
            zona.setMunicipio(
                    municipio
            );
        }

        if (zonaModificar.getLocalidad() != null) {
            zona.setLocalidad(
                    normalizarTexto(
                            zonaModificar.getLocalidad()
                    )
            );
        }

        ZonaCobertura zonaActualizada =
                zonaRepository.save(
                        zona
                );

        return convertirASalida(
                zonaActualizada
        );
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        ZonaCobertura zona =
                buscarZona(id);

        zona.setActivo(false);

        zonaRepository.save(
                zona
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ZonaCoberturaSalida obtenerPorId(
            Long id
    ) {

        return convertirASalida(
                buscarZona(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZonaCoberturaSalida> listar() {

        return convertirLista(
                zonaRepository
                        .findAllByOrderByDepartamentoAscMunicipioAsc()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZonaCoberturaSalida> listarActivas() {

        return convertirLista(
                zonaRepository
                        .findAllByActivoTrueOrderByDepartamentoAscMunicipioAsc()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZonaCoberturaSalida> buscarPorDepartamento(
            String departamento
    ) {

        String departamentoNormalizado =
                normalizarTexto(
                        departamento
                );

        if (departamentoNormalizado == null) {
            throw new ReglaNegocioException(
                    "El departamento es obligatorio"
            );
        }

        return convertirLista(
                zonaRepository
                        .findAllByDepartamentoIgnoreCaseOrderByMunicipioAsc(
                                departamentoNormalizado
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZonaCoberturaSalida> buscarPorMunicipio(
            String municipio
    ) {

        String municipioNormalizado =
                normalizarTexto(
                        municipio
                );

        if (municipioNormalizado == null) {
            throw new ReglaNegocioException(
                    "El municipio es obligatorio"
            );
        }

        return convertirLista(
                zonaRepository
                        .findAllByMunicipioIgnoreCaseOrderByDepartamentoAsc(
                                municipioNormalizado
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaSalida<ZonaCoberturaSalida> buscarConFiltros(
            ZonaCoberturaFiltroDTO filtro,
            int pagina,
            int tamanio
    ) {

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

        String localidad =
                normalizarTexto(
                        filtro.getLocalidad()
                );

        Pageable pageable =
                crearPageable(
                        pagina,
                        tamanio
                );

        Page<ZonaCoberturaSalida> resultado =
                zonaRepository
                        .buscarConFiltros(
                                texto,
                                departamento,
                                municipio,
                                localidad,
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
    public List<ZonaCoberturaBusquedaSalida> buscarParaAutocomplete(
            String texto,
            int limite
    ) {

        String textoNormalizado =
                normalizarTexto(
                        texto
                );

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

        List<ZonaCobertura> zonas =
                zonaRepository
                        .buscarParaAutocomplete(
                                textoNormalizado,
                                pageable
                        );

        List<ZonaCoberturaBusquedaSalida> salida =
                new ArrayList<>();

        for (ZonaCobertura zona : zonas) {

            salida.add(
                    convertirABusquedaSalida(
                            zona
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
                        Sort.Order.asc(
                                "departamento"
                        ),
                        Sort.Order.asc(
                                "municipio"
                        ),
                        Sort.Order.asc(
                                "localidad"
                        )
                )
        );
    }

    private ZonaCobertura buscarZona(
            Long id
    ) {

        return zonaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró la zona de cobertura"
                        )
                );
    }

    private void validarDuplicado(
            String departamento,
            String municipio,
            Long idExcluido
    ) {

        boolean existe;

        if (idExcluido == null) {

            existe =
                    zonaRepository
                            .existsByDepartamentoIgnoreCaseAndMunicipioIgnoreCase(
                                    departamento,
                                    municipio
                            );

        } else {

            existe =
                    zonaRepository
                            .existsByDepartamentoIgnoreCaseAndMunicipioIgnoreCaseAndIdNot(
                                    departamento,
                                    municipio,
                                    idExcluido
                            );
        }

        if (existe) {

            throw new ReglaNegocioException(
                    "Ya existe una zona de cobertura para ese "
                            + "departamento y municipio"
            );
        }
    }

    private void validarTextoObligatorio(
            String valor,
            String mensaje
    ) {

        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(
                    mensaje
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

    private ZonaCoberturaSalida convertirASalida(
            ZonaCobertura zona
    ) {

        ZonaCoberturaSalida salida =
                new ZonaCoberturaSalida();

        salida.setId(
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

        salida.setActivo(
                zona.getActivo()
        );

        return salida;
    }

    private List<ZonaCoberturaSalida> convertirLista(
            List<ZonaCobertura> zonas
    ) {

        List<ZonaCoberturaSalida> lista =
                new ArrayList<>();

        for (ZonaCobertura zona : zonas) {

            lista.add(
                    convertirASalida(
                            zona
                    )
            );
        }

        return lista;
    }

    private ZonaCoberturaBusquedaSalida convertirABusquedaSalida(
            ZonaCobertura zona
    ) {

        return new ZonaCoberturaBusquedaSalida(
                zona.getId(),
                zona.getDepartamento(),
                zona.getMunicipio(),
                zona.getLocalidad()
        );
    }
}