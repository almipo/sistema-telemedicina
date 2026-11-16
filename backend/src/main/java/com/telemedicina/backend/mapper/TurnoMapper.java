package com.telemedicina.backend.mapper;

import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.dto.request.*;
import com.telemedicina.backend.dto.response.*;
import java.util.*;
public final class TurnoMapper {
 private TurnoMapper() {}
 public static void apply(TurnoRequest r, Turno e) { e.setFecha(r.fecha()); e.setHora(r.hora()); e.setEstado(r.estado()==null?EstadoTurno.A_CONFIRMAR:r.estado()); }
 public static TurnoResponse toResponse(Turno e) {return new TurnoResponse(e.getId(),e.getFecha(),e.getHora(),e.getEstado(),e.getPacienteId(),e.getMedicoId());}
}
