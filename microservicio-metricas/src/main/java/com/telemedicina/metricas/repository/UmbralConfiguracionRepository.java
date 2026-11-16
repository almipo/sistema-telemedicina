package com.telemedicina.metricas.repository;

import com.telemedicina.metricas.model.UmbralConfiguracion;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UmbralConfiguracionRepository extends MongoRepository<UmbralConfiguracion, String> {}
