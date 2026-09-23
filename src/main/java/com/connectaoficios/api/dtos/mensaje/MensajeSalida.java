package com.connectaoficios.api.dtos.mensaje;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MensajeSalida {

    private Long id;
    private Long conversacionId;
    private Integer remitenteId;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private Boolean leido;
}