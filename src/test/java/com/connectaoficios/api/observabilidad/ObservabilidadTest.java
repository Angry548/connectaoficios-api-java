package com.connectaoficios.api.observabilidad;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

class ObservabilidadTest {
    @Test void traceEsUnicoSeDevuelveYSeLimpiaSinGuardarDatosSensibles() throws Exception {
        var actividad = new ActividadReciente();
        var filtro = new FiltroTrazabilidad(actividad);
        var request = new MockHttpServletRequest("GET", "/api/secret-token");
        request.setQueryString("password=secreto");
        request.addHeader("Authorization", "Bearer secreto");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/{id}");
        var response = new MockHttpServletResponse();
        filtro.doFilter(request, response, (req, res) -> assertEquals(response.getHeader("X-Trace-Id"), MDC.get("traceId")));
        String primero = response.getHeader("X-Trace-Id");
        assertNotNull(primero);
        assertNull(MDC.get("traceId"));
        String resumen = actividad.resumen().toString();
        assertFalse(resumen.contains("secreto"));
        assertFalse(resumen.contains("secret-token"));
        var otro = new MockHttpServletResponse();
        filtro.doFilter(new MockHttpServletRequest("GET", "/"), otro, (req, res) -> {});
        assertNotEquals(primero, otro.getHeader("X-Trace-Id"));
    }
    @Test void bufferTieneLimite() {
        var actividad = new ActividadReciente();
        for (int i=0; i<250; i++) actividad.agregar(new ActividadReciente.Evento(Instant.now(), "id", "GET", "/api", 200, i));
        assertEquals(200, actividad.resumen().get("peticiones"));
    }
}
