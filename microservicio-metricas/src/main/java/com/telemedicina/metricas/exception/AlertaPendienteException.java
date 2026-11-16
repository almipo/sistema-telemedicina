package com.telemedicina.metricas.exception;
import lombok.Getter;
@Getter
public class AlertaPendienteException extends RuntimeException {
 private final String medicionId;
 public AlertaPendienteException(String medicionId,Throwable causa){super("La medición fue guardada, pero falló la notificación de alerta",causa);this.medicionId=medicionId;}
}
