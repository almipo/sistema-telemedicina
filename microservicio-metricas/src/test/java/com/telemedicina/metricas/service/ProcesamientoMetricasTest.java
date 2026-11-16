package com.telemedicina.metricas.service;



import com.telemedicina.metricas.dto.request.MedicionRequest;
import com.telemedicina.metricas.dto.response.MedicionResponse;
import com.telemedicina.metricas.exception.*;
import com.telemedicina.metricas.mapper.MedicionMapper;
import com.telemedicina.metricas.model.Medicion;
import com.telemedicina.metricas.repository.MedicionRepository;
import com.telemedicina.metricas.service.estrategia.*;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProcesamientoMetricasTest {
    @Mock
    MedicionRepository repository;
    @Mock
    RestClient client;
    @Mock
    RestClient.RequestHeadersUriSpec get;
    @Mock
    RestClient.RequestHeadersSpec getHeaders;
    @Mock
    RestClient.RequestBodyUriSpec post;
    @Mock
    RestClient.RequestBodySpec postBody;
    @Mock
    RestClient.ResponseSpec response;
    ProcesamientoMetricas servicio;
    UUID paciente = UUID.randomUUID(), dispositivo = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(repository.save(any(Medicion.class))).thenAnswer(inv -> {
            Medicion m = inv.getArgument(0);
            m.setId("id-mongo");
            return m;
        });
        servicio = new ProcesamientoMetricas(new HashMap<>(Map.of("PULSO", new FrecuenciaCardiaca())), new HashMap<>(Map.of("PULSO", new UmbralFrecuencia(BigDecimal.valueOf(50), BigDecimal.valueOf(120)))), new NullObject(), repository, new MedicionMapper(), client, "http://backend:8080", "http://alertas:8082");
        when(client.get()).thenReturn(get);
        when(get.uri(anyString(), any(Object[].class))).thenReturn(getHeaders);
        when(getHeaders.retrieve()).thenReturn(response);
        when(response.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
    }

    private MedicionRequest request(String tipo, int valor) {
        return new MedicionRequest(tipo, BigDecimal.valueOf(valor), dispositivo, paciente);
    }

    @Test
    void persisteMedicionNormalSinAlerta() {
        MedicionResponse resultado = servicio.procesar(request("PULSO", 80));
        assertEquals("id-mongo", resultado.id());
        assertEquals("lpm", resultado.unidad());
        verify(repository).save(any());
        verify(client, never()).post();
    }

    @Test
    void rechazaTipoDesconocidoAntesDePersistir() {
        assertThrows(MedicionInvalidaException.class, () -> servicio.procesar(request("OTRO", 80)));
        verifyNoInteractions(repository, client);
    }

    @Test
    void rechazaValorTecnicoInvalido() {
        assertThrows(MedicionInvalidaException.class, () -> servicio.procesar(request("PULSO", 301)));
        verifyNoInteractions(repository, client);
    }

    @Test
    void enviaAlertaAlSuperarUmbral() {
        when(client.post()).thenReturn(post);
        when(post.uri(anyString())).thenReturn(postBody);
        when(postBody.body(any(Map.class))).thenReturn(postBody);
        when(postBody.retrieve()).thenReturn(response);
        servicio.procesar(request("PULSO", 130));
        verify(repository).save(any());
        verify(client).post();
    }

    @Test
    void dejaIdSiFallaAlerta() {
        when(client.post()).thenReturn(post);
        when(post.uri(anyString())).thenReturn(postBody);
        when(postBody.body(any(Map.class))).thenReturn(postBody);
        when(postBody.retrieve()).thenThrow(new org.springframework.web.client.ResourceAccessException("alertas sin conexión"));
        var ex = assertThrows(AlertaPendienteException.class, () -> servicio.procesar(request("PULSO", 130)));
        assertEquals("id-mongo", ex.getMedicionId());
        verify(repository).save(any());
    }

    @Test
    void noGuardaSiBackendRechaza() {
        when(getHeaders.retrieve()).thenThrow(
                HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "no existe",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        null
                )
        );

        assertThrows(
                ServicioExternoException.class,
                () -> servicio.procesar(request("PULSO", 80))
        );

        verify(repository, never()).save(any());
    }

    @Test
    void permiteRegistrarEstrategiaYUmbral() {
        servicio.registrarEstrategia("TEMPERATURA", new Temperatura());
        servicio.registrarUmbral("TEMPERATURA", new UmbralTemperatura(BigDecimal.valueOf(35), BigDecimal.valueOf(38)));
        Medicion m = new Medicion();
        m.setTipo("TEMPERATURA");
        m.setValor(BigDecimal.valueOf(36));
        assertTrue(servicio.evaluarUmbral(m));
    }
}
