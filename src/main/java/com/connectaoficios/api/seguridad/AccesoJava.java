package com.connectaoficios.api.seguridad;

import com.connectaoficios.api.dtos.solicitud.SolicitudServicioGuardar;
import com.connectaoficios.api.repositorios.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("accesoJava")
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class AccesoJava {
    private final IPerfilTrabajadorRepository perfiles;
    private final IServicioRepository servicios;
    private final ISolicitudServicioRepository solicitudes;
    private final IConversacionRepository conversaciones;

    public AccesoJava(IPerfilTrabajadorRepository perfiles, IServicioRepository servicios,
                      ISolicitudServicioRepository solicitudes, IConversacionRepository conversaciones) {
        this.perfiles = perfiles; this.servicios = servicios;
        this.solicitudes = solicitudes; this.conversaciones = conversaciones;
    }

    public boolean perfil(Authentication auth, Long id) {
        int usuario = IdentidadJwt.usuarioId(auth);
        return perfiles.findById(id).map(p -> Integer.valueOf(usuario).equals(p.getTrabajadorId())).orElse(false);
    }

    public boolean servicio(Authentication auth, Long id) {
        int usuario = IdentidadJwt.usuarioId(auth);
        return servicios.findByIdAndEliminadoFalse(id).map(s -> s.getPerfilTrabajador() != null
                && Integer.valueOf(usuario).equals(s.getPerfilTrabajador().getTrabajadorId())).orElse(false);
    }

    public boolean solicitud(Authentication auth, Long id, boolean soloTrabajador) {
        int usuario = IdentidadJwt.usuarioId(auth);
        return solicitudes.findById(id).map(s -> Integer.valueOf(usuario).equals(s.getTrabajadorId())
                || (!soloTrabajador && (IdentidadJwt.administrador(auth)
                || Integer.valueOf(usuario).equals(s.getClienteId())))).orElse(false);
    }

    public boolean usuario(Authentication auth, Integer id) {
        return IdentidadJwt.administrador(auth) || IdentidadJwt.usuarioId(auth).equals(id);
    }

    public boolean conversacion(Authentication auth, Long id) {
        int usuario = IdentidadJwt.usuarioId(auth);
        return conversaciones.findById(id).map(c -> c.getSolicitud() != null
                && (Integer.valueOf(usuario).equals(c.getSolicitud().getClienteId())
                || Integer.valueOf(usuario).equals(c.getSolicitud().getTrabajadorId()))).orElse(false);
    }

    public void prepararSolicitud(Authentication auth, SolicitudServicioGuardar dto) {
        var servicio = servicios.findByIdAndEliminadoFalse(dto.getServicioId())
                .orElseThrow(() -> new com.connectaoficios.api.excepciones.RecursoNoEncontradoException("Servicio inexistente"));
        int cliente = IdentidadJwt.usuarioId(auth);
        int trabajador = servicio.getPerfilTrabajador().getTrabajadorId();
        if ((dto.getClienteId() != null && dto.getClienteId() != cliente)
                || (dto.getTrabajadorId() != null && dto.getTrabajadorId() != trabajador)) {
            throw new AccessDeniedException("Los participantes no corresponden al servicio o al JWT");
        }
        dto.setClienteId(cliente);
        dto.setTrabajadorId(trabajador);
    }
}
