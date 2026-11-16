package com.telemedicina.backend.mapper;

import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.dto.request.*;
import com.telemedicina.backend.dto.response.*;
import java.util.*;
public final class DispositivoMapper {
 private DispositivoMapper() {}
 public static void apply(DispositivoRequest r, Dispositivo e) { e.setTipo(r.tipo()); e.setNumeroSerie(r.numeroSerie()); e.setEstado(r.estado()); e.setNombre(r.nombre()); }
 public static DispositivoResponse toResponse(Dispositivo e) {return new DispositivoResponse(e.getId(),e.getTipo(),e.getNumeroSerie(),e.getEstado(),e.getNombre(),e.getPacienteId());}
}
