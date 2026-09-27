package com.connectaoficios.api.servicios.interfaces;

import com.connectaoficios.api.dtos.comun.PaginaSalida;
import com.connectaoficios.api.dtos.mensaje.MensajeFiltroDTO;
import com.connectaoficios.api.dtos.mensaje.MensajeGuardar;
import com.connectaoficios.api.dtos.mensaje.MensajeSalida;

import java.util.List;

public interface IMensajeService {

    MensajeSalida guardar(
            MensajeGuardar mensajeGuardar,
            Integer remitenteId
    );

    List<MensajeSalida> obtenerPorConversacion(
            Long conversacionId,
            Integer usuarioId
    );

    PaginaSalida<MensajeSalida> buscarConFiltros(
            MensajeFiltroDTO filtro,
            Integer usuarioId,
            int pagina,
            int tamanio
    );

    void marcarComoLeido(
            Long mensajeId,
            Integer usuarioId
    );

    long contarNoLeidos(
            Long conversacionId,
            Integer usuarioId
    );
}