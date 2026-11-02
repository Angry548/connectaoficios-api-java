package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.resena.ResenaGuardar;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Angel Mate: rango del DTO; conserva intactas las pruebas de Nuria. */
class ResenaGuardarValidationTest {
    @Test
    void calificacionAceptaLimitesYRechazaValoresFueraDelRango() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var dto = new ResenaGuardar();
            dto.setSolicitudId(1L);
            for (int valor : new int[]{1, 5}) {
                dto.setCalificacion(valor);
                assertTrue(validator.validate(dto).isEmpty(), "Limite permitido: " + valor);
            }
            for (Integer valor : new Integer[]{null, 0, 6}) {
                dto.setCalificacion(valor);
                var violations = validator.validate(dto);
                assertEquals(1, violations.size(), "Valor invalido: " + valor);
                assertEquals("calificacion", violations.iterator().next().getPropertyPath().toString());
            }
        }
    }
}
