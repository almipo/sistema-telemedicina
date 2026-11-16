package com.telemedicina.backend.mapper;

import com.telemedicina.backend.dto.request.MedicionRequest;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MedicionMapperTest {
    @Test void mantieneContratoEntreDtoYModeloDelUml() {
        UUID paciente = UUID.randomUUID();
        UUID dispositivo = UUID.randomUUID();
        var model = MedicionMapper.toEntity(new MedicionRequest("PULSACIONES", new BigDecimal("85"), dispositivo, paciente));
        assertEquals(paciente, model.getPacienteId());
        assertEquals(dispositivo, model.getDispositivoId());
        var response = MedicionMapper.toResponse(model);
        assertEquals("PULSACIONES", response.tipo());
        assertEquals(new BigDecimal("85"), response.valor());
    }
}
