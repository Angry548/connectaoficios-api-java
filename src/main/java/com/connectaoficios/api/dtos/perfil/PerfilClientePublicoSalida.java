package com.connectaoficios.api.dtos.perfil;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilClientePublicoSalida {

    private Long id;
    private Integer clienteId;
    private String fotoUrl;
}
