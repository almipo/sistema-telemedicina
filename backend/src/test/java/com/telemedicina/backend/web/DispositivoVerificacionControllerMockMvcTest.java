package com.telemedicina.backend.web;

import com.telemedicina.backend.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.telemedicina.backend.repository.DispositivoRepository;
import com.telemedicina.backend.entity.Dispositivo;
import com.telemedicina.backend.entity.EstadoDispositivo;

@WebMvcTest(DispositivoVerificacionController.class)
class DispositivoVerificacionControllerMockMvcTest {
    @Autowired MockMvc mvc;
    @MockitoBean DispositivoRepository repo;

    private Dispositivo dispositivo(UUID id, UUID paciente, EstadoDispositivo estado) {
        Dispositivo d = new Dispositivo();
        d.setId(id);
        d.setPacienteId(paciente);
        d.setTipo("PULSOMETRO");
        d.setNumeroSerie("SERIE-1");
        d.setNombre("Pulsera");
        d.setEstado(estado);
        return d;
    }

    @Test void activoYAsociadoResponde200() throws Exception {
        UUID id = UUID.randomUUID(), paciente = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(dispositivo(id, paciente, EstadoDispositivo.ACTIVO)));
        mvc.perform(get("/api/interno/dispositivos/{id}/verificar", id).param("pacienteId", paciente.toString()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(id.toString()))
           .andExpect(jsonPath("$.pacienteId").value(paciente.toString()));
    }

    @Test void inactivoResponde400() throws Exception {
        UUID id = UUID.randomUUID(), paciente = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(dispositivo(id, paciente, EstadoDispositivo.INACTIVO)));
        mvc.perform(get("/api/interno/dispositivos/{id}/verificar", id).param("pacienteId", paciente.toString()))
           .andExpect(status().isBadRequest());
    }

    @Test void pacienteDistintoResponde400() throws Exception {
        UUID id = UUID.randomUUID(), paciente = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(dispositivo(id, UUID.randomUUID(), EstadoDispositivo.ACTIVO)));
        mvc.perform(get("/api/interno/dispositivos/{id}/verificar", id).param("pacienteId", paciente.toString()))
           .andExpect(status().isBadRequest());
    }

    @Test void inexistenteResponde404() throws Exception {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        mvc.perform(get("/api/interno/dispositivos/{id}/verificar", id).param("pacienteId", UUID.randomUUID().toString()))
           .andExpect(status().isNotFound());
    }
}
