package com.connectaoficios.api.servicios.implementaciones;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;
@Service
public class AlmacenChatService {
 private final ObjectProvider<Cloudinary> cloudinary;
 public AlmacenChatService(ObjectProvider<Cloudinary> cloudinary){this.cloudinary=cloudinary;}
 private Cloudinary cliente(){Cloudinary c=cloudinary.getIfAvailable();if(c==null)throw new ReglaNegocioException("Almacenamiento de chat no disponible");return c;}
 public String nuevaReferencia(){return "connectaoficios/chat/"+UUID.randomUUID();}
 public void subir(String referencia, byte[] datos){
  try { cliente().uploader().upload(datos,ObjectUtils.asMap("public_id",referencia,"resource_type","raw","type","authenticated","overwrite",false)); }
  catch(Exception e){throw new ReglaNegocioException("No se pudo almacenar el adjunto");}
 }
 public void eliminar(String referencia){try{cliente().uploader().destroy(referencia,ObjectUtils.asMap("resource_type","raw","type","authenticated","invalidate",true));}catch(Exception ignored){}}
 private String urlTemporal(String referencia){
  try{return cliente().privateDownload(referencia,"",ObjectUtils.asMap("resource_type","raw","type","authenticated","expires_at",Instant.now().plusSeconds(120).getEpochSecond(),"attachment",false));}
  catch(Exception e){throw new ReglaNegocioException("No se pudo obtener el adjunto");}
 }
 public byte[] descargar(String referencia){
  try {
   var request=java.net.http.HttpRequest.newBuilder(java.net.URI.create(urlTemporal(referencia))).timeout(java.time.Duration.ofSeconds(15)).GET().build();
   var response=java.net.http.HttpClient.newHttpClient().send(request,java.net.http.HttpResponse.BodyHandlers.ofInputStream());
   try(var body=response.body()) {
    if(response.statusCode()!=200)throw new IllegalStateException();
    byte[] bytes=body.readNBytes(5242881);if(bytes.length>5242880)throw new IllegalStateException();return bytes;
   }
  }catch(InterruptedException e){Thread.currentThread().interrupt();throw new ReglaNegocioException("No se pudo descargar el adjunto");}
   catch(Exception e){throw new ReglaNegocioException("No se pudo descargar el adjunto");}
 }
}
