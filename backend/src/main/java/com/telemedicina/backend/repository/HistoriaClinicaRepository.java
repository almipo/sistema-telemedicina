package com.telemedicina.backend.repository;
import com.telemedicina.backend.entity.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica,UUID> { List<HistoriaClinica> findByPaciente_Id(UUID pacienteId); }
