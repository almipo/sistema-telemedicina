package com.telemedicina.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import com.telemedicina.backend.entity.*;
@Schema(description = "Datos de un paciente devueltos por la API")
public record PacienteResponse(
    @Schema(description = "Identificador del paciente", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,
    @Schema(description = "Nombre del paciente", example = "Juan")
    String nombre,
    @Schema(description = "Apellido del paciente", example = "Pérez")
    String apellido,
    @Schema(description = "Documento de identidad", example = "30123456")
    String documento,
    @Schema(description = "Fecha de nacimiento", example = "1990-05-15")
    LocalDate fechaNacimiento,
    @Schema(description = "Teléfono de contacto", example = "1123456789")
    String telefono,
    @Schema(description = "Correo electrónico", example = "juan@example.com")
    String email
) {}
