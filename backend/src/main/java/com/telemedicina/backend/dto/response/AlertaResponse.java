package com.telemedicina.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Alerta médica informada por el microservicio de alertas")
public record AlertaResponse(
    @Schema(description = "Identificador de alerta") UUID id,
    @Schema(description = "Identificador de medición en MongoDB") String medicionId,
    @Schema(description = "Paciente vinculado") UUID pacienteId,
    @Schema(description = "Tipo de alerta") String tipo,
    @Schema(description = "Mensaje clínico") String mensaje,
    @Schema(description = "Fecha y hora de generación") Instant fechaHora,
    @Schema(description = "Estado de la alerta") String estado
) {}
