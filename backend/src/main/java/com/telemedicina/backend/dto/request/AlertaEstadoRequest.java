package com.telemedicina.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud de cambio de estado de una alerta")
public record AlertaEstadoRequest(
    @Schema(description = "Nuevo estado de la alerta", example = "ATENDIDA") @NotBlank String estado
) {}
