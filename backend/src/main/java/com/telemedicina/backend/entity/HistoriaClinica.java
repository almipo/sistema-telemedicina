package com.telemedicina.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import java.util.*;


@Entity
@Table(name = "historia_clinica")
@Getter @Setter @NoArgsConstructor
public class HistoriaClinica {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoHistorial estado;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    @Transient @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private UUID pacienteId;

    @JsonProperty("pacienteId")
    public UUID getPacienteId() { return paciente != null ? paciente.getId() : pacienteId; }

    @JsonProperty("pacienteId")
    public void setPacienteId(UUID id) { this.pacienteId = id; }

    @ElementCollection
    @CollectionTable(name = "nota_medica", joinColumns = @JoinColumn(name = "historia_clinica_id"))
    @OrderColumn(name = "orden")
    @Column(name = "nota", nullable = false, columnDefinition = "text")
    private List<String> notasMedicas = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "historial_medicacion", joinColumns = @JoinColumn(name = "historia_clinica_id"))
    @OrderColumn(name = "orden")
    @Column(name = "medicacion", nullable = false, columnDefinition = "text")
    private List<String> historialMedicacion = new ArrayList<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "historia_medico", joinColumns = @JoinColumn(name = "historia_clinica_id"),
        inverseJoinColumns = @JoinColumn(name = "medico_id"))
    private List<Medico> medicos = new ArrayList<>();

    public void agregarNota(String nota) {
        if (nota == null || nota.isBlank()) throw new IllegalArgumentException("La nota es obligatoria");
        notasMedicas.add(nota);
    }

    public void agregarMedicacion(String medicacion) {
        if (medicacion == null || medicacion.isBlank()) throw new IllegalArgumentException("La medicación es obligatoria");
        historialMedicacion.add(medicacion);
    }

    public void eliminarMedicacion(String medicacion) { historialMedicacion.remove(medicacion); }

    public void agregarMedico(Medico medico) {
        if (medico == null || medico.getId() == null) throw new IllegalArgumentException("Médico requerido");
        if (medicos.stream().noneMatch(m -> Objects.equals(m.getId(), medico.getId()))) medicos.add(medico);
    }
}
