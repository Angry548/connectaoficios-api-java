package com.connectaoficios.api.dtos.perfil;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PerfilTrabajadorSalida {

    private Long id;
    private Integer trabajadorId;
    private String oficioPrincipal;
    private String descripcionProfesional;
    private String experienciaLaboral;
    private String fotoUrl;
    private Integer porcentajeCompletitud;

    private Long zonaPrincipalId;
    private String departamento;
    private String municipio;
    private String localidad;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
