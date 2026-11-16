package com.telemedicina.backend.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MedicionTest {
    @Test
    void medicionNoEsEntidadJpa() {
        assertFalse(Medicion.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void representaLaRespuestaRealDelMicroservicio() throws Exception {
        UUID paciente = UUID.randomUUID();
        UUID dispositivo = UUID.randomUUID();
        String json = """
            {"id":"mongo-001","tipo":"PULSACIONES","valor":85.5,
             "fechaHora":"2026-11-14T12:00:00Z","dispositivoId":"%s","pacienteId":"%s"}
            """.formatted(dispositivo, paciente);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Medicion medicion = mapper.readValue(json, Medicion.class);
        assertEquals("mongo-001", medicion.getId());
        assertEquals(new BigDecimal("85.5"), medicion.getValor());
        assertEquals(Instant.parse("2026-11-14T12:00:00Z"), medicion.getFechaHora());
        assertEquals(dispositivo, medicion.getDispositivoId());
        assertEquals(paciente, medicion.getPacienteId());
    }
}
