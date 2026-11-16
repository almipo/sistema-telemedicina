package com.telemedicina.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Medicion {
    private String id;

    @NotBlank
    private String tipo;

    @NotNull
    private BigDecimal valor;

    private Instant fechaHora;

    @NotNull
    private UUID dispositivoId;

    @NotNull
    private UUID pacienteId;

    public boolean validar() {
        return tipo != null && !tipo.isBlank()
                && valor != null
                && fechaHora != null
                && dispositivoId != null
                && pacienteId != null;
    }
}
