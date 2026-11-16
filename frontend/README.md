# Frontend — Panel de gestión médica

Interfaz web en **HTML, CSS y JavaScript** sin framework de compilación. Permite gestionar registros clínicos y visualizar mediciones y alertas procedentes de los servicios reales. No simula lecturas en el navegador.

## Funcionalidad

- **Inicio:** resumen de entidades y alertas recientes.
- **Pacientes:** CRUD, ficha individual, dispositivos asociados, historial clínico, medicaciones, turnos y mediciones.
- **Médicos:** altas, consultas, cambios y bajas.
- **Turnos:** gestión y selección de estados obtenidos del backend.
- **Dispositivos:** asociación con paciente y selección de tipo `PULSOMETRO`, `TERMOMETRO`, `TENSIOMETRO` o `MONITOR_MULTIPARAMETRICO`.
- **Umbrales:** consulta y edición de límites generales de medición en Métricas.
- **Alertas:** listado, acciones **Datos** (ficha del paciente), **Atender**, **Descartar**, y notificaciones WebSocket en tiempo real.

## Conexiones

| Sistema | URL local |
|---|---|
| Frontend Nginx (Docker) | `http://localhost:3000` |
| Frontend en IntelliJ | URL generada por servidor integrado, usualmente puerto `63342` |
| Backend | `http://localhost:8080` |
| Métricas | `http://localhost:8081` |
| Alertas REST | `http://localhost:8082` |
| Alertas WebSocket | `ws://localhost:8082/ws/alertas` |

El archivo `app.js` utiliza `window.APP_CONFIG` cuando existe y estos valores locales como fallback. Ejemplo de `config.js` en desarrollo:

```javascript
window.APP_CONFIG = {
  API_BASE_URL: "http://localhost:8080",
  METRICAS_API_URL: "http://localhost:8081",
  ALERTAS_API_URL: "http://localhost:8082",
  ALERTAS_WS_URL: "ws://localhost:8082/ws/alertas"
};
```

**Configuración en Docker:** `frontend/docker-entrypoint.d/40-config.sh` genera `config.js` al iniciar Nginx utilizando las cuatro variables `API_BASE_URL`, `METRICAS_API_URL`, `ALERTAS_API_URL` y `ALERTAS_WS_URL`, que el Compose recibe desde `.env` mediante `FRONTEND_*`. Todas son URLs públicas utilizadas por el navegador. No incluir credenciales de base de datos en `config.js`.

## Ejecutar

**Docker:** en la raíz del repositorio, ejecutar:

```bash
docker compose up -d --build frontend
```

Abrir `http://localhost:3000`. Si las dependencias no están iniciadas, arrancar también backend, MongoDB, Métricas y Alertas.

**IntelliJ:** abrir `frontend/index.html` usando el servidor de vista previa/servidor HTTP integrado; no usar `file://`. Si el origen es `http://localhost:63342`, asegurarse de que backend y Métricas permitan ese origen mediante CORS; Alertas contempla localhost en su configuración revisada.

Tras editar `app.js`, realizar recarga forzada con **Ctrl + Shift + R**; si está servido en Docker, reconstruir el frontend (`docker compose up -d --build frontend`).

## Flujo funcional de ejemplo

1. Crear un paciente ficticio.
2. Registrar un dispositivo **ACTIVO**, seleccionar su tipo y asociarlo al paciente.
3. Iniciar el simulador con `--todos --intervalo 10 --cantidad 100 --riesgo 0.2`.
4. Verificar las mediciones en la ficha del paciente.
5. Consultar o editar límites desde **Umbrales**.
6. Abrir **Alertas** y usar **Datos** para navegar a la ficha sin cambiar el estado; usar **Atender** o **Descartar** cuando corresponda.

## Diagnóstico

| Problema | Acción |
|---|---|
| `Failed to fetch` | Revisar puerto/origen y que el servicio esté iniciado |
| Error CORS en Umbrales | Añadir origen `localhost:63342` a `CORS_ORIGINS` de Métricas |
| `HTTP 400` en Alertas | Verificar que la petición vaya a `:8082/api/alertas`, no a `:8080` |
| WebSocket sin conexión | Revisar `ALERTAS_WS_URL` y servicio :8082 |
| No hay mediciones | Verificar simulador, tipo/estado del dispositivo, ID de paciente y Métricas |

El frontend no almacena secretos y no incluye autenticación ni autorización por rol. **No utilizar datos de pacientes reales**.
