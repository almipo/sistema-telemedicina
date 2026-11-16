package com.telemedicina.alertas.dto;
import com.telemedicina.alertas.entity.Estado;
import java.time.LocalDateTime;
import java.util.UUID;
public record AlertaResponse(UUID id, String tipo, String mensaje, LocalDateTime fechaHora,
                             Estado estado, String medicionId, UUID pacienteId) {}
