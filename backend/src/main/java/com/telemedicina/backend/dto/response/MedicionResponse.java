package com.telemedicina.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Medición recibida desde el microservicio de métricas")
public record MedicionResponse(
    @Schema(description = "ID en MongoDB", example = "mongo-001") String id,
    @Schema(description = "Tipo de signo vital", example = "PULSACIONES") String tipo,
    @Schema(description = "Valor medido", example = "85") BigDecimal valor,
    @Schema(description = "Momento de la medición") Instant fechaHora,
    @Schema(description = "ID del dispositivo", format = "uuid") UUID dispositivoId,
    @Schema(description = "ID del paciente", format = "uuid") UUID pacienteId
) {}
