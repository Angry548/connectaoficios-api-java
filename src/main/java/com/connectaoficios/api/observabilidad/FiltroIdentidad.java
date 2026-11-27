package com.connectaoficios.api.observabilidad;

import com.connectaoficios.api.seguridad.IdentidadJwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class FiltroIdentidad extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            request.setAttribute("observabilidad.userId", IdentidadJwt.usuarioId(SecurityContextHolder.getContext().getAuthentication()));
        } catch (RuntimeException ignored) { /* La identidad no siempre está disponible. */ }
        chain.doFilter(request, response);
    }
}
