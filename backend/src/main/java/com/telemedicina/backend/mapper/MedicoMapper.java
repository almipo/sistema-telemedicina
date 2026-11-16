package com.telemedicina.backend.mapper;

import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.dto.request.*;
import com.telemedicina.backend.dto.response.*;
import java.util.*;
public final class MedicoMapper {
 private MedicoMapper() {}
 public static void apply(MedicoRequest r, Medico e) { e.setNombre(r.nombre()); e.setApellido(r.apellido()); e.setDocumento(r.documento()); e.setFechaNacimiento(r.fechaNacimiento()); e.setTelefono(r.telefono()); e.setEmail(r.email()); e.setMatricula(r.matricula()); e.setEspecialidad(r.especialidad()); }
 public static MedicoResponse toResponse(Medico e) {return new MedicoResponse(e.getId(),e.getNombre(),e.getApellido(),e.getDocumento(),e.getFechaNacimiento(),e.getTelefono(),e.getEmail(),e.getMatricula(),e.getEspecialidad());}
}
