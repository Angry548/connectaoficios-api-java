package com.connectaoficios.api.dtos.perfil;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilTrabajadorModificar {

    @Size(max = 100, message = "El oficio principal no puede superar los 100 caracteres")
    private String oficioPrincipal;

    @Size(max = 1000, message = "La descripción profesional no puede superar los 1000 caracteres")
    private String descripcionProfesional;

    @Size(max = 2000, message = "La experiencia laboral no puede superar los 2000 caracteres")
    private String experienciaLaboral;



    private String fotoUrl;

    private Long zonaPrincipalId;
}
