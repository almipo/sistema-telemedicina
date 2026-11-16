package com.telemedicina.backend.web;

import com.telemedicina.backend.dto.response.DispositivoResponse;
import com.telemedicina.backend.entity.Dispositivo;
import com.telemedicina.backend.entity.EstadoDispositivo;
import com.telemedicina.backend.mapper.DispositivoMapper;
import com.telemedicina.backend.repository.DispositivoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Tag(name = "Integración interna", description = "Validación de dispositivos para microservicios")
@RestController
@RequestMapping("/api/interno/dispositivos")
public class DispositivoVerificacionController {
    private final DispositivoRepository repo;
    public DispositivoVerificacionController(DispositivoRepository repo) { this.repo = repo; }

    @Operation(summary = "Verificar dispositivo activo y paciente asociado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dispositivo válido"),
        @ApiResponse(responseCode = "400", description = "No activo o no asociado"),
        @ApiResponse(responseCode = "404", description = "Dispositivo inexistente")
    })
    @GetMapping("/{id}/verificar")
    public DispositivoResponse verificar(@Parameter(description = "ID del dispositivo") @PathVariable UUID id,
                                         @Parameter(description = "ID del paciente") @RequestParam UUID pacienteId) {
        Dispositivo d = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dispositivo no existe"));
        if (!pacienteId.equals(d.getPacienteId()) || d.getEstado() != EstadoDispositivo.ACTIVO)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dispositivo no activo o no asociado al paciente");
        return DispositivoMapper.toResponse(d);
    }
}
