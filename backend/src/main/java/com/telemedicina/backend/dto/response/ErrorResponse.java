package com.telemedicina.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
@Schema(description = "Formato común de respuesta cuando ocurre un error HTTP")
public record ErrorResponse(
    @Schema(description = "Fecha y hora del error en UTC", example = "2026-11-14T13:28:00Z")
    Instant fechaHora,
    @Schema(description = "Código de estado HTTP", example = "404")
    int estado,
    @Schema(description = "Descripción general del error", example = "Recurso no encontrado")
    String error,
    @Schema(description = "Lista de detalles del error")
    List<String> detalles
) {}
