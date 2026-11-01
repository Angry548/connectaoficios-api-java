package com.connectaoficios.api.servicios.implementaciones;
import com.connectaoficios.api.modelos.*;
import com.connectaoficios.api.repositorios.*;
import com.connectaoficios.api.dtos.perfil.*;
import com.connectaoficios.api.dtos.notificacion.*;
import com.connectaoficios.api.dtos.servicio.*;
import com.connectaoficios.api.enums.*;
import com.connectaoficios.api.excepciones.*;
import org.junit.jupiter.api.*;
import java.util.*;
import org.springframework.data.domain.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class ChatTiempoRealServiceTest {

 @Test void publicaSoloEnConversacionIndicada(){var broker=mock(org.springframework.messaging.simp.SimpMessagingTemplate.class); var service=new ChatTiempoRealService(broker); var dto=new com.connectaoficios.api.dtos.mensaje.MensajeSalida(); dto.setConversacionId(42L); service.publicarMensaje(dto); verify(broker).convertAndSend("/topic/conversaciones/42",dto); verifyNoMoreInteractions(broker);}
 @Test void aislaDestinos(){var broker=mock(org.springframework.messaging.simp.SimpMessagingTemplate.class); var service=new ChatTiempoRealService(broker); var a=new com.connectaoficios.api.dtos.mensaje.MensajeSalida(); a.setConversacionId(1L); var b=new com.connectaoficios.api.dtos.mensaje.MensajeSalida(); b.setConversacionId(2L); service.publicarMensaje(a); service.publicarMensaje(b); verify(broker).convertAndSend("/topic/conversaciones/1",a); verify(broker).convertAndSend("/topic/conversaciones/2",b); verifyNoMoreInteractions(broker);}

}
