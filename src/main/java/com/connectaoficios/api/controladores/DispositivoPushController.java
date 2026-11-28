package com.connectaoficios.api.controladores;
import com.connectaoficios.api.seguridad.IdentidadJwt;
import com.connectaoficios.api.servicios.implementaciones.DispositivoPushService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/dispositivos-push")
@PreAuthorize("hasAnyRole('CLIENTE','TRABAJADOR')")
public class DispositivoPushController {
 private final DispositivoPushService servicio;
 public DispositivoPushController(DispositivoPushService servicio){this.servicio=servicio;}
 public record Registro(@NotBlank String token,@NotBlank String plataforma){}
 @PostMapping public Map<String,Long> registrar(@Valid @RequestBody Registro entrada,@AuthenticationPrincipal Jwt jwt){
  return Map.of("id",servicio.registrar(IdentidadJwt.usuarioId(jwt),entrada.token(),entrada.plataforma()));
 }
 @DeleteMapping("/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
 public void eliminar(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt){servicio.desregistrar(IdentidadJwt.usuarioId(jwt),id);}
}
