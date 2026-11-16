package com.telemedicina.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import com.telemedicina.backend.entity.*;
@Schema(description = "Datos para crear o actualizar una historia clínica")
public record HistoriaClinicaRequest(
    @Schema(description = "Paciente al que pertenece la historia clínica", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull UUID pacienteId,
    @Schema(description = "Estado de la historia clínica", example = "ACTIVO", allowableValues = {"ACTIVO", "ARCHIVADO"})
    @NotNull EstadoHistorial estado,
    @Schema(description = "Listado de notas médicas")
    List<String> notasMedicas,
    @Schema(description = "Listado de medicaciones registradas")
    List<String> historialMedicacion,
    @Schema(description = "Identificadores de los médicos asociados")
    List<UUID> medicosIds
) {}
