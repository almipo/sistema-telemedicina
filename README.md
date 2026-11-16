# Sistema de Gestión Médica y Monitoreo de Pacientes con Telemedicina

Sistema académico orientado a la gestión de pacientes, médicos, turnos, historias clínicas y dispositivos médicos, con monitoreo mediante mediciones IoT simuladas y generación de alertas en tiempo real.

El proyecto utiliza una arquitectura distribuida basada en **Java 21, Spring Boot, PostgreSQL, MongoDB, REST, WebSocket y Docker Compose**.

> **Importante:** utilizar exclusivamente datos ficticios. El sistema fue desarrollado con fines académicos y no cuenta con las certificaciones ni los mecanismos de seguridad necesarios para utilizar datos sanitarios reales.

## Arquitectura del sistema

```text
Navegador (Frontend :3000 / :63342)
  ├── REST → Backend (:8080) ──→ PostgreSQL
  ├── REST → Métricas (:8081) ──→ MongoDB
  └── REST/WebSocket → Alertas (:8082) ──→ PostgreSQL

Simulador IoT (Java 21)
  ├── Consulta dispositivos → Backend (:8080)
  └── Envía mediciones → Métricas (:8081)
                           ├── Verifica dispositivo y paciente → Backend
                           └── Detecta valores fuera de umbral → Alertas
```

El backend concentra la gestión clínica, mientras que los microservicios de Métricas y Alertas implementan responsabilidades específicas y se comunican mediante interfaces REST.

Esta separación permite mantener, actualizar o reemplazar componentes de forma independiente, siempre que se respeten sus contratos de comunicación.

### Componentes y puertos

| Componente | Responsabilidad | Puerto | Persistencia |
|---|---|---|---|
| `frontend/` | Panel de gestión médica | 3000 (Docker) / 63342 (IntelliJ) | Sin persistencia propia |
| `backend/` | Gestión clínica y API REST/GraphQL | 8080 | PostgreSQL |
| `microservicio-metricas/` | Mediciones y umbrales | 8081 | MongoDB |
| `microservicio-alertas/` | Alertas y notificaciones WebSocket | 8082 | PostgreSQL |
| `simulador-iot/` | Generación de mediciones simuladas | Sin puerto de escucha | Sin persistencia propia |
| PostgreSQL | Almacenamiento relacional | 5433 (host) / 5432 (contenedor) | Volumen persistente |
| MongoDB | Almacenamiento de mediciones y umbrales | 27018 (host) / 27017 (contenedor) | Volumen persistente |

Los puertos corresponden a la configuración local definida en `docker-compose.yml`.

## Tecnologías utilizadas

- **Java 21:** desarrollo del backend, microservicios y simulador.
- **Spring Boot:** implementación de servicios y API.
- **Maven:** gestión de dependencias y construcción de los módulos Java.
- **PostgreSQL:** persistencia de información relacional.
- **MongoDB:** almacenamiento de mediciones y configuración de umbrales.
- **REST y GraphQL:** interfaces de acceso a los datos.
- **WebSocket:** comunicación de alertas en tiempo real.
- **HTML, CSS y JavaScript:** interfaz web.
- **Docker y Docker Compose:** construcción y ejecución de servicios en contenedores.
- **Swagger/OpenAPI:** documentación y pruebas de endpoints REST.

## Organización del proyecto

