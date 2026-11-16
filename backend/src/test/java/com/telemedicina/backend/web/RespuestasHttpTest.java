package com.telemedicina.backend.web;

import com.telemedicina.backend.dto.response.PacienteResponse;
import com.telemedicina.backend.exception.ConflictoException;
import com.telemedicina.backend.exception.RecursoNoEncontradoException;
import com.telemedicina.backend.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PacienteController.class)
class RespuestasHttpTest {
    @Autowired MockMvc mvc;
    @MockitoBean PacienteService service;

    @Test void crearPacienteResponde201YNoDevuelveEntidadesJpa() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.crear(any())).thenReturn(new PacienteResponse(id, "Ana", "Lopez", "12345",
            LocalDate.of(2000, 1, 1), "111", "ana@example.com"));
        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nombre":"Ana","apellido":"Lopez","documento":"12345",
                 "fechaNacimiento":"2000-01-01","telefono":"111","email":"ana@example.com"}
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test void correoMalFormadoResponde400() throws Exception {
        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nombre":"Ana","apellido":"Lopez","documento":"12345",
                 "fechaNacimiento":"2000-01-01","email":"invalido"}
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.estado").value(400));
        verify(service, never()).crear(any());
    }

    @Test void pacienteInexistenteResponde404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.obtenerDto(id)).thenThrow(new RecursoNoEncontradoException("Paciente inexistente"));
        mvc.perform(get("/api/pacientes/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.estado").value(404));
    }

    @Test void conflictoResponde409() throws Exception {
        when(service.crear(any())).thenThrow(new ConflictoException("Documento duplicado"));
        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nombre":"Ana","apellido":"Lopez","documento":"12345",
                 "fechaNacimiento":"2000-01-01","email":"ana@example.com"}
                """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.estado").value(409));
    }

    @Test void eliminacionExitosaResponde204() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(delete("/api/pacientes/{id}", id)).andExpect(status().isNoContent());
        verify(service).eliminar(id);
    }
}
