package com.pezbackend.realtime.infrastructure.websocket.interceptors;

import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UserDetailsServiceExtension;
import com.pezbackend.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.pezbackend.iam.infrastructure.tokens.jwt.BearerTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interceptor de canal STOMP que realiza la autenticación por JWT
 * y asegura el aislamiento de inquilinos (multi-tenancy) en la suscripción a tópicos.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final BearerTokenService tokenService;
    private final UserDetailsServiceExtension userDetailsService;

    private static final Pattern RESTAURANT_TOPIC_PATTERN = Pattern.compile("^/topic/restaurants/(\\d+)/(tables|kitchen|alerts)$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = org.springframework.messaging.support.MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            authenticateConnection(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscription(accessor);
        }

        return message;
    }

    private void authenticateConnection(StompHeaderAccessor accessor) {
        List<String> authorization = accessor.getNativeHeader("Authorization");
        if (authorization == null || authorization.isEmpty()) {
            log.warn("Intento de conexión WebSocket sin cabecera de autorización.");
            throw new MessageDeliveryException("Falta token de autenticación.");
        }

        String bearerToken = authorization.get(0);
        if (!bearerToken.startsWith("Bearer ")) {
            log.warn("Cabecera de autorización con formato inválido.");
            throw new MessageDeliveryException("Formato de token inválido.");
        }

        String token = bearerToken.substring(7);
        try {
            if (tokenService.validateToken(token)) {
                Long userId = tokenService.getUserIdFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationTokenBuilder.build(userDetails);
                accessor.setUser(authentication);
                System.out.println("DEBUG WS INTERCEPTOR: Conexión autenticada para " + userDetails.getUsername() + ", Tenant " + ((UserDetailsImpl) userDetails).getRestaurantId());
                log.info("Conexión WebSocket autenticada para el usuario: {}", userDetails.getUsername());
            } else {
                throw new MessageDeliveryException("Token inválido.");
            }
        } catch (Exception e) {
            System.err.println("DEBUG WS INTERCEPTOR AUTH FAILED: " + e.getMessage());
            log.error("Fallo de autenticación en conexión WebSocket: {}", e.getMessage());
            throw new MessageDeliveryException("Autenticación fallida: " + e.getMessage());
        }
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            System.err.println("DEBUG WS INTERCEPTOR SUB DENIED: Usuario no autenticado.");
            log.warn("Intento de suscripción no autorizado (usuario no autenticado).");
            throw new MessageDeliveryException("Acceso denegado: Usuario no autenticado.");
        }

        String destination = accessor.getDestination();
        if (!StringUtils.hasText(destination)) {
            throw new MessageDeliveryException("Destino no especificado.");
        }

        System.out.println("DEBUG WS INTERCEPTOR SUB: Solicitada suscripción de " + userDetails.getUsername() + " a " + destination);

        Matcher matcher = RESTAURANT_TOPIC_PATTERN.matcher(destination);
        if (matcher.matches()) {
            Long topicRestaurantId = Long.parseLong(matcher.group(1));
            Long userRestaurantId = userDetails.getRestaurantId();

            if (!topicRestaurantId.equals(userRestaurantId)) {
                System.err.println("DEBUG WS INTERCEPTOR SUB REJECTED (Tenant Mismatch): User Tenant " + userRestaurantId + " != Topic Tenant " + topicRestaurantId);
                log.warn("⚠️ [CONCURRENCIA] Intento de acceso cross-tenant en WebSocket: Usuario '{}' (Tenant {}) intentó suscribirse al tópico '{}'",
                        userDetails.getUsername(), userRestaurantId, destination);
                throw new MessageDeliveryException("Acceso denegado: No pertenece a este restaurante.");
            }
            System.out.println("DEBUG WS INTERCEPTOR SUB APPROVED: " + userDetails.getUsername() + " a " + destination);
            log.debug("Suscripción WebSocket autorizada para '{}' al tópico '{}'", userDetails.getUsername(), destination);
        } else {
            System.err.println("DEBUG WS INTERCEPTOR SUB REJECTED (Invalid topic): " + destination);
            log.warn("Intento de suscripción a un tópico inválido o no reconocido: {}", destination);
            throw new MessageDeliveryException("Tópico de suscripción no válido.");
        }
    }
}
