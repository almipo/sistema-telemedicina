package com.telemedicina.metricas.web;
import com.telemedicina.metricas.dto.request.MedicionRequest;
import com.telemedicina.metricas.dto.response.MedicionResponse;
import com.telemedicina.metricas.dto.response.ErrorResponse;
import com.telemedicina.metricas.service.MedicionService;
import com.telemedicina.metricas.service.ProcesamientoMetricas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/mediciones")
@Tag(name="Mediciones",description="Signos vitales recibidos de dispositivos IoT")
public class MedicionController {
 private final MedicionService servicio;private final ProcesamientoMetricas procesamiento;
 public MedicionController(MedicionService servicio,ProcesamientoMetricas procesamiento){this.servicio=servicio;this.procesamiento=procesamiento;}
 @GetMapping
 @Operation(summary="Listar mediciones, opcionalmente por paciente")
 @ApiResponses({@ApiResponse(responseCode="200",description="Consulta correcta")})
 public List<MedicionResponse> listar(@RequestParam(required=false) UUID pacienteId){return servicio.listar(pacienteId);}
 @GetMapping("/{id}")
 @Operation(summary="Consultar medición por ID MongoDB")
 @ApiResponses({@ApiResponse(responseCode="200",description="Medición encontrada"),@ApiResponse(responseCode="404",description="Medición inexistente")})
 public MedicionResponse obtener(@PathVariable String id){return servicio.obtener(id);}
 @PostMapping
 @Operation(summary="Registrar y evaluar una medición")
 @ApiResponses({@ApiResponse(responseCode="201",description="Medición almacenada"),@ApiResponse(responseCode="400",description="Datos inválidos"),
 @ApiResponse(responseCode="404",description="Dispositivo inexistente"),@ApiResponse(responseCode="502",description="Dependencia no disponible")})
 public ResponseEntity<MedicionResponse> crear(@Valid @RequestBody MedicionRequest entrada){return ResponseEntity.status(HttpStatus.CREATED).body(procesamiento.procesar(entrada));}
}
