package com.telemedicina.metricas.service;

import com.telemedicina.metricas.exception.RecursoNoEncontradoException;
import com.telemedicina.metricas.mapper.MedicionMapper;
import com.telemedicina.metricas.model.Medicion;
import com.telemedicina.metricas.repository.MedicionRepository;
import com.telemedicina.metricas.service.estrategia.*;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MedicionServiceTest {
    @Test
    void consultaPorId() {
        MedicionRepository repo = mock(MedicionRepository.class);
        Medicion m = new Medicion();
        m.setId("abc");
        m.setTipo("PULSO");
        m.setValor(BigDecimal.valueOf(70));
        when(repo.findById("abc")).thenReturn(Optional.of(m));
        MedicionService s = new MedicionService(repo, new MedicionMapper(), Map.of("PULSO", new FrecuenciaCardiaca()), new NullObject());
        assertEquals("lpm", s.obtener("abc").unidad());
    }

    @Test
    void idInexistenteDevuelveExcepcion() {
        MedicionRepository repo = mock(MedicionRepository.class);
        when(repo.findById("abc")).thenReturn(Optional.empty());
        MedicionService s = new MedicionService(repo, new MedicionMapper(), Map.of(), new NullObject());
        assertThrows(RecursoNoEncontradoException.class, () -> s.obtener("abc"));
    }

    @Test
    void filtraPaciente() {
        MedicionRepository repo = mock(MedicionRepository.class);
        UUID paciente = UUID.randomUUID();
        when(repo.findByPacienteIdOrderByFechaHoraDesc(paciente)).thenReturn(List.of());
        MedicionService s = new MedicionService(repo, new MedicionMapper(), Map.of(), new NullObject());
        assertTrue(s.listar(paciente).isEmpty());
        verify(repo).findByPacienteIdOrderByFechaHoraDesc(paciente);
    }
}
