package com.telemedicina.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "turno")
@Getter
@Setter
@NoArgsConstructor
public class Turno {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private LocalDate fecha;

    @NotNull
    private LocalTime hora;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoTurno estado;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Transient
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private UUID pacienteId;

    @Transient
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private UUID medicoId;

    @JsonProperty("pacienteId")
    public UUID getPacienteId() {
        return paciente != null ? paciente.getId() : pacienteId;
    }

    @JsonProperty("pacienteId")
    public void setPacienteId(UUID id) {
        pacienteId = id;
    }

    @JsonProperty("medicoId")
    public UUID getMedicoId() {
        return medico != null ? medico.getId() : medicoId;
    }

    @JsonProperty("medicoId")
    public void setMedicoId(UUID id) {
        medicoId = id;
    }

    public void cancelar() {
        estado = EstadoTurno.CANCELADO;
    }

    public void confirmar() {
        estado = EstadoTurno.CONFIRMADO;
    }

    public void reprogramar(LocalDate nuevaFecha, LocalTime nuevaHora) {
        fecha = nuevaFecha;
        hora = nuevaHora;
        estado = EstadoTurno.A_CONFIRMAR;
    }
}
