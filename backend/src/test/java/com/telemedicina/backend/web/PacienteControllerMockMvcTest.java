package com.telemedicina.backend.web;

import com.telemedicina.backend.service.PacienteService;
import com.telemedicina.backend.dto.response.PacienteResponse;
import com.telemedicina.backend.web.PacienteController;
import com.telemedicina.backend.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(PacienteController.class) class PacienteControllerMockMvcTest {
 @Autowired MockMvc mvc;@MockitoBean PacienteService service;
 @Test void listarVacio() throws Exception{when(service.listarDto()).thenReturn(List.of());mvc.perform(get("/api/pacientes")).andExpect(status().isOk()).andExpect(content().json("[]"));}
 @Test void rechazarDatosIncompletos() throws Exception{mvc.perform(post("/api/pacientes").contentType("application/json").content("{}" )).andExpect(status().isBadRequest());}
}
