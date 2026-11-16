package com.telemedicina.alertas.dto;
import com.telemedicina.alertas.entity.EstadoNotificacion;
import java.util.UUID;
public record NotificacionResponse(UUID id, String tipo, UUID alertaId,
                                   String destinatario, EstadoNotificacion estado) {}
