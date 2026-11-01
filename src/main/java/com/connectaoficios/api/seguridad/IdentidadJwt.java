package com.connectaoficios.api.seguridad;

import com.connectaoficios.api.excepciones.ReglaNegocioException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Contrato .NET: NameIdentifier; compatibilidad con tokens anteriores que usan sub. */
public final class IdentidadJwt {
    public static final String NAME_IDENTIFIER = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier";
    private IdentidadJwt() { }

    public static Integer usuarioId(Jwt jwt) {
        if (jwt == null) throw new ReglaNegocioException("No se pudo identificar al usuario autenticado");
        String id = jwt.getClaimAsString(NAME_IDENTIFIER);
        if (id == null || id.isBlank()) id = jwt.getSubject();
        try {
            int numero = Integer.parseInt(id);
            if (numero > 0) return numero;
        } catch (NumberFormatException ignored) { }
        throw new ReglaNegocioException("El identificador del usuario autenticado no es válido");
    }

    public static Integer usuarioId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken token) return usuarioId(token.getToken());
        throw new org.springframework.security.access.AccessDeniedException("Se requiere identidad JWT");
    }

    public static boolean administrador(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMINISTRADOR") || a.getAuthority().equals("ROLE_ADMINISTRADORPRINCIPAL"));
    }
}
