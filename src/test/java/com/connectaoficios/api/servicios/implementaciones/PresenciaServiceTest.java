package com.connectaoficios.api.servicios.implementaciones;
import com.connectaoficios.api.repositorios.IConversacionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class PresenciaServiceTest {
 @Test void sesionesMultiplesYEventos(){var repo=mock(IConversacionRepository.class);var broker=mock(SimpMessagingTemplate.class);when(repo.idsDelParticipante(1)).thenReturn(List.of(9L));var p=new PresenciaService(repo,broker);
  p.conectar(1,"a");assertTrue(p.online(1));p.conectar(1,"b");p.conectar(2,"c");assertTrue(p.online(2));p.desconectar("a");assertTrue(p.online(1));p.desconectar("b");assertFalse(p.online(1));assertTrue(p.online(2));
  verify(broker,times(1)).convertAndSend("/topic/conversaciones/9/presencia",new PresenciaService.EventoPresencia("PRESENCIA",1,true));
  verify(broker,times(1)).convertAndSend("/topic/conversaciones/9/presencia",new PresenciaService.EventoPresencia("PRESENCIA",1,false));
 }
 @Test void eventosDuplicadosNoAlteranContador(){var repo=mock(IConversacionRepository.class);var broker=mock(SimpMessagingTemplate.class);when(repo.idsDelParticipante(1)).thenReturn(List.of(9L));var p=new PresenciaService(repo,broker);p.conectar(1,"a");p.conectar(1,"a");p.conectar(1,"b");p.desconectar("a");p.desconectar("a");assertTrue(p.online(1));verify(broker,never()).convertAndSend("/topic/conversaciones/9/presencia",new PresenciaService.EventoPresencia("PRESENCIA",1,false));p.desconectar("b");assertFalse(p.online(1));}
}
