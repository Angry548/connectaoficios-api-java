package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.servicios.interfaces.IImagenService;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Simulación explícita: valida la entrada, no almacena ni transmite archivos. */
@Service
@Profile("local | test")
public class ImagenLocalService implements IImagenService {
    private String subir(MultipartFile archivo, Integer id) {
        if (id == null || id <= 0) throw new ReglaNegocioException("Usuario inválido");
        if (archivo == null || archivo.isEmpty()) throw new ReglaNegocioException("Archivo vacío");
        if (archivo.getSize() > 5L * 1024 * 1024) throw new ReglaNegocioException("Máximo 5 MB");
        if (!java.util.Set.of("image/jpeg", "image/png", "image/webp").contains(
                archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase(java.util.Locale.ROOT)))
            throw new ReglaNegocioException("Formato inválido");
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(
                "<svg xmlns='http://www.w3.org/2000/svg' width='64' height='64'><rect width='64' height='64' fill='#64748b'/></svg>"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    public String subirFotoPerfil(MultipartFile a, Integer id) { return subir(a, id); }
    public String subirFotoPerfilCliente(MultipartFile a, Integer id) { return subir(a, id); }
    public String subirFotoPerfilAdministrador(MultipartFile a, Integer id) { return subir(a, id); }
}
