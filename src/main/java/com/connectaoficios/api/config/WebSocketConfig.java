package com.connectaoficios.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {

    private final com.connectaoficios.api.seguridad.StompSeguridad seguridad;
    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins}")
    private String origins;
    public WebSocketConfig(com.connectaoficios.api.seguridad.StompSeguridad seguridad) { this.seguridad = seguridad; }

    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(seguridad);
    }

    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry
    ) {

        registry.enableSimpleBroker(
                "/topic",
                "/queue"
        );

        registry.setApplicationDestinationPrefixes(
                "/app"
        );
    }

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {

        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins.split(","));
    }
}