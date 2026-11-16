package com.telemedicina.metricas.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;
@Schema(description="Medición enviada por dispositivo IoT")
public record MedicionRequest(
 @NotBlank @Schema(example="PULSO",description="PULSO, PRESION_SISTOLICA, PRESION_DIASTOLICA o TEMPERATURA") String tipo,
 @NotNull @DecimalMin(value="0.0",inclusive=false) @Schema(example="80") BigDecimal valor,
 @NotNull UUID dispositivoId,
 @NotNull UUID pacienteId
) {}
