package com.telemedicina.metricas.service.estrategia;
import java.math.BigDecimal;
/** Contrato Strategy del UML. */
public interface EstrategiaUmbral {
 boolean estaEnRango(BigDecimal valor);
}
