package com.telemedicina.simulador.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MedicionSimuladaTest {
    private final UUID paciente = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID dispositivo = UUID.fromString("00000000-0000-0000-0000-000000000002");
    @Test void serializaExactamenteContratoDeMetricas() {
        var medicion = new MedicionSimulada(TipoMedicion.TEMPERATURA, new BigDecimal("36.7"), paciente, dispositivo);
        assertEquals("{\"tipo\":\"TEMPERATURA\",\"valor\":36.7,\"pacienteId\":\"" + paciente
            + "\",\"dispositivoId\":\"" + dispositivo + "\"}", medicion.aJson());
    }
    @Test void rechazaValorNuloONoPositivo() {
        assertThrows(IllegalArgumentException.class,
            () -> new MedicionSimulada(TipoMedicion.PULSO, BigDecimal.ZERO, paciente, dispositivo));
        assertThrows(NullPointerException.class,
            () -> new MedicionSimulada(null, BigDecimal.ONE, paciente, dispositivo));
    }
}
