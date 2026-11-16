package com.telemedicina.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import com.telemedicina.backend.entity.*;
@Schema(description = "Datos para registrar o actualizar un médico")
public record MedicoRequest(
    @Schema(description = "Nombre del médico", example = "Ana")
    @NotBlank String nombre,
    @Schema(description = "Apellido del médico", example = "García")
    @NotBlank String apellido,
    @Schema(description = "Documento de identidad", example = "28999888")
    @NotBlank String documento,
    @Schema(description = "Fecha de nacimiento", example = "1985-09-10")
    @NotNull LocalDate fechaNacimiento,
    @Schema(description = "Teléfono de contacto", example = "1122223333")
    String telefono,
    @Schema(description = "Correo electrónico profesional", example = "ana@example.com")
    @NotBlank @Email String email,
    @Schema(description = "Matrícula profesional", example = "MN12345")
    @NotBlank String matricula,
    @Schema(description = "Especialidad médica", example = "Cardiología")
    @NotBlank String especialidad
) {}
