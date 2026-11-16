package com.telemedicina.backend.service;

import com.telemedicina.backend.entity.Medico;
import com.telemedicina.backend.dto.request.MedicoRequest;
import com.telemedicina.backend.dto.response.MedicoResponse;
import com.telemedicina.backend.mapper.MedicoMapper;
import com.telemedicina.backend.repository.MedicoRepository;
import com.telemedicina.backend.exception.ConflictoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class MedicoService extends CrudService<Medico> {
 private final MedicoRepository repository;
 public MedicoService(MedicoRepository repository) {super(repository); this.repository=repository;}
 @Transactional(readOnly=true) public List<MedicoResponse> listarDto(){return listar().stream().map(MedicoMapper::toResponse).toList();}
 @Transactional(readOnly=true) public MedicoResponse obtenerDto(UUID id){return MedicoMapper.toResponse(obtener(id));}
 @Transactional public MedicoResponse crear(MedicoRequest r){
   validarUnicidad(r, null);
   Medico e = new Medico();
   MedicoMapper.apply(r,e);
   return MedicoMapper.toResponse(guardar(e));
 }
 @Transactional public MedicoResponse actualizar(UUID id,MedicoRequest r){
   Medico e=obtener(id);
   validarUnicidad(r, id);
   MedicoMapper.apply(r,e);
   return MedicoMapper.toResponse(guardar(e));
 }
 private void validarUnicidad(MedicoRequest r, UUID id) {
   boolean documentoDuplicado = id==null ? repository.existsByDocumento(r.documento()) : repository.existsByDocumentoAndIdNot(r.documento(), id);
   if(documentoDuplicado) throw new ConflictoException("Ya existe una persona de tipo medico con ese documento");
   boolean matriculaDuplicada = id==null ? repository.existsByMatricula(r.matricula()) : repository.existsByMatriculaAndIdNot(r.matricula(), id);
   if(matriculaDuplicada) throw new ConflictoException("Ya existe un médico con esa matrícula");
 }
}
