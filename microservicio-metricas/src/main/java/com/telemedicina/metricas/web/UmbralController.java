package com.telemedicina.metricas.web;

import com.telemedicina.metricas.dto.request.UmbralRequest;
import com.telemedicina.metricas.model.UmbralConfiguracion;
import com.telemedicina.metricas.service.UmbralConfiguracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/umbrales")
@Tag(name = "Umbrales", description = "Configuración general de rangos demostrativos")
public class UmbralController {
    private final UmbralConfiguracionService servicio;
    public UmbralController(UmbralConfiguracionService servicio) { this.servicio = servicio; }

    @GetMapping
    @Operation(summary = "Consultar rangos mínimo y máximo")
    public List<UmbralConfiguracion> listar() { return servicio.listar(); }

    @PutMapping("/{tipo}")
    @Operation(summary = "Actualizar un rango y guardarlo en MongoDB")
    public UmbralConfiguracion actualizar(@PathVariable String tipo, @Valid @RequestBody UmbralRequest valores) {
        return servicio.actualizar(tipo, valores);
    }
}
