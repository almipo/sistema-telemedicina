package com.telemedicina.alertas.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "notificaciones")
public class Notificacion {
    @Id private UUID id;
    @Column(nullable = false) private String tipo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alerta_id", nullable = false)
    private Alerta alerta;
    @Column(nullable = false) private String destinatario;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private EstadoNotificacion estado;

    protected Notificacion() {}
    public Notificacion(String tipo, Alerta alerta, String destinatario) {
        this.id = UUID.randomUUID();
        this.tipo = java.util.Objects.requireNonNull(tipo);
        this.alerta = java.util.Objects.requireNonNull(alerta);
        this.destinatario = java.util.Objects.requireNonNull(destinatario);
        this.estado = EstadoNotificacion.PENDIENTE;
    }
    public void cambiarEstado(EstadoNotificacion estado) { this.estado = java.util.Objects.requireNonNull(estado); }
    public UUID getId() { return id; }
    public String getTipo() { return tipo; }
    public Alerta getAlerta() { return alerta; }
    public String getDestinatario() { return destinatario; }
    public EstadoNotificacion getEstado() { return estado; }
}
