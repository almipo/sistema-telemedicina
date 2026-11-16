package com.telemedicina.alertas.service;

import com.telemedicina.alertas.dto.*;
import org.springframework.stereotype.Service;

/** Entrada del flujo Medición fuera de rango -> Alerta -> Notificación. */
@Service
public class GestorAlertas {
    private final AlertasService servicio;
    public GestorAlertas(AlertasService servicio) { this.servicio = servicio; }
    public AlertaResponse procesar(AlertaEntrada medicion) { return generarAlerta(medicion); }
    public AlertaResponse generarAlerta(AlertaEntrada medicion) { return servicio.registrar(medicion); }
}
