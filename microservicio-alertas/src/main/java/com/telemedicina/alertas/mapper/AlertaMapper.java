package com.telemedicina.alertas.mapper;
import com.telemedicina.alertas.dto.*;
import com.telemedicina.alertas.entity.*;
import org.springframework.stereotype.Component;
@Component
public class AlertaMapper {
    public AlertaResponse toResponse(Alerta a) {
        return new AlertaResponse(a.getId(), a.getTipo(), a.getMensaje(), a.getFechaHora(),
                a.getEstado(), a.getMedicionId(), a.getPacienteId());
    }
    public NotificacionResponse toResponse(Notificacion n) {
        return new NotificacionResponse(n.getId(), n.getTipo(), n.getAlerta().getId(),
                n.getDestinatario(), n.getEstado());
    }
}
