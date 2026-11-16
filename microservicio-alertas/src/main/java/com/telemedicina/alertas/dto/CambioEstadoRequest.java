package com.telemedicina.alertas.dto;
import com.telemedicina.alertas.entity.Estado;
import jakarta.validation.constraints.NotNull;
public record CambioEstadoRequest(@NotNull Estado estado) {}
