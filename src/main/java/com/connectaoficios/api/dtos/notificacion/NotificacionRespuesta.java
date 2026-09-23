package com.connectaoficios.api.dtos.notificacion;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificacionRespuesta {

    private Long idNotificacion;
    private Integer usuarioDestinoId;
    private String tipo;
    private String titulo;
    private String mensaje;
    private Long referenciaId;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
}
