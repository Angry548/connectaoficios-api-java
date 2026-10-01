package com.connectaoficios.api.servicios.implementaciones;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import com.connectaoficios.api.servicios.interfaces.IImagenService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImagenService implements IImagenService {

    private static final long TAMANIO_MAXIMO =
            5L * 1024L * 1024L;

    private final Cloudinary cloudinary;

    public ImagenService(
            Cloudinary cloudinary
    ) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String subirFotoPerfil(
            MultipartFile archivo,
            Integer trabajadorId
    ) {

        validarArchivo(archivo);
        validarTrabajador(trabajadorId);

        try {

            Map<?, ?> resultado =
                    cloudinary.uploader().upload(
                            archivo.getBytes(),
                            ObjectUtils.asMap(
                                    "folder",
                                    "connectaoficios/perfiles",
                                    "public_id",
                                    "trabajador_" + trabajadorId,
                                    "overwrite",
                                    true,
                                    "invalidate",
                                    true,
                                    "resource_type",
                                    "image"
                            )
                    );

            Object secureUrl =
                    resultado.get("secure_url");

            if (secureUrl == null
                    || secureUrl.toString().isBlank()) {

                throw new ReglaNegocioException(
                        "Cloudinary no devolvió la URL de la fotografía"
                );
            }

            return secureUrl.toString();

        } catch (ReglaNegocioException exception) {

            throw exception;

        } catch (IOException exception) {

            String detalle =
                    exception.getMessage() != null
                            ? exception.getMessage()
                            : "Error desconocido de Cloudinary";

            throw new ReglaNegocioException(
                    "No se pudo subir la fotografía a Cloudinary: "
                            + detalle
            );

        } catch (RuntimeException exception) {

            String detalle =
                    exception.getMessage() != null
                            ? exception.getMessage()
                            : exception.getClass().getSimpleName();

            throw new ReglaNegocioException(
                    "No se pudo procesar la fotografía: "
                            + detalle
            );
        }
    }

    private void validarTrabajador(
            Integer trabajadorId
    ) {

        if (trabajadorId == null
                || trabajadorId <= 0) {

            throw new ReglaNegocioException(
                    "El identificador del trabajador no es válido"
            );
        }
    }

    private void validarArchivo(
            MultipartFile archivo
    ) {

        if (archivo == null
                || archivo.isEmpty()) {

            throw new ReglaNegocioException(
                    "Debe seleccionar una fotografía"
            );
        }

        if (archivo.getSize() > TAMANIO_MAXIMO) {

            throw new ReglaNegocioException(
                    "La fotografía no puede superar los 5 MB"
            );
        }

        String tipoContenido =
                archivo.getContentType();

        if (tipoContenido == null
                || !tipoContenido.startsWith("image/")) {

            throw new ReglaNegocioException(
                    "El archivo seleccionado debe ser una imagen"
            );
        }

        if (!tipoContenido.equalsIgnoreCase("image/jpeg")
                && !tipoContenido.equalsIgnoreCase("image/png")
                && !tipoContenido.equalsIgnoreCase("image/webp")) {

            throw new ReglaNegocioException(
                    "Solo se permiten imágenes JPG, PNG o WEBP"
            );
        }
    }
}