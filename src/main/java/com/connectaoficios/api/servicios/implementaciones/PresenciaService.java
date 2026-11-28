package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.repositorios.IConversacionRepository;
import com.connectaoficios.api.seguridad.IdentidadJwt;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

/** Presencia local a esta instancia; requiere almacenamiento compartido en multiples replicas. */
@Service
public class PresenciaService {
    private final Map<Integer, Set<String>> sesiones = new ConcurrentHashMap<>();
    private final Map<String, Integer> propietarios = new ConcurrentHashMap<>();
    private final IConversacionRepository conversaciones;
    private final SimpMessagingTemplate broker;
    public record EventoPresencia(String tipo, Integer usuarioId, boolean online) {}
    public PresenciaService(IConversacionRepository conversaciones, SimpMessagingTemplate broker) {
        this.conversaciones = conversaciones; this.broker = broker;
    }
    public synchronized boolean online(Integer usuarioId) {
        return !sesiones.getOrDefault(usuarioId, Set.of()).isEmpty();
    }
    public synchronized void conectar(Integer usuarioId, String sessionId) {
        if (usuarioId == null || sessionId == null || propietarios.containsKey(sessionId)) return;
        boolean anterior = online(usuarioId);
        propietarios.put(sessionId, usuarioId);
        sesiones.computeIfAbsent(usuarioId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        if (!anterior) publicar(usuarioId, true);
    }
    public synchronized void desconectar(String sessionId) {
        Integer usuarioId = propietarios.remove(sessionId);
        if (usuarioId == null) return;
        Set<String> activas = sesiones.get(usuarioId);
        if (activas != null) {
            activas.remove(sessionId);
            if (activas.isEmpty()) { sesiones.remove(usuarioId); publicar(usuarioId, false); }
        }
    }
    private void publicar(Integer usuarioId, boolean online) {
        var evento = new EventoPresencia("PRESENCIA", usuarioId, online);
        for (Long id : conversaciones.idsDelParticipante(usuarioId))
            broker.convertAndSend("/topic/conversaciones/" + id + "/presencia", evento);
    }
    @EventListener public void conectado(SessionConnectedEvent event) {
        if (event.getUser() instanceof Authentication auth && auth.isAuthenticated()) {
            conectar(IdentidadJwt.usuarioId(auth), SimpMessageHeaderAccessor.getSessionId(event.getMessage().getHeaders()));
        }
    }
    @EventListener public void desconectado(SessionDisconnectEvent event) { desconectar(event.getSessionId()); }
}
