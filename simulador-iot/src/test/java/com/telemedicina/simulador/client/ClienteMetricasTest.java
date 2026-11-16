package com.telemedicina.simulador.client;

import com.sun.net.httpserver.HttpServer;
import com.telemedicina.simulador.model.MedicionSimulada;
import com.telemedicina.simulador.model.TipoMedicion;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class ClienteMetricasTest {
    @Test void enviaPostJsonYRecibe201() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var metodo = new AtomicReference<String>();
        var tipoContenido = new AtomicReference<String>();
        var body = new AtomicReference<String>();
        server.createContext("/api/mediciones", exchange -> {
            metodo.set(exchange.getRequestMethod());
            tipoContenido.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            var bytes = "{\"id\":\"mongo123\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(201, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        try (var cliente = new ClienteMetricas(java.net.URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/mediciones"), Duration.ofSeconds(5))) {
            var id = UUID.randomUUID();
            var resultado = cliente.enviar(new MedicionSimulada(TipoMedicion.PULSO, BigDecimal.valueOf(78), id, id));
            assertTrue(resultado.correcto());
            assertEquals(201, resultado.codigoHttp());
            assertEquals("POST", metodo.get());
            assertEquals("application/json", tipoContenido.get());
            assertTrue(body.get().contains("\"tipo\":\"PULSO\""));
        } finally { server.stop(0); }
    }
    @Test void noTrata400ComoExito() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/mediciones", exchange -> {exchange.sendResponseHeaders(400, -1); exchange.close();});
        server.start();
        try (var cliente = new ClienteMetricas(java.net.URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/mediciones"), Duration.ofSeconds(5))) {
            var id = UUID.randomUUID();
            assertFalse(cliente.enviar(new MedicionSimulada(TipoMedicion.PULSO, BigDecimal.valueOf(80), id, id)).correcto());
        } finally { server.stop(0); }
    }
}
