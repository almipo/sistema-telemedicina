package com.telemedicina.simulador;

import com.telemedicina.simulador.client.ClienteMetricas;
import com.telemedicina.simulador.client.ClienteDispositivos;
import java.io.IOException;
import com.telemedicina.simulador.config.ConfiguracionSimulador;
import com.telemedicina.simulador.service.EjecutorSimulacion;
import com.telemedicina.simulador.service.GeneradorMediciones;
import java.util.random.RandomGenerator;

/** Aplicación Java 21 independiente: no inicia un servidor ni necesita Spring Boot. */
public final class SimuladorIotApplication {
    private SimuladorIotApplication() {}

    public static void main(String[] args) {
        try {
            var configuracion = ConfiguracionSimulador.desde(args, System.getenv());
            System.out.printf("Simulador IoT iniciado. Destino: %s | cantidad: %s | intervalo: %s%n",
                    configuracion.endpoint(), configuracion.cantidad() == 0 ? "continua" : configuracion.cantidad(), configuracion.intervalo());
            try (var cliente = new ClienteMetricas(configuracion.endpoint(), configuracion.timeout())) {
                var ejecutor = new EjecutorSimulacion(configuracion,
                    new GeneradorMediciones(RandomGenerator.getDefault(), configuracion.riesgo()),
                    cliente, System.out);
                if (configuracion.todos()) {
                    try (var dispositivos = new ClienteDispositivos(configuracion.endpointDispositivos(), configuracion.timeout())) {
                        ejecutor.ejecutarTodos(dispositivos);
                    }
                } else ejecutor.ejecutar();
            }
        } catch (ConfiguracionSimulador.SolicitudAyudaException ex) {
            System.out.println("Modo automático: --todos [--backend http://localhost:8080] [--api http://localhost:8081] "
                + "[--intervalo 2] [--cantidad 100] [--riesgo 0.2]");
            System.out.println("Modo individual: --paciente UUID --dispositivo UUID [--api http://localhost:8081] "
                + "[--intervalo 5] [--cantidad 0] [--riesgo 0.2] [--timeout 10]");
        } catch (IOException ex) {
            System.err.println("Error al consultar dispositivos: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            System.out.println("Simulación detenida");
        } catch (IllegalArgumentException ex) {
            System.err.println("Configuración inválida: " + ex.getMessage());
            System.exit(2);
        }
    }
}
