package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.perfil.*;
import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.modelos.PerfilCliente;
import com.connectaoficios.api.repositorios.IPerfilClienteRepository;
import com.connectaoficios.api.servicios.interfaces.IPerfilClienteService;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilClienteService implements IPerfilClienteService {
    private final IPerfilClienteRepository perfilRepository;

    public PerfilClienteService(IPerfilClienteRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    @Override
    @Transactional
    public PerfilClienteSalida guardar(PerfilClienteGuardar dto, Integer clienteId) {
        if (perfilRepository.existsByClienteId(clienteId)) {
            throw new ReglaNegocioException("El cliente ya posee un perfil");
        }
        PerfilCliente perfil = new PerfilCliente();
        perfil.setClienteId(clienteId);
        perfil.setFotoUrl(dto.getFotoUrl());
        try {
            return convertirASalida(perfilRepository.saveAndFlush(perfil));
        } catch (DataIntegrityViolationException exception) {
            // El flush permite detectar el UNIQUE incluso con creaciones simultáneas.
            for (Throwable causa = exception; causa != null; causa = causa.getCause()) {
                if (causa instanceof org.hibernate.exception.ConstraintViolationException violacion) {
                    String nombre = violacion.getConstraintName();
                    if (nombre != null && (nombre.equalsIgnoreCase("uk_perfil_cliente")
                            || nombre.toLowerCase(java.util.Locale.ROOT).endsWith(".uk_perfil_cliente"))) {
                        throw new ReglaNegocioException("El cliente ya posee un perfil");
                    }
                }
            }
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilClienteSalida obtenerPorId(Long id) {
        return convertirASalida(perfilRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el perfil del cliente")));
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilClienteSalida obtenerPorClienteId(Integer clienteId) {
        return convertirASalida(buscarPorClienteId(clienteId));
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilClientePublicoSalida obtenerPerfilPublicoPorClienteId(Integer clienteId) {
        PerfilCliente perfil = buscarPorClienteId(clienteId);
        PerfilClientePublicoSalida salida = new PerfilClientePublicoSalida();
        salida.setId(perfil.getId());
        salida.setClienteId(perfil.getClienteId());
        salida.setFotoUrl(perfil.getFotoUrl());
        return salida;
    }

    @Override
    @Transactional
    public PerfilClienteSalida modificar(Integer clienteId, PerfilClienteModificar dto) {
        PerfilCliente perfil = buscarPorClienteId(clienteId);
        if (dto.getFotoUrl() != null) {
            perfil.setFotoUrl(dto.getFotoUrl());
        }
        return convertirASalida(perfilRepository.save(perfil));
    }

    private PerfilCliente buscarPorClienteId(Integer clienteId) {
        return perfilRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el perfil del cliente"));
    }

    private PerfilClienteSalida convertirASalida(PerfilCliente perfil) {
        PerfilClienteSalida salida = new PerfilClienteSalida();
        salida.setId(perfil.getId());
        salida.setClienteId(perfil.getClienteId());
        salida.setFotoUrl(perfil.getFotoUrl());
        salida.setFechaCreacion(perfil.getFechaCreacion());
        salida.setFechaActualizacion(perfil.getFechaActualizacion());
        return salida;
    }
}
