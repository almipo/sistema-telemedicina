package com.telemedicina.backend.repository;
import com.telemedicina.backend.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TurnoRepository extends JpaRepository<Turno,UUID> { List<Turno> findByPaciente_Id(UUID pacienteId); List<Turno> findByMedico_Id(UUID medicoId); }
