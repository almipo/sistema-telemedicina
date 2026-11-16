package com.telemedicina.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import com.telemedicina.backend.entity.*;
@Schema(description = "Datos de un médico devueltos por la API")
public record MedicoResponse(
    @Schema(description = "Identificador del médico", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID id,
    @Schema(description = "Nombre", example = "Ana")
    String nombre,
    @Schema(description = "Apellido", example = "García")
    String apellido,
    @Schema(description = "Documento", example = "28999888")
    String documento,
    @Schema(description = "Fecha de nacimiento", example = "1985-09-10")
    LocalDate fechaNacimiento,
    @Schema(description = "Teléfono", example = "1122223333")
    String telefono,
    @Schema(description = "Correo electrónico", example = "ana@example.com")
    String email,
    @Schema(description = "Matrícula profesional", example = "MN12345")
    String matricula,
    @Schema(description = "Especialidad", example = "Cardiología")
    String especialidad
) {}
