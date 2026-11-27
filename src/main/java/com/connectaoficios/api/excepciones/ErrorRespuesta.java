package com.connectaoficios.api.excepciones;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorRespuesta(

        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> errores,
        String traceId

) {
    public ErrorRespuesta(LocalDateTime fecha, int estado, String error, String mensaje, String ruta, Map<String, String> errores) {
        this(fecha, estado, error, mensaje, ruta, errores, org.slf4j.MDC.get("traceId"));
    }
}