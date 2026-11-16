package com.telemedicina.alertas.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
@Configuration @EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final AlertasSocketHandler handler;
    private final String allowedOrigins;
    public WebSocketConfig(AlertasSocketHandler handler,
            @Value("${app.websocket.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}") String allowedOrigins) {
        this.handler = handler;
        this.allowedOrigins = allowedOrigins;
    }
    @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/alertas")
                .setAllowedOriginPatterns(allowedOrigins.split(","));
    }
}
