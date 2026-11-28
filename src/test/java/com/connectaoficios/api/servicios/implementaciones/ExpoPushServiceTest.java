package com.connectaoficios.api.servicios.implementaciones;
import com.connectaoficios.api.dtos.mensaje.MensajeSalida;
import com.connectaoficios.api.enums.TipoMensaje;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ExpoPushServiceTest {
 @Test void previewsPorTipo(){var m=new MensajeSalida();m.setTipo(TipoMensaje.IMAGE);assertEquals("Te envi\u00f3 una foto",ExpoPushService.preview(m));m.setTipo(TipoMensaje.DOCUMENT);assertEquals("Te envi\u00f3 un documento",ExpoPushService.preview(m));m.setTipo(TipoMensaje.STICKER);assertEquals("Te envi\u00f3 un sticker",ExpoPushService.preview(m));m.setTipo(TipoMensaje.TEXT);m.setContenido("a".repeat(120)+"\n");assertEquals(100,ExpoPushService.preview(m).length());}
}
