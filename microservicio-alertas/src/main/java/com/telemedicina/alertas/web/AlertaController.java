package com.telemedicina.alertas.web;

import com.telemedicina.alertas.dto.*;
import com.telemedicina.alertas.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class AlertaController {
    private final GestorAlertas gestor;
    private final AlertasService servicio;
    public AlertaController(GestorAlertas gestor, AlertasService servicio) {
        this.gestor = gestor; this.servicio = servicio;
    }
    @GetMapping("/api/alertas")
    public List<AlertaResponse> listar(@RequestParam(required = false) UUID pacienteId) {
        return servicio.listar(pacienteId);
    }
    @GetMapping("/api/alertas/{id}")
    public AlertaResponse una(@PathVariable UUID id) { return servicio.buscar(id); }
    @PostMapping("/api/interno/alertas")
    public ResponseEntity<AlertaResponse> crear(@Valid @RequestBody AlertaEntrada entrada) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestor.procesar(entrada));
    }
    @PatchMapping("/api/alertas/{id}/estado")
    public AlertaResponse estado(@PathVariable UUID id, @Valid @RequestBody CambioEstadoRequest entrada) {
        return servicio.cambiar(id, entrada.estado());
    }
    @GetMapping("/api/alertas/{id}/notificaciones")
    public List<NotificacionResponse> notificaciones(@PathVariable UUID id) {
        return servicio.notificacionesDe(id);
    }
}
