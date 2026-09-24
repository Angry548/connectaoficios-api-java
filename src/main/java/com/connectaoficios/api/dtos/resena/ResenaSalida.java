package com.connectaoficios.api.dtos.resena;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ResenaSalida {

    private Long id;
    private Long solicitudId;
    private Long servicioId;
    private Long perfilTrabajadorId;
    private Integer clienteId;
    private Integer calificacion;
    private String comentario;
    private LocalDateTime fechaCreacion;
}
