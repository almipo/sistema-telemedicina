package com.telemedicina.metricas.model;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Umbrales generales persistidos; valores demostrativos, no diagnósticos. */
@Document(collection = "umbrales_configuracion")
public class UmbralConfiguracion {
    @Id private String tipo;
    private BigDecimal minimo;
    private BigDecimal maximo;

    public UmbralConfiguracion() {}
    public UmbralConfiguracion(String tipo, BigDecimal minimo, BigDecimal maximo) {
        this.tipo = tipo;
        this.minimo = minimo;
        this.maximo = maximo;
    }
    public String getTipo() { return tipo; }
    public BigDecimal getMinimo() { return minimo; }
    public BigDecimal getMaximo() { return maximo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setMinimo(BigDecimal minimo) { this.minimo = minimo; }
    public void setMaximo(BigDecimal maximo) { this.maximo = maximo; }
}
