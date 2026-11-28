package com.connectaoficios.api.servicios.implementaciones;
import com.connectaoficios.api.modelos.DispositivoPush;
import com.connectaoficios.api.repositorios.IDispositivoPushRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
@Service
public class DispositivoPushService {
 private final IDispositivoPushRepository repo;
 public DispositivoPushService(IDispositivoPushRepository repo){this.repo=repo;}
 @Transactional public Long registrar(Integer usuario,String token,String plataforma){
  if(token==null || !token.matches("(?:ExponentPushToken|ExpoPushToken)\\[[A-Za-z0-9_-]{1,200}\\]")) throw new IllegalArgumentException("Token Expo invalido");
  String p=plataforma==null?"":plataforma.toUpperCase(Locale.ROOT);
  if(!p.equals("ANDROID")&&!p.equals("IOS")) throw new IllegalArgumentException("Plataforma invalida");
  DispositivoPush d=repo.findByToken(token).orElseGet(DispositivoPush::new);
  if(d.getUsuarioId()!=null&&!d.getUsuarioId().equals(usuario)) throw new AccessDeniedException("Dispositivo ajeno");
  d.setUsuarioId(usuario);d.setToken(token);d.setPlataforma(p);d.setActivo(true);
  return repo.save(d).getId();
 }
 @Transactional public void desregistrar(Integer usuario,Long id){
  DispositivoPush d=repo.findById(id).orElseThrow(() -> new com.connectaoficios.api.excepciones.RecursoNoEncontradoException("Dispositivo no encontrado"));
  if(!usuario.equals(d.getUsuarioId())) throw new AccessDeniedException("Dispositivo ajeno");
  d.setActivo(false);repo.save(d);
 }
}
