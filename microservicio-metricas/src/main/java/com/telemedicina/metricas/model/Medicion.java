package com.telemedicina.metricas.model;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter @Setter @NoArgsConstructor
@Document(collection="mediciones")
public class Medicion {
 @Id private String id;
 private String tipo;
 private BigDecimal valor;
 private Instant fechaHora;
 private UUID dispositivoId;
 private UUID pacienteId;
}
