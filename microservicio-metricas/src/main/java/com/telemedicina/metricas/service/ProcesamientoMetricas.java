package com.telemedicina.metricas.service;
import com.telemedicina.metricas.dto.request.MedicionRequest;
import com.telemedicina.metricas.dto.response.MedicionResponse;
import com.telemedicina.metricas.exception.*;
import com.telemedicina.metricas.mapper.MedicionMapper;
import com.telemedicina.metricas.model.Medicion;
import com.telemedicina.metricas.repository.MedicionRepository;
import com.telemedicina.metricas.service.estrategia.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class ProcesamientoMetricas {
 private final Map<String,EstrategiaMedicion> estrategiasMedicion;
 private final Map<String,EstrategiaUmbral> estrategiasUmbral;
 private final NullObject nullObject;
 private final MedicionRepository repository;
 private final MedicionMapper mapper;
 private final RestClient client;
 private final String backendUrl,alertasUrl;
 public ProcesamientoMetricas(@Qualifier("estrategiasMedicion") Map<String,EstrategiaMedicion> estrategiasMedicion,
 @Qualifier("estrategiasUmbral") Map<String,EstrategiaUmbral> estrategiasUmbral,NullObject nullObject,
 MedicionRepository repository,MedicionMapper mapper,RestClient client,
 @Value("${services.backend}") String backendUrl,@Value("${services.alertas}") String alertasUrl) {
  this.estrategiasMedicion=new HashMap<>(estrategiasMedicion);
  this.estrategiasUmbral=new java.util.concurrent.ConcurrentHashMap<>(estrategiasUmbral);
  this.nullObject=nullObject;this.repository=repository;this.mapper=mapper;this.client=client;
  this.backendUrl=backendUrl;this.alertasUrl=alertasUrl;
 }
 /** UML: permite registrar estrategias sin cambiar el procesador. */
 public void registrarEstrategia(String tipo,EstrategiaMedicion estrategia){estrategiasMedicion.put(normalizar(tipo),Objects.requireNonNull(estrategia));}
 /** UML: permite registrar umbrales sin cambiar el procesador. */
 public void registrarUmbral(String tipo,EstrategiaUmbral estrategia){estrategiasUmbral.put(normalizar(tipo),Objects.requireNonNull(estrategia));}
 public boolean evaluarUmbral(Medicion medicion){
  EstrategiaUmbral umbral=estrategiasUmbral.get(medicion.getTipo());
  if(umbral==null)throw new MedicionInvalidaException("No existe umbral para "+medicion.getTipo());
  return umbral.estaEnRango(medicion.getValor());
 }
 public MedicionResponse procesar(MedicionRequest request) {
  Medicion medicion=mapper.toModel(request);
  // NullObject representa el fallback del UML, pero no admite persistir tipos no configurados.
  EstrategiaMedicion estrategia=estrategiasMedicion.getOrDefault(medicion.getTipo(),nullObject);
  if(estrategia==nullObject||!estrategiasUmbral.containsKey(medicion.getTipo()))
   throw new MedicionInvalidaException("Tipo de medición no soportado: "+medicion.getTipo());
  estrategia.procesar(medicion);
  verificarDispositivo(medicion);
  Medicion guardada=repository.save(medicion);
  if(!evaluarUmbral(guardada)) enviarAlerta(guardada);
  return mapper.toResponse(guardada,estrategia.obtenerUnidad());
 }
 private void verificarDispositivo(Medicion medicion) {
  try {
   client.get().uri(backendUrl+"/api/interno/dispositivos/{id}/verificar?pacienteId={pacienteId}",medicion.getDispositivoId(),medicion.getPacienteId())
    .retrieve().toBodilessEntity();
  } catch(RestClientResponseException ex) {
   throw new ServicioExternoException("Backend rechazó la verificación del dispositivo",ex.getStatusCode(),ex);
  } catch(RestClientException ex) {
   throw new ServicioExternoException("No se puede verificar el dispositivo con el backend",org.springframework.http.HttpStatus.BAD_GATEWAY,ex);
  }
 }
 private void enviarAlerta(Medicion m) {
  String mensaje="Valor fuera del rango demostrativo configurado: "+m.getTipo()+"="+m.getValor();
  Map<String,Object> evento=Map.of("medicionId",m.getId(),"pacienteId",m.getPacienteId(),"tipo",m.getTipo(),"mensaje",mensaje);
  try {
   client.post().uri(alertasUrl+"/api/interno/alertas").body(evento).retrieve().toBodilessEntity();
  } catch(RestClientException ex) {
   throw new AlertaPendienteException(m.getId(),ex);
  }
 }
 private String normalizar(String tipo){if(tipo==null||tipo.isBlank())throw new IllegalArgumentException("Tipo vacío");return tipo.strip().toUpperCase(Locale.ROOT);}
}
