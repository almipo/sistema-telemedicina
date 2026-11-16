package com.telemedicina.alertas.integration;

import com.telemedicina.alertas.service.AlertasService;
import com.telemedicina.alertas.service.GestorAlertas;
import com.telemedicina.alertas.web.AlertaController;
import com.telemedicina.alertas.web.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Pruebas de contrato HTTP y validaciones, sin iniciar PostgreSQL. */
@WebMvcTest(AlertaController.class)
@Import(ApiExceptionHandler.class)
class AlertasApiMockMvcTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private GestorAlertas gestor;
    @MockitoBean private AlertasService servicio;

    @Test
    void rechazaAlertaSinCamposObligatorios() throws Exception {
        mvc.perform(post("/api/interno/alertas")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void rechazaPacienteIdInvalido() throws Exception {
        mvc.perform(post("/api/interno/alertas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicionId\":\"m1\",\"pacienteId\":\"no-es-uuid\",\"tipo\":\"PULSO\",\"mensaje\":\"alto\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaEstadoDeAlertaInvalido() throws Exception {
        mvc.perform(patch("/api/alertas/{id}/estado", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"INEXISTENTE\"}"))
                .andExpect(status().isBadRequest());
    }
}
