package com.telemedicina.backend.service;
import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.UUID;
@Service public class EntityIntegrity {
 private final PacienteRepository pacientes;
 private final MedicoRepository medicos;
 public EntityIntegrity(PacienteRepository pacientes,MedicoRepository medicos){this.pacientes=pacientes;this.medicos=medicos;}
 public Paciente paciente(UUID id){return pacientes.findById(require(id,"Paciente")).orElseThrow(()->bad("Paciente inexistente"));}
 public Medico medico(UUID id){return medicos.findById(require(id,"Médico")).orElseThrow(()->bad("Médico inexistente"));}
 private UUID require(UUID id,String type){if(id==null)throw bad(type+" requerido");return id;}
 private ResponseStatusException bad(String mensaje){return new ResponseStatusException(HttpStatus.BAD_REQUEST,mensaje);}
 public void verify(Object entity){
  if(entity instanceof Dispositivo d){d.setPaciente(paciente(d.getPacienteId()));}
  else if(entity instanceof Turno t){t.setPaciente(paciente(t.getPacienteId()));t.setMedico(medico(t.getMedicoId()));}
  else if(entity instanceof HistoriaClinica h){h.setPaciente(paciente(h.getPacienteId()));
   for(Medico m:h.getMedicos()){medico(m.getId());}
  }
 }
}
