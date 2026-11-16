package com.telemedicina.metricas.service;
import com.telemedicina.metricas.dto.response.MedicionResponse;
import com.telemedicina.metricas.exception.RecursoNoEncontradoException;
import com.telemedicina.metricas.mapper.MedicionMapper;
import com.telemedicina.metricas.model.Medicion;
import com.telemedicina.metricas.repository.MedicionRepository;
import com.telemedicina.metricas.service.estrategia.EstrategiaMedicion;
import com.telemedicina.metricas.service.estrategia.NullObject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
@Service
public class MedicionService {
 private final MedicionRepository repository;private final MedicionMapper mapper;
 private final Map<String,EstrategiaMedicion> estrategias;private final NullObject fallback;
 public MedicionService(MedicionRepository repository,MedicionMapper mapper,
  @Qualifier("estrategiasMedicion") Map<String,EstrategiaMedicion> estrategias,NullObject fallback){
  this.repository=repository;this.mapper=mapper;this.estrategias=estrategias;this.fallback=fallback;
 }
 public List<MedicionResponse> listar(UUID pacienteId) {
  List<Medicion> items=pacienteId==null?repository.findAllByOrderByFechaHoraDesc():repository.findByPacienteIdOrderByFechaHoraDesc(pacienteId);
  return items.stream().map(this::respuesta).toList();
 }
 public MedicionResponse obtener(String id){return respuesta(repository.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Medición no encontrada")));}
 private MedicionResponse respuesta(Medicion m){return mapper.toResponse(m,estrategias.getOrDefault(m.getTipo(),fallback).obtenerUnidad());}
}
