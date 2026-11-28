package com.connectaoficios.api.servicios.implementaciones;
import com.connectaoficios.api.enums.TipoMensaje;
import com.connectaoficios.api.excepciones.ReglaNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Locale;
@Component
public class ValidadorAdjunto {
 private final long maximo;
 public ValidadorAdjunto(@Value("${app.chat.max-archivo-bytes:5242880}") long maximo){this.maximo=maximo;}
 public byte[] validar(MultipartFile archivo,TipoMensaje tipo){
  if(archivo==null||archivo.isEmpty()||archivo.getSize()>maximo)throw new ReglaNegocioException("Archivo vacio o superior al limite");
  if(tipo!=TipoMensaje.IMAGE&&tipo!=TipoMensaje.DOCUMENT)throw new ReglaNegocioException("Tipo de adjunto no permitido");
  String nombre=archivo.getOriginalFilename();
  if(nombre==null||nombre.contains("/")||nombre.contains("\\")||nombre.length()>255)throw new ReglaNegocioException("Nombre de archivo invalido");
  String n=nombre.toLowerCase(Locale.ROOT);String mime=archivo.getContentType();
  boolean pdf=tipo==TipoMensaje.DOCUMENT&&n.endsWith(".pdf")&&"application/pdf".equals(mime);
  boolean jpg=tipo==TipoMensaje.IMAGE&&(n.endsWith(".jpg")||n.endsWith(".jpeg"))&&"image/jpeg".equals(mime);
  boolean png=tipo==TipoMensaje.IMAGE&&n.endsWith(".png")&&"image/png".equals(mime);
  boolean webp=tipo==TipoMensaje.IMAGE&&n.endsWith(".webp")&&"image/webp".equals(mime);
  if(!(pdf||jpg||png||webp))throw new ReglaNegocioException("Extension o MIME no permitido");
  try {
   byte[] b=archivo.getBytes();
   if(b.length>maximo||b.length<12)throw new ReglaNegocioException("Archivo invalido");
   boolean ok=pdf&&b[0]==37&&b[1]==80&&b[2]==68&&b[3]==70&&b[4]==45
     ||jpg&&(b[0]&255)==255&&(b[1]&255)==216&&(b[2]&255)==255
     ||png&&(b[0]&255)==137&&b[1]==80&&b[2]==78&&b[3]==71&&b[4]==13&&b[5]==10&&b[6]==26&&b[7]==10
     ||webp&&b[0]==82&&b[1]==73&&b[2]==70&&b[3]==70&&b[8]==87&&b[9]==69&&b[10]==66&&b[11]==80;
   if(!ok)throw new ReglaNegocioException("El contenido no coincide con el tipo declarado");return b;
  }catch(IOException e){throw new ReglaNegocioException("No se pudo leer el archivo");}
 }
}
