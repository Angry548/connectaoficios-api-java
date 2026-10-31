package com.connectaoficios.api.dtos.perfil;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilAdministradorPublicoSalida {

    private Long id;
    private Integer administradorId;
    private String fotoUrl;
}
