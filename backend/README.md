# Backend — API principal de telemedicina

Servicio central de gestión médica desarrollado en **Java 21**, **Spring Boot 3.4.5** y **Maven**. Mantiene datos de pacientes, médicos, turnos, dispositivos e historias clínicas con **PostgreSQL/JPA**, y ofrece API **REST** y consultas **GraphQL**. Las mediciones y alertas se generan y almacenan en microservicios independientes.

## Responsabilidades

- CRUD de pacientes, médicos, turnos, dispositivos e historias clínicas.
- Gestión de notas, medicaciones y profesionales asociados a historias.
- Consulta de datos relacionados mediante `/api/consultas/...`.
- Verificación interna de dispositivos activos asociados a pacientes.
- Consultas y operaciones de integración con Métricas y Alertas; no almacena sus mediciones MongoDB.

Estructura principal: `entity/`, `repository/`, `dto/request/`, `dto/response/`, `mapper/`, `service/`, `web/`, `exception/`, `graphql/`, `config/`.

## Dependencias y puertos

| Elemento | Valor |
|---|---|
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| GraphQL | `http://localhost:8080/graphql` |
| PostgreSQL | Puerto publicado local (por ejemplo 5433) o `postgres:5432` en Docker |
| Métricas | `METRICAS_URL`, predeterminado `http://localhost:8081` |
| Alertas | `ALERTAS_URL`, predeterminado `http://localhost:8082` |

## Configuración

`src/main/resources/application.yml` toma los datos JDBC **obligatoriamente** de variables de entorno:

```text
DB_URL=jdbc:postgresql://localhost:5433/telemedicina_dev
DB_USER=telemedicina
DB_PASSWORD=<contraseña de la BD>
METRICAS_URL=http://localhost:8081
ALERTAS_URL=http://localhost:8082
```

Comprobar nombre y puerto reales de PostgreSQL; el ejemplo utiliza 5433. Para Docker usar `jdbc:postgresql://postgres:5432/<base>` y los nombres de servicio. La configuración utiliza `spring.jpa.hibernate.ddl-auto=update` para desarrollo; no reemplaza migraciones controladas.

## Iniciar

**IntelliJ:** abrir `backend/pom.xml`, configurar **JDK 21**, crear/editar la ejecución `BackendApplication`, colocar las variables en **Environment variables** y ejecutar. Iniciar PostgreSQL antes del backend.

**Terminal:** desde `backend/`, con variables de entorno ya configuradas:

```bash
mvn clean test
mvn spring-boot:run
```

**Docker:** desde la raíz del proyecto: `docker compose up -d --build postgres backend`. El `Dockerfile` crea un JAR con Maven y lo ejecuta en Java 21. Verificar `docker compose logs -f backend`.

## API REST principal

| Método | Ruta | Uso |
|---|---|---|
| GET / POST | `/api/pacientes` | Listar / crear pacientes |
| GET / POST | `/api/medicos` | Listar / crear médicos |
| GET / POST | `/api/dispositivos` | Listar / crear dispositivos |
| GET / POST | `/api/turnos` | Listar / crear turnos |
| GET / POST | `/api/historias` | Listar / crear historias |
| GET | `/api/turnos/estados` | Estados válidos del enum |
| GET | `/api/consultas/pacientes/{id}/dispositivos` | Dispositivos del paciente |
| GET | `/api/consultas/pacientes/{id}/historias` | Historias del paciente |
| GET | `/api/interno/dispositivos/{id}/verificar` | Verificación utilizada por Métricas |

Los recursos de CRUD también admiten `GET /{id}`, `PUT /{id}` y `DELETE /{id}` según controlador. Las notas, medicaciones y asociaciones de médicos tienen rutas específicas bajo `/api/historias/{id}`. Ver contratos y campos exactos en Swagger.

**Importante:** el backend contiene rutas `/api/mediciones` y `/api/alertas` de integración, pero la versión actual del panel consulta directamente Métricas por **8081** y Alertas por **8082**. Esto evita confundir un error de proxy con un fallo del servicio final.

## Verificación y mantenimiento

1. Comprobar que PostgreSQL esté operativo.
2. Abrir Swagger y consultar `GET /api/pacientes` y `GET /api/dispositivos`.
3. Registrar un paciente ficticio y asociar un dispositivo con estado `ACTIVO`.
4. Ejecutar `mvn test` y revisar los resultados.

Si falla el arranque, comprobar `DB_URL`, credenciales, puerto y base existente. Si solo fallan mediciones/alertas, revisar las URLs de microservicios. No colocar credenciales en el repositorio ni utilizar datos clínicos reales.

## Docker y variables compartidas

En `docker-compose.yml`, el backend se comunica con PostgreSQL por `postgres:5432` y recibe `METRICAS_URL=http://metricas:8081` y `ALERTAS_URL=http://alertas:8082`. Los puertos publicados al anfitrión son independientes. Los datos persisten en el volumen `postgres_data` y **no se suben a GitHub**.
