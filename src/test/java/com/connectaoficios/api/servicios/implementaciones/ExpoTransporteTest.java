package com.connectaoficios.api.servicios.implementaciones;
import com.connectaoficios.api.dtos.mensaje.MensajeSalida;
import com.connectaoficios.api.enums.TipoMensaje;
import com.connectaoficios.api.modelos.DispositivoPush;
import com.connectaoficios.api.repositorios.IDispositivoPushRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class ExpoTransporteTest {
 @Test void enviaSoloDispositivosActivosDelReceptorYDesactivaUnicamenteElInvalido() throws Exception {
  var repo=mock(IDispositivoPushRepository.class);var client=mock(HttpClient.class);var tm=mock(PlatformTransactionManager.class);
  when(tm.getTransaction(any())).thenAnswer(i->new SimpleTransactionStatus());
  var primero=new DispositivoPush();primero.setId(1L);primero.setUsuarioId(20);primero.setToken("ExpoPushToken[uno]");primero.setActivo(true);
  var segundo=new DispositivoPush();segundo.setId(2L);segundo.setUsuarioId(20);segundo.setToken("ExpoPushToken[dos]");segundo.setActivo(true);
  when(repo.findByUsuarioIdAndActivoTrue(20)).thenReturn(List.of(primero,segundo),List.of(segundo));when(repo.findById(1L)).thenReturn(Optional.of(primero));
  @SuppressWarnings("unchecked") HttpResponse<String> invalido=mock(HttpResponse.class),ok=mock(HttpResponse.class);
  when(invalido.statusCode()).thenReturn(200);when(invalido.body()).thenReturn("{\"data\":{\"details\":{\"error\":\"DeviceNotRegistered\"}}}");
  when(ok.statusCode()).thenReturn(200);when(ok.body()).thenReturn("{\"data\":{\"status\":\"ok\"}}");
  when(client.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenReturn(invalido,ok,ok);
  var servicio=new ExpoPushService(repo,new ObjectMapper(),tm,client);var m=new MensajeSalida();m.setId(7L);m.setConversacionId(4L);m.setTipo(TipoMensaje.TEXT);m.setContenido("hola");
  servicio.enviar(20,m);assertFalse(primero.isActivo());assertTrue(segundo.isActivo());verify(repo).save(primero);verify(repo,never()).findByUsuarioIdAndActivoTrue(10);
  servicio.enviar(20,m);verify(client,times(3)).send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class));
 }
 @Test void falloDeTransporteEsBestEffort() throws Exception {
  var repo=mock(IDispositivoPushRepository.class);var client=mock(HttpClient.class);var tm=mock(PlatformTransactionManager.class);when(tm.getTransaction(any())).thenAnswer(i->new SimpleTransactionStatus());
  var d=new DispositivoPush();d.setToken("ExpoPushToken[uno]");when(repo.findByUsuarioIdAndActivoTrue(20)).thenReturn(List.of(d));when(client.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenThrow(new java.io.IOException("fallo"));
  var m=new MensajeSalida();m.setId(7L);m.setConversacionId(4L);m.setTipo(TipoMensaje.IMAGE);assertDoesNotThrow(()->new ExpoPushService(repo,new ObjectMapper(),tm,client).enviar(20,m));
 }
 @org.junit.jupiter.params.ParameterizedTest
 @org.junit.jupiter.params.provider.EnumSource(TipoMensaje.class)
 void payloadContieneIdentificadoresYVista(TipoMensaje tipo) throws Exception {
  var repo=mock(IDispositivoPushRepository.class);var client=mock(HttpClient.class);var tm=mock(PlatformTransactionManager.class);when(tm.getTransaction(any())).thenAnswer(x->new SimpleTransactionStatus());
  var d=new DispositivoPush();d.setToken("ExpoPushToken[prueba]");when(repo.findByUsuarioIdAndActivoTrue(20)).thenReturn(List.of(d));
  @SuppressWarnings("unchecked") HttpResponse<String> ok=mock(HttpResponse.class);when(ok.statusCode()).thenReturn(200);when(ok.body()).thenReturn("{\"data\":{\"status\":\"ok\"}}");when(client.send(any(HttpRequest.class),any(HttpResponse.BodyHandler.class))).thenReturn(ok);
  var m=new MensajeSalida();m.setId(7L);m.setConversacionId(4L);m.setTipo(tipo);m.setContenido("hola");new ExpoPushService(repo,new ObjectMapper(),tm,client).enviar(20,m);
  var captor=org.mockito.ArgumentCaptor.forClass(HttpRequest.class);verify(client).send(captor.capture(),any(HttpResponse.BodyHandler.class));var bytes=new java.io.ByteArrayOutputStream();var fin=new java.util.concurrent.CountDownLatch(1);
  captor.getValue().bodyPublisher().orElseThrow().subscribe(new java.util.concurrent.Flow.Subscriber<java.nio.ByteBuffer>() {
   public void onSubscribe(java.util.concurrent.Flow.Subscription sub){sub.request(Long.MAX_VALUE);}public void onNext(java.nio.ByteBuffer b){byte[] chunk=new byte[b.remaining()];b.get(chunk);bytes.writeBytes(chunk);}public void onError(Throwable t){fin.countDown();}public void onComplete(){fin.countDown();}
  });assertTrue(fin.await(3,java.util.concurrent.TimeUnit.SECONDS));var payload=new ObjectMapper().readTree(bytes.toByteArray());assertEquals("CHAT_MESSAGE",payload.path("data").path("type").asText());assertEquals(4,payload.path("data").path("conversacionId").asInt());assertEquals(7,payload.path("data").path("mensajeId").asInt());assertEquals(ExpoPushService.preview(m),payload.path("body").asText());
 }
}
