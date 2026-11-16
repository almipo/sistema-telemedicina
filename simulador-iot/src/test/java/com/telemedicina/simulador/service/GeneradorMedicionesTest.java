package com.telemedicina.simulador.service;

import com.telemedicina.simulador.model.TipoMedicion;
import org.junit.jupiter.api.Test;
import java.util.Random;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class GeneradorMedicionesTest {
    private final UUID id = UUID.randomUUID();
    @Test void riesgoCeroGeneraSoloValoresNormalesParaTodosLosTipos() {
        var generador = new GeneradorMediciones(new Random(19), 0);
        for (var tipo : TipoMedicion.values()) for (int i = 0; i < 30; i++) {
            double v = generador.generar(tipo, id, id).valor().doubleValue();
            switch (tipo) {
                case PULSO -> assertTrue(v >= 65 && v <= 90);
                case PRESION_SISTOLICA -> assertTrue(v >= 105 && v <= 130);
                case PRESION_DIASTOLICA -> assertTrue(v >= 65 && v <= 85);
                case TEMPERATURA -> assertTrue(v >= 36.0 && v <= 37.5);
            }
        }
    }
    @Test void riesgoUnoGeneraSoloValoresFueraDeRango() {
        var generador = new GeneradorMediciones(new Random(20), 1);
        for (var tipo : TipoMedicion.values()) {
            double v = generador.generar(tipo, id, id).valor().doubleValue();
            switch (tipo) {
                case PULSO -> assertTrue(v > 120);
                case PRESION_SISTOLICA -> assertTrue(v > 140);
                case PRESION_DIASTOLICA -> assertTrue(v > 90);
                case TEMPERATURA -> assertTrue(v > 38);
            }
        }
    }
    @Test void rechazaProbabilidadInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new GeneradorMediciones(new Random(), -0.1));
        assertThrows(IllegalArgumentException.class, () -> new GeneradorMediciones(new Random(), Double.NaN));
    }
}
