package com.telemedicina.backend.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.telemedicina.backend.dto.request.HistoriaClinicaRequest;
import com.telemedicina.backend.dto.response.HistoriaClinicaResponse;
import com.telemedicina.backend.service.HistoriaClinicaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;
@Tag(name = "Historias clínicas", description = "Gestión de historias clínicas")
@RestController @RequestMapping("/api/historias")
public class HistoriaClinicaController {
 private final HistoriaClinicaService service;
 public HistoriaClinicaController(HistoriaClinicaService service){this.service=service;}
 @Operation(summary = "Listar registros")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
})
@GetMapping public List<HistoriaClinicaResponse> listar(){return service.listarDto();}
 @Operation(summary = "Consultar registro por identificador")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Registro encontrado"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
})
@GetMapping("/{id}") public HistoriaClinicaResponse obtener(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id){return service.obtenerDto(id);}
 @Operation(summary = "Crear historia clínica")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Registro creado"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud incorrecta"),
    @ApiResponse(responseCode = "409", description = "Conflicto o restricción de integridad")
})
@PostMapping public ResponseEntity<HistoriaClinicaResponse> crear(@Valid @RequestBody HistoriaClinicaRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));}
 @Operation(summary = "Actualizar registro")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Registro actualizado"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud incorrecta"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
    @ApiResponse(responseCode = "409", description = "Conflicto o restricción de integridad")
})
@PutMapping("/{id}") public HistoriaClinicaResponse actualizar(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id,@Valid @RequestBody HistoriaClinicaRequest request){return service.actualizar(id,request);}
 @Operation(summary = "Eliminar registro")
@ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Registro eliminado"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
    @ApiResponse(responseCode = "409", description = "Conflicto o restricción de integridad")
})
@DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id){service.eliminar(id);return ResponseEntity.noContent().build();}
 
}
