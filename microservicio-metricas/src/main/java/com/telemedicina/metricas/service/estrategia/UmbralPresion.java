package com.telemedicina.metricas.service.estrategia;
import java.math.BigDecimal;
import lombok.Getter;
/** Umbral configurable: límites inclusivos para pruebas, sin criterio clínico diagnóstico. */
@Getter
public class UmbralPresion implements EstrategiaUmbral {
 private final BigDecimal minimo;
 private final BigDecimal maximo;
 public UmbralPresion(BigDecimal minimo,BigDecimal maximo) {
  if(minimo==null||maximo==null||minimo.compareTo(maximo)>0)throw new IllegalArgumentException("Umbral inválido");
  this.minimo=minimo;this.maximo=maximo;
 }
 @Override public boolean estaEnRango(BigDecimal valor) {return valor!=null&&valor.compareTo(minimo)>=0&&valor.compareTo(maximo)<=0;}
}
