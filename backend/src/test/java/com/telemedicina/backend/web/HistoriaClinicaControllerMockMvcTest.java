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
import com.telemedicina.backend.service.HistoriaClinicaService;
import com.telemedicina.backend.dto.response.HistoriaClinicaResponse;
import com.telemedicina.backend.entity.EstadoHistorial;

@WebMvcTest(HistoriaClinicaController.class)
class HistoriaClinicaControllerMockMvcTest {
    @Autowired MockMvc mvc;
    @MockitoBean HistoriaClinicaService service;

    @Test void listarResponde200() throws Exception {
        when(service.listarDto()).thenReturn(List.of());
        mvc.perform(get("/api/historias"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test void crearResponde201() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.crear(any())).thenReturn(new HistoriaClinicaResponse(id,UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),EstadoHistorial.ACTIVO,List.of(),List.of(),List.of()));
        mvc.perform(post("/api/historias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pacienteId\":\"550e8400-e29b-41d4-a716-446655440000\",\"estado\":\"ACTIVO\",\"notasMedicas\":[],\"historialMedicacion\":[],\"medicosIds\":[]}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id.toString()));
        verify(service).crear(any());
    }

    @Test void datosInvalidosResponden400() throws Exception {
        mvc.perform(post("/api/historias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"ACTIVO\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.estado").value(400));
        verify(service, never()).crear(any());
    }

    @Test void recursoInexistenteResponde404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.obtenerDto(id)).thenThrow(new RecursoNoEncontradoException("No encontrado"));
        mvc.perform(get("/api/historias/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.estado").value(404));
    }

    @Test void eliminarResponde204() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(delete("/api/historias/{id}", id)).andExpect(status().isNoContent());
        verify(service).eliminar(id);
    }
}
