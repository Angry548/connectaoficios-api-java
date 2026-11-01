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
 @Test void archivoVacio(){var s=new ImagenService(mock(Cloudinary.class)); assertThrows(ReglaNegocioException.class,()->s.subirFotoPerfil(new MockMultipartFile("archivo",new byte[0]),1));}
 @Test void sobreLimite(){var s=new ImagenService(mock(Cloudinary.class)); assertThrows(ReglaNegocioException.class,()->s.subirFotoPerfil(new MockMultipartFile("archivo","x.png","image/png",new byte[5*1024*1024+1]),1));}
 @Test void mimeInvalido(){var s=new ImagenService(mock(Cloudinary.class)); assertThrows(ReglaNegocioException.class,()->s.subirFotoPerfil(new MockMultipartFile("archivo","x.txt","text/plain",new byte[]{1}),1));}
 @Test void proveedorFalla() throws Exception {var c=mock(Cloudinary.class,RETURNS_DEEP_STUBS); when(c.uploader().upload(any(byte[].class),anyMap())).thenThrow(new java.io.IOException("fallo")); var s=new ImagenService(c); assertThrows(ReglaNegocioException.class,()->s.subirFotoPerfil(new MockMultipartFile("archivo","x.png","image/png",new byte[]{1}),1));}
 @Test void exactamenteCincoMbPermitidos() throws Exception {var c=mock(Cloudinary.class,RETURNS_DEEP_STUBS); when(c.uploader().upload(any(byte[].class),anyMap())).thenReturn(Map.of("secure_url","https://example.invalid/photo.png")); var s=new ImagenService(c); assertNotNull(s.subirFotoPerfil(new MockMultipartFile("archivo","x.png","image/png",new byte[5*1024*1024]),1));}

}
