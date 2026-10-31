package com.connectaoficios.api.servicios.implementaciones;

import com.cloudinary.Cloudinary;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ImagenServiceTest {
    @Test void mantieneContratoTrabajadorYSeparaCliente() throws Exception {
        Cloudinary cloudinary = mock(Cloudinary.class, RETURNS_DEEP_STUBS);
        when(cloudinary.uploader().upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", "https://foto.png"));
        var service = new ImagenService(cloudinary);
        var archivo = new MockMultipartFile("archivo", "foto.png", "image/png", new byte[]{1});
        assertEquals("https://foto.png", service.subirFotoPerfil(archivo, 10));
        assertEquals("https://foto.png", service.subirFotoPerfilCliente(archivo, 10));
        assertEquals("https://foto.png", service.subirFotoPerfilAdministrador(archivo, 10));
        verify(cloudinary.uploader()).upload(any(byte[].class), argThat(m ->
                "trabajador_10".equals(m.get("public_id"))
                        && Boolean.TRUE.equals(m.get("overwrite"))
                        && Boolean.TRUE.equals(m.get("invalidate"))));
        verify(cloudinary.uploader()).upload(any(byte[].class), argThat(m ->
                "cliente_10".equals(m.get("public_id"))
                        && Boolean.TRUE.equals(m.get("overwrite"))
                        && Boolean.TRUE.equals(m.get("invalidate"))));
        verify(cloudinary.uploader()).upload(any(byte[].class), argThat(m ->
                "administrador_10".equals(m.get("public_id"))
                        && Boolean.TRUE.equals(m.get("overwrite"))
                        && Boolean.TRUE.equals(m.get("invalidate"))));
    }

    @Test void validaTamanioYFormatoParaCliente() {
        var service = new ImagenService(mock(Cloudinary.class));
        assertThrows(ReglaNegocioException.class, () -> service.subirFotoPerfilCliente(
                new MockMultipartFile("archivo", "foto.gif", "image/gif", new byte[]{1}), 10));
        assertThrows(ReglaNegocioException.class, () -> service.subirFotoPerfilCliente(
                new MockMultipartFile("archivo", "foto.png", "image/png", new byte[5 * 1024 * 1024 + 1]), 10));
    }
}