```text
sistema-telemedicina/
├── backend/
│   ├── src/
│   ├── Dockerfile
│   └── README.md
├── microservicio-metricas/
│   ├── src/
│   ├── Dockerfile
│   └── README.md
├── microservicio-alertas/
│   ├── src/
│   ├── Dockerfile
│   └── README.md
├── frontend/
│   ├── Dockerfile
│   └── README.md
├── simulador-iot/
│   └── README.md
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

Cada módulo incluye su propia documentación, con información sobre responsabilidades, configuración, ejecución y funcionalidades.

## Requisitos de ejecución

El sistema puede ejecutarse de dos maneras.

### Opción A: ejecución con Docker

Requiere:

- Docker Desktop o Docker Engine.
- Docker Compose.
- Git, para clonar el repositorio.
- Navegador web actualizado.

No es necesario instalar Java, Maven, PostgreSQL ni MongoDB de manera independiente para ejecutar los servicios contenedorizados.

### Opción B: desarrollo desde IntelliJ IDEA

Requiere:

- Java 21.
- Maven 3.9 o superior.
- IntelliJ IDEA.
- Docker, para ejecutar PostgreSQL y MongoDB.
- Navegador web actualizado.

Esta modalidad permite ejecutar y depurar individualmente el backend y los microservicios.

## Ejecución y despliegue con Docker Compose

Docker Compose permite administrar los contenedores del frontend, backend, microservicios y bases de datos mediante una configuración centralizada.

### 1. Clonar el repositorio

```bash
git clone URL_DEL_REPOSITORIO
cd sistema-telemedicina
```

Reemplazar `URL_DEL_REPOSITORIO` por la dirección del repositorio publicado en GitHub.

### 2. Configurar las variables de entorno

Crear el archivo `.env` a partir de `.env.example`.

En PowerShell:

```powershell
Copy-Item .env.example .env
```

Completar las variables de conexión correspondientes a PostgreSQL y MongoDB.

La configuración local utilizada durante el desarrollo incluye:

```dotenv
POSTGRES_DB=telemedicina_dev
DB_USER=telemedicina
DB_PASSWORD=CAMBIAR_CONTRASENA
MONGO_URI_DOCKER=mongodb://USUARIO:CONTRASENA@mongodb:27017/telemedicina_metricas?authSource=admin
```

Los valores anteriores son ilustrativos. Deben coincidir con los usuarios realmente existentes en cada base de datos.

**MongoDB utiliza autenticación.** La configuración actual conserva los usuarios almacenados en el volumen existente. Para desplegar el sistema en una instalación completamente nueva es necesario inicializar previamente el usuario correspondiente. Este procedimiento debe verificarse antes de utilizar el repositorio como instalación reproducible desde cero.

El archivo `.env` no debe publicarse en GitHub.

### 3. Validar la configuración

```bash
docker compose config --quiet
```

Si no se muestran errores, continuar con el arranque.

### 4. Construir e iniciar los contenedores

```bash
docker compose up -d --build
```

Este comando descarga las imágenes necesarias, construye las aplicaciones e inicia los servicios definidos en Docker Compose.

### 5. Verificar los contenedores

```bash
docker compose ps
```

Los servicios previstos son:

| Servicio | Puerto local |
|---|---|
| Frontend | 3000 |
| Backend | 8080 |
| Métricas | 8081 |
| Alertas | 8082 |
| PostgreSQL | 5433 |
| MongoDB | 27018 |

Que un contenedor figure como iniciado no garantiza por sí solo que todas las integraciones funcionen. Se recomienda realizar las comprobaciones funcionales indicadas más adelante.

### 6. Acceder a los servicios

- **Panel médico:** http://localhost:3000
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Documentación OpenAPI:** http://localhost:8080/v3/api-docs
- **API de Métricas:** http://localhost:8081/api/umbrales
- **API de Alertas:** http://localhost:8082/api/alertas

### 7. Actualizar los contenedores

Después de realizar modificaciones en el código:

```bash
docker compose up -d --build
```

Docker reconstruirá los servicios que correspondan y aplicará la configuración actualizada.

### 8. Detener el sistema

```bash
docker compose down
```

Este comando detiene y elimina los contenedores del proyecto, conservando los datos almacenados en los volúmenes persistentes.

> **Advertencia:** no ejecutar `docker compose down -v` si se desean conservar los datos, ya que elimina los volúmenes administrados por el proyecto.

## Ejecución desde IntelliJ IDEA

Esta modalidad utiliza Docker únicamente para las bases de datos, mientras las aplicaciones Java se ejecutan desde IntelliJ.

### 1. Iniciar PostgreSQL y MongoDB

Desde la raíz del proyecto:

```bash
docker compose up -d postgres mongodb
```

Comprobar su estado:

```bash
docker compose ps
```

Si existen contenedores del backend, Métricas o Alertas en ejecución, detenerlos para evitar conflictos de puertos:

```bash
docker compose stop backend metricas alertas
```

### 2. Configurar las aplicaciones

Configurar las variables de entorno de cada aplicación mediante **Run → Edit Configurations**.

Ejemplo de configuración para desarrollo local:

```text
Backend:
  DB_URL=jdbc:postgresql://localhost:5433/telemedicina_dev
  DB_USER=telemedicina
  DB_PASSWORD=<contraseña local>

