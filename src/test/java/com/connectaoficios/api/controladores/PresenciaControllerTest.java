package com.connectaoficios.api.controladores;
import com.connectaoficios.api.modelos.Conversacion;
import com.connectaoficios.api.repositorios.IConversacionRepository;
import com.connectaoficios.api.servicios.implementaciones.PresenciaService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PresenciaControllerTest {
 @Test void soloParticipanteConsultaOtro(){var r=mock(IConversacionRepository.class);var p=mock(PresenciaService.class);var c=new Conversacion();c.setClienteId(10);c.setTrabajadorId(20);when(r.findById(1L)).thenReturn(Optional.of(c));var controller=new PresenciaController(r,p);var jwt=Jwt.withTokenValue("x").header("alg","HS256").subject("10").build();when(p.online(20)).thenReturn(true);assertEquals(20,controller.obtener(1L,jwt).get("usuarioId"));assertEquals(true,controller.obtener(1L,jwt).get("online"));}
 @Test void ajenoDenegado(){var r=mock(IConversacionRepository.class);var p=mock(PresenciaService.class);var c=new Conversacion();c.setClienteId(10);c.setTrabajadorId(20);when(r.findById(1L)).thenReturn(Optional.of(c));var jwt=Jwt.withTokenValue("x").header("alg","HS256").subject("30").build();assertThrows(org.springframework.security.access.AccessDeniedException.class,()->new PresenciaController(r,p).obtener(1L,jwt));verifyNoInteractions(p);}
}
