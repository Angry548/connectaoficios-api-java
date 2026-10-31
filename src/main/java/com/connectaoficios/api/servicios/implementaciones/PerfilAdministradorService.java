package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.perfil.*;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PerfilAdministrador;
import com.connectaoficios.api.repositorios.IPerfilAdministradorRepository;
import com.connectaoficios.api.servicios.interfaces.IPerfilAdministradorService;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilAdministradorService implements IPerfilAdministradorService {
    private final IPerfilAdministradorRepository perfilRepository;

    public PerfilAdministradorService(IPerfilAdministradorRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    @Override
    @Transactional
    public PerfilAdministradorSalida guardar(PerfilAdministradorGuardar dto, Integer administradorId) {
        if (perfilRepository.existsByAdministradorId(administradorId)) {
            throw new ReglaNegocioException("El administrador ya posee un perfil");
        }
        PerfilAdministrador perfil = new PerfilAdministrador();
        perfil.setAdministradorId(administradorId);
        perfil.setFotoUrl(dto.getFotoUrl());
        try {
            return convertirASalida(perfilRepository.saveAndFlush(perfil));
        } catch (DataIntegrityViolationException exception) {
            // El flush permite detectar el UNIQUE incluso con creaciones simultáneas.
            for (Throwable causa = exception; causa != null; causa = causa.getCause()) {
                if (causa instanceof org.hibernate.exception.ConstraintViolationException violacion) {
                    String nombre = violacion.getConstraintName();
                    if (nombre != null && (nombre.equalsIgnoreCase("uk_perfil_administrador")
                            || nombre.toLowerCase(java.util.Locale.ROOT).endsWith(".uk_perfil_administrador"))) {
                        throw new ReglaNegocioException("El administrador ya posee un perfil");
                    }
                }
            }
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilAdministradorSalida obtenerPorId(Long id) {
        return convertirASalida(perfilRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el perfil del administrador")));
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilAdministradorSalida obtenerPorAdministradorId(Integer administradorId) {
        return convertirASalida(buscarPorAdministradorId(administradorId));
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilAdministradorPublicoSalida obtenerPerfilPublicoPorAdministradorId(Integer administradorId) {
        PerfilAdministrador perfil = buscarPorAdministradorId(administradorId);
        PerfilAdministradorPublicoSalida salida = new PerfilAdministradorPublicoSalida();
        salida.setId(perfil.getId());
        salida.setAdministradorId(perfil.getAdministradorId());
        salida.setFotoUrl(perfil.getFotoUrl());
        return salida;
    }

    @Override
    @Transactional
    public PerfilAdministradorSalida modificar(Integer administradorId, PerfilAdministradorModificar dto) {
        PerfilAdministrador perfil = buscarPorAdministradorId(administradorId);
        if (dto.getFotoUrl() != null) {
            perfil.setFotoUrl(dto.getFotoUrl());
        }
        return convertirASalida(perfilRepository.save(perfil));
    }

    private PerfilAdministrador buscarPorAdministradorId(Integer administradorId) {
        return perfilRepository.findByAdministradorId(administradorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el perfil del administrador"));
    }

    private PerfilAdministradorSalida convertirASalida(PerfilAdministrador perfil) {
        PerfilAdministradorSalida salida = new PerfilAdministradorSalida();
        salida.setId(perfil.getId());
        salida.setAdministradorId(perfil.getAdministradorId());
        salida.setFotoUrl(perfil.getFotoUrl());
        salida.setFechaCreacion(perfil.getFechaCreacion());
        salida.setFechaActualizacion(perfil.getFechaActualizacion());
        return salida;
    }
}
