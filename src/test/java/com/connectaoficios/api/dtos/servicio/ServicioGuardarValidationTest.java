package com.connectaoficios.api.dtos.servicio;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServicioGuardarValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void crearValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void cerrarValidator() {
        validatorFactory.close();
    }

    @Test
    void debeRechazarCamposObligatoriosNulos() {
        ServicioGuardar dto = new ServicioGuardar();

        assertEquals(
                Set.of(
                        "perfilTrabajadorId",
                        "categoriaId",
                        "titulo",
                        "descripcion",
                        "tarifaMinima"
                ),
                camposInvalidos(dto)
        );
    }

    @Test
    void debeRechazarTituloYDescripcionEnBlanco() {
        ServicioGuardar dto = dtoValido();
        dto.setTitulo(" \t ");
        dto.setDescripcion("\n");

        assertEquals(
                Set.of("titulo", "descripcion"),
                camposInvalidos(dto)
        );
    }

    @Test
    void debeRechazarValoresFueraDeLosLimitesDeclarados() {
        ServicioGuardar dto = dtoValido();
        dto.setTitulo("T".repeat(151));
        dto.setDescripcion("D".repeat(2001));
        dto.setTarifaMinima(new BigDecimal("-0.01"));
        dto.setTarifaMaxima(new BigDecimal("-0.01"));

        assertEquals(
                Set.of("titulo", "descripcion", "tarifaMinima", "tarifaMaxima"),
                camposInvalidos(dto)
        );
    }

    @Test
    void debeAceptarDatosValidosSinTarifaMaximaNiZonas() {
        assertEquals(Set.of(), camposInvalidos(dtoValido()));
    }

    private ServicioGuardar dtoValido() {
        ServicioGuardar dto = new ServicioGuardar();
        dto.setPerfilTrabajadorId(1L);
        dto.setCategoriaId(1L);
        dto.setTitulo("Servicio");
        dto.setDescripcion("Descripción del servicio");
        dto.setTarifaMinima(new BigDecimal("0.00"));
        return dto;
    }

    private Set<String> camposInvalidos(ServicioGuardar dto) {
        return validator.validate(dto)
                .stream()
                .map(violacion -> violacion.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
