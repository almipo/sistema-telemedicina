package com.telemedicina.backend.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.telemedicina.backend.dto.request.MedicoRequest;
import com.telemedicina.backend.dto.response.MedicoResponse;
import com.telemedicina.backend.service.MedicoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;
@Tag(name = "Médicos", description = "Gestión de profesionales médicos")
@RestController @RequestMapping("/api/medicos")
public class MedicoController {
 private final MedicoService service;
 public MedicoController(MedicoService service){this.service=service;}
 @Operation(summary = "Listar registros")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
})
@GetMapping public List<MedicoResponse> listar(){return service.listarDto();}
 @Operation(summary = "Consultar registro por identificador")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Registro encontrado"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
})
@GetMapping("/{id}") public MedicoResponse obtener(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id){return service.obtenerDto(id);}
 @Operation(summary = "Registrar médico")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Registro creado"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud incorrecta"),
    @ApiResponse(responseCode = "409", description = "Conflicto o restricción de integridad")
})
@PostMapping public ResponseEntity<MedicoResponse> crear(@Valid @RequestBody MedicoRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));}
 @Operation(summary = "Actualizar registro")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Registro actualizado"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud incorrecta"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
    @ApiResponse(responseCode = "409", description = "Conflicto o restricción de integridad")
})
@PutMapping("/{id}") public MedicoResponse actualizar(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id,@Valid @RequestBody MedicoRequest request){return service.actualizar(id,request);}
 @Operation(summary = "Eliminar registro")
@ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Registro eliminado"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
    @ApiResponse(responseCode = "409", description = "Conflicto o restricción de integridad")
})
@DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id){service.eliminar(id);return ResponseEntity.noContent().build();}
 
}
