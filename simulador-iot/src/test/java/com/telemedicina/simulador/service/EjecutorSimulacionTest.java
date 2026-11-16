package com.telemedicina.simulador.service;

import com.sun.net.httpserver.HttpServer;
import com.telemedicina.simulador.client.ClienteMetricas;
import com.telemedicina.simulador.config.ConfiguracionSimulador;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class EjecutorSimulacionTest {
    @Test void respetaCantidadYAlternaCuatroTipos() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var contador = new AtomicInteger();
        var tipos = new java.util.ArrayList<String>();
        server.createContext("/api/mediciones", exchange -> {
            var json = new String(exchange.getRequestBody().readAllBytes());
            tipos.add(json.split("\"tipo\":\"")[1].split("\"")[0]);
            contador.incrementAndGet();
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        });
        server.start();
        var endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/mediciones");
        var config = new ConfiguracionSimulador(UUID.randomUUID(), UUID.randomUUID(),
            URI.create("http://127.0.0.1:" + server.getAddress().getPort()), Duration.ZERO, 5, 0, Duration.ofSeconds(5));
        try (var client = new ClienteMetricas(endpoint, Duration.ofSeconds(5))) {
            var salida = new ByteArrayOutputStream();
            new EjecutorSimulacion(config, new GeneradorMediciones(new Random(1), 0), client,
                new PrintStream(salida)).ejecutar();
            assertEquals(5, contador.get());
            assertEquals(java.util.List.of("PULSO", "PRESION_SISTOLICA", "PRESION_DIASTOLICA", "TEMPERATURA", "PULSO"), tipos);
            assertTrue(salida.toString().contains("201 CREATED"));
        } finally {server.stop(0);}
    }
}
