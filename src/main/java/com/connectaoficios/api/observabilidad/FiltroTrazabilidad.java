package com.connectaoficios.api.observabilidad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FiltroTrazabilidad extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(FiltroTrazabilidad.class);
    private final ActividadReciente actividad;
    @org.springframework.beans.factory.annotation.Autowired
    public FiltroTrazabilidad(org.springframework.beans.factory.ObjectProvider<ActividadReciente> actividad) {
        this.actividad = actividad.getIfAvailable(ActividadReciente::new);
    }
    FiltroTrazabilidad(ActividadReciente actividad) { this.actividad = actividad; }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString();
        long inicio = System.nanoTime();
        MDC.put("traceId", traceId);
        request.setAttribute("traceId", traceId);
        response.setHeader("X-Trace-Id", traceId);
        try {
            chain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException ex) {
            if (!response.isCommitted()) response.setStatus(500);
            throw ex;
        } finally {
            long duracion = (System.nanoTime() - inicio) / 1_000_000;
            Object patron = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String endpoint = patron == null ? "[sin-ruta]" : patron.toString();
            String metodo = request.getMethod().matches("[A-Z]{1,16}") ? request.getMethod() : "OTHER";
            int estado = response.getStatus();
            Object usuario = request.getAttribute("observabilidad.userId");
            String userId = usuario == null ? "-" : usuario.toString();
            LOG.atLevel(estado >= 500 ? org.slf4j.event.Level.ERROR : estado >= 400 ? org.slf4j.event.Level.WARN : org.slf4j.event.Level.INFO)
                    .log("traceId={} userId={} method={} endpoint={} status={} duration={}ms", traceId, userId, metodo, endpoint, estado, duracion);
            actividad.agregar(new ActividadReciente.Evento(Instant.now(), traceId, metodo, endpoint, estado, duracion));
            MDC.remove("traceId");
        }
    }
}
