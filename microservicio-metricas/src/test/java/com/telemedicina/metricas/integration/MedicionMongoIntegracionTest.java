package com.telemedicina.metricas.integration;

import com.telemedicina.metricas.model.Medicion;
import com.telemedicina.metricas.repository.MedicionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/** Integracion REAL con una base MongoDB de pruebas; no usa mocks. */
@DataMongoTest
class MedicionMongoIntegracionTest {
    @Autowired MedicionRepository repository;

    @DynamicPropertySource
    static void mongoDePruebas(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> System.getenv().getOrDefault(
                "MONGO_TEST_URI", "mongodb://localhost:27017/telemedicina_metricas_test"));
    }

    private Medicion nueva(UUID paciente, Instant fecha, int valor) {
        Medicion m = new Medicion();
        m.setTipo("PULSO");
        m.setValor(BigDecimal.valueOf(valor));
        m.setFechaHora(fecha);
        m.setPacienteId(paciente);
        m.setDispositivoId(UUID.randomUUID());
        return m;
    }

    @Test
    void guardaYRecuperaUnaMedicionReal() {
        Medicion guardada = repository.save(nueva(UUID.randomUUID(), Instant.now(), 78));
        try {
            assertNotNull(guardada.getId());
            Medicion recuperada = repository.findById(guardada.getId()).orElseThrow();
            assertEquals("PULSO", recuperada.getTipo());
            assertEquals(0, BigDecimal.valueOf(78).compareTo(recuperada.getValor()));
            assertEquals(guardada.getPacienteId(), recuperada.getPacienteId());
        } finally {
            repository.deleteById(guardada.getId());
        }
    }

    @Test
    void filtraPorPacienteYOrdenaPorFechaDescendente() {
        UUID paciente = UUID.randomUUID();
        Medicion antigua = repository.save(nueva(paciente, Instant.parse("2026-01-01T10:00:00Z"), 65));
        Medicion reciente = repository.save(nueva(paciente, Instant.parse("2026-01-02T10:00:00Z"), 85));
        try {
            var mediciones = repository.findByPacienteIdOrderByFechaHoraDesc(paciente);
            assertEquals(2, mediciones.size());
            assertEquals(reciente.getId(), mediciones.get(0).getId());
            assertEquals(antigua.getId(), mediciones.get(1).getId());
        } finally {
            repository.deleteById(antigua.getId());
            repository.deleteById(reciente.getId());
        }
    }
}
