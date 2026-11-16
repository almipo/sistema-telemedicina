package com.telemedicina.metricas.config;
import com.telemedicina.metricas.service.estrategia.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EstrategiasConfigTest {
 @Test void registraLosCuatroTipos(){var c=new EstrategiasConfig();var estrategias=c.estrategiasMedicion(new PresionArterial(),new FrecuenciaCardiaca(),new Temperatura());assertEquals(4,estrategias.size());assertSame(estrategias.get("PRESION_SISTOLICA"),estrategias.get("PRESION_DIASTOLICA"));}
 @Test void umbralesSonIndependientes(){var c=new EstrategiasConfig();var limites=c.estrategiasUmbral(BigDecimal.valueOf(50),BigDecimal.valueOf(120),BigDecimal.valueOf(90),BigDecimal.valueOf(140),BigDecimal.valueOf(60),BigDecimal.valueOf(90),BigDecimal.valueOf(35),BigDecimal.valueOf(38));assertTrue(limites.get("PRESION_SISTOLICA").estaEnRango(BigDecimal.valueOf(120)));assertFalse(limites.get("PRESION_DIASTOLICA").estaEnRango(BigDecimal.valueOf(120)));}
}
