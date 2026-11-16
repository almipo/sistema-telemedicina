package com.telemedicina.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import com.telemedicina.backend.entity.*;
@Schema(description = "Datos para registrar o actualizar un dispositivo IoT")
public record DispositivoRequest(
    @Schema(description = "Tipo de dispositivo o sensor", example = "PULSOMETRO")
    @NotBlank String tipo,
    @Schema(description = "Número de serie del dispositivo", example = "SERIE-001")
    @NotBlank String numeroSerie,
    @Schema(description = "Estado operativo", example = "ACTIVO", allowableValues = {"ACTIVO", "INACTIVO"})
    @NotNull EstadoDispositivo estado,
    @Schema(description = "Nombre descriptivo del dispositivo", example = "Pulsera de Juan")
    @NotBlank String nombre,
    @Schema(description = "Identificador del paciente vinculado", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull UUID pacienteId
) {}