Alertas:
  DB_URL=jdbc:postgresql://localhost:5433/telemedicina_dev
  DB_USER=telemedicina
  DB_PASSWORD=<contraseña local>

Métricas:
  MONGO_URI=mongodb://USUARIO:CONTRASENA@localhost:27018/telemedicina_metricas?authSource=admin
  BACKEND_URL=http://localhost:8080
  ALERTAS_URL=http://localhost:8082
```

En esta modalidad, las aplicaciones Java utilizan `localhost` para acceder a los puertos publicados por Docker.

La configuración detallada de cada módulo se encuentra en su respectivo README.

### 3. Iniciar las aplicaciones

Ejecutar desde IntelliJ:

1. `BackendApplication`.
2. `AlertasApplication`.
3. `MetricasApplication`.

### 4. Abrir el frontend

Utilizar `http://localhost:3000` si el frontend está servido por Docker, o la dirección proporcionada por el servidor HTTP de IntelliJ, normalmente bajo `http://localhost:63342/`.

No utilizar directamente el protocolo `file://`.

### 5. Registrar información inicial

Registrar pacientes ficticios y asociarles dispositivos activos con tipos compatibles.

Posteriormente, iniciar el simulador IoT para generar mediciones.

## Documentación y pruebas de la API

### Swagger UI

El backend dispone de documentación interactiva mediante Swagger/OpenAPI.

**Puerto:** `8080`

**Interfaz Swagger:** http://localhost:8080/swagger-ui/index.html

**Especificación OpenAPI:** http://localhost:8080/v3/api-docs

Desde Swagger UI es posible consultar los endpoints, revisar parámetros, visualizar estructuras de solicitudes y probar operaciones REST.

Para acceder, el backend debe estar ejecutándose.

## Monitoreo mediante el simulador IoT

El simulador genera mediciones ficticias asociadas a dispositivos registrados en el sistema.

En el modo `--todos`, consulta los dispositivos disponibles y selecciona aquellos que poseen:

- Estado `ACTIVO`.
- Paciente asociado.
- Tipo de dispositivo reconocido.

### Tipos de dispositivos y mediciones

| Dispositivo | Mediciones generadas |
|---|---|
| `PULSOMETRO` | `PULSO` |
| `TERMOMETRO` | `TEMPERATURA` |
| `TENSIOMETRO` | `PRESION_SISTOLICA`, `PRESION_DIASTOLICA` |
| `MONITOR_MULTIPARAMETRICO` | Pulso, temperatura y ambas presiones |

Cada medición incluye:

```json
{
  "tipo": "PULSO",
  "valor": 85,
  "pacienteId": "UUID_DEL_PACIENTE",
  "dispositivoId": "UUID_DEL_DISPOSITIVO"
}
```

El microservicio de Métricas recibe la medición, verifica su asociación con el dispositivo y paciente, la registra y evalúa el valor utilizando el umbral correspondiente al tipo de medición.

Cuando se detecta un valor fuera del rango configurado, se inicia el flujo de generación de alertas.

### Ejecutar el simulador

Desde IntelliJ IDEA, seleccionar `SimuladorIotApplication` y abrir:

**Run → Edit Configurations → Program arguments**

Ingresar:

```text
--todos --backend http://localhost:8080 --api http://localhost:8081 --intervalo 10 --cantidad 100 --riesgo 0.2
```

Parámetros principales:

| Parámetro | Descripción |
|---|---|
| `--todos` | Utiliza dispositivos registrados y compatibles |
| `--backend` | Dirección de la API principal |
| `--api` | Dirección de Métricas |
| `--intervalo 10` | Envía una medición cada 10 segundos |
| `--cantidad 100` | Genera 100 mediciones |
| `--riesgo 0.2` | Probabilidad del 20 % de producir valores anómalos simulados |

