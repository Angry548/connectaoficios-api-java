package com.connectaoficios.api.controladores;

import com.connectaoficios.api.excepciones.RecursoNoEncontradoException;
import com.connectaoficios.api.repositorios.IConversacionRepository;
import com.connectaoficios.api.seguridad.IdentidadJwt;
import com.connectaoficios.api.servicios.implementaciones.PresenciaService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/conversaciones")
public class PresenciaController {
    private final IConversacionRepository repositorio;
    private final PresenciaService presencia;
    public PresenciaController(IConversacionRepository repositorio, PresenciaService presencia) {
        this.repositorio = repositorio; this.presencia = presencia;
    }
    @GetMapping("/{id}/presencia")
    @PreAuthorize("hasAnyRole('CLIENTE', 'TRABAJADOR')")
    public Map<String, Object> obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        var c = repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Conversacion no encontrada"));
        Integer usuario = IdentidadJwt.usuarioId(jwt);
        if (!usuario.equals(c.getClienteId()) && !usuario.equals(c.getTrabajadorId())) throw new AccessDeniedException("Conversacion ajena");
        Integer otro = usuario.equals(c.getClienteId()) ? c.getTrabajadorId() : c.getClienteId();
        return Map.of("usuarioId", otro, "online", presencia.online(otro));
    }
}
