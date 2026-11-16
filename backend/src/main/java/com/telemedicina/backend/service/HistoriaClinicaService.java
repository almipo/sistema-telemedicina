package com.telemedicina.backend.service;

import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.repository.HistoriaClinicaRepository;
import com.telemedicina.backend.dto.request.HistoriaClinicaRequest;
import com.telemedicina.backend.dto.response.HistoriaClinicaResponse;
import com.telemedicina.backend.mapper.HistoriaClinicaMapper;
import com.telemedicina.backend.exception.ConflictoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class HistoriaClinicaService extends CrudService<HistoriaClinica> {
    private final EntityIntegrity integrity;
    private final HistoriaClinicaRepository repo;

    public HistoriaClinicaService(HistoriaClinicaRepository repo, EntityIntegrity integrity) {
        super(repo);
        this.repo = repo;
        this.integrity = integrity;
    }

    @Transactional(readOnly = true)
    public List<HistoriaClinicaResponse> listarDto() {
        return listar().stream().map(HistoriaClinicaMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HistoriaClinicaResponse obtenerDto(UUID id) {
        return HistoriaClinicaMapper.toResponse(obtener(id));
    }

    @Transactional
    public HistoriaClinicaResponse crear(HistoriaClinicaRequest r) {
        if (!repo.findByPaciente_Id(r.pacienteId()).isEmpty())
            throw new ConflictoException("El paciente ya posee una historia clinica");
        return aplicarYGuardar(new HistoriaClinica(), r);
    }

    @Transactional
    public HistoriaClinicaResponse actualizar(UUID id, HistoriaClinicaRequest r) {
        return aplicarYGuardar(obtener(id), r);
    }

    private HistoriaClinicaResponse aplicarYGuardar(HistoriaClinica h, HistoriaClinicaRequest r) {
        HistoriaClinicaMapper.apply(r, h);
        h.setPaciente(integrity.paciente(r.pacienteId()));
        h.getNotasMedicas().clear();
        if (r.notasMedicas() != null) r.notasMedicas().forEach(h::agregarNota);
        h.getHistorialMedicacion().clear();
        if (r.historialMedicacion() != null) r.historialMedicacion().forEach(h::agregarMedicacion);
        h.getMedicos().clear();
        if (r.medicosIds() != null) r.medicosIds().forEach(id -> h.agregarMedico(integrity.medico(id)));
        return HistoriaClinicaMapper.toResponse(guardar(h));
    }
}
