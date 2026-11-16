package com.telemedicina.backend.service;

import com.telemedicina.backend.dto.request.AlertaEstadoRequest;
import com.telemedicina.backend.dto.response.AlertaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;

@Service
public class AlertaService {
    private final RestClient client;
    private final String url;
    public AlertaService(RestClient.Builder builder, @Value("${services.alertas}") String url) {
        this.client = builder.build();
        this.url = url;
    }
    public List<AlertaResponse> listar(UUID pacienteId) {
        return client.get().uri(url + "/api/alertas" + (pacienteId == null ? "" : "?pacienteId=" + pacienteId))
            .retrieve().body(new ParameterizedTypeReference<List<AlertaResponse>>() {});
    }
    public AlertaResponse obtener(UUID id) {
        return client.get().uri(url + "/api/alertas/{id}", id).retrieve().body(AlertaResponse.class);
    }
    public AlertaResponse cambiarEstado(UUID id, AlertaEstadoRequest body) {
        return client.patch().uri(url + "/api/alertas/{id}/estado", id)
            .body(body).retrieve().body(AlertaResponse.class);
    }
}
