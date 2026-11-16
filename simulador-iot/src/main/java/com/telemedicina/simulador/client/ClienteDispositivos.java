package com.telemedicina.simulador.client;

import com.telemedicina.simulador.model.DispositivoDisponible;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Obtiene dispositivos desde el backend (no consulta la base de datos directamente). */
public final class ClienteDispositivos implements AutoCloseable {
    private static final Pattern ATRIBUTO = Pattern.compile("\\\"(id|pacienteId|tipo|estado)\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"");
    private final HttpClient cliente;
    private final URI url;
    private final Duration timeout;

    public ClienteDispositivos(URI url, Duration timeout) {
        this(HttpClient.newBuilder().connectTimeout(timeout).build(), url, timeout);
    }
    public ClienteDispositivos(HttpClient cliente, URI url, Duration timeout) {
        this.cliente = cliente;
        this.url = url;
        this.timeout = timeout;
    }

    public List<DispositivoDisponible> obtenerActivos() throws IOException, InterruptedException {
        var peticion = HttpRequest.newBuilder(url).GET().timeout(timeout)
            .header("Accept", "application/json").build();
        var respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
        if (respuesta.statusCode() != 200)
            throw new IOException("GET " + url + " devolvió HTTP " + respuesta.statusCode());
        return parsearDispositivos(respuesta.body());
    }

    /** Parsea únicamente los cuatro campos JSON de los DTO planos del backend. */
    public static List<DispositivoDisponible> parsearDispositivos(String json) throws IOException {
        if (json == null || !json.trim().startsWith("[") || !json.trim().endsWith("]"))
            throw new IOException("Respuesta de dispositivos inválida: se esperaba lista JSON");
        List<DispositivoDisponible> resultado = new ArrayList<>();
        for (String objeto : objetos(json)) {
            Map<String, String> campos = new HashMap<>();
            Matcher matcher = ATRIBUTO.matcher(objeto);
            while (matcher.find()) campos.put(matcher.group(1), matcher.group(2));
            if (!"ACTIVO".equalsIgnoreCase(campos.get("estado"))) continue;
            try {
                UUID id = UUID.fromString(campos.get("id"));
                UUID paciente = UUID.fromString(campos.get("pacienteId"));
                String tipo = campos.get("tipo");
                var mediciones = DispositivoDisponible.tiposPara(tipo);
                if (!mediciones.isEmpty()) resultado.add(new DispositivoDisponible(id, paciente, tipo, mediciones));
                else System.err.println("[OMITIDO] Tipo de dispositivo no reconocido: " + tipo + " (" + id + ")");
            } catch (IllegalArgumentException | NullPointerException e) {
                System.err.println("[OMITIDO] Dispositivo activo sin identificadores válidos");
            }
        }
        return resultado;
    }

    private static List<String> objetos(String json) throws IOException {
        List<String> objetos = new ArrayList<>();
        int profundidad = 0, inicio = -1;
        boolean cadena = false, escape = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (cadena) {
                if (escape) escape = false;
                else if (c == '\\') escape = true;
                else if (c == '"') cadena = false;
                continue;
            }
            if (c == '"') cadena = true;
            else if (c == '{') { if (profundidad++ == 0) inicio = i; }
            else if (c == '}') {
                if (--profundidad < 0) throw new IOException("JSON de dispositivos inválido");
                if (profundidad == 0) objetos.add(json.substring(inicio, i + 1));
            }
        }
        if (profundidad != 0 || cadena) throw new IOException("JSON de dispositivos incompleto");
        return objetos;
    }

    @Override public void close() { cliente.close(); }
}
