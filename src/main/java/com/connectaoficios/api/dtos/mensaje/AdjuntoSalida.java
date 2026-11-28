package com.connectaoficios.api.dtos.mensaje;
import com.connectaoficios.api.enums.TipoMensaje;
public record AdjuntoSalida(Long id, TipoMensaje tipo, String nombre, String mimeType, long tamanoBytes, String url) { }
