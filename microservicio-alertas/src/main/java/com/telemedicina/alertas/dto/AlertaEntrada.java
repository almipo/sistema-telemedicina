package com.telemedicina.alertas.dto;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record AlertaEntrada(
    @NotBlank String medicionId,
    @NotNull UUID pacienteId,
    @NotBlank String tipo,
    @NotBlank String mensaje
) {}
