package com.connectaoficios.api.seguridad;

import com.connectaoficios.api.seguridad.configuracion.JwtAuthenticationConverter;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class StompSeguridad implements ChannelInterceptor {
    private final JwtDecoder decoder;
    private final JwtAuthenticationConverter converter;
    private final AccesoJava acceso;
    public StompSeguridad(JwtDecoder decoder, JwtAuthenticationConverter converter, AccesoJava acceso) {
        this.decoder = decoder; this.converter = converter; this.acceso = acceso;
    }
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var headers = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (headers == null || headers.getCommand() == null) return message;
        if (headers.getCommand() == StompCommand.CONNECT) {
            String bearer = headers.getFirstNativeHeader("Authorization");
            if (bearer == null || !bearer.startsWith("Bearer ")) throw new AccessDeniedException("JWT requerido");
            var auth = converter.convert(decoder.decode(bearer.substring(7)));
            IdentidadJwt.usuarioId(auth);
            headers.setUser(auth);
            return message;
        }
        if (!(headers.getUser() instanceof Authentication auth) || !auth.isAuthenticated())
            throw new AccessDeniedException("Sesión STOMP no autenticada");
        if (headers.getCommand() == StompCommand.SEND)
            throw new AccessDeniedException("Los mensajes se publican exclusivamente mediante la API REST autorizada");
        if (headers.getCommand() == StompCommand.SUBSCRIBE) {
            String destination = headers.getDestination();
            String prefix = "/topic/conversaciones/";
            if (destination == null || !destination.matches("/topic/conversaciones/[1-9][0-9]*"))
                throw new AccessDeniedException("Destino no permitido");
            try {
                if (!acceso.conversacion(auth, Long.valueOf(destination.substring(prefix.length()))))
                    throw new AccessDeniedException("Conversación ajena");
            } catch (NumberFormatException ex) { throw new AccessDeniedException("Destino inválido"); }
        }
        return message;
    }
}
