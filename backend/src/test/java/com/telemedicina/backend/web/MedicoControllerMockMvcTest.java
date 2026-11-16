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
import com.telemedicina.backend.service.MedicoService;
import com.telemedicina.backend.dto.response.MedicoResponse;
import java.time.LocalDate;

@WebMvcTest(MedicoController.class)
class MedicoControllerMockMvcTest {
    @Autowired MockMvc mvc;
    @MockitoBean MedicoService service;

    @Test void listarResponde200() throws Exception {
        when(service.listarDto()).thenReturn(List.of());
        mvc.perform(get("/api/medicos"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test void crearResponde201() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.crear(any())).thenReturn(new MedicoResponse(id,"Ana","Garcia","28999888",LocalDate.of(1985,9,10),null,"ana@example.com","MN12345","Cardiologia"));
        mvc.perform(post("/api/medicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ana\",\"apellido\":\"Garcia\",\"documento\":\"28999888\",\"fechaNacimiento\":\"1985-09-10\",\"email\":\"ana@example.com\",\"matricula\":\"MN12345\",\"especialidad\":\"Cardiologia\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id.toString()));
        verify(service).crear(any());
    }

    @Test void datosInvalidosResponden400() throws Exception {
        mvc.perform(post("/api/medicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ana\",\"apellido\":\"Garcia\",\"documento\":\"28999888\",\"email\":\"correo-invalido\",\"matricula\":\"MN12345\",\"especialidad\":\"Cardiologia\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.estado").value(400));
        verify(service, never()).crear(any());
    }

    @Test void recursoInexistenteResponde404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.obtenerDto(id)).thenThrow(new RecursoNoEncontradoException("No encontrado"));
        mvc.perform(get("/api/medicos/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.estado").value(404));
    }

    @Test void eliminarResponde204() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(delete("/api/medicos/{id}", id)).andExpect(status().isNoContent());
        verify(service).eliminar(id);
    }
}
