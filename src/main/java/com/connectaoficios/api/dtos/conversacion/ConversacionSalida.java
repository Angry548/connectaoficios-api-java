package com.connectaoficios.api.dtos.conversacion;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ConversacionSalida {

    private Long id;
    private Long solicitudId;
    private Integer clienteId;
    private Integer trabajadorId;
    private String clienteFotoUrl;
    private String trabajadorFotoUrl;
    private LocalDateTime fechaCreacion;
    private Boolean puedeEnviarMensajes;
}
