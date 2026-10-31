package com.connectaoficios.api.servicios.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface IImagenService {

    String subirFotoPerfilAdministrador(MultipartFile archivo, Integer administradorId);

    String subirFotoPerfilCliente(MultipartFile archivo, Integer clienteId);

    String subirFotoPerfil(
            MultipartFile archivo,
            Integer trabajadorId
    );
}
