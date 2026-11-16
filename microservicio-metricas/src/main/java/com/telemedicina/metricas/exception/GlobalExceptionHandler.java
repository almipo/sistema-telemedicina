package com.telemedicina.metricas.exception;
import com.telemedicina.metricas.dto.response.ErrorResponse;
import java.time.Instant;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
@RestControllerAdvice
public class GlobalExceptionHandler {
 private ResponseEntity<ErrorResponse> error(HttpStatusCode code,String mensaje){return ResponseEntity.status(code).body(new ErrorResponse(Instant.now(),code.value(),"Error",mensaje));}
 @ExceptionHandler({MedicionInvalidaException.class,IllegalArgumentException.class,MethodArgumentNotValidException.class,HttpMessageNotReadableException.class})
 public ResponseEntity<ErrorResponse> invalido(Exception ex){return error(HttpStatus.BAD_REQUEST,"Medición o solicitud inválida");}
 @ExceptionHandler(RecursoNoEncontradoException.class)
 public ResponseEntity<ErrorResponse> inexistente(RecursoNoEncontradoException ex){return error(HttpStatus.NOT_FOUND,ex.getMessage());}
 @ExceptionHandler(ServicioExternoException.class)
 public ResponseEntity<ErrorResponse> remoto(ServicioExternoException ex){
  HttpStatusCode estado=ex.getEstado().value()==404?HttpStatus.NOT_FOUND:ex.getEstado().value()==400?HttpStatus.BAD_REQUEST:HttpStatus.BAD_GATEWAY;
  return error(estado,ex.getMessage());
 }
 @ExceptionHandler(AlertaPendienteException.class)
 public ResponseEntity<ErrorResponse> alerta(AlertaPendienteException ex){return error(HttpStatus.BAD_GATEWAY,ex.getMessage()+" (id="+ex.getMedicionId()+"). Comprobar la alerta antes de reintentar");}
}
