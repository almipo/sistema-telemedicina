package com.telemedicina.alertas.service;

import com.telemedicina.alertas.config.AlertasSocketHandler;
import com.telemedicina.alertas.dto.*;
import com.telemedicina.alertas.entity.*;
import com.telemedicina.alertas.mapper.AlertaMapper;
import com.telemedicina.alertas.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertasServiceTest {
    private final AlertaRepository alertas = mock(AlertaRepository.class);
    private final NotificacionRepository notificaciones = mock(NotificacionRepository.class);
    private final NotificacionService notificacionService = mock(NotificacionService.class);
    private final AlertasSocketHandler socket = mock(AlertasSocketHandler.class);
    private final AlertasService service = new AlertasService(
            alertas, notificaciones, notificacionService, socket, new AlertaMapper(), "PERSONAL_MEDICO");

    @Test void nuevaAlertaComienzaPendiente() {
        Alerta a = new Alerta("m1", UUID.randomUUID(), "PULSO", "Fuera de rango");
        assertNotNull(a.getId());
        assertEquals(Estado.PENDIENTE, a.getEstado());
    }
    @Test void duplicadoRetornaMismaAlerta() {
        Alerta a = new Alerta("m1", UUID.randomUUID(), "PULSO", "Fuera de rango");
        when(alertas.findByMedicionId("m1")).thenReturn(Optional.of(a));
        AlertaResponse r = service.registrar(new AlertaEntrada("m1", a.getPacienteId(), "PULSO", "Fuera de rango"));
        assertEquals(a.getId(), r.id());
        verify(alertas, never()).saveAndFlush(any());
        verifyNoInteractions(notificaciones, socket);
    }
    @Test void alertaInexistenteDevuelve404() {
        UUID id = UUID.randomUUID();
        when(alertas.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.cambiar(id, Estado.ATENDIDA));
    }
    @Test void notificacionPendiente() {
        Alerta a = new Alerta("m1", UUID.randomUUID(), "TEMPERATURA", "Alerta");
        Notificacion n = new Notificacion("ALERTA_MEDICA", a, "PERSONAL_MEDICO");
        assertEquals(EstadoNotificacion.PENDIENTE, n.getEstado());
        n.cambiarEstado(EstadoNotificacion.ENVIADA);
        assertEquals(EstadoNotificacion.ENVIADA, n.getEstado());
    }
    @Test void descartarAlerta() {
        Alerta a = new Alerta("m1", UUID.randomUUID(), "PULSO", "Alerta");
        a.descartar();
        assertEquals(Estado.DESCARTADA, a.getEstado());
    }
}
