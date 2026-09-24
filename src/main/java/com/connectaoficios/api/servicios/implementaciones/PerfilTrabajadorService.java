package com.connectaoficios.api.servicios.implementaciones;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PerfilTrabajadorService
        implements IPerfilTrabajadorService {

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
        perfil.setOficioPrincipal(dto.getOficioPrincipal());
        perfil.setDescripcionProfesional(
                dto.getDescripcionProfesional()
        );
        perfil.setExperienciaLaboral(
                dto.getExperienciaLaboral()
        );
        perfil.setFotoUrl(dto.getFotoUrl());
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

        PerfilTrabajador perfil = perfilRepository
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

        PerfilTrabajador perfil = perfilRepository
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

        PerfilTrabajador perfil = perfilRepository
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
            perfil.setFotoUrl(dto.getFotoUrl());
        }

        if (dto.getZonaPrincipalId() != null) {
            perfil.setZonaPrincipal(
                    buscarZona(dto.getZonaPrincipalId())
            );
        }

        perfil.setPorcentajeCompletitud(
                calcularPorcentaje(perfil)
        );

        return convertirASalida(
                perfilRepository.save(perfil)
        );
    }

    private ZonaCobertura buscarZona(Long zonaId) {

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

        if (tieneTexto(perfil.getOficioPrincipal())) {
            completados++;
        }

        if (tieneTexto(perfil.getDescripcionProfesional())) {
            completados++;
        }

        if (tieneTexto(perfil.getExperienciaLaboral())) {
            completados++;
        }

        if (tieneTexto(perfil.getFotoUrl())) {
            completados++;
        }

        if (perfil.getZonaPrincipal() != null) {
            completados++;
        }

        return (completados * 100) / total;
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private PerfilTrabajadorSalida convertirASalida(
            PerfilTrabajador perfil
    ) {

        PerfilTrabajadorSalida salida =
                new PerfilTrabajadorSalida();

        salida.setId(perfil.getId());
        salida.setTrabajadorId(perfil.getTrabajadorId());
        salida.setOficioPrincipal(
                perfil.getOficioPrincipal()
        );
        salida.setDescripcionProfesional(
                perfil.getDescripcionProfesional()
        );
        salida.setExperienciaLaboral(
                perfil.getExperienciaLaboral()
        );
        salida.setFotoUrl(perfil.getFotoUrl());
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

            salida.setZonaPrincipalId(zona.getId());
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
}
