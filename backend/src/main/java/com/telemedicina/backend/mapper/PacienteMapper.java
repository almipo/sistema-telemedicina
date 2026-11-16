package com.telemedicina.backend.mapper;

import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.dto.request.*;
import com.telemedicina.backend.dto.response.*;
import java.util.*;
public final class PacienteMapper {
 private PacienteMapper() {}
 public static void apply(PacienteRequest r, Paciente e) { e.setNombre(r.nombre()); e.setApellido(r.apellido()); e.setDocumento(r.documento()); e.setFechaNacimiento(r.fechaNacimiento()); e.setTelefono(r.telefono()); e.setEmail(r.email()); }
 public static PacienteResponse toResponse(Paciente e) {return new PacienteResponse(e.getId(),e.getNombre(),e.getApellido(),e.getDocumento(),e.getFechaNacimiento(),e.getTelefono(),e.getEmail());}
}
