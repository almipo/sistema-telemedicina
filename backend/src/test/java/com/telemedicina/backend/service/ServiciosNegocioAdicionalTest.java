package com.telemedicina.backend.service;

import com.telemedicina.backend.dto.request.*;
import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.exception.ConflictoException;
import com.telemedicina.backend.exception.RecursoNoEncontradoException;
import com.telemedicina.backend.repository.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ServiciosNegocioAdicionalTest {
    @Test void pacienteSeActualizaSinCambiarIdentificador() {
        PacienteRepository repo = mock(PacienteRepository.class);
        Paciente p = new Paciente(); UUID id = UUID.randomUUID(); p.setId(id);
        when(repo.findById(id)).thenReturn(Optional.of(p));
        when(repo.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));
        var actualizado = new PacienteService(repo).actualizar(id,
            new PacienteRequest("Elena", "Suarez", "123456", LocalDate.of(1995, 2, 3), "111", "elena@example.com"));
        assertEquals("Elena", actualizado.nombre());
        assertEquals(id, actualizado.id());
        verify(repo).save(same(p));
    }

    @Test void eliminarPacienteInexistenteNoLlamaDelete() {
        PacienteRepository repo = mock(PacienteRepository.class);
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> new PacienteService(repo).eliminar(id));
        verify(repo, never()).deleteById(any());
    }

    @Test void medicoPuedeActualizarEspecialidad() {
        MedicoRepository repo = mock(MedicoRepository.class);
        Medico medico = new Medico(); UUID id = UUID.randomUUID(); medico.setId(id);
        when(repo.findById(id)).thenReturn(Optional.of(medico));
        when(repo.save(any(Medico.class))).thenAnswer(inv -> inv.getArgument(0));
        var respuesta = new MedicoService(repo).actualizar(id,
            new MedicoRequest("Laura", "Paz", "321", LocalDate.of(1980, 1, 1), "555", "laura@example.com", "MAT-01", "Cardiologia"));
        assertEquals("Cardiologia", respuesta.especialidad());
        verify(repo).save(same(medico));
    }

    @Test void dispositivoSinPacienteValidoNoSeGuarda() {
        DispositivoRepository repo = mock(DispositivoRepository.class);
        EntityIntegrity integrity = mock(EntityIntegrity.class);
        UUID id = UUID.randomUUID();
        when(integrity.paciente(id)).thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Paciente inexistente"));
        var servicio = new DispositivoService(repo, integrity);
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            () -> servicio.crear(new DispositivoRequest("Pulso", "SN-1", EstadoDispositivo.ACTIVO, "Sensor", id)));
        verify(repo, never()).save(any());
    }

    @Test void solicitudTurnoDetectaHorarioYaOcupado() {
        TurnoRepository repo = mock(TurnoRepository.class);
        EntityIntegrity integrity = mock(EntityIntegrity.class);
        UUID medicoId = UUID.randomUUID(); UUID pacienteId = UUID.randomUUID();
        Medico medico = new Medico(); medico.setId(medicoId);
        Paciente paciente = new Paciente(); paciente.setId(pacienteId);
        Turno existente = new Turno(); existente.setId(UUID.randomUUID()); existente.setMedico(medico);
        existente.setPaciente(paciente); existente.setFecha(LocalDate.of(2027, 3, 10));
        existente.setHora(LocalTime.of(10, 30)); existente.setEstado(EstadoTurno.CONFIRMADO);
        when(repo.findAll()).thenReturn(List.of(existente));
        var servicio = new TurnoService(repo, integrity);
        var solicitud = new TurnoRequest(LocalDate.of(2027, 3, 10), LocalTime.of(10, 30), pacienteId, medicoId, EstadoTurno.A_CONFIRMAR);
        assertThrows(ConflictoException.class, () -> servicio.crear(solicitud));
        verify(repo, never()).save(any());
    }

    @Test void confirmarYCancelarTurnoActualizaEstado() {
        TurnoRepository repo = mock(TurnoRepository.class);
        EntityIntegrity integrity = mock(EntityIntegrity.class);
        Turno turno = new Turno(); UUID id = UUID.randomUUID(); turno.setId(id);
        turno.setEstado(EstadoTurno.A_CONFIRMAR);
        when(repo.findById(id)).thenReturn(Optional.of(turno));
        when(repo.save(any(Turno.class))).thenAnswer(inv -> inv.getArgument(0));
        TurnoService servicio = new TurnoService(repo, integrity);
        assertEquals(EstadoTurno.CONFIRMADO, servicio.confirmar(id).estado());
        assertEquals(EstadoTurno.CANCELADO, servicio.cancelar(id).estado());
        verify(repo, times(2)).save(same(turno));
    }

    @Test void historiaAsociaPacienteYMedicoYGuardaNotas() {
        HistoriaClinicaRepository repo = mock(HistoriaClinicaRepository.class);
        EntityIntegrity integrity = mock(EntityIntegrity.class);
        UUID pacienteId = UUID.randomUUID(); UUID medicoId = UUID.randomUUID();
        Paciente paciente = new Paciente(); paciente.setId(pacienteId);
        Medico medico = new Medico(); medico.setId(medicoId);
        when(repo.findByPaciente_Id(pacienteId)).thenReturn(List.of());
        when(integrity.paciente(pacienteId)).thenReturn(paciente);
        when(integrity.medico(medicoId)).thenReturn(medico);
        when(repo.save(any(HistoriaClinica.class))).thenAnswer(inv -> inv.getArgument(0));
        var respuesta = new HistoriaClinicaService(repo, integrity).crear(new HistoriaClinicaRequest(
            pacienteId, EstadoHistorial.ACTIVO, List.of("Control"), List.of("Medicación A"), List.of(medicoId)));
        assertEquals(pacienteId, respuesta.pacienteId());
        assertEquals(List.of("Control"), respuesta.notasMedicas());
        assertEquals(List.of(medicoId), respuesta.medicosIds());
        verify(repo).save(any(HistoriaClinica.class));
    }
}
