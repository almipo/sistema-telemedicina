package com.telemedicina.backend.graphql;

import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.repository.*;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Controller
public class HistoriaGraphQL {
    private final HistoriaClinicaRepository historias;

    public HistoriaGraphQL(HistoriaClinicaRepository historias) {
        this.historias = historias;
    }

    @QueryMapping
    public HistoriaClinica historiaClinica(@Argument UUID id) {
        return historias.findById(id).orElse(null);
    }

    @QueryMapping
    public List<HistoriaClinica> historiasPaciente(@Argument UUID pacienteId) {
        return historias.findByPaciente_Id(pacienteId);
    }

    @SchemaMapping(typeName = "HistoriaClinica", field = "notasMedicas")
    @Transactional(readOnly = true)
    public List<String> notas(HistoriaClinica h) {
        return List.copyOf(historias.findById(h.getId()).orElseThrow().getNotasMedicas());
    }

    @SchemaMapping(typeName = "HistoriaClinica", field = "historialMedicacion")
    @Transactional(readOnly = true)
    public List<String> medicacion(HistoriaClinica h) {
        return List.copyOf(historias.findById(h.getId()).orElseThrow().getHistorialMedicacion());
    }

    @SchemaMapping(typeName = "HistoriaClinica", field = "medicos")
    @Transactional(readOnly = true)
    public List<Medico> medicos(HistoriaClinica h) {
        return List.copyOf(historias.findById(h.getId()).orElseThrow().getMedicos());
    }
}
