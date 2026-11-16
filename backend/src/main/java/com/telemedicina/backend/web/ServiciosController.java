package com.telemedicina.backend.web;

import com.telemedicina.backend.dto.request.*;
import com.telemedicina.backend.dto.response.*;
import com.telemedicina.backend.mapper.MedicionMapper;
import com.telemedicina.backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Tag(name = "Métricas y alertas", description = "Consultas a microservicios mediante contratos DTO")
@RestController
@RequestMapping("/api")
public class ServiciosController {
    private final MedicionService mediciones;
    private final AlertaService alertas;
    public ServiciosController(MedicionService mediciones, AlertaService alertas) {
        this.mediciones = mediciones;
        this.alertas = alertas;
    }

    @Operation(summary = "Listar mediciones")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Mediciones obtenidas"),
                   @ApiResponse(responseCode = "502", description = "Microservicio no disponible")})
    @GetMapping("/mediciones")
    public List<MedicionResponse> mediciones(@Parameter(description = "Paciente opcional") @RequestParam(required = false) UUID pacienteId) {
        return mediciones.listar(pacienteId).stream().map(MedicionMapper::toResponse).toList();
    }

    @Operation(summary = "Obtener medición por identificador")
    @GetMapping("/mediciones/{id}")
    public MedicionResponse medicion(@PathVariable String id) {
        return MedicionMapper.toResponse(mediciones.obtener(id));
    }

    @Operation(summary = "Registrar medición")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Medición registrada"),
                   @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                   @ApiResponse(responseCode = "502", description = "Microservicio no disponible")})
    @PostMapping("/mediciones")
    public ResponseEntity<MedicionResponse> crear(@Valid @RequestBody MedicionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(MedicionMapper.toResponse(mediciones.registrar(MedicionMapper.toEntity(request))));
    }

    @Operation(summary = "Listar alertas")
    @GetMapping("/alertas")
    public List<AlertaResponse> alertas(@RequestParam(required = false) UUID pacienteId) {
        return alertas.listar(pacienteId);
    }

    @Operation(summary = "Obtener alerta por identificador")
    @GetMapping("/alertas/{id}")
    public AlertaResponse alerta(@PathVariable UUID id) { return alertas.obtener(id); }

    @Operation(summary = "Cambiar estado de alerta")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Estado modificado"),
                   @ApiResponse(responseCode = "400", description = "Estado incorrecto"),
                   @ApiResponse(responseCode = "502", description = "Microservicio no disponible")})
    @PatchMapping("/alertas/{id}/estado")
    public AlertaResponse cambiar(@PathVariable UUID id, @Valid @RequestBody AlertaEstadoRequest body) {
        return alertas.cambiarEstado(id, body);
    }
}
