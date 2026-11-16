package com.telemedicina.metricas.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UmbralRequest(@NotNull BigDecimal minimo, @NotNull BigDecimal maximo) {}
