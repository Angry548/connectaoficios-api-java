package com.connectaoficios.api.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ManejadorAccesoDenegado implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ManejadorAccesoDenegado(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> error = new LinkedHashMap<>();

        error.put("fecha", LocalDateTime.now());
        error.put("estado", HttpStatus.FORBIDDEN.value());
        error.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        error.put(
                "mensaje",
                "No tiene permisos para acceder a este recurso."
        );
        error.put("ruta", request.getRequestURI());
        error.put("errores", null);

        objectMapper.writeValue(
                response.getOutputStream(),
                error
        );
    }
}