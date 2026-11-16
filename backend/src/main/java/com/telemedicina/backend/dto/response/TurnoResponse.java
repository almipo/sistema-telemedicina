package com.telemedicina.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import com.telemedicina.backend.entity.*;
@Schema(description = "Datos de un turno médico devueltos por la API")
public record TurnoResponse(
    @Schema(description = "Identificador del turno", example = "550e8400-e29b-41d4-a716-446655440003")
    UUID id,
    @Schema(description = "Fecha del turno", example = "2026-12-10")
    LocalDate fecha,
    @Schema(description = "Hora del turno", example = "10:30:00")
    LocalTime hora,
    @Schema(description = "Estado del turno", example = "A_CONFIRMAR", allowableValues = {"A_CONFIRMAR", "CONFIRMADO", "CANCELADO"})
    EstadoTurno estado,
    @Schema(description = "Identificador del paciente", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID pacienteId,
    @Schema(description = "Identificador del médico", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID medicoId
) {}
