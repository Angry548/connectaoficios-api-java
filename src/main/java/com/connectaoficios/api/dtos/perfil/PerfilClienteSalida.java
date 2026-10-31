package com.connectaoficios.api.dtos.perfil;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PerfilClienteSalida {

    private Long id;
    private Integer clienteId;
    private String fotoUrl;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
