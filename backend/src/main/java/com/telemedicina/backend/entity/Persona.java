package com.telemedicina.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @NotBlank
    @Column(nullable = false)
    private String apellido;

    @NotBlank
    @Column(nullable = false)
    private String documento;

    @NotNull
    private LocalDate fechaNacimiento;

    @NotBlank
    private String telefono;

    @Email
    @NotBlank
    @Column(nullable = false)
    private String email;
}
