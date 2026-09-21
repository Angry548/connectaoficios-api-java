package com.connectaoficios.api.excepciones;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorRespuesta(

        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> errores

) {
}