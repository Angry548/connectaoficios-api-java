package com.connectaoficios.api.seguridad;
import com.connectaoficios.api.modelos.*;
import com.connectaoficios.api.repositorios.*;
import com.connectaoficios.api.dtos.servicio.*;
import com.connectaoficios.api.dtos.solicitud.*;
import com.connectaoficios.api.controladores.*;
import com.connectaoficios.api.servicios.interfaces.*;
import com.connectaoficios.api.seguridad.configuracion.JwtAuthenticationConverter;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class IdentidadJwtTest {
 Jwt jwt(String sub,String id){var b=Jwt.withTokenValue("test").header("alg","HS256").subject(sub); if(id!=null)b.claim(IdentidadJwt.NAME_IDENTIFIER,id);return b.build();}
 @Test void contratoDotnet(){assertEquals(42,IdentidadJwt.usuarioId(jwt("correo-local", "42")));}
 @Test void compatibilidadSub(){assertEquals(42,IdentidadJwt.usuarioId(jwt("42",null)));}
 @Test void prioridadNameIdentifier(){assertEquals(42,IdentidadJwt.usuarioId(jwt("99","42")));}
 @Test void invalidoNoHaceFallback(){assertThrows(com.connectaoficios.api.excepciones.ReglaNegocioException.class,()->IdentidadJwt.usuarioId(jwt("42","invalido")));}
 @Test void rechazaCeroNegativosYDesbordamiento(){for(String id:List.of("0","-1","2147483648"))assertThrows(com.connectaoficios.api.excepciones.ReglaNegocioException.class,()->IdentidadJwt.usuarioId(jwt(id,null)));}
}
