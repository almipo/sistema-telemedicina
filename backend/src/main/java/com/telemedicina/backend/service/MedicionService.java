package com.telemedicina.backend.service;

import com.telemedicina.backend.entity.Medicion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;
@Service public class MedicionService {
 private final RestClient client;
 private final String url;
 public MedicionService(RestClient.Builder builder,@Value("${services.metricas}") String url){this.client=builder.build();this.url=url;}
 public List<Medicion> listar(UUID pacienteId){return client.get().uri(url+"/api/mediciones"+(pacienteId==null?"":"?pacienteId="+pacienteId)).retrieve().body(new ParameterizedTypeReference<List<Medicion>>(){});}
 public Medicion obtener(String id){return client.get().uri(url+"/api/mediciones/{id}",id).retrieve().body(Medicion.class);}
 public Medicion registrar(Medicion m){if(m.getTipo()==null||m.getTipo().isBlank()||m.getValor()==null||m.getPacienteId()==null||m.getDispositivoId()==null)throw new IllegalArgumentException("Medicion incompleta");
  return client.post().uri(url+"/api/mediciones").body(Map.of("tipo",m.getTipo(),"valor",m.getValor(),"pacienteId",m.getPacienteId(),"dispositivoId",m.getDispositivoId())).retrieve().body(Medicion.class);}
}
