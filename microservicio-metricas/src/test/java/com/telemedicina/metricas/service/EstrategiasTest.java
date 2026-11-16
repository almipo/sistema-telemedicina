package com.telemedicina.metricas.service;
import com.telemedicina.metricas.exception.MedicionInvalidaException;
import com.telemedicina.metricas.model.Medicion;
import com.telemedicina.metricas.service.estrategia.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EstrategiasTest {
 @Test void unidadesCorrectas(){assertEquals("lpm",new FrecuenciaCardiaca().obtenerUnidad());assertEquals("mmHg",new PresionArterial().obtenerUnidad());assertEquals("°C",new Temperatura().obtenerUnidad());}
 @Test void valoresValidos(){for(var estrategia:new EstrategiaMedicion[]{new FrecuenciaCardiaca(),new PresionArterial(),new Temperatura()}){Medicion m=new Medicion();m.setValor(BigDecimal.valueOf(20));assertDoesNotThrow(()->estrategia.procesar(m));}}
 @Test void rechazaValoresNoPositivos(){Medicion m=new Medicion();m.setValor(BigDecimal.ZERO);assertThrows(MedicionInvalidaException.class,()->new FrecuenciaCardiaca().procesar(m));assertThrows(MedicionInvalidaException.class,()->new PresionArterial().procesar(m));assertThrows(MedicionInvalidaException.class,()->new Temperatura().procesar(m));}
 @Test void rechazaValoresExcesivos(){Medicion m=new Medicion();m.setValor(new BigDecimal("301"));assertThrows(MedicionInvalidaException.class,()->new FrecuenciaCardiaca().procesar(m));assertThrows(MedicionInvalidaException.class,()->new PresionArterial().procesar(m));m.setValor(new BigDecimal("61"));assertThrows(MedicionInvalidaException.class,()->new Temperatura().procesar(m));}
 @Test void umbralesInclusivos(){for(var umbral:new EstrategiaUmbral[]{new UmbralFrecuencia(BigDecimal.TEN,BigDecimal.valueOf(20)),new UmbralPresion(BigDecimal.TEN,BigDecimal.valueOf(20)),new UmbralTemperatura(BigDecimal.TEN,BigDecimal.valueOf(20))}){assertTrue(umbral.estaEnRango(BigDecimal.TEN));assertTrue(umbral.estaEnRango(BigDecimal.valueOf(20)));assertFalse(umbral.estaEnRango(BigDecimal.valueOf(21)));assertFalse(umbral.estaEnRango(BigDecimal.valueOf(9)));}}
 @Test void umbralInvalido(){assertThrows(IllegalArgumentException.class,()->new UmbralFrecuencia(BigDecimal.TEN,BigDecimal.ONE));}
 @Test void nullObjectNoFalla(){Medicion m=new Medicion();assertDoesNotThrow(()->new NullObject().procesar(m));assertEquals("",new NullObject().obtenerUnidad());}
}
