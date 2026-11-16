package com.telemedicina.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import com.telemedicina.backend.entity.*;
@Schema(description = "Datos del dispositivo IoT devueltos por la API")
public record DispositivoResponse(
    @Schema(description = "Identificador del dispositivo", example = "550e8400-e29b-41d4-a716-446655440002")
    UUID id,
    @Schema(description = "Tipo de dispositivo", example = "PULSOMETRO")
    String tipo,
    @Schema(description = "Número de serie", example = "SERIE-001")
    String numeroSerie,
    @Schema(description = "Estado operativo", example = "ACTIVO", allowableValues = {"ACTIVO", "INACTIVO"})
    EstadoDispositivo estado,
    @Schema(description = "Nombre descriptivo", example = "Pulsera de Juan")
    String nombre,
    @Schema(description = "Identificador del paciente vinculado", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID pacienteId
) {}
