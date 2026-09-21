package com.connectaoficios.api.seguridad.configuracion;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String DOTNET_ROLE_CLAIM =
            "http://schemas.microsoft.com/ws/2008/06/identity/claims/role";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        Collection<SimpleGrantedAuthority> authorities =
                new ArrayList<>();

        String role = jwt.getClaimAsString(DOTNET_ROLE_CLAIM);

        if (role == null || role.isBlank()) {
            role = jwt.getClaimAsString("role");
        }

        if (role != null && !role.isBlank()) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_" + role.toUpperCase()
                    )
            );
        }

        return new JwtAuthenticationToken(
                jwt,
                authorities,
                obtenerNombre(jwt)
        );
    }

    private String obtenerNombre(Jwt jwt) {

        String nombre = jwt.getClaimAsString(
                "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"
        );

        if (nombre != null && !nombre.isBlank()) {
            return nombre;
        }

        String email = jwt.getClaimAsString("email");

        if (email != null && !email.isBlank()) {
            return email;
        }

        return jwt.getSubject();
    }
}