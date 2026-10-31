package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.perfil.UsuarioFotoConsulta;
import com.connectaoficios.api.dtos.perfil.UsuarioFotoSalida;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.repositorios.IPerfilAdministradorRepository;
import com.connectaoficios.api.repositorios.IPerfilClienteRepository;
import com.connectaoficios.api.repositorios.IPerfilTrabajadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class UsuarioAvatarService {
    private final IPerfilClienteRepository clientes;
    private final IPerfilTrabajadorRepository trabajadores;
    private final IPerfilAdministradorRepository administradores;

    public UsuarioAvatarService(IPerfilClienteRepository clientes,
            IPerfilTrabajadorRepository trabajadores, IPerfilAdministradorRepository administradores) {
        this.clientes = clientes;
        this.trabajadores = trabajadores;
        this.administradores = administradores;
    }

    public String resolverFoto(Integer usuarioId, String rol) {
        UsuarioFotoConsulta consulta = new UsuarioFotoConsulta();
        consulta.setUsuarioId(usuarioId);
        consulta.setRol(rol);
        return resolverFotos(List.of(consulta)).getFirst().getFotoUrl();
    }

    public List<UsuarioFotoSalida> resolverFotos(List<UsuarioFotoConsulta> usuarios) {
        if (usuarios == null || usuarios.size() > 200) {
            throw new ReglaNegocioException("Debe enviar como máximo 200 usuarios");
        }
        Set<Integer> idsClientes = new HashSet<>();
        Set<Integer> idsTrabajadores = new HashSet<>();
        Set<Integer> idsAdministradores = new HashSet<>();
        for (UsuarioFotoConsulta usuario : usuarios) {
            if (usuario == null || usuario.getUsuarioId() == null || usuario.getUsuarioId() <= 0) {
                throw new ReglaNegocioException("El identificador del usuario no es válido");
            }
            switch (normalizarRol(usuario.getRol())) {
                case "CLIENTE" -> idsClientes.add(usuario.getUsuarioId());
                case "TRABAJADOR" -> idsTrabajadores.add(usuario.getUsuarioId());
                default -> idsAdministradores.add(usuario.getUsuarioId());
            }
        }
        Map<Integer, String> fotosClientes = new HashMap<>();
        Map<Integer, String> fotosTrabajadores = new HashMap<>();
        Map<Integer, String> fotosAdministradores = new HashMap<>();
        if (!idsClientes.isEmpty()) {
            clientes.findByClienteIdIn(idsClientes)
                    .forEach(p -> fotosClientes.put(p.getClienteId(), p.getFotoUrl()));
        }
        if (!idsTrabajadores.isEmpty()) {
            trabajadores.findByTrabajadorIdIn(idsTrabajadores)
                    .forEach(p -> fotosTrabajadores.put(p.getTrabajadorId(), p.getFotoUrl()));
        }
        if (!idsAdministradores.isEmpty()) {
            administradores.findByAdministradorIdIn(idsAdministradores)
                    .forEach(p -> fotosAdministradores.put(p.getAdministradorId(), p.getFotoUrl()));
        }
        return usuarios.stream().map(u -> {
            String rol = normalizarRol(u.getRol());
            Map<Integer, String> fotos = switch (rol) {
                case "CLIENTE" -> fotosClientes;
                case "TRABAJADOR" -> fotosTrabajadores;
                default -> fotosAdministradores;
            };
            return new UsuarioFotoSalida(u.getUsuarioId(), rol, fotos.get(u.getUsuarioId()));
        }).toList();
    }

    private String normalizarRol(String rol) {
        if (rol == null) {
            throw new ReglaNegocioException("Debe indicar el rol del usuario");
        }
        String normalizado = rol.toUpperCase(Locale.ROOT);
        if (!Set.of("CLIENTE", "TRABAJADOR", "ADMINISTRADOR", "ADMINISTRADORPRINCIPAL")
                .contains(normalizado)) {
            throw new ReglaNegocioException("El rol del usuario no es válido");
        }
        return normalizado;
    }
}
