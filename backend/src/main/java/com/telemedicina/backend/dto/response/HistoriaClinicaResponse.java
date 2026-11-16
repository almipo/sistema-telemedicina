package com.telemedicina.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import com.telemedicina.backend.entity.*;
@Schema(description = "Información de una historia clínica devuelta por la API")
public record HistoriaClinicaResponse(
    @Schema(description = "Identificador de la historia clínica", example = "550e8400-e29b-41d4-a716-446655440004")
    UUID id,
    @Schema(description = "Paciente propietario de la historia", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID pacienteId,
    @Schema(description = "Estado de la historia", example = "ACTIVO", allowableValues = {"ACTIVO", "ARCHIVADO"})
    EstadoHistorial estado,
    @Schema(description = "Listado de notas médicas")
    List<String> notasMedicas,
    @Schema(description = "Listado de medicaciones")
    List<String> historialMedicacion,
    @Schema(description = "Médicos asociados a la historia clínica")
    List<UUID> medicosIds
) {}
