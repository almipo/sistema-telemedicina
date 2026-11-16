package com.telemedicina.backend.repository;
import com.telemedicina.backend.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MedicoRepository extends JpaRepository<Medico,UUID> { boolean existsByDocumento(String documento); boolean existsByDocumentoAndIdNot(String documento, UUID id); boolean existsByMatricula(String matricula); boolean existsByMatriculaAndIdNot(String matricula, UUID id); }
