package com.connectaoficios.api.observabilidad;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "monitoring.ui.enabled", havingValue = "true")
public class MonitoreoController {
    private final ActividadReciente actividad;
    private final DataSource dataSource;
    public MonitoreoController(ActividadReciente actividad, DataSource dataSource) { this.actividad = actividad; this.dataSource = dataSource; }
    @GetMapping(value = "/monitoring", produces = MediaType.TEXT_HTML_VALUE)
    public ClassPathResource pagina() { return new ClassPathResource("monitoring/index.html"); }
    @GetMapping("/monitoring/data")
    public Map<String, Object> datos() {
        boolean conectada;
        try (var conexion = dataSource.getConnection()) { conectada = conexion.isValid(2); }
        catch (java.sql.SQLException ex) { conectada = false; }
        return Map.of("api", "UP", "baseDatos", conectada ? "UP" : "DOWN", "actividad", actividad.resumen());
    }
}
