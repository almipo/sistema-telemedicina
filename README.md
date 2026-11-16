# Sistema de Gestión Médica y Monitoreo de Pacientes con Telemedicina

Sistema académico para administrar pacientes, médicos, turnos, historias clínicas y dispositivos, registrar mediciones IoT simuladas y visualizar alertas en tiempo real. **Utilizar exclusivamente datos ficticios**: no es un sistema clínico certificado ni dispone de controles de seguridad suficientes para datos sanitarios reales.

## Arquitectura

```text
Navegador (frontend, :3000 o :63342)
  ├─ REST → Backend (:8080) ─→ PostgreSQL
  ├─ REST → Métricas (:8081) ─→ MongoDB
  └─ REST/WebSocket → Alertas (:8082) ─→ PostgreSQL

Simulador IoT (Java 21, sin puerto de escucha)
  ├─ GET dispositivos → Backend (:8080)
  └─ POST mediciones → Métricas (:8081)
                               ├─ verifica dispositivo/paciente → Backend
                               └─ detecta valores fuera de umbral → Alertas
```

| Componente | Función | Puerto local | Persistencia |
|---|---|---|---|
| `frontend/` | Panel de gestión médica | 3000 (Docker), 63342 (vista de IntelliJ) | No propia |
| `backend/` | API REST/GraphQL, gestión clínica y dispositivos | 8080 | PostgreSQL |
| `microservicio-metricas/` | Recepción de mediciones y configuración de umbrales | 8081 | MongoDB |
| `microservicio-alertas/` | Alertas, cambios de estado y notificaciones WebSocket | 8082 | PostgreSQL |
| `simulador-iot/` | Generación de mediciones por dispositivo y paciente | No publica puerto | No propia |
| PostgreSQL | Almacenamiento relacional | 5433 (host) / 5432 (contenedor) | Volumen persistente |
| MongoDB | Mediciones y umbrales | 27018 (host) / 27017 (contenedor) | Volumen persistente |

> Puertos correspondientes al `docker-compose.yml` incluido en este repositorio. Al ejecutar aplicaciones desde IntelliJ, utilizar los puertos publicados (5433/27018), salvo que el entorno local tenga otra configuración.

## Requisitos

- Java **21**, Maven **3.9+**, IntelliJ IDEA (para ejecución local).
- Docker Engine con Docker Compose (para servicios en contenedores).
- PostgreSQL y MongoDB disponibles en los puertos configurados.
- Navegador actualizado. Para pruebas manuales de API, Postman o Swagger UI.

## Organización

```text
sistema-telemedicina/
├── backend/README.md
├── microservicio-metricas/README.md
├── microservicio-alertas/README.md
├── frontend/README.md
├── simulador-iot/README.md
├── docker-compose.yml
└── README.md
```

## Puesta en marcha: IntelliJ + bases de datos Docker

1. Iniciar PostgreSQL y MongoDB con la configuración de Docker Compose existente. Verificar `docker compose ps`.
2. Iniciar **BackendApplication**, luego **AlertasApplication** y **MetricasApplication** desde IntelliJ, con variables de entorno correspondientes (detalladas en los README de cada módulo).
3. Abrir el frontend en `http://localhost:3000` si está servido por Nginx/Docker o mediante el servidor HTTP de IntelliJ (por ejemplo `http://localhost:63342/...`). No usar `file://`.
4. Registrar pacientes y dispositivos de tipo compatible, activos y asociados al paciente.
5. Iniciar **SimuladorIotApplication** con los argumentos descritos a continuación.

Variables orientativas para los procesos **locales** (reemplazar credenciales, bases y puertos por los efectivos):

```text
Backend: DB_URL=jdbc:postgresql://localhost:5433/telemedicina_dev
         DB_USER=telemedicina
         DB_PASSWORD=<contraseña local>
Alertas: DB_URL=jdbc:postgresql://localhost:5433/telemedicina_dev
         DB_USER=telemedicina
         DB_PASSWORD=<contraseña local>
Métricas: MONGO_URI=mongodb://USUARIO:CONTRASENA@localhost:27018/telemedicina_metricas?authSource=admin
          BACKEND_URL=http://localhost:8080
          ALERTAS_URL=http://localhost:8082
          CORS_ORIGINS=http://localhost:3000,http://127.0.0.1:3000,http://localhost:63342,http://127.0.0.1:63342
```

MongoDB utiliza autenticación. Ajustar el usuario, la contraseña y `authSource` según el usuario realmente creado. Si tu instancia local no usa el puerto 27018, adaptar la URL. **El ejemplo no reemplaza los valores de tu entorno existente**.

### Ejecución completa por Docker Compose

1. Crear `.env` a partir de `.env.example` (o conservar el `.env` privado existente). Definir `POSTGRES_DB`, `DB_USER`, `DB_PASSWORD`, `MONGO_ROOT_USER`, `MONGO_ROOT_PASSWORD` y `MONGO_URI_DOCKER` con los valores correctos. **No reemplazar credenciales de una base ya existente a ciegas**. `MONGO_URI_DOCKER` usa `mongodb:27017`, nunca `localhost:27018`, porque se consume desde un contenedor.
2. Verificar la configuración antes del despliegue: `docker compose config --quiet`.
3. Ejecutar desde la raíz:

```bash
docker compose up -d --build
docker compose ps
docker compose logs -f backend metricas alertas
```

