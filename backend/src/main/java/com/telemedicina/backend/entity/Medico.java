package com.telemedicina.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "medico")
@Getter
@Setter
@NoArgsConstructor
public class Medico extends Persona {

        @NotBlank
        @Column(nullable = false)
        private String matricula;

        @NotBlank
        @Column(nullable = false)
        private String especialidad;

        @JsonIgnore
        @OneToMany(mappedBy = "medico")
        private List<Turno> turnos = new ArrayList<>();

        @JsonIgnore
        @ManyToMany(mappedBy = "medicos")
        private List<HistoriaClinica> historias = new ArrayList<>();

        public HistoriaClinica solicitarHistorialMedico(Paciente paciente) {
            Objects.requireNonNull(paciente, "El paciente es obligatorio");
            return paciente.getHistoriaClinica();
        }
    }

