package com.telemedicina.simulador.config;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Configuración externa; argumentos CLI prevalecen sobre variables de entorno. */
public record ConfiguracionSimulador(UUID pacienteId, UUID dispositivoId, URI apiBase,
                                     Duration intervalo, long cantidad, double riesgo,
                                     Duration timeout, boolean todos, URI backendBase) {
    /** Compatibilidad con el modo original y sus pruebas. */
    public ConfiguracionSimulador(UUID pacienteId, UUID dispositivoId, URI apiBase,
                                 Duration intervalo, long cantidad, double riesgo, Duration timeout) {
        this(pacienteId, dispositivoId, apiBase, intervalo, cantidad, riesgo, timeout,
            false, URI.create("http://localhost:8080"));
    }

    public ConfiguracionSimulador {
        if (!todos && (pacienteId == null || dispositivoId == null))
            throw new IllegalArgumentException("Se requieren UUID de paciente y dispositivo, o --todos");
        validarUrl(apiBase, "SIM_METRICAS_URL");
        validarUrl(backendBase, "SIM_BACKEND_URL");
        if (intervalo == null || intervalo.isNegative()) throw new IllegalArgumentException("Intervalo debe ser >= 0");
        if (cantidad < 0) throw new IllegalArgumentException("Cantidad debe ser >= 0");
        if (!Double.isFinite(riesgo) || riesgo < 0 || riesgo > 1)
            throw new IllegalArgumentException("Riesgo debe estar entre 0 y 1");
        if (timeout == null || timeout.isZero() || timeout.isNegative())
            throw new IllegalArgumentException("Timeout debe ser > 0");
    }

    private static void validarUrl(URI url, String nombre) {
        if (url == null || url.getScheme() == null
            || !(url.getScheme().equalsIgnoreCase("http") || url.getScheme().equalsIgnoreCase("https"))
            || url.getHost() == null || url.getUserInfo() != null || url.getQuery() != null
            || url.getFragment() != null)
            throw new IllegalArgumentException(nombre + " debe ser una URL HTTP(S) válida");
    }

    public URI endpoint() { return URI.create(apiBase.toString().replaceAll("/+$", "") + "/api/mediciones"); }
    public URI endpointDispositivos() { return URI.create(backendBase.toString().replaceAll("/+$", "") + "/api/dispositivos"); }

    public static ConfiguracionSimulador desde(String[] args, Map<String, String> env) {
        Map<String, String> cli = parsearArgumentos(args);
        boolean todos = cli.containsKey("todos") || Boolean.parseBoolean(env.getOrDefault("SIM_TODOS", "false"));
        String paciente = valor(cli, "paciente", env, "SIM_PACIENTE_ID", null);
        String dispositivo = valor(cli, "dispositivo", env, "SIM_DISPOSITIVO_ID", null);
        if (!todos && (paciente == null || dispositivo == null))
            throw new IllegalArgumentException("Faltan IDs de paciente y dispositivo (o --todos)");
        try {
            return new ConfiguracionSimulador(
                paciente == null || todos ? null : UUID.fromString(paciente),
                dispositivo == null || todos ? null : UUID.fromString(dispositivo),
                URI.create(valor(cli, "api", env, "SIM_METRICAS_URL", "http://localhost:8081")),
                Duration.ofMillis(Math.round(Double.parseDouble(valor(cli, "intervalo", env, "SIM_INTERVALO_SEGUNDOS", "5")) * 1000)),
                Long.parseLong(valor(cli, "cantidad", env, "SIM_CANTIDAD", "0")),
                Double.parseDouble(valor(cli, "riesgo", env, "SIM_RIESGO", "0.20")),
                Duration.ofMillis(Math.round(Double.parseDouble(valor(cli, "timeout", env, "SIM_TIMEOUT_SEGUNDOS", "10")) * 1000)),
                todos,
                URI.create(valor(cli, "backend", env, "SIM_BACKEND_URL", "http://localhost:8080"))
            );
        } catch (NumberFormatException | java.time.DateTimeException e) {
            throw new IllegalArgumentException("Valores inválidos en la configuración", e);
        }
    }

    private static String valor(Map<String, String> cli, String opcion, Map<String, String> env, String clave, String defecto) {
        return cli.containsKey(opcion) ? cli.get(opcion) : env.getOrDefault(clave, defecto);
    }

    private static Map<String, String> parsearArgumentos(String[] args) {
        Map<String, String> parsed = new HashMap<>();
        Set<String> permitidas = Set.of("paciente", "dispositivo", "api", "backend", "intervalo", "cantidad", "riesgo", "timeout");
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--help")) throw new SolicitudAyudaException();
            if (arg.equals("--todos")) {
                if (parsed.putIfAbsent("todos", "true") != null) throw new IllegalArgumentException("Opción duplicada: --todos");
                continue;
            }
            if (!arg.startsWith("--") || i + 1 >= args.length || args[i+1].startsWith("--"))
                throw new IllegalArgumentException("Argumento inválido: " + arg);
            String key = arg.substring(2);
            if (!permitidas.contains(key)) throw new IllegalArgumentException("Opción desconocida: " + arg);
            if (parsed.putIfAbsent(key, args[++i]) != null) throw new IllegalArgumentException("Opción duplicada: " + key);
        }
        return parsed;
    }

    public static final class SolicitudAyudaException extends RuntimeException {}
}
