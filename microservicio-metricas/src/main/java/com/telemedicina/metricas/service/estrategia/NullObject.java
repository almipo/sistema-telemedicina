package com.telemedicina.metricas.service.estrategia;
import com.telemedicina.metricas.model.Medicion;
import org.springframework.stereotype.Component;
/** Fallback que evita referencias nulas; los tipos desconocidos se rechazan antes de persistir. */
@Component
public class NullObject implements EstrategiaMedicion {
 @Override public void procesar(Medicion medicion) { /* no-op intencional */ }
 @Override public String obtenerUnidad() {return "";}
}
