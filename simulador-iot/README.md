# Simulador IoT — Generador de mediciones

Cliente de consola desarrollado en **Java 21 / Maven** que genera valores ficticios y envía mediciones mediante HTTP al microservicio de Métricas. No utiliza sensores físicos, no requiere una base de datos propia y **no publica puertos**.

## Funcionamiento

En modo `--todos` consulta `GET /api/dispositivos` del backend, filtra dispositivos con `estado=ACTIVO`, paciente asociado y tipo reconocido, y genera una medición cada intervalo configurado. La **primera ronda** recorre una vez cada dispositivo elegible (si la cantidad alcanza); luego selecciona aleatoriamente entre ellos.

| Tipo registrado de dispositivo | Mediciones emitidas |
|---|---|
| `PULSOMETRO` | `PULSO` |
| `TERMOMETRO` | `TEMPERATURA` |
| `TENSIOMETRO` | `PRESION_SISTOLICA`, `PRESION_DIASTOLICA` |
| `MONITOR_MULTIPARAMETRICO` | Las cuatro |

Los tipos de dispositivo se seleccionan desde el frontend al crear o editar el registro. El simulador asigna el `pacienteId` y `dispositivoId` reales de cada dispositivo y envía `tipo` y `valor`. **No decide qué umbral corresponde:** Métricas lo identifica por el tipo de medición recibido.

## Requisitos y conexiones

- JDK 21 y Maven; módulo `simulador-iot/pom.xml`.
- Backend accesible por HTTP (predeterminado `http://localhost:8080`).
- Métricas accesible por HTTP (predeterminado `http://localhost:8081`).
- Dispositivos activos asociados a pacientes ya cargados.
- MongoDB, PostgreSQL y Alertas operativos para comprobar el circuito completo.

## Ejecución recomendada con IntelliJ

1. Abrir/importar `simulador-iot/pom.xml` como proyecto Maven.
2. Seleccionar la clase principal `com.telemedicina.simulador.SimuladorIotApplication`.
3. Ir a **Run → Edit Configurations** y activar **Program arguments**.
4. Pegar exactamente:

```text
--todos --backend http://localhost:8080 --api http://localhost:8081 --intervalo 10 --cantidad 100 --riesgo 0.2
```

5. Ejecutar. En consola se muestran lecturas aceptadas (`[201 CREATED]`) o errores HTTP. Se enviará **una lectura cada 10 segundos** hasta completar **100 lecturas**. El riesgo `0.2` corresponde a una probabilidad del 20 % de producir valores anómalos respecto del generador; los umbrales definitivos son evaluados en Métricas.

## Argumentos disponibles

| Argumento | Descripción | Valor por defecto |
|---|---|---|
| `--todos` | Selecciona dispositivos activos reconocidos del backend | No habilitado |
| `--backend URL` | URL base del backend, utilizada por `--todos` | `http://localhost:8080` |
| `--api URL` | URL base de Métricas | `http://localhost:8081` |
| `--intervalo N` | Segundos entre mediciones (admite decimales) | `5` |
| `--cantidad N` | Total de mediciones; `0` = sin límite | `0` |
| `--riesgo N` | Probabilidad de lectura de riesgo, entre `0` y `1` | `0.20` |
| `--paciente UUID` | Paciente para modo individual | Sin valor |
| `--dispositivo UUID` | Dispositivo para modo individual | Sin valor |
| `--timeout N` | Tiempo máximo de espera HTTP en segundos | `10` |

El modo individual (`--paciente` + `--dispositivo`, sin `--todos`) es compatible con la ejecución original y recorre los cuatro tipos de medición; **para respetar automáticamente los tipos reales de los dispositivos utilizar siempre `--todos`**.

Variables equivalentes: `SIM_TODOS`, `SIM_BACKEND_URL`, `SIM_METRICAS_URL`, `SIM_INTERVALO_SEGUNDOS`, `SIM_CANTIDAD`, `SIM_RIESGO`, `SIM_PACIENTE_ID`, `SIM_DISPOSITIVO_ID` y `SIM_TIMEOUT_SEGUNDOS`. Los argumentos prevalecen sobre las variables de entorno.

## Ejecución con terminal

Desde `simulador-iot/`:

```bash
mvn clean test
mvn package
java -jar target/simulador-iot-1.0.0.jar --todos --backend http://localhost:8080 --api http://localhost:8081 --intervalo 10 --cantidad 100 --riesgo 0.2
```

El `Dockerfile` disponible empaqueta el simulador en Java 21 y permite ejecutarlo como contenedor **cliente**. No es un servicio expuesto públicamente y el Compose general revisado no lo incluye por defecto. No se agrega al Compose por defecto para evitar una simulación automática. Si se ejecuta manualmente en un contenedor unido a la red Compose, utilizar `http://backend:8080` y `http://metricas:8081`. Ejecutarlo después de registrar dispositivos, con el intervalo deseado.

## Verificaciones

- `GET http://localhost:8080/api/dispositivos`: existen dispositivos activos con paciente asignado.
- `GET http://localhost:8081/api/mediciones`: crecen las lecturas al ejecutar.
- `GET http://localhost:8082/api/alertas`: aparecen alertas si alguna lectura queda fuera del umbral vigente.
- En el panel, la ficha del paciente muestra las mediciones, y Alertas permite **Datos**, **Atender** y **Descartar**.

**Errores frecuentes:** sin dispositivos aptos (`--todos` no encuentra tipos asociados válidos), `[SIN CONEXIÓN]` (servicio Métricas inaccesible), HTTP 400 (cuerpo/valor inválido), error de asociación (dispositivo no pertenece al paciente). Disminuir la cantidad de avisos espaciando `--intervalo 10` y reduciendo `--riesgo 0.2`. Detener la ejecución desde IntelliJ si se usa `--cantidad 0`.
