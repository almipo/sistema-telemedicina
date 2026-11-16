package com.telemedicina.metricas.service.estrategia;
import com.telemedicina.metricas.model.Medicion;
/** Contrato Strategy del UML: procesamiento específico y unidad de medida. */
public interface EstrategiaMedicion {
 void procesar(Medicion medicion);
 String obtenerUnidad();
}
