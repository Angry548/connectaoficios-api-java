package com.connectaoficios.api.dtos.perfil;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PerfilAdministradorSalida {

    private Long id;
    private Integer administradorId;
    private String fotoUrl;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