El simulador utiliza rangos internos para generar valores. La evaluación definitiva corresponde al microservicio de Métricas, que aplica los umbrales actualmente configurados.

El parámetro `--cantidad 0` permite realizar una simulación continua. Para detenerla, finalizar el proceso de IntelliJ.

El simulador se ejecuta de manera independiente de los contenedores principales.

## Gestión de umbrales

Los umbrales se configuran desde el frontend y son administrados por el microservicio de Métricas.

Se contemplan cuatro tipos de medición:

- Pulso.
- Temperatura.
- Presión sistólica.
- Presión diastólica.

Cada tipo posee valores mínimos y máximos editables. Los cambios se almacenan en MongoDB y se utilizan para evaluar las nuevas mediciones.

Los umbrales son generales por tipo de medición, no individuales por dispositivo o paciente.

## Gestión de alertas

Las alertas se generan como consecuencia de mediciones que quedan fuera de los umbrales establecidos.

El microservicio de Alertas permite consultar registros, actualizar su estado y enviar notificaciones en tiempo real mediante WebSocket.

Desde el frontend se dispone de las siguientes acciones:

| Acción | Funcionalidad |
|---|---|
| **Datos** | Abre la ficha del paciente asociado |
| **Atender** | Actualiza el estado a `ATENDIDA` |
| **Descartar** | Actualiza el estado a `DESCARTADA` |

Las alertas se consultan mediante:

```text
http://localhost:8082/api/alertas
```

Las notificaciones WebSocket utilizan:

```text
ws://localhost:8082/ws/alertas
```

## Validación funcional

Para comprobar el funcionamiento del sistema se recomienda:

1. Consultar pacientes: `GET http://localhost:8080/api/pacientes`.
2. Consultar dispositivos: `GET http://localhost:8080/api/dispositivos`.
3. Consultar umbrales: `GET http://localhost:8081/api/umbrales`.
4. Consultar mediciones: `GET http://localhost:8081/api/mediciones`.
5. Consultar alertas: `GET http://localhost:8082/api/alertas`.
6. Registrar y consultar un paciente desde el frontend.
7. Asociar un dispositivo activo al paciente.
8. Ejecutar el simulador IoT y comprobar la recepción de mediciones.
9. Verificar la generación de alertas y sus notificaciones en tiempo real.
10. Probar las acciones Datos, Atender y Descartar.

## Pruebas automatizadas

Cada módulo Java dispone de su propio proyecto Maven.

Para ejecutar sus pruebas:

```bash
mvn test
```

Para construir un módulo:

```bash
mvn package
```

Las pruebas automatizadas deben complementarse con verificaciones de integración utilizando las bases de datos y los microservicios en ejecución.

## Diagnóstico de problemas frecuentes

| Problema | Comprobación recomendada |
|---|---|
| `Failed to fetch` o CORS | Revisar URL, puerto y origen permitido |
| Error HTTP 400 en Alertas | Comprobar endpoint `:8082/api/alertas` y registros del servicio |
| Error de conexión con MongoDB | Revisar credenciales, URI, puerto y `authSource` |
| Error de conexión con PostgreSQL | Verificar credenciales, base de datos y puerto 5433 |
| No se generan mediciones | Comprobar dispositivos activos, asociación y tipos compatibles |
| Exceso de alertas | Ajustar `--intervalo` y `--riesgo` |
| WebSocket desconectado | Verificar Alertas en el puerto 8082 |
| Conflicto de puertos | Evitar ejecutar simultáneamente los mismos servicios desde Docker e IntelliJ |

Para revisar los registros de los contenedores:

```bash
docker compose logs --tail=100 backend metricas alertas
```

## Alcance y consideraciones de seguridad

Este sistema constituye un prototipo académico y no está destinado al uso clínico real.

No incorpora controles integrales de autenticación, autorización, privacidad ni protección de información sanitaria requeridos en un entorno productivo.

Las credenciales deben configurarse mediante variables de entorno y mantenerse fuera del repositorio público.

Los datos persistidos en los volúmenes Docker no forman parte del código fuente publicado en GitHub.

Para información específica de instalación, configuración y funcionamiento, consultar el archivo `README.md` de cada módulo.
