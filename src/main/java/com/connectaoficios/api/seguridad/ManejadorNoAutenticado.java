package com.connectaoficios.api.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ManejadorNoAutenticado implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public ManejadorNoAutenticado(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new LinkedHashMap<>();

        error.put("fecha", LocalDateTime.now());
        error.put("estado", HttpStatus.UNAUTHORIZED.value());
        error.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        error.put(
                "mensaje",
                "Debe autenticarse con un token válido para acceder a este recurso."
        );
        error.put("ruta", request.getRequestURI());
        error.put("errores", null);

        objectMapper.writeValue(
                response.getOutputStream(),
                error
        );
    }
}