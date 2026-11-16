package com.telemedicina.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "paciente")
@Getter
@Setter
@NoArgsConstructor
public class Paciente extends Persona {

    @JsonIgnore
    @OneToMany(mappedBy = "paciente")
    private List<Dispositivo> dispositivos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "paciente")
    private List<Turno> turnos = new ArrayList<>();

    @JsonIgnore
    @OneToOne(mappedBy = "paciente")
    private HistoriaClinica historiaClinica;

    public void agregarDispositivo(Dispositivo d) {
        d.setPaciente(this);
        dispositivos.add(d);
    }

    public void eliminarDispositivo(Dispositivo d) {
        dispositivos.remove(d);
        d.setPaciente(null);
    }

    public void agregarTurno(Turno t) {
        t.setPaciente(this);
        turnos.add(t);
    }

    public void eliminarTurno(Turno t) {
        turnos.remove(t);
        t.setPaciente(null);
    }

    public HistoriaClinica consultarHistoria() {
        return historiaClinica;
    }


 public Turno solicitarTurno(Medico medico, LocalDate fecha, LocalTime hora) {
  Turno turno = new Turno();

  turno.setPaciente(this);
  turno.setMedico(medico);
  turno.setFecha(fecha);
  turno.setHora(hora);
  turno.setEstado(EstadoTurno.A_CONFIRMAR);

  this.turnos.add(turno);

  return turno;
 }

}
