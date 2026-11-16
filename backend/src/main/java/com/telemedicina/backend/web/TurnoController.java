package com.telemedicina.backend.web;

import com.telemedicina.backend.entity.EstadoTurno;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.telemedicina.backend.dto.request.TurnoRequest;
import com.telemedicina.backend.dto.response.TurnoResponse;
import com.telemedicina.backend.service.TurnoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;
@Tag(name = "Turnos", description = "Gestión de turnos médicos")
@RestController @RequestMapping("/api/turnos")
public class TurnoController {
 private final TurnoService service;
 public TurnoController(TurnoService service){this.service=service;}
 @Operation(summary = "Listar registros")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
})
@GetMapping public List<TurnoResponse> listar(){return service.listarDto();}
 @Operation(summary = "Consultar registro por identificador")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Registro encontrado"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
})
@GetMapping("/{id}") public TurnoResponse obtener(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id){return service.obtenerDto(id);}
 @Operation(summary = "Solicitar turno")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Registro creado"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud incorrecta"),
    @ApiResponse(responseCode = "409", description = "Conflicto o restricción de integridad")
})
@PostMapping public ResponseEntity<TurnoResponse> crear(@Valid @RequestBody TurnoRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));}
 @Operation(summary = "Actualizar registro")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Registro actualizado"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud incorrecta"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
    @ApiResponse(responseCode = "409", description = "Conflicto o restricción de integridad")
})
@PutMapping("/{id}") public TurnoResponse actualizar(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id,@Valid @RequestBody TurnoRequest request){return service.actualizar(id,request);}
 @Operation(summary = "Eliminar registro")
@ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Registro eliminado"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
    @ApiResponse(responseCode = "409", description = "Conflicto o restricción de integridad")
})
@DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id){service.eliminar(id);return ResponseEntity.noContent().build();}
 
 @Operation(summary = "Confirmar turno")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Turno confirmado"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud incorrecta"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
})
@PatchMapping("/{id}/confirmar") public TurnoResponse confirmar(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id){return service.confirmar(id);}
 @Operation(summary = "Cancelar turno")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Turno cancelado"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud incorrecta"),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado")
})
@PatchMapping("/{id}/cancelar") public TurnoResponse cancelar(@Parameter(description = "Identificador UUID del recurso") @PathVariable UUID id){return service.cancelar(id);}

 @Operation(summary = "Listar estados disponibles de turnos")
 @GetMapping("/estados")
 public List<String> listarEstados() {
  return Arrays.stream(EstadoTurno.values())
          .map(Enum::name)
          .toList();
 }



}
