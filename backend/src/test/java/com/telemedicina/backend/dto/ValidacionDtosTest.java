package com.telemedicina.backend.dto;

import com.telemedicina.backend.dto.request.*;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ValidacionDtosTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test void pacienteSinNombreOEmailValidoEsRechazado() {
        var invalido = new PacienteRequest("", "Perez", "123", java.time.LocalDate.of(1990, 1, 1), "123", "correo-invalido");
        var errores = validator.validate(invalido);
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("nombre")));
        assertTrue(errores.stream().anyMatch(e -> e.getPropertyPath().toString().equals("email")));
    }

    @Test void dispositivoSinPacienteEsRechazado() {
        var dto = new DispositivoRequest("Pulso", "SN01", com.telemedicina.backend.entity.EstadoDispositivo.ACTIVO, "Sensor", null);
        assertTrue(validator.validate(dto).stream().anyMatch(e -> e.getPropertyPath().toString().equals("pacienteId")));
    }

    @Test void medicionSinValorEsRechazada() {
        var dto = new MedicionRequest("PULSACIONES", null, UUID.randomUUID(), UUID.randomUUID());
        assertTrue(validator.validate(dto).stream().anyMatch(e -> e.getPropertyPath().toString().equals("valor")));
    }

    @Test void medicionCompletaEsValida() {
        var dto = new MedicionRequest("PULSACIONES", BigDecimal.valueOf(85), UUID.randomUUID(), UUID.randomUUID());
        assertTrue(validator.validate(dto).isEmpty());
    }
}
