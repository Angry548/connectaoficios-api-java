package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.mensaje.MensajeSalida;
import com.connectaoficios.api.repositorios.IDispositivoPushRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ExpoPushService {
 private final IDispositivoPushRepository repo;
 private final ObjectMapper json;
 private final TransactionTemplate tx;
 private final HttpClient client;
 @org.springframework.beans.factory.annotation.Autowired
 public ExpoPushService(IDispositivoPushRepository repo,ObjectMapper json,org.springframework.transaction.PlatformTransactionManager tm){this(repo,json,tm,HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build());}
 ExpoPushService(IDispositivoPushRepository repo,ObjectMapper json,org.springframework.transaction.PlatformTransactionManager tm,HttpClient client){this.repo=repo;this.json=json;this.client=client;this.tx=new TransactionTemplate(tm);this.tx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);}
 public void enviar(Integer receptor, MensajeSalida mensaje){
  var dispositivos=tx.execute(status -> repo.findByUsuarioIdAndActivoTrue(receptor));
  if(dispositivos==null)return;
  for(var d: dispositivos){
   try {
    var payload=Map.of("to",d.getToken(),"title","Nuevo mensaje","body",preview(mensaje),"data",Map.of("type","CHAT_MESSAGE","conversacionId",mensaje.getConversacionId(),"mensajeId",mensaje.getId()));
    var request=HttpRequest.newBuilder(URI.create("https://exp.host/--/api/v2/push/send")).timeout(Duration.ofSeconds(8)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload))).build();
    var response=client.send(request,HttpResponse.BodyHandlers.ofString());
    if(response.statusCode()>=200&&response.statusCode()<300 && "DeviceNotRegistered".equals(json.readTree(response.body()).path("data").path("details").path("error").asText())) {
     tx.executeWithoutResult(status -> repo.findById(d.getId()).ifPresent(actual -> {actual.setActivo(false);repo.save(actual);}));
    }
   } catch (InterruptedException ex) { Thread.currentThread().interrupt(); return; }
     catch (Exception ex) { /* La entrega es best-effort; nunca registrar token o respuesta. */ }
  }
 }
 static String preview(MensajeSalida m){
  if(m.getTipo()==null)return "Nuevo mensaje";
  return switch(m.getTipo()) {
   case IMAGE -> "Te envi\u00f3 una foto"; case DOCUMENT -> "Te envi\u00f3 un documento"; case STICKER -> "Te envi\u00f3 un sticker";
   case TEXT -> {String s=m.getContenido()==null?"":m.getContenido().replaceAll("[\\p{Cntrl}]"," ").trim();yield s.length()>100?s.substring(0,100):s;}
  };
 }
}
