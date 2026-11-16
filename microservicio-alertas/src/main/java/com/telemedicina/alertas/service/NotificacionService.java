package com.telemedicina.alertas.service;

import com.telemedicina.alertas.entity.*;
import com.telemedicina.alertas.repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class NotificacionService {
    private final NotificacionRepository repository;
    public NotificacionService(NotificacionRepository repository) { this.repository = repository; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void actualizarEstado(UUID id, EstadoNotificacion nuevoEstado) {
        repository.findById(id).ifPresent(n -> n.cambiarEstado(nuevoEstado));
    }
}
