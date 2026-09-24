package com.connectaoficios.api.dtos.notificacion;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificacionMarcarLeida {

    @NotNull(message = "El estado de lectura es obligatorio")
    private Boolean leida;
}
