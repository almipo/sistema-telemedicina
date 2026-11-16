package com.telemedicina.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemedicina.backend.entity.Dispositivo;
import com.telemedicina.backend.entity.EstadoDispositivo;
import com.telemedicina.backend.entity.Paciente;
import com.telemedicina.backend.repository.DispositivoRepository;
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

/**
 * Integración real REST -> controlador -> servicio -> repositorio -> PostgreSQL.
 * Cada prueba realiza rollback para no dejar datos en telemedicina_dev.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class BackendIntegracionTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PacienteRepository pacientes;
    @Autowired DispositivoRepository dispositivos;

    private Paciente crearPaciente() {
        Paciente p = new Paciente();
        p.setNombre("Paciente");
        p.setApellido("Integracion");
        p.setDocumento("TEST-" + UUID.randomUUID());
        p.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        p.setTelefono("1111111111");
        p.setEmail("integracion@example.com");
        return pacientes.saveAndFlush(p);
    }

    private Dispositivo crearDispositivo(Paciente paciente, EstadoDispositivo estado) {
        Dispositivo d = new Dispositivo();
        d.setTipo("PULSOMETRO");
        d.setNombre("Sensor integracion");
        d.setNumeroSerie("TEST-" + UUID.randomUUID());
        d.setEstado(estado);
        d.setPaciente(paciente);
        return dispositivos.saveAndFlush(d);
    }

    @Test
    void altaPacientePorRestPersisteEnPostgresql() throws Exception {
        String documento = "TEST-" + UUID.randomUUID();
        String cuerpo = json.writeValueAsString(Map.of(
            "nombre", "Ana", "apellido", "Prueba", "documento", documento,
            "fechaNacimiento", "1995-05-10", "telefono", "1112345678",
            "email", "ana.prueba@example.com"));

        String respuesta = mvc.perform(post("/api/pacientes")
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.documento").value(documento))
            .andReturn().getResponse().getContentAsString();

        UUID id = UUID.fromString(json.readTree(respuesta).path("id").asText());
        assertTrue(pacientes.findById(id).isPresent());
        mvc.perform(get("/api/pacientes/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void datosInvalidosDevuelven400SinPersistir() throws Exception {
        long antes = pacientes.count();
        mvc.perform(post("/api/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"\",\"apellido\":\"\",\"email\":\"invalido\"}"))
            .andExpect(status().isBadRequest());
        assertEquals(antes, pacientes.count());
    }

    @Test
    void endpointInternoAceptaDispositivoActivoDelPaciente() throws Exception {
        Paciente paciente = crearPaciente();
        Dispositivo dispositivo = crearDispositivo(paciente, EstadoDispositivo.ACTIVO);
        mvc.perform(get("/api/interno/dispositivos/{id}/verificar", dispositivo.getId())
                .param("pacienteId", paciente.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(dispositivo.getId().toString()));
    }

    @Test
    void endpointInternoRechazaDispositivoInactivoYOtroPaciente() throws Exception {
        Paciente paciente = crearPaciente();
        Dispositivo dispositivo = crearDispositivo(paciente, EstadoDispositivo.INACTIVO);
        mvc.perform(get("/api/interno/dispositivos/{id}/verificar", dispositivo.getId())
                .param("pacienteId", paciente.getId().toString()))
            .andExpect(status().isBadRequest());

        dispositivo.setEstado(EstadoDispositivo.ACTIVO);
        dispositivos.saveAndFlush(dispositivo);
        mvc.perform(get("/api/interno/dispositivos/{id}/verificar", dispositivo.getId())
                .param("pacienteId", UUID.randomUUID().toString()))
            .andExpect(status().isBadRequest());
    }
}
