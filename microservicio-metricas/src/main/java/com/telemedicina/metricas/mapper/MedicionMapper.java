package com.telemedicina.metricas.mapper;
import com.telemedicina.metricas.dto.request.MedicionRequest;
import com.telemedicina.metricas.dto.response.MedicionResponse;
import com.telemedicina.metricas.model.Medicion;
import com.telemedicina.metricas.service.ProcesamientoMetricas;
import java.time.Instant;
import org.springframework.stereotype.Component;
@Component
public class MedicionMapper {
 public Medicion toModel(MedicionRequest request) {
  Medicion m=new Medicion();
  m.setTipo(request.tipo().trim().toUpperCase(java.util.Locale.ROOT));
  m.setValor(request.valor());m.setPacienteId(request.pacienteId());m.setDispositivoId(request.dispositivoId());
  m.setFechaHora(Instant.now());return m;
 }
 public MedicionResponse toResponse(Medicion m,String unidad) {
  return new MedicionResponse(m.getId(),m.getTipo(),m.getValor(),m.getFechaHora(),m.getDispositivoId(),m.getPacienteId(),unidad);
 }
}
