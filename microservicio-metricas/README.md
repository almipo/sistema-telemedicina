# Microservicio de Métricas

Servicio de **Java 21 / Spring Boot 3.4.5** que recibe mediciones IoT, valida su estructura y la asociación del dispositivo, evalúa umbrales y persiste las lecturas en **MongoDB**. Cuando una lectura está fuera de rango, se comunica con el microservicio de Alertas.

## Funciones y flujo

```text
POST /api/mediciones → validación de datos → verificación dispositivo/paciente
  → evaluación de la estrategia de medición/umbral
  → almacenamiento en MongoDB → aviso a Alertas si corresponde
```

Tipos aceptados: `PULSO`, `PRESION_SISTOLICA`, `PRESION_DIASTOLICA` y `TEMPERATURA`. Cada solicitud contiene `tipo`, `valor`, `dispositivoId` y `pacienteId`. La presión sistólica y la diastólica llegan como **dos mediciones diferenciadas**; el servicio determina el rango a partir de `tipo`.

## Puertos y dependencias

| Elemento | Configuración |
|---|---|
| API REST | `http://localhost:8081` |
| Swagger UI | `http://localhost:8081/swagger-ui/index.html` |
| MongoDB | `MONGO_URI` |
| Backend de dispositivos | `BACKEND_URL` (8080 local) |
| Servicio de alertas | `ALERTAS_URL` (8082 local) |

## Configuración

Ejemplo para iniciar en **IntelliJ** si MongoDB está publicado en el puerto 27018:

```text
MONGO_URI=mongodb://localhost:27018/telemedicina_metricas
BACKEND_URL=http://localhost:8080
ALERTAS_URL=http://localhost:8082
CORS_ORIGINS=http://localhost:3000,http://127.0.0.1:3000,http://localhost:63342,http://127.0.0.1:63342
```

Si tu instancia MongoDB usa puerto 27017 o autenticación, adaptar la URI. El valor predeterminado del archivo `application.yml` del módulo revisado utiliza `mongodb://localhost:27018/telemedicina_metricas`; prevalece `MONGO_URI` cuando se define. Para contenedores, utilizar `mongodb:27017` y los hosts `backend`/`alertas` de Docker. Verificar `CORS_ORIGINS` al abrir el panel desde la vista IntelliJ (63342); el código de configuración CORS exige configurar los orígenes autorizados.

## Umbrales configurables

El módulo de umbrales integrado posteriormente permite editar rangos **generales por tipo de medición**; los almacena en MongoDB y actualiza la evaluación en uso.

| Medición | Mínimo inicial | Máximo inicial | Unidad |
|---|---:|---:|---|
| PULSO | 50 | 120 | lpm |
| PRESION_SISTOLICA | 90 | 140 | mmHg |
| PRESION_DIASTOLICA | 60 | 90 | mmHg |
| TEMPERATURA | 35 | 38 | °C |

Valores en los extremos son admitidos (`mínimo ≤ valor ≤ máximo`). Son **rangos demostrativos**, no recomendaciones médicas.

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/umbrales` | Consultar tipos y límites |
| `PUT` | `/api/umbrales/{tipo}` | Actualizar rango con `{"minimo":60,"maximo":90}` |
| `GET` | `/api/mediciones` | Listar mediciones; admite `?pacienteId=UUID` |
| `GET` | `/api/mediciones/{id}` | Ver medición |
| `POST` | `/api/mediciones` | Registrar y evaluar medición |

> **Control de versión:** `/api/umbrales` pertenece a la actualización de umbrales. Si ese endpoint devuelve 404, revisar que el módulo instalado sea el actualizado con `UmbralController` y no un ZIP anterior.

## Puesta en marcha

1. Iniciar MongoDB, backend y Alertas.
2. Abrir `microservicio-metricas/pom.xml` con Java 21 en IntelliJ.
3. Configurar variables y ejecutar `MetricasApplication`.
4. Comprobar `GET http://localhost:8081/api/umbrales` y `GET http://localhost:8081/api/mediciones`.

Con Maven, desde el módulo: `mvn test` y `mvn spring-boot:run`. Con Docker, desde la raíz: `docker compose up -d --build metricas` (requiere servicios dependientes y Compose configurado).

## Ejemplo de medición

```json
{
  "tipo": "PRESION_DIASTOLICA",
  "valor": 80,
  "pacienteId": "UUID_DEL_PACIENTE",
  "dispositivoId": "UUID_DEL_DISPOSITIVO"
}
```

Sustituir los UUID por registros reales **ficticios**. Con un dispositivo inactivo, inexistente o no asociado, la validación rechazará la lectura. Una lectura fuera de umbral desencadena la integración de alertas si el servicio está disponible.

## Diagnóstico

- `Failed to fetch` desde el navegador: comprobar URL y CORS.
- Fallo MongoDB: revisar puerto, URI y autenticación.
- `400`/`404`: revisar tipo, formato, IDs y asociación.
- No hay alertas: revisar límites actuales y servicio :8082.

No utilizar umbrales como criterios clínicos reales.

## Autenticación MongoDB con Docker

El servicio Métricas necesita `MONGO_URI` con usuario, contraseña, host y `authSource` correctos. En Compose se obtiene de `MONGO_URI_DOCKER` y utiliza `mongodb:27017`; desde IntelliJ se utiliza el puerto publicado `localhost:27018` (o el realmente configurado). Las variables `MONGO_ROOT_USER` y `MONGO_ROOT_PASSWORD` se usan para inicializar un **volumen MongoDB nuevo** y no cambian los usuarios existentes. No enviar claves al repositorio.
