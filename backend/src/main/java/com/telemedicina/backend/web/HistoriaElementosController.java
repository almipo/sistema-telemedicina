package com.telemedicina.backend.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.repository.HistoriaClinicaRepository;
import com.telemedicina.backend.service.EntityIntegrity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Tag(name = "Historias clínicas - contenido", description = "Notas, medicaciones y médicos asociados a historias clínicas")
@RestController
@RequestMapping("/api/historias")
public class HistoriaElementosController {
    private final HistoriaClinicaRepository historias;
    private final EntityIntegrity integrity;

    public HistoriaElementosController(HistoriaClinicaRepository historias, EntityIntegrity integrity) {
        this.historias = historias;
        this.integrity = integrity;
    }

    private HistoriaClinica historia(UUID id) {
        return historias.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Historia inexistente"));
    }

    /** JSON: {"texto":"Control clínico"}. */
    public record Texto(@NotBlank String texto) {}

    @Operation(summary = "Agregar nota médica")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Nota agregada"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud incorrecta"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
})
@PostMapping("/{id}/notas")
    @Transactional
    public ResponseEntity<String> agregarNota(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id, @Valid @RequestBody Texto body) {
        historia(id).agregarNota(body.texto());
        return ResponseEntity.status(HttpStatus.CREATED).body(body.texto());
    }

    @Operation(summary = "Agregar medicación")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Medicación agregada"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud incorrecta"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
})
@PostMapping("/{id}/medicaciones")
    @Transactional
    public ResponseEntity<String> agregarMedicacion(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id, @Valid @RequestBody Texto body) {
        historia(id).agregarMedicacion(body.texto());
        return ResponseEntity.status(HttpStatus.CREATED).body(body.texto());
    }

    @Operation(summary = "Eliminar medicación por índice")
@ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Medicación eliminada"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
})
@DeleteMapping("/{id}/medicaciones/{indice}")
    @Transactional
    public ResponseEntity<Void> eliminarMedicacion(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id, @Parameter(description = "Posición de la medicación en el historial") @PathVariable int indice) {
        HistoriaClinica h = historia(id);
        if (indice < 0 || indice >= h.getHistorialMedicacion().size())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Índice inexistente");
        h.eliminarMedicacion(h.getHistorialMedicacion().get(indice));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Asociar médico a historia clínica")
@ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Médico asociado"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud incorrecta"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
})
@PostMapping("/{id}/medicos/{medicoId}")
    @Transactional
    public ResponseEntity<Void> asociarMedico(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id, @Parameter(description = "Identificador UUID del médico") @PathVariable UUID medicoId) {
        historia(id).agregarMedico(integrity.medico(medicoId));
        return ResponseEntity.noContent().build();
    }
}
