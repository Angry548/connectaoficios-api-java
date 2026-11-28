package com.connectaoficios.api.controladores;
import com.connectaoficios.api.dtos.mensaje.MensajeSalida;
import com.connectaoficios.api.enums.TipoMensaje;
import com.connectaoficios.api.seguridad.IdentidadJwt;
import com.connectaoficios.api.servicios.implementaciones.AdjuntoChatService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
@RestController @PreAuthorize("hasAnyRole('CLIENTE','TRABAJADOR')")
public class AdjuntoChatController {
 private final AdjuntoChatService servicio;
 public AdjuntoChatController(AdjuntoChatService servicio){this.servicio=servicio;}
 @PostMapping(value="/api/mensajes/adjunto",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public ResponseEntity<MensajeSalida> enviar(@RequestParam Long conversacionId,@RequestParam TipoMensaje tipo,@RequestPart MultipartFile archivo,@AuthenticationPrincipal Jwt jwt){return ResponseEntity.status(HttpStatus.CREATED).body(servicio.enviar(conversacionId,IdentidadJwt.usuarioId(jwt),tipo,archivo));}
 public record StickerRequest(Long conversacionId,String codigo){}
 @GetMapping("/api/stickers")
 public java.util.List<java.util.Map<String,String>> catalogo(){return java.util.List.of(java.util.Map.of("codigo","feliz","nombre","Feliz"));}
 @PostMapping("/api/mensajes/sticker")
 public ResponseEntity<MensajeSalida> sticker(@RequestBody StickerRequest entrada,@AuthenticationPrincipal Jwt jwt){return ResponseEntity.status(HttpStatus.CREATED).body(servicio.enviarSticker(entrada.conversacionId(),IdentidadJwt.usuarioId(jwt),entrada.codigo()));}
 @GetMapping("/api/adjuntos/{id}/contenido")
 public ResponseEntity<byte[]> contenido(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt){var archivo=servicio.acceso(id,IdentidadJwt.usuarioId(jwt));return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.parseMediaType(archivo.mimeType())).header("X-Content-Type-Options","nosniff").body(archivo.bytes());}
}
