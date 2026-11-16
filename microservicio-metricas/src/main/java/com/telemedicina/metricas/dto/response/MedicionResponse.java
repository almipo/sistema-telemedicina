package com.telemedicina.metricas.dto.response;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Schema(description="Medición almacenada en MongoDB")
public record MedicionResponse(String id,String tipo,BigDecimal valor,Instant fechaHora,UUID dispositivoId,UUID pacienteId,String unidad) {}
