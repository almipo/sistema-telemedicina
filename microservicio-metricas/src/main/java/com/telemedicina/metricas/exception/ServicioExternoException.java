package com.telemedicina.metricas.exception;
import org.springframework.http.HttpStatusCode;
import lombok.Getter;
@Getter
public class ServicioExternoException extends RuntimeException {
 private final HttpStatusCode estado;
 public ServicioExternoException(String mensaje,HttpStatusCode estado,Throwable causa){super(mensaje,causa);this.estado=estado;}
}
