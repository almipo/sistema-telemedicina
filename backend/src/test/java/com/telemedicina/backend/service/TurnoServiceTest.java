package com.telemedicina.backend.service;

import com.telemedicina.backend.entity.*;
import com.telemedicina.backend.repository.*;
import com.telemedicina.backend.dto.request.TurnoRequest;
import com.telemedicina.backend.service.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class TurnoServiceTest {
 @Test void solicitaTurnoAConfirmar(){TurnoRepository repo=mock(TurnoRepository.class);EntityIntegrity integrity=mock(EntityIntegrity.class);Paciente p=new Paciente();Medico m=new Medico();UUID pId=UUID.randomUUID(),mId=UUID.randomUUID();p.setId(pId);m.setId(mId);
 when(integrity.paciente(pId)).thenReturn(p);when(integrity.medico(mId)).thenReturn(m);when(repo.save(any(Turno.class))).thenAnswer(i->i.getArgument(0));
 TurnoService s=new TurnoService(repo,integrity);var response=s.crear(new TurnoRequest(LocalDate.of(2027,1,2),LocalTime.of(10,0),pId,mId,null));assertEquals(EstadoTurno.A_CONFIRMAR,response.estado());}
}
