package com.telemedicina.backend.service;

import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.repository.HistoriaClinicaRepository;
import com.telemedicina.backend.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service public class MedicoConsultaService {
 private final HistoriaClinicaRepository historias;private final MedicionService mediciones;private final AlertaService alertas;
 public MedicoConsultaService(HistoriaClinicaRepository historias,MedicionService mediciones,AlertaService alertas){this.historias=historias;this.mediciones=mediciones;this.alertas=alertas;}
 @Transactional(readOnly=true) public HistoriaClinica solicitarHistorialMedico(UUID pacienteId){return historias.findByPaciente_Id(pacienteId).stream().findFirst().orElseThrow(()->new RecursoNoEncontradoException("Historia inexistente"));}
 public List<Medicion> consultarMediciones(UUID pacienteId){return mediciones.listar(pacienteId);}
 public Object consultarAlertas(UUID pacienteId){return alertas.listar(pacienteId);}
}
