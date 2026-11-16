package com.telemedicina.simulador.client;

import com.telemedicina.simulador.model.MedicionSimulada;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/** Cliente HTTP que transmite al microservicio de métricas, nunca directamente a MongoDB. */
public final class ClienteMetricas implements AutoCloseable {
    private final HttpClient client;
    private final URI endpoint;
    private final Duration timeout;

    public ClienteMetricas(URI endpoint, Duration timeout) {
        this(HttpClient.newBuilder().connectTimeout(timeout).build(), endpoint, timeout);
    }

    // Visible para pruebas automatizadas; se inyecta un cliente HTTP de prueba.
    public ClienteMetricas(HttpClient client, URI endpoint, Duration timeout) {
        this.client = Objects.requireNonNull(client);
        this.endpoint = Objects.requireNonNull(endpoint);
        this.timeout = Objects.requireNonNull(timeout);
    }

    public ResultadoEnvio enviar(MedicionSimulada medicion) throws IOException, InterruptedException {
        HttpRequest solicitud = HttpRequest.newBuilder(endpoint)
            .timeout(timeout)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(medicion.aJson()))
            .build();
        HttpResponse<String> respuesta = client.send(solicitud, HttpResponse.BodyHandlers.ofString());
        return new ResultadoEnvio(respuesta.statusCode(), respuesta.body());
    }

    public record ResultadoEnvio(int codigoHttp, String cuerpo) {
        public boolean correcto() { return codigoHttp == 201; }
    }

    @Override public void close() { client.close(); }
}
