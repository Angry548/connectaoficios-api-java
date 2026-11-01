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
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
class StompSeguridadTest {
 final JwtDecoder decoder=mock(JwtDecoder.class); final AccesoJava acceso=mock(AccesoJava.class);
 final StompSeguridad interceptor=new StompSeguridad(decoder,new JwtAuthenticationConverter(),acceso);
 final Jwt jwt=Jwt.withTokenValue("test").header("alg","HS256").subject("10").claim("role","Trabajador").build();
 org.springframework.security.core.Authentication auth(){return new JwtAuthenticationConverter().convert(jwt);}
 Message<?> frame(StompCommand c,String destino,boolean usuario){var h=StompHeaderAccessor.create(c);h.setSessionId("local");h.setSubscriptionId("1");if(destino!=null)h.setDestination(destino);if(usuario)h.setUser(auth());h.setLeaveMutable(true);return MessageBuilder.createMessage(new byte[0],h.getMessageHeaders());}
 @Test void conectarConJwt(){when(decoder.decode("test")).thenReturn(jwt);var h=StompHeaderAccessor.create(StompCommand.CONNECT);h.setNativeHeader("Authorization","Bearer test");h.setLeaveMutable(true);var msg=MessageBuilder.createMessage(new byte[0],h.getMessageHeaders());interceptor.preSend(msg,null);assertNotNull(h.getUser());}
 @Test void conectarSinJwt(){assertThrows(AccessDeniedException.class,()->interceptor.preSend(frame(StompCommand.CONNECT,null,false),null));}
 @Test void tokenInvalido(){when(decoder.decode("bad")).thenThrow(new BadJwtException("invalid"));var h=StompHeaderAccessor.create(StompCommand.CONNECT);h.setNativeHeader("Authorization","Bearer bad");h.setLeaveMutable(true);assertThrows(BadJwtException.class,()->interceptor.preSend(MessageBuilder.createMessage(new byte[0],h.getMessageHeaders()),null));}
 @Test void participantePuedeSuscribirse(){when(acceso.conversacion(any(),eq(1L))).thenReturn(true);assertNotNull(interceptor.preSend(frame(StompCommand.SUBSCRIBE,"/topic/conversaciones/1",true),null));}
 @Test void ajenoNoPuedeSuscribirse(){assertThrows(AccessDeniedException.class,()->interceptor.preSend(frame(StompCommand.SUBSCRIBE,"/topic/conversaciones/2",true),null));}
 @Test void noPermiteWildcardsNiOtrosDestinos(){for(String d:List.of("/topic/conversaciones/*","/topic/conversaciones/1/extra","/queue/1"))assertThrows(AccessDeniedException.class,()->interceptor.preSend(frame(StompCommand.SUBSCRIBE,d,true),null));verifyNoInteractions(acceso);}
 @Test void noAutenticado(){assertThrows(AccessDeniedException.class,()->interceptor.preSend(frame(StompCommand.SUBSCRIBE,"/topic/conversaciones/1",false),null));}
 @Test void envioDirectoAlBrokerDenegado(){assertThrows(AccessDeniedException.class,()->interceptor.preSend(frame(StompCommand.SEND,"/topic/conversaciones/1",true),null));}
}
