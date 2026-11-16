package com.telemedicina.backend.web;

import com.telemedicina.backend.dto.request.AlertaEstadoRequest;
import com.telemedicina.backend.dto.response.AlertaResponse;
import com.telemedicina.backend.entity.Medicion;
import com.telemedicina.backend.service.AlertaService;
import com.telemedicina.backend.service.MedicionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicionesYAlertasControllerTest {
    @Test
    void listaMedicionesConvertidasADto() {
        MedicionService mediciones = mock(MedicionService.class);
        AlertaService alertas = mock(AlertaService.class);
        Medicion m = new Medicion();
        m.setId("mongo-1");
        m.setTipo("PULSACIONES");
        m.setValor(new BigDecimal("82"));
        m.setFechaHora(Instant.parse("2026-11-14T11:00:00Z"));
        UUID pacienteId = UUID.randomUUID();
        m.setPacienteId(pacienteId);
        m.setDispositivoId(UUID.randomUUID());
        when(mediciones.listar(pacienteId)).thenReturn(List.of(m));
        var respuesta = new ServiciosController(mediciones, alertas).mediciones(pacienteId);
        assertEquals(1, respuesta.size());
        assertEquals("mongo-1", respuesta.get(0).id());
        assertEquals(pacienteId, respuesta.get(0).pacienteId());
    }

    @Test
    void delegaCambioEstadoAlMicroservicioDeAlertas() {
        MedicionService mediciones = mock(MedicionService.class);
        AlertaService alertas = mock(AlertaService.class);
        UUID id = UUID.randomUUID(), paciente = UUID.randomUUID();
        AlertaEstadoRequest entrada = new AlertaEstadoRequest("ATENDIDA");
        AlertaResponse esperado = new AlertaResponse(id, "medicion-1", paciente,
                "RIESGO", "Control requerido", Instant.parse("2026-11-14T11:00:00Z"), "ATENDIDA");
        when(alertas.cambiarEstado(id, entrada)).thenReturn(esperado);
        var result = new ServiciosController(mediciones, alertas).cambiar(id, entrada);
        assertEquals("ATENDIDA", result.estado());
        verify(alertas).cambiarEstado(id, entrada);
    }
}
