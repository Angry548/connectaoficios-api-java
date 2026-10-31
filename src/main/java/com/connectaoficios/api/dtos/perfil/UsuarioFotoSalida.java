package com.connectaoficios.api.dtos.perfil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UsuarioFotoSalida {
    private Integer usuarioId;
    private String rol;
    private String fotoUrl;
}
