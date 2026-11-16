package com.telemedicina.backend.service;

import com.telemedicina.backend.entity.Paciente;
import com.telemedicina.backend.repository.PacienteRepository;
import com.telemedicina.backend.dto.request.PacienteRequest;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import com.telemedicina.backend.service.PacienteService;
class PacienteServiceTest {
 @Test void crearAplicaDto(){PacienteRepository repo=mock(PacienteRepository.class);when(repo.save(any(Paciente.class))).thenAnswer(i->i.getArgument(0));PacienteService service=new PacienteService(repo);
 var result=service.crear(new PacienteRequest("Ana","Lopez","123",LocalDate.of(2000,1,2),"123","ana@example.com"));assertEquals("Ana",result.nombre());verify(repo).save(any(Paciente.class));}
 @Test void noEncontrado(){PacienteRepository repo=mock(PacienteRepository.class);when(repo.findById(any())).thenReturn(Optional.empty());assertThrows(com.telemedicina.backend.exception.RecursoNoEncontradoException.class,()->new PacienteService(repo).obtener(UUID.randomUUID()));}
}
