package com.telemedicina.alertas.service;

import com.telemedicina.alertas.config.AlertasSocketHandler;
import com.telemedicina.alertas.dto.*;
import com.telemedicina.alertas.entity.*;
import com.telemedicina.alertas.mapper.AlertaMapper;
import com.telemedicina.alertas.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class AlertasService {
    private static final Logger log = LoggerFactory.getLogger(AlertasService.class);
    private final AlertaRepository alertas;
    private final NotificacionRepository notificaciones;
    private final NotificacionService notificacionService;
    private final AlertasSocketHandler socket;
    private final AlertaMapper mapper;
    private final String destinatario;

    public AlertasService(AlertaRepository alertas, NotificacionRepository notificaciones,
                         NotificacionService notificacionService, AlertasSocketHandler socket,
                         AlertaMapper mapper, @Value("${app.alertas.destinatario:PERSONAL_MEDICO}") String destinatario) {
        this.alertas = alertas;
        this.notificaciones = notificaciones;
        this.notificacionService = notificacionService;
        this.socket = socket;
        this.mapper = mapper;
        this.destinatario = destinatario;
    }

    @Transactional
    public AlertaResponse registrar(AlertaEntrada entrada) {
        Optional<Alerta> previa = alertas.findByMedicionId(entrada.medicionId());
        if (previa.isPresent()) return mapper.toResponse(previa.get());
        Alerta alerta = alertas.saveAndFlush(new Alerta(
                entrada.medicionId(), entrada.pacienteId(), entrada.tipo(), entrada.mensaje()));
        Notificacion notificacion = notificaciones.saveAndFlush(new Notificacion("ALERTA_MEDICA", alerta, destinatario));
        AlertaResponse respuesta = mapper.toResponse(alerta);
        alConfirmar(() -> emitir(respuesta, notificacion.getId()));
        return respuesta;
    }

    @Transactional
    public AlertaResponse cambiar(UUID id, Estado estado) {
        Alerta alerta = alertas.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Alerta inexistente"));
        alerta.cambiarEstado(estado);
        AlertaResponse respuesta = mapper.toResponse(alerta);
        alConfirmar(() -> {
            try { socket.notificar(respuesta); }
            catch (Exception ex) { log.warn("No se pudo emitir cambio de estado de alerta {}", id, ex); }
        });
        return respuesta;
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listar(UUID pacienteId) {
        List<Alerta> lista = pacienteId == null ? alertas.findAllByOrderByFechaHoraDesc()
                : alertas.findByPacienteIdOrderByFechaHoraDesc(pacienteId);
        return lista.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AlertaResponse buscar(UUID id) {
        return mapper.toResponse(alertas.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Alerta inexistente")));
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> notificacionesDe(UUID alertaId) {
        if (!alertas.existsById(alertaId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta inexistente");
        return notificaciones.findByAlertaId(alertaId).stream().map(mapper::toResponse).toList();
    }

    private void emitir(AlertaResponse alerta, UUID notificacionId) {
        try {
            boolean entregado = socket.notificar(alerta);
            // Sin clientes conectados, queda PENDIENTE para distinguir de una entrega real.
            if (entregado) notificacionService.actualizarEstado(notificacionId, EstadoNotificacion.ENVIADA);
        } catch (Exception ex) {
            log.warn("No pudo emitirse la alerta {} por WebSocket", alerta.id(), ex);
            try { notificacionService.actualizarEstado(notificacionId, EstadoNotificacion.FALLIDA); }
            catch (Exception updateError) { log.error("No se pudo marcar la notificación fallida {}", notificacionId, updateError); }
        }
    }

    private void alConfirmar(Runnable accion) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                try { accion.run(); } catch (Exception ex) { log.error("Error notificando después de confirmar transacción", ex); }
            }
        });
    }
}
