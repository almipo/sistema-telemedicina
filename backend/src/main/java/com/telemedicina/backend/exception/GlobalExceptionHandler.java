package com.telemedicina.backend.exception;

import com.telemedicina.backend.dto.response.ErrorResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import java.time.Instant;
import java.util.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
 private ResponseEntity<ErrorResponse> error(HttpStatusCode status,String mensaje,List<String> detalles){
  return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(),status.value(),mensaje,detalles));
 }
 @ExceptionHandler(RecursoNoEncontradoException.class)
 public ResponseEntity<ErrorResponse> notFound(RecursoNoEncontradoException e){return error(HttpStatus.NOT_FOUND,e.getMessage(),List.of());}
 @ExceptionHandler(ConflictoException.class)
 public ResponseEntity<ErrorResponse> conflict(ConflictoException e){return error(HttpStatus.CONFLICT,e.getMessage(),List.of());}
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e){return error(HttpStatus.BAD_REQUEST,"Datos invalidos",e.getBindingResult().getFieldErrors().stream().map(f->f.getField()+": "+f.getDefaultMessage()).toList());}
 @ExceptionHandler(HttpMessageNotReadableException.class)
 public ResponseEntity<ErrorResponse> malformed(HttpMessageNotReadableException e){return error(HttpStatus.BAD_REQUEST,"JSON invalido o tipo de dato incorrecto",List.of());}
 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<ErrorResponse> argument(IllegalArgumentException e){return error(HttpStatus.BAD_REQUEST,e.getMessage(),List.of());}
 @ExceptionHandler(DataIntegrityViolationException.class)
 public ResponseEntity<ErrorResponse> integrity(DataIntegrityViolationException e){return error(HttpStatus.CONFLICT,"Restriccion de integridad de datos",List.of());}
 @ExceptionHandler(ResponseStatusException.class)
 public ResponseEntity<ErrorResponse> response(ResponseStatusException e){return error(e.getStatusCode(),e.getReason()==null?"Error HTTP":e.getReason(),List.of());}
 @ExceptionHandler(RestClientResponseException.class)
 public ResponseEntity<ErrorResponse> upstream(RestClientResponseException e){return error(e.getStatusCode(),"Error del microservicio",List.of());}
 @ExceptionHandler(ResourceAccessException.class)
 public ResponseEntity<ErrorResponse> unavailable(ResourceAccessException e){return error(HttpStatus.BAD_GATEWAY,"Microservicio no disponible",List.of());}
}
