package com.telemedicina.backend.repository;

import com.telemedicina.backend.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;
import java.time.LocalDate;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MapeoJpaTest {
    @Autowired
    PacienteRepository pacientes;
    @Autowired
    MedicoRepository medicos;
    @Autowired
    DispositivoRepository dispositivos;
    @Autowired
    TurnoRepository turnos;
    @Autowired
    HistoriaClinicaRepository historias;

    private Paciente paciente() {
        Paciente p = new Paciente();
     p.setNombre("Juan");
     p.setApellido("Pérez");
     p.setDocumento("30123456");
     p.setFechaNacimiento(LocalDate.of(1990, 5, 15));
     p.setTelefono("1123456789");
     p.setEmail("juan@test.com");

     return pacientes.saveAndFlush(p);
    }

    private Medico medico() {
        Medico m = new Medico();
        m.setNombre("Doc");
        m.setApellido("Paz");
     m.setTelefono("1123456789");
     m.setFechaNacimiento(LocalDate.of(1980, 3, 20));
        m.setDocumento(UUID.randomUUID().toString());
        m.setEmail("doc@example.com");
        m.setMatricula("M123");
        m.setEspecialidad("General");
        return medicos.saveAndFlush(m);
    }

    @Test
    void herenciaYRelacionDispositivo() {
        Paciente p = paciente();
        Dispositivo d = new Dispositivo();
        d.setPaciente(p);
        d.setTipo("Pulso");
        d.setNombre("Reloj");
        d.setNumeroSerie(UUID.randomUUID().toString());
        d.setEstado(EstadoDispositivo.ACTIVO);
        Dispositivo stored = dispositivos.saveAndFlush(d);
        assertNotNull(stored.getId());
        assertEquals(1, dispositivos.findByPaciente_Id(p.getId()).size());
    }

    @Test
    void turnoTieneClavesForaneas() {
        Paciente p = paciente();
        Medico m = medico();
        Turno t = new Turno();
        t.setPaciente(p);
        t.setMedico(m);
        t.setFecha(LocalDate.of(2027, 2, 1));
        t.setHora(LocalTime.of(11, 0));
        t.setEstado(EstadoTurno.A_CONFIRMAR);
        turnos.saveAndFlush(t);
        assertEquals(1, turnos.findByPaciente_Id(p.getId()).size());
    }

    @Test
    void historiaPersisteNotasYMedicamentosEnTablasSeparadas() {
        Paciente p = paciente();
        HistoriaClinica h = new HistoriaClinica();
        h.setPaciente(p);
        h.setEstado(EstadoHistorial.ACTIVO);
        h.agregarNota("Control");
        h.agregarMedicacion("Medicamento 500mg, dosis diaria");
        HistoriaClinica saved = historias.saveAndFlush(h);
        assertNotNull(saved.getId());
        assertEquals(1, historias.findByPaciente_Id(p.getId()).size());
        assertEquals(1, saved.getNotasMedicas().size());
        assertEquals(1, saved.getHistorialMedicacion().size());
    }
}
