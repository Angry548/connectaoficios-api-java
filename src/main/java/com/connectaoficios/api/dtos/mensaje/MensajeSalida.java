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
    private com.connectaoficios.api.enums.TipoMensaje tipo;
    private LocalDateTime fechaEnvio;
    private Boolean leido;
    private java.util.List<AdjuntoSalida> adjuntos = java.util.List.of();
}