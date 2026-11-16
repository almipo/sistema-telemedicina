package com.telemedicina.backend.repository;
import com.telemedicina.backend.entity.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DispositivoRepository extends JpaRepository<Dispositivo,UUID> { List<Dispositivo> findByPaciente_Id(UUID pacienteId); }
