package com.connectaoficios.api.servicios.implementaciones;
import com.connectaoficios.api.enums.TipoMensaje;
import com.connectaoficios.api.modelos.*;
import com.connectaoficios.api.repositorios.*;
import com.connectaoficios.api.servicios.interfaces.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class AdjuntoChatServiceTest {
 IConversacionRepository conversaciones=mock(IConversacionRepository.class); IMensajeRepository mensajes=mock(IMensajeRepository.class);
 IAdjuntoMensajeRepository adjuntos=mock(IAdjuntoMensajeRepository.class); AlmacenChatService almacen=mock(AlmacenChatService.class);
 IChatTiempoRealService broker=mock(IChatTiempoRealService.class); ExpoPushService push=mock(ExpoPushService.class); INotificacionService avisos=mock(INotificacionService.class);
 AdjuntoChatService service=new AdjuntoChatService(conversaciones,mensajes,adjuntos,new ValidadorAdjunto(5242880),almacen,broker,push,avisos);
 Conversacion c; MockMultipartFile archivo=new MockMultipartFile("archivo","a.pdf","application/pdf","%PDF-123456789".getBytes());
 @BeforeEach void init(){c=new Conversacion();c.setId(1L);c.setClienteId(10);c.setTrabajadorId(20);when(conversaciones.findById(1L)).thenReturn(Optional.of(c));}
 @Test void ajenoRechazado(){assertThrows(RuntimeException.class,()->service.enviar(1L,30,TipoMensaje.DOCUMENT,archivo));verifyNoInteractions(almacen,mensajes,adjuntos,broker,push);}
 @Test void participanteYRollback(){
  when(almacen.nuevaReferencia()).thenReturn("ref");when(mensajes.save(any())).thenAnswer(i->{Mensaje m=i.getArgument(0);m.setId(4L);return m;});when(adjuntos.save(any())).thenAnswer(i->{AdjuntoMensaje a=i.getArgument(0);a.setId(7L);return a;});
  TransactionSynchronizationManager.initSynchronization();try {var dto=service.enviar(1L,10,TipoMensaje.DOCUMENT,archivo);assertEquals(TipoMensaje.DOCUMENT,dto.getTipo());assertEquals(7L,dto.getAdjuntos().getFirst().id());verifyNoInteractions(broker,push);
   TransactionSynchronizationManager.getSynchronizations().forEach(s->s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));verify(almacen).eliminar("ref");verifyNoInteractions(broker,push);
  }finally{TransactionSynchronizationManager.clearSynchronization();}
 }
 @Test void almacenamientoFallidoNoPersiste(){when(almacen.nuevaReferencia()).thenReturn("ref");doThrow(new IllegalStateException()).when(almacen).subir(anyString(),any());TransactionSynchronizationManager.initSynchronization();try{assertThrows(RuntimeException.class,()->service.enviar(1L,10,TipoMensaje.DOCUMENT,archivo));verifyNoInteractions(mensajes,adjuntos,broker,push);TransactionSynchronizationManager.getSynchronizations().forEach(s->s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));verify(almacen).eliminar("ref");}finally{TransactionSynchronizationManager.clearSynchronization();}}
 @Test void lecturaDeAjenoRechazada(){var m=new Mensaje();m.setConversacion(c);var a=new AdjuntoMensaje();a.setMensaje(m);when(adjuntos.findById(1L)).thenReturn(Optional.of(a));assertThrows(RuntimeException.class,()->service.acceso(1L,30));verifyNoInteractions(almacen);}
 @Test void commitPublicaUnaVezYNotificaSoloReceptor(){
  when(almacen.nuevaReferencia()).thenReturn("ref");when(mensajes.save(any())).thenAnswer(i->{Mensaje m=i.getArgument(0);m.setId(4L);return m;});when(adjuntos.save(any())).thenAnswer(i->{AdjuntoMensaje a=i.getArgument(0);a.setId(7L);return a;});
  TransactionSynchronizationManager.initSynchronization();try{var dto=service.enviar(1L,10,TipoMensaje.DOCUMENT,archivo);TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);verify(broker,times(1)).publicarMensaje(dto);verify(push,times(1)).enviar(20,dto);verify(push,never()).enviar(eq(10),any());}finally{TransactionSynchronizationManager.clearSynchronization();}
 }
 @Test void stickerControladoSeGuardaComoSticker(){
  when(mensajes.save(any())).thenAnswer(i->{Mensaje m=i.getArgument(0);m.setId(4L);return m;});when(adjuntos.save(any())).thenAnswer(i->{AdjuntoMensaje a=i.getArgument(0);a.setId(7L);return a;});
  TransactionSynchronizationManager.initSynchronization();try{var dto=service.enviarSticker(1L,10,"feliz");assertEquals(TipoMensaje.STICKER,dto.getTipo());assertEquals("image/png",dto.getAdjuntos().getFirst().mimeType());assertThrows(RuntimeException.class,()->service.enviarSticker(1L,10,"https://ajeno"));}finally{TransactionSynchronizationManager.clearSynchronization();}
 }
 @Test void stickerDeConversacionAjenaRechazado(){assertThrows(RuntimeException.class,()->service.enviarSticker(1L,30,"feliz"));verifyNoInteractions(mensajes,adjuntos,broker,push);}
 @Test void falloPushPosteriorAlCommitNoPropagaError(){
  when(almacen.nuevaReferencia()).thenReturn("ref");when(mensajes.save(any())).thenAnswer(i->{Mensaje m=i.getArgument(0);m.setId(4L);return m;});when(adjuntos.save(any())).thenAnswer(i->{AdjuntoMensaje a=i.getArgument(0);a.setId(7L);return a;});doThrow(new IllegalStateException()).when(push).enviar(eq(20),any());
  TransactionSynchronizationManager.initSynchronization();try{service.enviar(1L,10,TipoMensaje.DOCUMENT,archivo);assertDoesNotThrow(()->TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit));verify(mensajes).save(any());verify(adjuntos).save(argThat(a->a.getMensaje().getConversacion().getId().equals(1L)));}finally{TransactionSynchronizationManager.clearSynchronization();}
 }
 @Test void falloDePersistenciaCompensaAlRollback(){when(almacen.nuevaReferencia()).thenReturn("ref");when(mensajes.save(any())).thenThrow(new IllegalStateException("fallo"));TransactionSynchronizationManager.initSynchronization();try{assertThrows(IllegalStateException.class,()->service.enviar(1L,10,TipoMensaje.DOCUMENT,archivo));TransactionSynchronizationManager.getSynchronizations().forEach(x->x.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));verify(almacen).eliminar("ref");verifyNoInteractions(broker,push);}finally{TransactionSynchronizationManager.clearSynchronization();}}
}
