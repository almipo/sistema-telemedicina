package com.telemedicina.alertas.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alertas", uniqueConstraints = @UniqueConstraint(name = "uk_alertas_medicion_id", columnNames = "medicion_id"))
public class Alerta {
    @Id private UUID id;
    @Column(name = "medicion_id", nullable = false, unique = true) private String medicionId;
    @Column(nullable = false) private UUID pacienteId;
    @Column(nullable = false) private String tipo;
    @Column(nullable = false, length = 1000) private String mensaje;
    @Column(nullable = false) private LocalDateTime fechaHora;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Estado estado;

    protected Alerta() {}
    public Alerta(String medicionId, UUID pacienteId, String tipo, String mensaje) {
        this.id = UUID.randomUUID();
        this.medicionId = medicionId;
        this.pacienteId = pacienteId;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.fechaHora = LocalDateTime.now(java.time.Clock.systemUTC());
        this.estado = Estado.PENDIENTE;
    }
    public void cambiarEstado(Estado nuevoEstado) { this.estado = java.util.Objects.requireNonNull(nuevoEstado); }
    public void descartar() { cambiarEstado(Estado.DESCARTADA); }
    public UUID getId() { return id; }
    public String getMedicionId() { return medicionId; }
    public UUID getPacienteId() { return pacienteId; }
    public String getTipo() { return tipo; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public Estado getEstado() { return estado; }
}
