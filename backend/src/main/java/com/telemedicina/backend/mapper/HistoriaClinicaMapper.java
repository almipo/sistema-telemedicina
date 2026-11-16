package com.telemedicina.backend.mapper;

import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.dto.request.*;
import com.telemedicina.backend.dto.response.*;
import java.util.*;
public final class HistoriaClinicaMapper {
 private HistoriaClinicaMapper() {}
 public static void apply(HistoriaClinicaRequest r, HistoriaClinica e) { e.setEstado(r.estado()); }
 public static HistoriaClinicaResponse toResponse(HistoriaClinica e) {return new HistoriaClinicaResponse(e.getId(),e.getPacienteId(),e.getEstado(),List.copyOf(e.getNotasMedicas()),List.copyOf(e.getHistorialMedicacion()),e.getMedicos().stream().map(Medico::getId).toList());}
}
