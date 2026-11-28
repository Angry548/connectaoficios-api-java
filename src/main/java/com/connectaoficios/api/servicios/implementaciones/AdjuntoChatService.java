package com.connectaoficios.api.servicios.implementaciones;
import com.connectaoficios.api.dtos.mensaje.*;
import com.connectaoficios.api.enums.TipoMensaje;
import com.connectaoficios.api.excepciones.*;
import com.connectaoficios.api.modelos.*;
import com.connectaoficios.api.repositorios.*;
import com.connectaoficios.api.servicios.interfaces.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
@Service
public class AdjuntoChatService {
 private final IConversacionRepository conversaciones; private final IMensajeRepository mensajes;
 private final IAdjuntoMensajeRepository adjuntos; private final ValidadorAdjunto validador;
 private final AlmacenChatService almacen; private final IChatTiempoRealService broker;
 private final ExpoPushService push; private final INotificacionService notificaciones;
 public AdjuntoChatService(IConversacionRepository conversaciones,IMensajeRepository mensajes,IAdjuntoMensajeRepository adjuntos,ValidadorAdjunto validador,AlmacenChatService almacen,IChatTiempoRealService broker,ExpoPushService push,INotificacionService notificaciones){this.conversaciones=conversaciones;this.mensajes=mensajes;this.adjuntos=adjuntos;this.validador=validador;this.almacen=almacen;this.broker=broker;this.push=push;this.notificaciones=notificaciones;}
 private void autorizar(Conversacion c,Integer id){if(id==null||(!id.equals(c.getClienteId())&&!id.equals(c.getTrabajadorId())))throw new ReglaNegocioException("El usuario no pertenece a esta conversacion");}
 @Transactional public MensajeSalida enviar(Long conversacionId,Integer usuario,TipoMensaje tipo,MultipartFile archivo){
  Conversacion c=conversaciones.findById(conversacionId).orElseThrow(()->new RecursoNoEncontradoException("Conversacion no encontrada"));
  autorizar(c,usuario);byte[] contenido=validador.validar(archivo,tipo);
  String ref=almacen.nuevaReferencia();
  // Registrar compensacion antes de subir, incluso si el proveedor falla parcialmente.
  TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCompletion(int estado){if(estado!=STATUS_COMMITTED)almacen.eliminar(ref);}});
  almacen.subir(ref,contenido);
  Mensaje m=new Mensaje();m.setConversacion(c);m.setRemitenteId(usuario);m.setTipo(tipo);m.setContenido("");m.setLeido(false);
  m=mensajes.save(m);
  AdjuntoMensaje a=new AdjuntoMensaje();a.setMensaje(m);a.setNombreOriginal(archivo.getOriginalFilename());a.setMimeType(archivo.getContentType());a.setTamanoBytes(contenido.length);a.setTipoAdjunto(tipo);a.setReferencia(ref);a=adjuntos.save(a);
  MensajeSalida dto=salida(m,a);Integer receptor=usuario.equals(c.getClienteId())?c.getTrabajadorId():c.getClienteId();
  var aviso=new com.connectaoficios.api.dtos.notificacion.NotificacionGuardar();aviso.setUsuarioDestinoId(receptor);aviso.setTipo("NUEVO_MENSAJE");aviso.setTitulo("Nuevo mensaje");aviso.setMensaje("Tienes un nuevo mensaje");aviso.setReferenciaId(c.getId());notificaciones.guardar(aviso);
  TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCommit(){try{broker.publicarMensaje(dto);}catch(RuntimeException ignored){}try{push.enviar(receptor,dto);}catch(RuntimeException ignored){}}});
  return dto;
 }
 @Transactional public MensajeSalida enviarSticker(Long conversacionId,Integer usuario,String codigo){
  if(!"feliz".equals(codigo))throw new ReglaNegocioException("Sticker no disponible");
  Conversacion c=conversaciones.findById(conversacionId).orElseThrow(()->new RecursoNoEncontradoException("Conversacion no encontrada"));autorizar(c,usuario);
  byte[] datos=sticker();
  Mensaje m=new Mensaje();m.setConversacion(c);m.setRemitenteId(usuario);m.setTipo(TipoMensaje.STICKER);m.setContenido("");m.setLeido(false);m=mensajes.save(m);
  AdjuntoMensaje a=new AdjuntoMensaje();a.setMensaje(m);a.setNombreOriginal("feliz.png");a.setMimeType("image/png");a.setTamanoBytes(datos.length);a.setTipoAdjunto(TipoMensaje.STICKER);a.setReferencia("catalog:feliz");a=adjuntos.save(a);
  MensajeSalida dto=salida(m,a);Integer receptor=usuario.equals(c.getClienteId())?c.getTrabajadorId():c.getClienteId();
  var aviso=new com.connectaoficios.api.dtos.notificacion.NotificacionGuardar();aviso.setUsuarioDestinoId(receptor);aviso.setTipo("NUEVO_MENSAJE");aviso.setTitulo("Nuevo mensaje");aviso.setMensaje("Tienes un nuevo mensaje");aviso.setReferenciaId(c.getId());notificaciones.guardar(aviso);
  TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCommit(){try{broker.publicarMensaje(dto);}catch(RuntimeException ignored){}try{push.enviar(receptor,dto);}catch(RuntimeException ignored){}}});return dto;
 }
 public static byte[] sticker(){
  try {var img=new java.awt.image.BufferedImage(96,96,java.awt.image.BufferedImage.TYPE_INT_ARGB);var g=img.createGraphics();
   g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,java.awt.RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(new java.awt.Color(255,206,51));g.fillOval(4,4,88,88);g.setColor(new java.awt.Color(75,45,20));g.fillOval(28,30,9,13);g.fillOval(59,30,9,13);g.setStroke(new java.awt.BasicStroke(5));g.drawArc(29,41,38,27,195,150);g.dispose();var out=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(img,"png",out);return out.toByteArray();
  }catch(java.io.IOException e){throw new IllegalStateException(e);}
 }
 private MensajeSalida salida(Mensaje m,AdjuntoMensaje a){MensajeSalida dto=new MensajeSalida();dto.setId(m.getId());dto.setConversacionId(m.getConversacion().getId());dto.setRemitenteId(m.getRemitenteId());dto.setContenido(m.getContenido());dto.setTipo(m.getTipo());dto.setLeido(m.getLeido());dto.setFechaEnvio(m.getFechaEnvio());dto.setAdjuntos(List.of(dto(a)));return dto;}
 public static AdjuntoSalida dto(AdjuntoMensaje a){return new AdjuntoSalida(a.getId(),a.getTipoAdjunto(),a.getNombreOriginal(),a.getMimeType(),a.getTamanoBytes(),"/api/adjuntos/"+a.getId()+"/contenido");}
 public record ArchivoDescargado(String mimeType, byte[] bytes){}
 @Transactional(readOnly=true) public ArchivoDescargado acceso(Long id,Integer usuario){AdjuntoMensaje a=adjuntos.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Adjunto no encontrado"));autorizar(a.getMensaje().getConversacion(),usuario);return new ArchivoDescargado(a.getMimeType(),a.getReferencia().equals("catalog:feliz") ? sticker() : almacen.descargar(a.getReferencia()));}
}
