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
import com.telemedicina.backend.service.DispositivoService;
import com.telemedicina.backend.dto.response.DispositivoResponse;
import com.telemedicina.backend.entity.EstadoDispositivo;

@WebMvcTest(DispositivoController.class)
class DispositivoControllerMockMvcTest {
    @Autowired MockMvc mvc;
    @MockitoBean DispositivoService service;

    @Test void listarResponde200() throws Exception {
        when(service.listarDto()).thenReturn(List.of());
        mvc.perform(get("/api/dispositivos"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test void crearResponde201() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.crear(any())).thenReturn(new DispositivoResponse(id,"PULSOMETRO","S-001",EstadoDispositivo.ACTIVO,"Pulsera",UUID.fromString("550e8400-e29b-41d4-a716-446655440000")));
        mvc.perform(post("/api/dispositivos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"PULSOMETRO\",\"numeroSerie\":\"S-001\",\"estado\":\"ACTIVO\",\"nombre\":\"Pulsera\",\"pacienteId\":\"550e8400-e29b-41d4-a716-446655440000\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id.toString()));
        verify(service).crear(any());
    }

    @Test void datosInvalidosResponden400() throws Exception {
        mvc.perform(post("/api/dispositivos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"\",\"numeroSerie\":\"S-001\",\"estado\":\"ACTIVO\",\"nombre\":\"Pulsera\",\"pacienteId\":\"550e8400-e29b-41d4-a716-446655440000\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.estado").value(400));
        verify(service, never()).crear(any());
    }

    @Test void recursoInexistenteResponde404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.obtenerDto(id)).thenThrow(new RecursoNoEncontradoException("No encontrado"));
        mvc.perform(get("/api/dispositivos/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.estado").value(404));
    }

    @Test void eliminarResponde204() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(delete("/api/dispositivos/{id}", id)).andExpect(status().isNoContent());
        verify(service).eliminar(id);
    }
}
