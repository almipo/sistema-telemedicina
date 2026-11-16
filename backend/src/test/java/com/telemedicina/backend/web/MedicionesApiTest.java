package com.telemedicina.backend.web;

import com.telemedicina.backend.dto.request.MedicionRequest;
import com.telemedicina.backend.dto.response.MedicionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.http.HttpMethod.*;

/** Pruebas aisladas de la API que conecta backend y microservicio; no requiere MongoDB. */
class MedicionesApiTest {
    private MockRestServiceServer server;
    private ServiciosController controller;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        controller = new ServiciosController(
            new com.telemedicina.backend.service.MedicionService(builder, "http://metricas:8081"),
            new com.telemedicina.backend.service.AlertaService(RestClient.builder(), "http://alertas:8082"));
    }

    @Test
    void consultaMedicionesUsandoContratoDelBackend() {
        UUID id = UUID.randomUUID();
        server.expect(requestTo("http://metricas:8081/api/mediciones?pacienteId=" + id))
            .andExpect(method(GET))
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        List<MedicionResponse> resultado = controller.mediciones(id);
        assertTrue(resultado.isEmpty());
        server.verify();
    }

    @Test
    void enviaMedicionYRecibeMismoContrato() {
        UUID dispositivo = UUID.randomUUID();
        UUID paciente = UUID.randomUUID();
        MedicionRequest entrada = new MedicionRequest("PULSACIONES", new BigDecimal("85"), dispositivo, paciente);
        String respuesta = """
            {"id":"mongo-001","tipo":"PULSACIONES","valor":85,
             "fechaHora":"2026-11-14T12:00:00Z","dispositivoId":"%s","pacienteId":"%s"}
            """.formatted(dispositivo, paciente);
        server.expect(requestTo("http://metricas:8081/api/mediciones"))
            .andExpect(method(POST))
            .andExpect(jsonPath("$.tipo").value("PULSACIONES"))
            .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(respuesta));
        var resultado = controller.crear(entrada);
        assertEquals(HttpStatus.CREATED, resultado.getStatusCode());
        assertNotNull(resultado.getBody());
        assertEquals("mongo-001", resultado.getBody().id());
        assertEquals(paciente, resultado.getBody().pacienteId());
        server.verify();
    }
}
