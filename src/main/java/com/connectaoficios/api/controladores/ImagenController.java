package com.connectaoficios.api.controladores;

import com.connectaoficios.api.dtos.perfil.FotoPerfilSalida;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.servicios.interfaces.IImagenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    private final IImagenService imagenService;

    public ImagenController(
            IImagenService imagenService
    ) {
        this.imagenService = imagenService;
    }

    @PostMapping(
            value = "/perfil",
            consumes = "multipart/form-data"
    )
    @PreAuthorize("hasAnyRole('TRABAJADOR', 'CLIENTE', 'ADMINISTRADOR', 'ADMINISTRADORPRINCIPAL')")
    public ResponseEntity<FotoPerfilSalida> subirFotoPerfil(
            @RequestPart("archivo")
            MultipartFile archivo,
            @AuthenticationPrincipal
            Jwt jwt,
            org.springframework.security.core.Authentication authentication
    ) {

        Integer usuarioId =
                obtenerUsuarioId(jwt);

        String fotoUrl;
        if (authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_ADMINISTRADORPRINCIPAL"))) {
            fotoUrl = imagenService.subirFotoPerfilAdministrador(archivo, usuarioId);
        } else if (authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_CLIENTE"))) {
            fotoUrl = imagenService.subirFotoPerfilCliente(archivo, usuarioId);
        } else {
            fotoUrl = imagenService.subirFotoPerfil(archivo, usuarioId);
        }

        return ResponseEntity.ok(
                new FotoPerfilSalida(
                        fotoUrl
                )
        );
    }

    private Integer obtenerUsuarioId(Jwt jwt) {
        return com.connectaoficios.api.seguridad.IdentidadJwt.usuarioId(jwt);
    }
}
