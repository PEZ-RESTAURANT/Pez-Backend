package com.pezbackend.realtime.infrastructure.websocket.configuration;

import com.pezbackend.realtime.infrastructure.websocket.interceptors.AuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración WebSocket habilitando mensajería STOMP con SockJS fallback.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitar un broker simple en memoria para los tópicos
        config.enableSimpleBroker("/topic");
        // Prefijo para los mensajes dirigidos a controladores @MessageMapping (si los hubiera)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registrar endpoint base de conexión
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
        
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Registrar el interceptor para autenticación y validación de seguridad por inquilino
        registration.interceptors(authChannelInterceptor);
    }
}
