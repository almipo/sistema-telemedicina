package com.telemedicina.simulador.config;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ConfiguracionSimuladorTest {
    private static final String PACIENTE = "00000000-0000-0000-0000-000000000001";
    private static final String DISPOSITIVO = "00000000-0000-0000-0000-000000000002";

    @Test void usaDefaultsYConstruyeEndpointCorrecto() {
        var config = ConfiguracionSimulador.desde(new String[]{"--paciente", PACIENTE, "--dispositivo", DISPOSITIVO}, Map.of());
        assertEquals(URI.create("http://localhost:8081/api/mediciones"), config.endpoint());
        assertEquals(5, config.intervalo().getSeconds());
        assertEquals(0, config.cantidad());
        assertEquals(0.20, config.riesgo());
    }
    @Test void losArgumentosSobrescribenElEntorno() {
        var env = Map.of("SIM_PACIENTE_ID", PACIENTE, "SIM_DISPOSITIVO_ID", DISPOSITIVO,
            "SIM_CANTIDAD", "1", "SIM_METRICAS_URL", "http://metricas:8081/");
        var config = ConfiguracionSimulador.desde(new String[]{"--cantidad", "4", "--riesgo", "0"}, env);
        assertEquals(4, config.cantidad());
        assertEquals(0, config.riesgo());
        assertEquals("http://metricas:8081/api/mediciones", config.endpoint().toString());
    }
    @Test void exigeIdentificadores() {
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionSimulador.desde(new String[]{}, Map.of()));
    }
    @Test void rechazaIntervaloNegativoYProbabilidadInvalida() {
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionSimulador.desde(new String[]{"--paciente", PACIENTE,
            "--dispositivo", DISPOSITIVO, "--intervalo", "-1"}, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionSimulador.desde(new String[]{"--paciente", PACIENTE,
            "--dispositivo", DISPOSITIVO, "--riesgo", "1.1"}, Map.of()));
    }
    @Test void rechazaOpcionesDuplicadasYUrlNoHttp() {
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionSimulador.desde(new String[]{"--paciente", PACIENTE,
            "--dispositivo", DISPOSITIVO, "--riesgo", "0", "--riesgo", "1"}, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracionSimulador(UUID.fromString(PACIENTE),
            UUID.fromString(DISPOSITIVO), URI.create("file:///tmp/x"), Duration.ZERO, 0, 0, Duration.ofSeconds(10)));
    }
}
