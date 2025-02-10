package com.github.mohammedismaiel.websocket.app.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private JwtDecoder jwtDecoder;

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Enable a simple in-memory broker with destinations for broadcasting
        // ("/topic")
        // and for user-specific queues ("/queue").
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages that are bound for methods annotated with @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        // User destination prefix – messages sent with convertAndSendToUser() are
        // routed here.
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Register a STOMP endpoint at "/ws" with SockJS fallback support.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // In production, restrict allowed origins.
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                // Obtain the STOMP header accessor.
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    List<String> authHeaders = accessor.getNativeHeader("Authorization");
                    if (authHeaders != null && !authHeaders.isEmpty()) {
                        String bearerToken = authHeaders.get(0);
                        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                            String token = bearerToken.substring(7);
                            try {
                                Jwt jwt = jwtDecoder.decode(token);
                                // Convert the JWT to an Authentication object.
                                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                                Authentication authentication = converter.convert(jwt);
                                // Set the authenticated user on the STOMP session.
                                accessor.setUser(authentication);
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Invalid JWT token", e);
                            }
                        }
                    }
                }
                return message;
            }
        });
    }
}
