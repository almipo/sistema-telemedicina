package com.telemedicina.backend.service;

import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.repository.DispositivoRepository;
import com.telemedicina.backend.dto.request.DispositivoRequest;
import com.telemedicina.backend.dto.response.DispositivoResponse;
import com.telemedicina.backend.mapper.DispositivoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service public class DispositivoService extends CrudService<Dispositivo> {
 private final EntityIntegrity integrity;
 public DispositivoService(DispositivoRepository repo,EntityIntegrity integrity){super(repo);this.integrity=integrity;}
 @Transactional(readOnly=true) public List<DispositivoResponse> listarDto(){return listar().stream().map(DispositivoMapper::toResponse).toList();}
 @Transactional(readOnly=true) public DispositivoResponse obtenerDto(UUID id){return DispositivoMapper.toResponse(obtener(id));}
 @Transactional public DispositivoResponse crear(DispositivoRequest r){Dispositivo d=new Dispositivo();return aplicarYGuardar(d,r);}
 @Transactional public DispositivoResponse actualizar(UUID id,DispositivoRequest r){return aplicarYGuardar(obtener(id),r);}
 private DispositivoResponse aplicarYGuardar(Dispositivo d,DispositivoRequest r){DispositivoMapper.apply(r,d);d.setPaciente(integrity.paciente(r.pacienteId()));return DispositivoMapper.toResponse(guardar(d));}
}
