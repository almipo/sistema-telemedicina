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
import com.telemedicina.backend.service.TurnoService;
import com.telemedicina.backend.dto.response.TurnoResponse;
import com.telemedicina.backend.entity.EstadoTurno;
import java.time.LocalDate;
import java.time.LocalTime;

@WebMvcTest(TurnoController.class)
class TurnoControllerMockMvcTest {
    @Autowired MockMvc mvc;
    @MockitoBean TurnoService service;

    @Test void listarResponde200() throws Exception {
        when(service.listarDto()).thenReturn(List.of());
        mvc.perform(get("/api/turnos"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test void crearResponde201() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.crear(any())).thenReturn(new TurnoResponse(id,LocalDate.of(2026,12,10),LocalTime.of(10,30),EstadoTurno.A_CONFIRMAR,UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),UUID.fromString("550e8400-e29b-41d4-a716-446655440001")));
        mvc.perform(post("/api/turnos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fecha\":\"2026-12-10\",\"hora\":\"10:30:00\",\"pacienteId\":\"550e8400-e29b-41d4-a716-446655440000\",\"medicoId\":\"550e8400-e29b-41d4-a716-446655440001\",\"estado\":\"A_CONFIRMAR\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id.toString()));
        verify(service).crear(any());
    }

    @Test void datosInvalidosResponden400() throws Exception {
        mvc.perform(post("/api/turnos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fecha\":\"2026-12-10\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.estado").value(400));
        verify(service, never()).crear(any());
    }

    @Test void recursoInexistenteResponde404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.obtenerDto(id)).thenThrow(new RecursoNoEncontradoException("No encontrado"));
        mvc.perform(get("/api/turnos/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.estado").value(404));
    }

    @Test void eliminarResponde204() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(delete("/api/turnos/{id}", id)).andExpect(status().isNoContent());
        verify(service).eliminar(id);
    }

    @Test void confirmarTurnoResponde200() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.confirmar(id)).thenReturn(new TurnoResponse(id, LocalDate.of(2026,12,10),
            LocalTime.of(10,30), EstadoTurno.CONFIRMADO, UUID.randomUUID(), UUID.randomUUID()));
        mvc.perform(patch("/api/turnos/{id}/confirmar",id))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }

    @Test void cancelarTurnoResponde200() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.cancelar(id)).thenReturn(new TurnoResponse(id, LocalDate.of(2026,12,10),
            LocalTime.of(10,30), EstadoTurno.CANCELADO, UUID.randomUUID(), UUID.randomUUID()));
        mvc.perform(patch("/api/turnos/{id}/cancelar",id))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CANCELADO"));
    }
}
