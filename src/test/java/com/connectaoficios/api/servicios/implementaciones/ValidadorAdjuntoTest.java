package com.connectaoficios.api.servicios.implementaciones;
import com.connectaoficios.api.enums.TipoMensaje;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.*;
class ValidadorAdjuntoTest {
 private final ValidadorAdjunto v=new ValidadorAdjunto(5*1024*1024);
 @Test void validaPdf(){assertNotNull(v.validar(new MockMultipartFile("archivo","doc.pdf","application/pdf","%PDF-12345678".getBytes()),TipoMensaje.DOCUMENT));}
 @Test void validaPng(){byte[] b={(byte)137,80,78,71,13,10,26,10,0,0,0,0};assertEquals(12,v.validar(new MockMultipartFile("archivo","x.png","image/png",b),TipoMensaje.IMAGE).length);}
 @Test void rechazaExtensionMimeContenidoYTamanio(){
  assertThrows(RuntimeException.class,()->v.validar(new MockMultipartFile("a","x.exe","application/pdf","%PDF-12345678".getBytes()),TipoMensaje.DOCUMENT));
  assertThrows(RuntimeException.class,()->v.validar(new MockMultipartFile("a","x.pdf","image/png","%PDF-12345678".getBytes()),TipoMensaje.DOCUMENT));
  assertThrows(RuntimeException.class,()->v.validar(new MockMultipartFile("a","x.pdf","application/pdf","no es un pdf".getBytes()),TipoMensaje.DOCUMENT));
  assertThrows(RuntimeException.class,()->v.validar(new MockMultipartFile("a","x.pdf","application/pdf",new byte[5*1024*1024+1]),TipoMensaje.DOCUMENT));
 }
 @Test void aceptaJpgYJpeg(){byte[] jpg={(byte)255,(byte)216,(byte)255,0,0,0,0,0,0,0,0,0};for(String nombre: new String[]{"a.jpg","b.jpeg"}) assertEquals(12,v.validar(new MockMultipartFile("archivo",nombre,"image/jpeg",jpg),TipoMensaje.IMAGE).length);}
 @Test void aceptaWebp(){byte[] webp={82,73,70,70,0,0,0,0,87,69,66,80};assertEquals(12,v.validar(new MockMultipartFile("archivo","a.webp","image/webp",webp),TipoMensaje.IMAGE).length);}
 @Test void rechazaArchivoVacio(){assertThrows(RuntimeException.class,()->v.validar(new MockMultipartFile("archivo","a.pdf","application/pdf",new byte[0]),TipoMensaje.DOCUMENT));}
}
