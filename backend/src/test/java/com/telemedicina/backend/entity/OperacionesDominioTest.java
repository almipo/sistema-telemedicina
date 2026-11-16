package com.telemedicina.backend.entity;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class OperacionesDominioTest {
 @Test void dispositivoConectaYDesconecta(){ Dispositivo d=new Dispositivo();d.conectar();assertEquals(EstadoDispositivo.ACTIVO,d.getEstado());d.desconectar();assertEquals(EstadoDispositivo.INACTIVO,d.getEstado());}
 @Test void turnoReprogramaConfirmaYCancela(){Turno t=new Turno();t.reprogramar(LocalDate.of(2027,1,10),LocalTime.of(9,30));assertEquals(EstadoTurno.A_CONFIRMAR,t.getEstado());assertEquals(LocalTime.of(9,30),t.getHora());t.confirmar();assertEquals(EstadoTurno.CONFIRMADO,t.getEstado());t.cancelar();assertEquals(EstadoTurno.CANCELADO,t.getEstado());}
 @Test void historiaAgrupaNotasYMedicaciones(){HistoriaClinica h=new HistoriaClinica();h.agregarNota("Control");h.agregarMedicacion("Paracetamol 500 mg: 1 cada 8 h");assertEquals(1,h.getNotasMedicas().size());assertEquals(1,h.getHistorialMedicacion().size());h.eliminarMedicacion("Paracetamol 500 mg: 1 cada 8 h");assertTrue(h.getHistorialMedicacion().isEmpty());}
 @Test void historiaRechazaNotasVacias(){HistoriaClinica h=new HistoriaClinica();assertThrows(IllegalArgumentException.class,()->h.agregarNota(" "));assertThrows(IllegalArgumentException.class,()->h.agregarMedicacion(null));}
 @Test void herenciaDePersona(){Paciente p=new Paciente();p.setNombre("Ana");Medico m=new Medico();m.setNombre("Luis");assertInstanceOf(Persona.class,p);assertInstanceOf(Persona.class,m);}
}
