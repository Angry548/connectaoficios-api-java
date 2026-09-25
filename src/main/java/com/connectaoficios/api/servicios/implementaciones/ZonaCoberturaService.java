package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.zona.ZonaCoberturaGuardar;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaModificar;
import com.connectaoficios.api.dtos.zona.ZonaCoberturaSalida;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.ZonaCobertura;
import com.connectaoficios.api.repositorios.IZonaCoberturaRepository;
import com.connectaoficios.api.servicios.interfaces.IZonaCoberturaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ZonaCoberturaService implements IZonaCoberturaService {

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

        String departamento = zonaGuardar.getDepartamento().trim();
        String municipio = zonaGuardar.getMunicipio().trim();

        validarDuplicado(departamento, municipio, null);

        ZonaCobertura zona = new ZonaCobertura();

        zona.setDepartamento(departamento);
        zona.setMunicipio(municipio);
        zona.setLocalidad(zonaGuardar.getLocalidad());
        zona.setActivo(true);

        ZonaCobertura zonaGuardada = zonaRepository.save(zona);

        return convertirASalida(zonaGuardada);
    }

    @Override
    @Transactional
    public ZonaCoberturaSalida modificar(
            Long id,
            ZonaCoberturaModificar zonaModificar
    ) {

        ZonaCobertura zona = buscarZona(id);

        String departamento = zonaModificar.getDepartamento() != null
                ? zonaModificar.getDepartamento().trim()
                : zona.getDepartamento();

        String municipio = zonaModificar.getMunicipio() != null
                ? zonaModificar.getMunicipio().trim()
                : zona.getMunicipio();

        validarDuplicado(departamento, municipio, id);

        if (zonaModificar.getDepartamento() != null) {
            zona.setDepartamento(departamento);
        }

        if (zonaModificar.getMunicipio() != null) {
            zona.setMunicipio(municipio);
        }

        if (zonaModificar.getLocalidad() != null) {
            zona.setLocalidad(zonaModificar.getLocalidad().trim());
        }

        ZonaCobertura zonaActualizada = zonaRepository.save(zona);

        return convertirASalida(zonaActualizada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        ZonaCobertura zona = buscarZona(id);

        zona.setActivo(false);

        zonaRepository.save(zona);
    }

    @Override
    @Transactional(readOnly = true)
    public ZonaCoberturaSalida obtenerPorId(Long id) {

        return convertirASalida(buscarZona(id));
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

        return convertirLista(
                zonaRepository.findAllByDepartamentoIgnoreCaseOrderByMunicipioAsc(
                        departamento.trim()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZonaCoberturaSalida> buscarPorMunicipio(
            String municipio
    ) {

        return convertirLista(
                zonaRepository.findAllByMunicipioIgnoreCaseOrderByDepartamentoAsc(
                        municipio.trim()
                )
        );
    }

    private ZonaCobertura buscarZona(Long id) {

        return zonaRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la zona de cobertura"
                ));
    }

    private void validarDuplicado(
            String departamento,
            String municipio,
            Long idExcluido
    ) {

        boolean existe;

        if (idExcluido == null) {
            existe = zonaRepository
                    .existsByDepartamentoIgnoreCaseAndMunicipioIgnoreCase(
                            departamento,
                            municipio
                    );
        } else {
            existe = zonaRepository
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

    private ZonaCoberturaSalida convertirASalida(
            ZonaCobertura zona
    ) {

        ZonaCoberturaSalida salida = new ZonaCoberturaSalida();

        salida.setId(zona.getId());
        salida.setDepartamento(zona.getDepartamento());
        salida.setMunicipio(zona.getMunicipio());
        salida.setLocalidad(zona.getLocalidad());
        salida.setActivo(zona.getActivo());

        return salida;
    }

    private List<ZonaCoberturaSalida> convertirLista(
            List<ZonaCobertura> zonas
    ) {

        List<ZonaCoberturaSalida> lista = new ArrayList<>();

        for (ZonaCobertura zona : zonas) {
            lista.add(convertirASalida(zona));
        }

        return lista;
    }
}