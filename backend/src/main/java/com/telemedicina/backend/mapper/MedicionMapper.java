package com.telemedicina.backend.mapper;

import com.telemedicina.backend.dto.request.MedicionRequest;
import com.telemedicina.backend.dto.response.MedicionResponse;
import com.telemedicina.backend.entity.Medicion;

public final class MedicionMapper {
    private MedicionMapper() {}
    public static Medicion toEntity(MedicionRequest request) {
        Medicion m = new Medicion();
        m.setTipo(request.tipo());
        m.setValor(request.valor());
        m.setDispositivoId(request.dispositivoId());
        m.setPacienteId(request.pacienteId());
        return m;
    }
    public static MedicionResponse toResponse(Medicion medicion) {
        return new MedicionResponse(medicion.getId(), medicion.getTipo(), medicion.getValor(),
            medicion.getFechaHora(), medicion.getDispositivoId(), medicion.getPacienteId());
    }
}
