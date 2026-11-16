package com.telemedicina.metricas.mapper;
import com.telemedicina.metricas.dto.request.MedicionRequest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MedicionMapperTest {
 private final MedicionMapper mapper=new MedicionMapper();
 @Test void convierteSinIdYConFecha(){UUID paciente=UUID.randomUUID(),dispositivo=UUID.randomUUID();var m=mapper.toModel(new MedicionRequest(" pulso ",BigDecimal.valueOf(75),dispositivo,paciente));assertNull(m.getId());assertEquals("PULSO",m.getTipo());assertNotNull(m.getFechaHora());assertEquals(paciente,m.getPacienteId());assertEquals(dispositivo,m.getDispositivoId());}
 @Test void convierteRespuesta(){var m=mapper.toModel(new MedicionRequest("TEMPERATURA",BigDecimal.valueOf(36),UUID.randomUUID(),UUID.randomUUID()));m.setId("mongo-id");var r=mapper.toResponse(m,"°C");assertEquals("mongo-id",r.id());assertEquals("°C",r.unidad());}
}
