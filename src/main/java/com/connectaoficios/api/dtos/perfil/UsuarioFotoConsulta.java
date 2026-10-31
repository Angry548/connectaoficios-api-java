package com.connectaoficios.api.dtos.perfil;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioFotoConsulta {
    @NotNull
    @Positive
    private Integer usuarioId;

    @NotBlank
    @Pattern(regexp = "(?i)CLIENTE|TRABAJADOR|ADMINISTRADOR|ADMINISTRADORPRINCIPAL")
    private String rol;
}
