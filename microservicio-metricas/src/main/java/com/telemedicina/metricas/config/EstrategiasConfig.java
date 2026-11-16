package com.telemedicina.metricas.config;
import com.telemedicina.metricas.service.estrategia.*;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class EstrategiasConfig {
 @Bean("estrategiasMedicion")
 public Map<String,EstrategiaMedicion> estrategiasMedicion(PresionArterial presion,FrecuenciaCardiaca frecuencia,Temperatura temperatura) {
  return Map.of("PULSO",frecuencia,"PRESION_SISTOLICA",presion,"PRESION_DIASTOLICA",presion,"TEMPERATURA",temperatura);
 }
 @Bean("estrategiasUmbral")
 public Map<String,EstrategiaUmbral> estrategiasUmbral(
 @Value("${metricas.umbrales.pulso.min}") BigDecimal pulsoMin,@Value("${metricas.umbrales.pulso.max}") BigDecimal pulsoMax,
 @Value("${metricas.umbrales.presion-sistolica.min}") BigDecimal sisMin,@Value("${metricas.umbrales.presion-sistolica.max}") BigDecimal sisMax,
 @Value("${metricas.umbrales.presion-diastolica.min}") BigDecimal diaMin,@Value("${metricas.umbrales.presion-diastolica.max}") BigDecimal diaMax,
 @Value("${metricas.umbrales.temperatura.min}") BigDecimal tempMin,@Value("${metricas.umbrales.temperatura.max}") BigDecimal tempMax) {
  return Map.of("PULSO",new UmbralFrecuencia(pulsoMin,pulsoMax),
                "PRESION_SISTOLICA",new UmbralPresion(sisMin,sisMax),
                "PRESION_DIASTOLICA",new UmbralPresion(diaMin,diaMax),
                "TEMPERATURA",new UmbralTemperatura(tempMin,tempMax));
 }
}
