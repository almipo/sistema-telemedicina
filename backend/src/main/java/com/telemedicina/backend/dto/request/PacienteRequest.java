package com.telemedicina.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import com.telemedicina.backend.entity.*;
@Schema(description = "Datos para registrar o actualizar un paciente")
public record PacienteRequest(
    @Schema(description = "Nombre del paciente", example = "Juan")
    @NotBlank String nombre,
    @Schema(description = "Apellido del paciente", example = "Pérez")
    @NotBlank String apellido,
    @Schema(description = "Documento de identidad", example = "30123456")
    @NotBlank String documento,
    @Schema(description = "Fecha de nacimiento (ISO 8601)", example = "1990-05-15")
    @NotNull LocalDate fechaNacimiento,
    @Schema(description = "Teléfono de contacto", example = "1123456789")
    String telefono,
    @Schema(description = "Correo electrónico", example = "juan@example.com")
    @NotBlank @Email String email
) {}
