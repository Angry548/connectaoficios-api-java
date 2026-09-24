package com.connectaoficios.api.dtos.resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResenaGuardar {

    @NotNull(message = "La solicitud es obligatoria")
    private Long solicitudId;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer calificacion;

    @Size(
            max = 1000,
            message = "El comentario no puede superar los 1000 caracteres"
    )
    private String comentario;
}
