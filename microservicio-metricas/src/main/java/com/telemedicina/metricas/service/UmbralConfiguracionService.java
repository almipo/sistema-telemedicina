package com.telemedicina.metricas.service;

import com.telemedicina.metricas.dto.request.UmbralRequest;
import com.telemedicina.metricas.model.UmbralConfiguracion;
import com.telemedicina.metricas.repository.UmbralConfiguracionRepository;
import com.telemedicina.metricas.service.estrategia.*;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class UmbralConfiguracionService {
    private final UmbralConfiguracionRepository repo;
    private final ProcesamientoMetricas procesamiento;
    private final Map<String, EstrategiaUmbral> estrategias;

    public UmbralConfiguracionService(UmbralConfiguracionRepository repo,
                                     ProcesamientoMetricas procesamiento,
                                     @Qualifier("estrategiasUmbral") Map<String, EstrategiaUmbral> estrategias) {
        this.repo = repo;
        this.procesamiento = procesamiento;
        this.estrategias = estrategias;
    }

    /** Al reiniciar, aplica los valores guardados en MongoDB sobre los valores iniciales del YAML. */
    @PostConstruct
    public void restaurar() {
        for (UmbralConfiguracion u : repo.findAll()) {
            if (estrategias.containsKey(u.getTipo())) {
                try {
                    procesamiento.registrarUmbral(u.getTipo(), crearEstrategia(u.getTipo(), u.getMinimo(), u.getMaximo()));
                } catch (IllegalArgumentException ex) {
                    throw new IllegalStateException("Configuración de umbral inválida en MongoDB para " + u.getTipo(), ex);
                }
            }
        }
    }

    public List<UmbralConfiguracion> listar() {
        return estrategias.keySet().stream().sorted(Comparator.naturalOrder())
            .map(tipo -> repo.findById(tipo).orElseGet(() -> {
                EstrategiaUmbral e = estrategias.get(tipo);
                if (e instanceof UmbralFrecuencia u) return new UmbralConfiguracion(tipo, u.getMinimo(), u.getMaximo());
                if (e instanceof UmbralPresion u) return new UmbralConfiguracion(tipo, u.getMinimo(), u.getMaximo());
                if (e instanceof UmbralTemperatura u) return new UmbralConfiguracion(tipo, u.getMinimo(), u.getMaximo());
                throw new IllegalStateException("Estrategia desconocida para " + tipo);
            })).toList();
    }

    public synchronized UmbralConfiguracion actualizar(String tipoSolicitado, UmbralRequest valores) {
        String tipo = tipoSolicitado.trim().toUpperCase(Locale.ROOT);
        if (!estrategias.containsKey(tipo)) throw new IllegalArgumentException("Tipo de medición no soportado: " + tipo);
        EstrategiaUmbral nueva = crearEstrategia(tipo, valores.minimo(), valores.maximo());
        UmbralConfiguracion guardado = repo.save(new UmbralConfiguracion(tipo, valores.minimo(), valores.maximo()));
        procesamiento.registrarUmbral(tipo, nueva);
        return guardado;
    }

    private EstrategiaUmbral crearEstrategia(String tipo, BigDecimal minimo, BigDecimal maximo) {
        if (minimo == null || maximo == null || minimo.compareTo(maximo) >= 0)
            throw new IllegalArgumentException("El mínimo debe ser menor que el máximo");
        return switch (tipo) {
            case "PULSO" -> new UmbralFrecuencia(minimo, maximo);
            case "PRESION_SISTOLICA", "PRESION_DIASTOLICA" -> new UmbralPresion(minimo, maximo);
            case "TEMPERATURA" -> new UmbralTemperatura(minimo, maximo);
            default -> throw new IllegalArgumentException("Tipo de medición no soportado: " + tipo);
        };
    }
}
