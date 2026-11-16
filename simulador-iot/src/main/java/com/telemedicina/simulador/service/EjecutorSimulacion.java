package com.telemedicina.simulador.service;

import com.telemedicina.simulador.client.ClienteMetricas;
import com.telemedicina.simulador.client.ClienteDispositivos;
import com.telemedicina.simulador.model.DispositivoDisponible;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.random.RandomGenerator;
import com.telemedicina.simulador.config.ConfiguracionSimulador;
import com.telemedicina.simulador.model.TipoMedicion;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;

/** Envía una medición por intervalo; cantidad=0 significa ejecución continua. */
public final class EjecutorSimulacion {
    private final ConfiguracionSimulador config;
    private final GeneradorMediciones generador;
    private final ClienteMetricas cliente;
    private final PrintStream salida;

    public EjecutorSimulacion(ConfiguracionSimulador config, GeneradorMediciones generador,
                             ClienteMetricas cliente, PrintStream salida) {
        this.config = Objects.requireNonNull(config);
        this.generador = Objects.requireNonNull(generador);
        this.cliente = Objects.requireNonNull(cliente);
        this.salida = Objects.requireNonNull(salida);
    }

    public void ejecutarTodos(ClienteDispositivos dispositivos) throws InterruptedException, IOException {
        List<DispositivoDisponible> activos = dispositivos.obtenerActivos();
        if (activos.isEmpty()) throw new IllegalArgumentException("No hay dispositivos activos con un tipo reconocido y paciente asociado");
        salida.printf("Dispositivos activos aptos: %d. Generando una medición cada %s.%n", activos.size(), config.intervalo());
        RandomGenerator random = RandomGenerator.getDefault();
        // Primera ronda garantiza que cada dispositivo participe una vez.
        List<DispositivoDisponible> ronda = new ArrayList<>(activos);
        Collections.shuffle(ronda);
        long indice = 0;
        while (!Thread.currentThread().isInterrupted() && (config.cantidad() == 0 || indice < config.cantidad())) {
            DispositivoDisponible dispositivo = indice < ronda.size()
                ? ronda.get((int) indice) : activos.get(random.nextInt(activos.size()));
            var mediciones = dispositivo.mediciones();
            var tipo = mediciones.get(random.nextInt(mediciones.size()));
            enviar(generador.generar(tipo, dispositivo.pacienteId(), dispositivo.id()));
            indice++;
            if (config.cantidad() == 0 || indice < config.cantidad())
                Thread.sleep(config.intervalo().toMillis());
        }
    }

    public void ejecutar() throws InterruptedException {
        var tipos = TipoMedicion.values();
        long indice = 0;
        while (!Thread.currentThread().isInterrupted() && (config.cantidad() == 0 || indice < config.cantidad())) {
            var medicion = generador.generar(tipos[(int)(indice % tipos.length)], config.pacienteId(), config.dispositivoId());
            enviar(medicion);
            indice++;
            if (config.cantidad() == 0 || indice < config.cantidad())
                Thread.sleep(config.intervalo().toMillis());
        }
    }

    private void enviar(com.telemedicina.simulador.model.MedicionSimulada medicion) throws InterruptedException {
            try {
                var resultado = cliente.enviar(medicion);
                if (resultado.correcto()) {
                    salida.printf("[201 CREATED] paciente=%s dispositivo=%s %s=%s%n", medicion.pacienteId(), medicion.dispositivoId(), medicion.tipo(), medicion.valor());
                } else {
                    salida.printf("[HTTP %d] dispositivo=%s %s=%s | %s%n", resultado.codigoHttp(), medicion.dispositivoId(), medicion.tipo(), medicion.valor(),
                        resumenError(resultado.cuerpo()));
                }
            } catch (IOException e) {
                salida.printf("[SIN CONEXIÓN] %s | %s%n", medicion.tipo(), e.getMessage());
            }
    }

    private String resumenError(String mensaje) {
        if (mensaje == null) return "";
        return mensaje.replaceAll("[\r\n\t]", " ").substring(0, Math.min(mensaje.length(), 200));
    }
}
