# Aplicación segura de los cambios

1. Hacé una copia de respaldo del proyecto actual.
2. Copiá los archivos de este ZIP sobre la raíz de `sistema-telemedicina` respetando las carpetas. **No contiene `.env`, no reemplaza credenciales ni incluye datos de BD**.
3. Conservá tu `.env` real y agregá, si todavía no están, `MONGO_ROOT_USER` y `MONGO_ROOT_PASSWORD`. Para una base existente, las variables nuevas **no cambian** las cuentas ya creadas: `MONGO_URI_DOCKER` debe seguir usando el usuario y contraseña válidos previamente.
4. Comprobá `docker compose config --quiet` antes de reiniciar contenedores. Si muestra variables faltantes, completalas en tu `.env` sin compartir secretos.
5. Ejecutá `docker compose up -d --build`; **no ejecutes `down -v`**.
6. Probá `http://localhost:8081/api/umbrales`, `http://localhost:8082/api/alertas` y `http://localhost:3000`.

**Alcance:** se ajustan Compose, variables de ejemplo, configuración pública de frontend, valor por defecto de URL del Backend en Métricas y documentación. Se conserva `mongod --auth`, la URI de conexión privada y la lógica de negocio. El simulador permanece fuera del Compose por decisión documentada. No se modifican tus bases de datos existentes.
