package com.telemedicina.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import com.telemedicina.backend.entity.*;
@Schema(description = "Datos necesarios para solicitar o modificar un turno médico")
public record TurnoRequest(
    @Schema(description = "Fecha del turno", example = "2026-12-10")
    @NotNull LocalDate fecha,
    @Schema(description = "Hora del turno, formato HH:mm:ss", example = "10:30:00")
    @NotNull LocalTime hora,
    @Schema(description = "Identificador del paciente", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull UUID pacienteId,
    @Schema(description = "Identificador del médico", example = "550e8400-e29b-41d4-a716-446655440001")
    @NotNull UUID medicoId,
    @Schema(description = "Estado del turno (si se omite, aplica la regla del servicio)", example = "A_CONFIRMAR", allowableValues = {"A_CONFIRMAR", "CONFIRMADO", "CANCELADO"})
    EstadoTurno estado
) {}
