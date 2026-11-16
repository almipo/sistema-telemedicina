package com.telemedicina.alertas.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemedicina.alertas.dto.AlertaResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlertasSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper json;
    public AlertasSocketHandler(ObjectMapper mapper) { json = mapper; }
    @Override public void afterConnectionEstablished(WebSocketSession session) { sessions.add(session); }
    @Override public void afterConnectionClosed(WebSocketSession session, CloseStatus status) { sessions.remove(session); }
    @Override public void handleTransportError(WebSocketSession session, Throwable exception) { sessions.remove(session); }

    /** Devuelve true si al menos un cliente recibió el evento. */
    public boolean notificar(AlertaResponse alerta) throws JsonProcessingException {
        String payload = json.writeValueAsString(alerta);
        boolean entregado = false;
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    synchronized (session) { session.sendMessage(new TextMessage(payload)); }
                    entregado = true;
                } else { sessions.remove(session); }
            } catch (IOException | IllegalStateException ex) { sessions.remove(session); }
        }
        return entregado;
    }
}