Accesos: Frontend <http://localhost:3000>, Backend <http://localhost:8080>, Métricas <http://localhost:8081>, Alertas <http://localhost:8082>. PostgreSQL y MongoDB solo publican sus puertos en `127.0.0.1` (5433 y 27018, respectivamente).

**Importante sobre MongoDB y los datos existentes:** el servicio utiliza `mongod --auth`. Las variables `MONGO_ROOT_USER` y `MONGO_ROOT_PASSWORD` permiten que la imagen oficial cree el administrador **solamente al iniciar una base con volumen vacío**. Si ya existe `mongo_data`, los usuarios y contraseñas persistidos **no se reescriben**; por eso la URI de Métricas debe contener credenciales que ya funcionen. Cambiar variables no cambia las claves de un usuario MongoDB ya creado. No utilizar `docker compose down -v` salvo que se quiera destruir deliberadamente la información de las bases.

**Simulador:** tiene `Dockerfile` propio, pero se ejecuta por separado y no se incluye en Compose para evitar generar mediciones automáticamente. Ver `simulador-iot/README.md`. Con IntelliJ puede usar `localhost`; en una ejecución de contenedor conectado a la red Compose debe utilizar los nombres `backend:8080` y `metricas:8081`.

**Frontend:** las cuatro URLs públicas se configuran en las variables `FRONTEND_*` del `.env` y se convierten en `config.js` al arrancar Nginx. Se trata de direcciones que usa **el navegador**, por lo que en un despliegue local apuntan a `localhost` y no a los nombres de servicios internos. La configuración es de desarrollo local; para otro host se deben adaptar las URLs y CORS.

Para detener sin borrar datos: `docker compose down`. Para reconstruir aplicaciones después de cambios: `docker compose up -d --build`. El repositorio conserva configuraciones, **no los datos de los volúmenes ni los contenedores en ejecución**.

## Flujo de monitoreo IoT

El simulador consulta los dispositivos registrados y, en modo `--todos`, selecciona únicamente aquellos con **estado ACTIVO**, **pacienteId válido** y **tipo reconocido**. Genera lecturas según el tipo:

| Tipo de dispositivo | Mediciones compatibles |
|---|---|
| `PULSOMETRO` | `PULSO` |
| `TERMOMETRO` | `TEMPERATURA` |
| `TENSIOMETRO` | `PRESION_SISTOLICA`, `PRESION_DIASTOLICA` |
| `MONITOR_MULTIPARAMETRICO` | Las cuatro anteriores |

Cada lectura incluye `tipo`, `valor`, `pacienteId` y `dispositivoId`. Métricas comprueba la asociación y evalúa el valor contra el umbral configurable del **tipo de medición**, no contra el tipo comercial del dispositivo.

Configuración recomendada en **Run → Edit Configurations → Program arguments** de `SimuladorIotApplication`:

```text
--todos --backend http://localhost:8080 --api http://localhost:8081 --intervalo 10 --cantidad 100 --riesgo 0.2
```

Genera **100 mediciones**, **una cada 10 segundos**, con **20 % de probabilidad de producir un valor anómalo según los rangos internos del generador**. El microservicio determina finalmente si se dispara una alerta utilizando los **umbrales vigentes**, que pueden cambiar desde el panel. El generador no recalcula automáticamente su distribución en función de esos umbrales. `--cantidad 0` indica ejecución continua. Para detener una simulación continua, detener el proceso en IntelliJ.

## Validación funcional

1. `GET http://localhost:8080/api/pacientes`: lista de pacientes.
2. `GET http://localhost:8080/api/dispositivos`: dispositivos registrados.
3. `GET http://localhost:8081/api/umbrales`: rangos editables.
4. `GET http://localhost:8081/api/mediciones`: mediciones persistidas.
5. `GET http://localhost:8082/api/alertas`: alertas registradas.
6. En el frontend: comprobar las alertas en tiempo real, los botones **Datos**, **Atender** y **Descartar**, y la ficha del paciente.

La consulta REST de alertas del frontend usa el servicio **8082**, no el proxy del backend **8080**. El WebSocket utiliza `ws://localhost:8082/ws/alertas`.

## Pruebas

Desde cada módulo Java: `mvn test`. El simulador también puede ejecutarse con `mvn package` y `java -jar target/simulador-iot-1.0.0.jar ...`. Las pruebas automatizadas no sustituyen la validación integrada con bases de datos y servicios activos.

## Diagnóstico rápido

| Síntoma | Comprobación |
|---|---|
| `Failed to fetch` / CORS | Revisar origen del frontend, URL destino y configuración CORS del servicio |
| `HTTP 400` en alertas | Verificar que se consulta `:8082/api/alertas`; revisar respuesta/logs |
| MongoDB no conecta | Comprobar `MONGO_URI`, puerto publicado, credenciales y `authSource` |
| No aparecen lecturas | Confirmar dispositivos `ACTIVO`, paciente asociado, tipo reconocido, Métricas accesible |
| Exceso de alertas | Ajustar `--intervalo` y `--riesgo` del simulador |
| WebSocket desconectado | Revisar microservicio :8082 y `ALERTAS_WS_URL` |

## Alcance y seguridad

Proyecto educativo. No incluye autenticación/autorización integral ni controles de privacidad adecuados para un entorno clínico real. No utilizar datos médicos reales ni exponer directamente los servicios a Internet. Cada componente se documenta en su propio `README.md`.
