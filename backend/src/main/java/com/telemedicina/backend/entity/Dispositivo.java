package com.telemedicina.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "dispositivo")
@Getter
@Setter
@NoArgsConstructor
public class Dispositivo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String tipo;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String numeroSerie;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDispositivo estado;

    @NotBlank
    private String nombre;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;
    @Transient
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private UUID pacienteId;

    @JsonProperty("pacienteId")
    public UUID getPacienteId() {
        return paciente != null ? paciente.getId() : pacienteId;
    }

    @JsonProperty("pacienteId")
    public void setPacienteId(UUID id) {
        pacienteId = id;
    }

    public void conectar() {
        estado = EstadoDispositivo.ACTIVO;
    }

    public void desconectar() {
        estado = EstadoDispositivo.INACTIVO;
    }
}
