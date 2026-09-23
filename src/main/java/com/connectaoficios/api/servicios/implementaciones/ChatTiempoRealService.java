package com.connectaoficios.api.servicios.implementaciones;

import com.connectaoficios.api.dtos.mensaje.MensajeSalida;
import com.connectaoficios.api.servicios.interfaces.IChatTiempoRealService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatTiempoRealService
        implements IChatTiempoRealService {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatTiempoRealService(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publicarMensaje(
            MensajeSalida mensaje
    ) {

        messagingTemplate.convertAndSend(
                "/topic/conversaciones/"
                        + mensaje.getConversacionId(),
                mensaje
        );
    }
}