package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.perfil.UsuarioFotosConsulta;
import com.connectaoficios.api.dtos.perfil.UsuarioFotoSalida;
import com.connectaoficios.api.servicios.implementaciones.UsuarioAvatarService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/perfiles")
public class UsuarioAvatarController {
    private final UsuarioAvatarService avatarService;

    public UsuarioAvatarController(UsuarioAvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @PostMapping("/fotos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')")
    public ResponseEntity<List<UsuarioFotoSalida>> obtenerFotos(@Valid @RequestBody UsuarioFotosConsulta dto) {
        return ResponseEntity.ok(avatarService.resolverFotos(dto.getUsuarios()));
    }
}
