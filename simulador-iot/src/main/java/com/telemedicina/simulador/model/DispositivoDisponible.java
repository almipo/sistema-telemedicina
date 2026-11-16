package com.telemedicina.simulador.model;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Un dispositivo activo y los signos vitales que su clase permite generar. */
public record DispositivoDisponible(UUID id, UUID pacienteId, String tipo, List<TipoMedicion> mediciones) {
    public static List<TipoMedicion> tiposPara(String valor) {
        if (valor == null) return List.of();
        String tipo = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT).trim().replaceAll("[^A-Z0-9]+", "_");
        if (tipo.contains("MULTIPARAMETRICO") || tipo.contains("MULTIPARAMETRO"))
            return List.of(TipoMedicion.values());
        if (tipo.contains("TENSIOMETRO") || tipo.contains("PRESION") || tipo.contains("ESFIGMOMANOMETRO"))
            return List.of(TipoMedicion.PRESION_SISTOLICA, TipoMedicion.PRESION_DIASTOLICA);
        if (tipo.contains("TERMOMETRO") || tipo.contains("TEMPERATURA"))
            return List.of(TipoMedicion.TEMPERATURA);
        if (tipo.contains("PULSOMETRO") || tipo.contains("PULSO") || tipo.contains("FRECUENCIA_CARDIACA")
            || tipo.contains("CARDIOMETRO") || tipo.contains("CARDIACO"))
            return List.of(TipoMedicion.PULSO);
        return List.of();
    }
}
