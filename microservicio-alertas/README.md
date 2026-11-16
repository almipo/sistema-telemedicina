# Microservicio de Alertas

Servicio **Java 21 / Spring Boot 3.4.5** para registrar, consultar y gestionar alertas generadas a partir de mediciones fuera de umbral. Almacena la información en **PostgreSQL** y emite eventos mediante **WebSocket** para el panel de gestión médica.

## Responsabilidades

- Recibir alertas internas desde Métricas.
- Persistir alertas y notificaciones.
- Permitir consultar alertas generales o filtradas por paciente.
- Actualizar estado de las alertas (por ejemplo, `ATENDIDA` y `DESCARTADA`).
- Publicar eventos en tiempo real por WebSocket.

## Conexiones y puertos

| Elemento | Dirección |
|---|---|
| API REST | `http://localhost:8082` |
| WebSocket | `ws://localhost:8082/ws/alertas` |
| PostgreSQL | `DB_URL` (mismo motor relacional utilizado por backend) |
| Frontend | REST en :8082 y WebSocket en :8082 |

## Variables de entorno

Ejemplo local cuando PostgreSQL publica el puerto 5433:

```text
DB_URL=jdbc:postgresql://localhost:5433/telemedicina_dev
DB_USER=telemedicina
DB_PASSWORD=<contraseña>
WS_ALLOWED_ORIGINS=http://localhost:*,http://127.0.0.1:*
```

El nombre real de la base y el puerto deben coincidir con PostgreSQL activo. En Docker utilizar `jdbc:postgresql://postgres:5432/<base>`. `WS_ALLOWED_ORIGINS` configura orígenes WebSocket; el controlador CORS REST también acepta orígenes de localhost/127.0.0.1 en desarrollo.

## Ejecutar

1. Iniciar PostgreSQL.
2. Abrir `microservicio-alertas/pom.xml` en IntelliJ con JDK 21.
3. Configurar las variables en **Run → Edit Configurations → Environment variables**.
4. Ejecutar `AlertasApplication`.
5. Abrir `http://localhost:8082/api/alertas`. Una respuesta `[]` es válida si no hay alertas.

Terminal, desde la carpeta del módulo:

```bash
mvn clean test
mvn spring-boot:run
```

Docker, desde la raíz: `docker compose up -d --build alertas`. Revisar `docker compose logs -f alertas`.

## API

| Método | Ruta | Función |
|---|---|---|
| `GET` | `/api/alertas` | Listar alertas |
| `GET` | `/api/alertas?pacienteId=UUID` | Filtrar por paciente |
| `GET` | `/api/alertas/{id}` | Consultar alerta |
| `POST` | `/api/interno/alertas` | Crear alerta desde Métricas |
| `PATCH` | `/api/alertas/{id}/estado` | Cambiar estado |
| `GET` | `/api/alertas/{id}/notificaciones` | Consultar notificaciones |

Ejemplo de actualización de estado:

```http
PATCH http://localhost:8082/api/alertas/UUID_DE_LA_ALERTA/estado
Content-Type: application/json

{"estado":"ATENDIDA"}
```

## Integración con el frontend

La pestaña **Alertas** presenta tres acciones: **Datos** (abre la ficha del paciente asociado sin cambiar el estado), **Atender** y **Descartar**. El frontend **consulta el servicio directamente en 8082**; por eso, ante un `400` obtenido de `http://localhost:8080/api/alertas`, primero confirmar que no se está utilizando una versión anterior de `app.js` que llamaba al proxy del backend.

WebSocket envía avisos de nuevas alertas mientras el panel permanece abierto. La persistencia permite recuperar eventos posteriormente aunque no hubiera navegador conectado. El destino `PERSONAL_MEDICO` es una referencia lógica de demostración; no implementa correo, push ni segmentación de usuarios reales.

## Prueba funcional

1. Consultar la lista inicial de alertas (`GET /api/alertas`).
2. Poner en marcha backend, MongoDB, Métricas y el simulador IoT.
3. Generar mediciones fuera del rango configurado.
4. Comprobar nueva alerta, aviso WebSocket y visualización desde el frontend.
5. Cambiar estado a `ATENDIDA` o `DESCARTADA` y volver a consultar la lista.

Si aparecen errores 400, revisar el cuerpo y los logs del **puerto 8082**, además del JSON del `PATCH`. Para pruebas automatizadas ejecutar `mvn test`.

**Seguridad:** el sistema está planteado para desarrollo local y datos ficticios; faltan autenticación/autorización y protección de WebSocket para uso sanitario real.

## Docker y persistencia

En `docker-compose.yml`, Alertas utiliza `postgres:5432` y el puerto HTTP/WebSocket `8082`. Comparte el servidor PostgreSQL configurado en Compose, pero mantiene sus controladores y lógica propios. La configuración y los Dockerfile se versionan en GitHub; los registros almacenados en el volumen de PostgreSQL no.
