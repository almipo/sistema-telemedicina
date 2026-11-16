package com.telemedicina.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Datos enviados al microservicio de métricas para registrar una medición")
public record MedicionRequest(
    @Schema(description = "Tipo de signo vital", example = "PULSACIONES") @NotBlank String tipo,
    @Schema(description = "Valor numérico medido", example = "85") @NotNull BigDecimal valor,
    @Schema(description = "ID del dispositivo registrado", format = "uuid") @NotNull UUID dispositivoId,
    @Schema(description = "ID del paciente asociado", format = "uuid") @NotNull UUID pacienteId
) {}
