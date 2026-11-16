package com.telemedicina.backend.service;
import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class EntityIntegrityTest {
 PacienteRepository pacientes=mock(PacienteRepository.class);MedicoRepository medicos=mock(MedicoRepository.class);
 EntityIntegrity integrity=new EntityIntegrity(pacientes,medicos);
 @Test void asociaDispositivoAPaciente(){UUID id=UUID.randomUUID();Paciente p=new Paciente();p.setId(id);when(pacientes.findById(id)).thenReturn(Optional.of(p));Dispositivo d=new Dispositivo();d.setPacienteId(id);integrity.verify(d);assertSame(p,d.getPaciente());}
 @Test void noPermiteDispositivoSinPaciente(){Dispositivo d=new Dispositivo();d.setPacienteId(UUID.randomUUID());assertThrows(ResponseStatusException.class,()->integrity.verify(d));}
 @Test void relacionaTurnoConPacienteYMedico(){UUID pi=UUID.randomUUID(),mi=UUID.randomUUID();Paciente p=new Paciente();p.setId(pi);Medico m=new Medico();m.setId(mi);when(pacientes.findById(pi)).thenReturn(Optional.of(p));when(medicos.findById(mi)).thenReturn(Optional.of(m));Turno t=new Turno();t.setPacienteId(pi);t.setMedicoId(mi);integrity.verify(t);assertSame(p,t.getPaciente());assertSame(m,t.getMedico());}
 @Test void noPermiteTurnoConMedicoInexistente(){UUID pi=UUID.randomUUID();Paciente p=new Paciente();p.setId(pi);when(pacientes.findById(pi)).thenReturn(Optional.of(p));Turno t=new Turno();t.setPacienteId(pi);t.setMedicoId(UUID.randomUUID());assertThrows(ResponseStatusException.class,()->integrity.verify(t));}
}
