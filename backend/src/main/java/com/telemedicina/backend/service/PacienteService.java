package com.telemedicina.backend.service;

import com.telemedicina.backend.entity.Paciente;
import com.telemedicina.backend.dto.request.PacienteRequest;
import com.telemedicina.backend.dto.response.PacienteResponse;
import com.telemedicina.backend.mapper.PacienteMapper;
import com.telemedicina.backend.repository.PacienteRepository;
import com.telemedicina.backend.exception.ConflictoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class PacienteService extends CrudService<Paciente> {
 private final PacienteRepository repository;
 public PacienteService(PacienteRepository repository) {super(repository); this.repository=repository;}
 @Transactional(readOnly=true) public List<PacienteResponse> listarDto(){return listar().stream().map(PacienteMapper::toResponse).toList();}
 @Transactional(readOnly=true) public PacienteResponse obtenerDto(UUID id){return PacienteMapper.toResponse(obtener(id));}
 @Transactional public PacienteResponse crear(PacienteRequest r){
   validarUnicidad(r, null);
   Paciente e = new Paciente();
   PacienteMapper.apply(r,e);
   return PacienteMapper.toResponse(guardar(e));
 }
 @Transactional public PacienteResponse actualizar(UUID id,PacienteRequest r){
   Paciente e=obtener(id);
   validarUnicidad(r, id);
   PacienteMapper.apply(r,e);
   return PacienteMapper.toResponse(guardar(e));
 }
 private void validarUnicidad(PacienteRequest r, UUID id) {
   boolean documentoDuplicado = id==null ? repository.existsByDocumento(r.documento()) : repository.existsByDocumentoAndIdNot(r.documento(), id);
   if(documentoDuplicado) throw new ConflictoException("Ya existe una persona de tipo paciente con ese documento");
 }
}
