package com.telemedicina.backend.web;

import com.telemedicina.backend.dto.response.*;
import com.telemedicina.backend.entity.HistoriaClinica;
import com.telemedicina.backend.mapper.*;
import com.telemedicina.backend.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Tag(name = "Consultas relacionadas", description = "Consultas por asociación entre entidades")
@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {
    private final DispositivoRepository dispositivos;
    private final TurnoRepository turnos;
    private final HistoriaClinicaRepository historias;

    public ConsultaController(DispositivoRepository dispositivos, TurnoRepository turnos, HistoriaClinicaRepository historias) {
        this.dispositivos = dispositivos;
        this.turnos = turnos;
        this.historias = historias;
    }

    @Operation(summary = "Consultar dispositivos del paciente")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    @GetMapping("/pacientes/{id}/dispositivos")
    @Transactional(readOnly = true)
    public List<DispositivoResponse> dispositivos(@Parameter(description = "ID del paciente") @PathVariable UUID id) {
        return dispositivos.findByPaciente_Id(id).stream().map(DispositivoMapper::toResponse).toList();
    }

    @Operation(summary = "Consultar turnos del paciente")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    @GetMapping("/pacientes/{id}/turnos")
    @Transactional(readOnly = true)
    public List<TurnoResponse> turnos(@Parameter(description = "ID del paciente") @PathVariable UUID id) {
        return turnos.findByPaciente_Id(id).stream().map(TurnoMapper::toResponse).toList();
    }

    @Operation(summary = "Consultar la agenda de un médico")
    @GetMapping("/medicos/{id}/turnos")
    @Transactional(readOnly = true)
    public List<TurnoResponse> agendaMedico(@PathVariable UUID id) {
        return turnos.findByMedico_Id(id).stream()
                .map(TurnoMapper::toResponse)
                .sorted(Comparator.comparing(TurnoResponse::fecha).thenComparing(TurnoResponse::hora))
                .toList();
    }

    @Operation(summary = "Consultar historias clínicas del paciente")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    @GetMapping("/pacientes/{id}/historias")
    @Transactional(readOnly = true)
    public List<HistoriaClinicaResponse> historias(@Parameter(description = "ID del paciente") @PathVariable UUID id) {
        return historias.findByPaciente_Id(id).stream().map(HistoriaClinicaMapper::toResponse).toList();
    }

    @Operation(summary = "Consultar notas médicas de una historia")
    @GetMapping("/historias/{id}/notas")
    @Transactional(readOnly = true)
    public List<String> notas(@PathVariable UUID id) { return List.copyOf(historia(id).getNotasMedicas()); }

    @Operation(summary = "Consultar medicaciones de una historia")
    @GetMapping("/historias/{id}/medicaciones")
    @Transactional(readOnly = true)
    public List<String> medicaciones(@PathVariable UUID id) { return List.copyOf(historia(id).getHistorialMedicacion()); }

    @Operation(summary = "Consultar médicos asociados a una historia")
    @GetMapping("/historias/{id}/medicos")
    @Transactional(readOnly = true)
    public List<MedicoResponse> medicos(@PathVariable UUID id) {
        return historia(id).getMedicos().stream().map(MedicoMapper::toResponse).toList();
    }

    private HistoriaClinica historia(UUID id) {
        return historias.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Historia inexistente"));
    }
}
