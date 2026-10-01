package com.connectaoficios.api.servicios.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface IImagenService {

    String subirFotoPerfil(
            MultipartFile archivo,
            Integer trabajadorId
    );
}