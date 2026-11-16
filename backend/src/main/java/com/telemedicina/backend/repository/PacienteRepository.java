package com.telemedicina.backend.repository;
import com.telemedicina.backend.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PacienteRepository extends JpaRepository<Paciente,UUID> { boolean existsByDocumento(String documento); boolean existsByDocumentoAndIdNot(String documento, UUID id); }
