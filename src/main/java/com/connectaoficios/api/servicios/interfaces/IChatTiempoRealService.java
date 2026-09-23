package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.mensaje.MensajeSalida;

public interface IChatTiempoRealService {

    void publicarMensaje(
            MensajeSalida mensaje
    );
}