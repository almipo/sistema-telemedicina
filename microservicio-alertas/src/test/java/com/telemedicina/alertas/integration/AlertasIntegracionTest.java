package com.telemedicina.alertas.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemedicina.alertas.entity.Estado;
import com.telemedicina.alertas.entity.EstadoNotificacion;
import com.telemedicina.alertas.repository.AlertaRepository;
import com.telemedicina.alertas.repository.NotificacionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integración real: Controller -> Service -> JPA -> PostgreSQL.
 * Requiere PostgreSQL de Docker activo y DB_URL/DB_USER/DB_PASSWORD configuradas.
 * @Transactional revierte los registros generados en cada test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlertasIntegracionTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private AlertaRepository alertas;
    @Autowired private NotificacionRepository notificaciones;

    @Test
    void crearAlertaPersisteAlertaYNotificacion() throws Exception {
        String medicionId = "it-" + UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        String body = json.writeValueAsString(Map.of(
                "medicionId", medicionId,
                "pacienteId", pacienteId.toString(),
                "tipo", "PULSO",
                "mensaje", "Pulso 145 fuera de rango"));

        mvc.perform(post("/api/interno/alertas")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.medicionId").value(medicionId))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        var alerta = alertas.findByMedicionId(medicionId).orElseThrow();
        assertThat(alerta.getPacienteId()).isEqualTo(pacienteId);
        assertThat(alerta.getEstado()).isEqualTo(Estado.PENDIENTE);
        var lista = notificaciones.findByAlertaId(alerta.getId());
        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getEstado()).isEqualTo(EstadoNotificacion.PENDIENTE);
    }

    @Test
    void medicionRepetidaNoGeneraDosAlertasNiNotificaciones() throws Exception {
        String medicionId = "it-" + UUID.randomUUID();
        String body = json.writeValueAsString(Map.of(
                "medicionId", medicionId,
                "pacienteId", UUID.randomUUID().toString(),
                "tipo", "TEMPERATURA",
                "mensaje", "Temperatura 39"));

        String id1 = json.readTree(mvc.perform(post("/api/interno/alertas")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();
        String id2 = json.readTree(mvc.perform(post("/api/interno/alertas")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();

        assertThat(id2).isEqualTo(id1);
        assertThat(notificaciones.findByAlertaId(UUID.fromString(id1))).hasSize(1);
    }

    @Test
    void permiteConsultarYCambiarEstado() throws Exception {
        String medicionId = "it-" + UUID.randomUUID();
        String body = json.writeValueAsString(Map.of(
                "medicionId", medicionId,
                "pacienteId", UUID.randomUUID().toString(),
                "tipo", "PULSO",
                "mensaje", "Pulso 145"));
        String response = mvc.perform(post("/api/interno/alertas")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = json.readTree(response).get("id").asText();

        mvc.perform(get("/api/alertas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
        mvc.perform(patch("/api/alertas/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"ATENDIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDA"));
        assertThat(alertas.findById(UUID.fromString(id)).orElseThrow().getEstado()).isEqualTo(Estado.ATENDIDA);
    }
}
