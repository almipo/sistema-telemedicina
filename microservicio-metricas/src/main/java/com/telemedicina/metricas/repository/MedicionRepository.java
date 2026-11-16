package com.telemedicina.metricas.repository;
import com.telemedicina.metricas.model.Medicion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface MedicionRepository extends MongoRepository<Medicion,String> {
 List<Medicion> findByPacienteIdOrderByFechaHoraDesc(UUID pacienteId);
 List<Medicion> findAllByOrderByFechaHoraDesc();
}
