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
    @PreAuthorize("hasRole('TRABAJADOR')")
    public ResponseEntity<FotoPerfilSalida> subirFotoPerfil(
            @RequestPart("archivo")
            MultipartFile archivo,
            @AuthenticationPrincipal
            Jwt jwt
    ) {

        Integer trabajadorId =
                obtenerUsuarioId(jwt);

        String fotoUrl =
                imagenService.subirFotoPerfil(
                        archivo,
                        trabajadorId
                );

        return ResponseEntity.ok(
                new FotoPerfilSalida(
                        fotoUrl
                )
        );
    }

    private Integer obtenerUsuarioId(
            Jwt jwt
    ) {

        if (jwt == null
                || jwt.getSubject() == null
                || jwt.getSubject().isBlank()) {

            throw new ReglaNegocioException(
                    "No se pudo identificar al usuario autenticado"
            );
        }

        try {

            Integer usuarioId =
                    Integer.valueOf(
                            jwt.getSubject()
                    );

            if (usuarioId <= 0) {
                throw new ReglaNegocioException(
                        "El identificador del usuario autenticado no es válido"
                );
            }

            return usuarioId;

        } catch (NumberFormatException exception) {

            throw new ReglaNegocioException(
                    "El identificador del usuario autenticado no es válido"
            );
        }
    }
}