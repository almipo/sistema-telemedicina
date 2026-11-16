package com.telemedicina.simulador.service;

import com.telemedicina.simulador.model.MedicionSimulada;
import com.telemedicina.simulador.model.TipoMedicion;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.random.RandomGenerator;

/** Simula signos vitales ficticios: rangos educativos, no límites clínicos. */
public final class GeneradorMediciones {
    private final RandomGenerator random;
    private final double probabilidadRiesgo;

    public GeneradorMediciones(RandomGenerator random, double probabilidadRiesgo) {
        this.random = Objects.requireNonNull(random);
        if (!Double.isFinite(probabilidadRiesgo) || probabilidadRiesgo < 0 || probabilidadRiesgo > 1)
            throw new IllegalArgumentException("Probabilidad fuera de [0,1]");
        this.probabilidadRiesgo = probabilidadRiesgo;
    }

    public MedicionSimulada generar(TipoMedicion tipo, UUID paciente, UUID dispositivo) {
        boolean fueraDeRango = random.nextDouble() < probabilidadRiesgo;
        BigDecimal valor = switch (tipo) {
            case PULSO -> BigDecimal.valueOf(fueraDeRango ? entero(125, 155) : entero(65, 90));
            case PRESION_SISTOLICA -> BigDecimal.valueOf(fueraDeRango ? entero(145, 185) : entero(105, 130));
            case PRESION_DIASTOLICA -> BigDecimal.valueOf(fueraDeRango ? entero(95, 115) : entero(65, 85));
            case TEMPERATURA -> BigDecimal.valueOf(fueraDeRango ? entero(390, 410) : entero(360, 375), 1);
        };
        return new MedicionSimulada(tipo, valor, paciente, dispositivo);
    }

    private int entero(int minimo, int maximo) {
        return random.nextInt(minimo, maximo + 1);
    }
}
