package com.telemedicina.metricas.web;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemedicina.metricas.dto.response.MedicionResponse;
import com.telemedicina.metricas.exception.*;
import com.telemedicina.metricas.service.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(MedicionController.class)
class MedicionControllerTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 @MockBean MedicionService servicio;
 @MockBean ProcesamientoMetricas procesamiento;
 private final UUID paciente=UUID.randomUUID(),dispositivo=UUID.randomUUID();
 private String datos(String tipo,String valor){return "{\"tipo\":\""+tipo+"\",\"valor\":"+valor+",\"pacienteId\":\""+paciente+"\",\"dispositivoId\":\""+dispositivo+"\"}";}
 @Test void getVacio() throws Exception {when(servicio.listar(null)).thenReturn(List.of());mvc.perform(get("/api/mediciones")).andExpect(status().isOk()).andExpect(content().json("[]"));}
 @Test void getNoExistente() throws Exception {when(servicio.obtener("falso")).thenThrow(new RecursoNoEncontradoException("No encontrada"));mvc.perform(get("/api/mediciones/falso")).andExpect(status().isNotFound());}
 @Test void postCorrecto() throws Exception {when(procesamiento.procesar(any())).thenReturn(new MedicionResponse("abc","PULSO",BigDecimal.valueOf(80),Instant.now(),dispositivo,paciente,"lpm"));mvc.perform(post("/api/mediciones").contentType("application/json").content(datos("PULSO","80"))).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value("abc"));}
 @Test void postInvalido() throws Exception {mvc.perform(post("/api/mediciones").contentType("application/json").content(datos("","0"))).andExpect(status().isBadRequest());verifyNoInteractions(procesamiento);}
 @Test void postTipoNoSoportado() throws Exception {when(procesamiento.procesar(any())).thenThrow(new MedicionInvalidaException("Tipo no soportado"));mvc.perform(post("/api/mediciones").contentType("application/json").content(datos("OTRO","80"))).andExpect(status().isBadRequest());}
 @Test void backendRechazaDispositivo() throws Exception {when(procesamiento.procesar(any())).thenThrow(new ServicioExternoException("No existe",HttpStatus.NOT_FOUND,new RuntimeException()));mvc.perform(post("/api/mediciones").contentType("application/json").content(datos("PULSO","80"))).andExpect(status().isNotFound());}
 @Test void fallaEnvioAlerta() throws Exception {when(procesamiento.procesar(any())).thenThrow(new AlertaPendienteException("id-persistido",new RuntimeException()));mvc.perform(post("/api/mediciones").contentType("application/json").content(datos("PULSO","150"))).andExpect(status().isBadGateway()).andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("id-persistido")));}
}
