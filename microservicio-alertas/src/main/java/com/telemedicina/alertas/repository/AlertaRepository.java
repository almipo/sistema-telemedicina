package com.telemedicina.alertas.repository;
import com.telemedicina.alertas.entity.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AlertaRepository extends JpaRepository<Alerta, UUID> {
    List<Alerta> findByPacienteIdOrderByFechaHoraDesc(UUID pacienteId);
    List<Alerta> findAllByOrderByFechaHoraDesc();
    Optional<Alerta> findByMedicionId(String medicionId);
}
