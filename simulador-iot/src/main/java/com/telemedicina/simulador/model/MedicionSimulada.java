package com.telemedicina.simulador.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/** Datos ficticios que se enviarán al DTO MedicionRequest (no es una entidad persistente). */
public record MedicionSimulada(TipoMedicion tipo, BigDecimal valor, UUID pacienteId, UUID dispositivoId) {
    public MedicionSimulada {
        Objects.requireNonNull(tipo, "tipo requerido");
        Objects.requireNonNull(valor, "valor requerido");
        Objects.requireNonNull(pacienteId, "pacienteId requerido");
        Objects.requireNonNull(dispositivoId, "dispositivoId requerido");
        if (valor.signum() <= 0) throw new IllegalArgumentException("El valor debe ser positivo");
    }

    /** JSON exacto que recibe POST /api/mediciones: no se necesita una librería externa. */
    public String aJson() {
        return "{\"tipo\":\"" + tipo.name() + "\",\"valor\":" + valor.toPlainString()
                + ",\"pacienteId\":\"" + pacienteId + "\",\"dispositivoId\":\"" + dispositivoId + "\"}";
    }
}
