package com.telemedicina.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemedicina.backend.entity.EstadoHistorial;
import com.telemedicina.backend.entity.HistoriaClinica;
import com.telemedicina.backend.entity.Paciente;
import com.telemedicina.backend.repository.HistoriaClinicaRepository;
import com.telemedicina.backend.repository.MedicoRepository;
import com.telemedicina.backend.repository.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Cobertura adicional del TP: modificación de pacientes, registro de médicos y GraphQL. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class BackendCoberturaTpTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PacienteRepository pacientes;
    @Autowired MedicoRepository medicos;
    @Autowired HistoriaClinicaRepository historias;

    @Test
    void modificaYConsultaPacientePorRest() throws Exception {
        Paciente p = new Paciente();
        p.setNombre("NombreOriginal");
        p.setApellido("Prueba");
        p.setDocumento("TEST-" + UUID.randomUUID());
        p.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        p.setTelefono("1111111111");
        p.setEmail("original@example.com");
        UUID id = pacientes.saveAndFlush(p).getId();

        String body = json.writeValueAsString(Map.of(
            "nombre", "NombreModificado", "apellido", "Prueba", "documento", p.getDocumento(),
            "fechaNacimiento", "1990-01-01", "telefono", "1111111111",
            "email", "modificado@example.com"));
        mvc.perform(put("/api/pacientes/{id}", id)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("NombreModificado"));
        mvc.perform(get("/api/pacientes/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("modificado@example.com"));
        assertEquals("NombreModificado", pacientes.findById(id).orElseThrow().getNombre());
    }

    @Test
    void registraYConsultaMedicoEnPostgresql() throws Exception {
        String documento = "TEST-" + UUID.randomUUID();
        String body = json.writeValueAsString(Map.of(
            "nombre", "Ana", "apellido", "Medica", "documento", documento,
            "fechaNacimiento", "1985-09-10", "telefono", "1122223333",
            "email", "ana.medica@example.com", "matricula", "MN-" + UUID.randomUUID(),
            "especialidad", "Cardiologia"));

        String respuesta = mvc.perform(post("/api/medicos")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.documento").value(documento))
            .andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(json.readTree(respuesta).path("id").asText());
        assertTrue(medicos.findById(id).isPresent());
        mvc.perform(get("/api/medicos/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.especialidad").value("Cardiologia"));
    }

    @Test
    void consultaGraphqlHistoriaClinicaPersistida() throws Exception {
        Paciente p = new Paciente();
        p.setNombre("Paciente");
        p.setApellido("Graphql");
        p.setDocumento("TEST-" + UUID.randomUUID());
        p.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        p.setTelefono("1111111111");
        p.setEmail("graphql@example.com");
        p = pacientes.saveAndFlush(p);

        HistoriaClinica h = new HistoriaClinica();
        h.setPaciente(p);
        h.setEstado(EstadoHistorial.ACTIVO);
        h = historias.saveAndFlush(h);

        String consulta = "query { historiaClinica(id: \"" + h.getId() + "\") { id pacienteId estado } }";
        String body = json.writeValueAsString(Map.of("query", consulta));
        String response = mvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        JsonNode result = json.readTree(response);
        assertFalse(result.has("errors"), "GraphQL devolvio errores: " + response);
        assertEquals(h.getId().toString(), result.path("data").path("historiaClinica").path("id").asText());
        assertEquals(p.getId().toString(), result.path("data").path("historiaClinica").path("pacienteId").asText());
        assertEquals("ACTIVO", result.path("data").path("historiaClinica").path("estado").asText());
    }
}
