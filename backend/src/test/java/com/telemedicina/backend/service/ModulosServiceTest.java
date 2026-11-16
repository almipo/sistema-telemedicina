package com.telemedicina.backend.service;
import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.repository.*;
import com.telemedicina.backend.dto.request.*;
import com.telemedicina.backend.exception.ConflictoException;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class ModulosServiceTest {
 @Test void medicoSeCreaConMatricula(){MedicoRepository repo=mock(MedicoRepository.class);when(repo.save(any(Medico.class))).thenAnswer(i->i.getArgument(0));
 var dto=new MedicoService(repo).crear(new MedicoRequest("Eva","Diaz","333",null,"123","eva@test.com","MAT01","Clinica"));
 assertEquals("MAT01",dto.matricula());verify(repo).save(any(Medico.class));}
 @Test void dispositivoQuedaAsociadoAlPaciente(){DispositivoRepository repo=mock(DispositivoRepository.class);EntityIntegrity integrity=mock(EntityIntegrity.class);Paciente p=new Paciente();p.setId(UUID.randomUUID());when(integrity.paciente(p.getId())).thenReturn(p);when(repo.save(any(Dispositivo.class))).thenAnswer(i->i.getArgument(0));
 var dto=new DispositivoService(repo,integrity).crear(new DispositivoRequest("Pulso","SER01",EstadoDispositivo.ACTIVO,"Reloj",p.getId()));
 assertEquals(p.getId(),dto.pacienteId());}
 @Test void historiaNoDuplicaPaciente(){HistoriaClinicaRepository repo=mock(HistoriaClinicaRepository.class);EntityIntegrity integrity=mock(EntityIntegrity.class);UUID paciente=UUID.randomUUID();
 when(repo.findByPaciente_Id(paciente)).thenReturn(List.of(new HistoriaClinica()));
 var service=new HistoriaClinicaService(repo,integrity);
 assertThrows(ConflictoException.class,()->service.crear(new HistoriaClinicaRequest(paciente,EstadoHistorial.ACTIVO,List.of(),List.of(),List.of())));
 verify(repo,never()).save(any());}
}
