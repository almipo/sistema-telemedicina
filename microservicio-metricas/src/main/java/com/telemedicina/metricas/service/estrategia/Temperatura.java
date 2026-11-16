package com.telemedicina.metricas.service.estrategia;
import com.telemedicina.metricas.model.Medicion;
import com.telemedicina.metricas.exception.MedicionInvalidaException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
/** Estrategia concreta definida por el UML. */
@Component
public class Temperatura implements EstrategiaMedicion {
 @Override public void procesar(Medicion medicion) {
  BigDecimal valor=medicion.getValor();
  if(valor==null||valor.signum()<=0||valor.compareTo(new BigDecimal("60"))>0)
   throw new MedicionInvalidaException("Valor de Temperatura fuera de los límites técnicos demostrativos");
 }
 @Override public String obtenerUnidad() {return "°C";}
}
