package com.telemedicina.metricas.integration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.telemedicina.metricas.repository.MedicionRepository;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** HTTP real en loopback contra servidores simulados y persistencia MongoDB real. */
@SpringBootTest
@AutoConfigureMockMvc
class MedicionHttpIntegracionTest {
    private static final AtomicInteger backendStatus = new AtomicInteger(200);
    private static final AtomicInteger alertasStatus = new AtomicInteger(201);
    private static final AtomicInteger verificaciones = new AtomicInteger();
    private static final AtomicInteger alertasRecibidas = new AtomicInteger();
    private static final AtomicReference<String> alertaBody = new AtomicReference<>();
    private static final HttpServer backend = crearServidor("/api/interno/dispositivos", false);
    private static final HttpServer alertas = crearServidor("/api/interno/alertas", true);

    @Autowired MockMvc mvc;
    @Autowired MedicionRepository repository;
    private final UUID paciente = UUID.randomUUID();
    private final UUID dispositivo = UUID.randomUUID();
    private String idCreado;

    static HttpServer crearServidor(String ruta, boolean esAlertas) {
        try {
            HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            servidor.createContext(ruta, ex -> {
                if (esAlertas) {
                    alertasRecibidas.incrementAndGet();
                    alertaBody.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                    responder(ex, alertasStatus.get());
                } else {
                    verificaciones.incrementAndGet();
                    responder(ex, backendStatus.get());
                }
            });
            servidor.start();
            return servidor;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static void responder(HttpExchange ex, int status) throws IOException {
        try {
            ex.sendResponseHeaders(status, -1);
        } finally {
            ex.close();
        }
    }

    @DynamicPropertySource
    static void configurar(DynamicPropertyRegistry props) {
        props.add("spring.data.mongodb.uri", () -> System.getenv().getOrDefault(
                "MONGO_TEST_URI", "mongodb://localhost:27017/telemedicina_metricas_test"));
        props.add("services.backend", () -> "http://127.0.0.1:" + backend.getAddress().getPort());
        props.add("services.alertas", () -> "http://127.0.0.1:" + alertas.getAddress().getPort());
    }

    @BeforeEach
    void antes() {
        backendStatus.set(200);
        alertasStatus.set(201);
        verificaciones.set(0);
        alertasRecibidas.set(0);
        alertaBody.set(null);
    }

    @AfterEach
    void limpiar() {
        if (idCreado != null) repository.deleteById(idCreado);
        idCreado = null;
    }

    @AfterAll
    static void cerrar() {
        backend.stop(0);
        alertas.stop(0);
    }

    private String solicitud(int pulso) {
        return "{\"tipo\":\"PULSO\",\"valor\":" + pulso + ",\"pacienteId\":\""
                + paciente + "\",\"dispositivoId\":\"" + dispositivo + "\"}";
    }

    private String crearYGuardarId(int pulso, int statusEsperado) throws Exception {
        String cuerpo = mvc.perform(post("/api/mediciones")
                        .contentType("application/json").content(solicitud(pulso)))
                .andExpect(status().is(statusEsperado))
                .andReturn().getResponse().getContentAsString();
        var lista = repository.findByPacienteIdOrderByFechaHoraDesc(paciente);
        assertEquals(1, lista.size(), "La medición debe persistirse exactamente una vez");
        idCreado = lista.get(0).getId();
        return cuerpo;
    }

    @Test
    void medicionNormalVerificaBackendPersisteYNoGeneraAlerta() throws Exception {
        String cuerpo = crearYGuardarId(80, 201);
        assertTrue(cuerpo.contains(idCreado));
        assertEquals(1, verificaciones.get());
        assertEquals(0, alertasRecibidas.get());
        mvc.perform(get("/api/mediciones/" + idCreado)).andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("PULSO"))
                .andExpect(jsonPath("$.valor").value(80));
    }

    @Test
    void medicionCriticaEnviaContratoHttpAAlertasYPersiste() throws Exception {
        crearYGuardarId(130, 201);
        assertEquals(1, verificaciones.get());
        assertEquals(1, alertasRecibidas.get());
        String evento = alertaBody.get();
        assertNotNull(evento);
        assertTrue(evento.contains(idCreado), "La alerta debe incluir medicionId");
        assertTrue(evento.contains(paciente.toString()), "La alerta debe incluir pacienteId");
        assertTrue(evento.contains("PULSO"), "La alerta debe incluir tipo");
    }

    @Test
    void backendRechazaDispositivoYNoSePersisteNiSeEnviaAlerta() throws Exception {
        backendStatus.set(404);
        mvc.perform(post("/api/mediciones").contentType("application/json")
                .content(solicitud(80))).andExpect(status().isNotFound());
        assertEquals(1, verificaciones.get());
        assertEquals(0, alertasRecibidas.get());
        assertTrue(repository.findByPacienteIdOrderByFechaHoraDesc(paciente).isEmpty());
    }

    @Test
    void alertasFallaDevuelve502PeroConservaMedicionParaReconciliar() throws Exception {
        alertasStatus.set(503);
        String cuerpo = crearYGuardarId(130, 502);
        assertEquals(1, verificaciones.get());
        assertEquals(1, alertasRecibidas.get());
        assertTrue(cuerpo.contains(idCreado), "El error debe indicar el ID persistido");
    }
}
