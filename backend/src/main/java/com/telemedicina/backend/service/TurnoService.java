package com.telemedicina.backend.service;

import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.repository.TurnoRepository;
import com.telemedicina.backend.dto.request.TurnoRequest;
import com.telemedicina.backend.dto.response.TurnoResponse;
import com.telemedicina.backend.mapper.TurnoMapper;
import com.telemedicina.backend.exception.ConflictoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service public class TurnoService extends CrudService<Turno> {
 private final EntityIntegrity integrity;
 public TurnoService(TurnoRepository repo,EntityIntegrity integrity){super(repo);this.integrity=integrity;}
 @Transactional(readOnly=true) public List<TurnoResponse> listarDto(){return listar().stream().map(TurnoMapper::toResponse).toList();}
 @Transactional(readOnly=true) public TurnoResponse obtenerDto(UUID id){return TurnoMapper.toResponse(obtener(id));}
 @Transactional public TurnoResponse crear(TurnoRequest r){return aplicarYGuardar(new Turno(),r);}
 @Transactional public TurnoResponse actualizar(UUID id,TurnoRequest r){return aplicarYGuardar(obtener(id),r);}
 private TurnoResponse aplicarYGuardar(Turno t,TurnoRequest r){
  for(Turno otro:listar()){
   if(!Objects.equals(otro.getId(),t.getId()) && otro.getEstado()!=EstadoTurno.CANCELADO && r.estado()!=EstadoTurno.CANCELADO &&
       Objects.equals(otro.getMedicoId(),r.medicoId()) && Objects.equals(otro.getFecha(),r.fecha()) && Objects.equals(otro.getHora(),r.hora()))
    throw new ConflictoException("El medico ya tiene un turno a esa hora");
  }
  TurnoMapper.apply(r,t);t.setPaciente(integrity.paciente(r.pacienteId()));t.setMedico(integrity.medico(r.medicoId()));return TurnoMapper.toResponse(guardar(t));
 }
 @Transactional public TurnoResponse confirmar(UUID id){Turno t=obtener(id);t.confirmar();return TurnoMapper.toResponse(guardar(t));}
 @Transactional public TurnoResponse cancelar(UUID id){Turno t=obtener(id);t.cancelar();return TurnoMapper.toResponse(guardar(t));}
}
