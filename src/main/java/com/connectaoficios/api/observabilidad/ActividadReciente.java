package com.connectaoficios.api.observabilidad;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ActividadReciente {
    private static final int LIMITE = 200;
    public record Evento(Instant fecha, String traceId, String metodo, String endpoint, int estado, long duracionMs) { }
    private final ArrayDeque<Evento> eventos = new ArrayDeque<>();
    public synchronized void agregar(Evento evento) {
        if (eventos.size() == LIMITE) eventos.removeFirst();
        eventos.addLast(evento);
    }
    public synchronized Map<String, Object> resumen() {
        List<Evento> copia = eventos.reversed().stream().toList();
        return Map.of("eventos", copia, "peticiones", copia.size(),
            "errores", copia.stream().filter(e -> e.estado() >= 400).count(),
            "promedioMs", copia.stream().mapToLong(Evento::duracionMs).average().orElse(0));
    }
}
