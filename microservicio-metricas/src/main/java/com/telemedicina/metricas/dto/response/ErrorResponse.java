package com.telemedicina.metricas.dto.response;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
@Schema(description="Respuesta uniforme de error")
public record ErrorResponse(Instant fechaHora,int estado,String error,String mensaje) {}
