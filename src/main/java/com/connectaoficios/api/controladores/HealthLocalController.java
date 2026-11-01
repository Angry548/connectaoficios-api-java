package com.connectaoficios.api.controladores;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@Profile("local")
public class HealthLocalController {
    private final DataSource datasource;
    public HealthLocalController(DataSource datasource) { this.datasource = datasource; }
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        try (var connection = datasource.getConnection(); var statement = connection.prepareStatement("SELECT 1")) {
            statement.execute();
            return ResponseEntity.ok(Map.of("status", "UP"));
        } catch (java.sql.SQLException ex) {
            return ResponseEntity.status(503).body(Map.of("status", "DOWN"));
        }
    }
}
