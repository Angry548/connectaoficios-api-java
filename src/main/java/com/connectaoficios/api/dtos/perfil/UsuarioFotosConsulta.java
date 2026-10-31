package com.connectaoficios.api.dtos.perfil;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class UsuarioFotosConsulta {
    @NotNull
    @Size(max = 200, message = "Solo se permiten hasta 200 usuarios por consulta")
    @Valid
    private List<@NotNull UsuarioFotoConsulta> usuarios;
}
