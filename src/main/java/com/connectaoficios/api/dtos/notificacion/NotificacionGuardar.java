package com.connectaoficios.api.dtos.notificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificacionGuardar {

    @NotNull(message = "El ID del usuario destino es obligatorio")
    private Integer usuarioDestinoId;

    @NotBlank(message = "El tipo de notificación es obligatorio")
    private String tipo;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;

    private Long referenciaId;
}
