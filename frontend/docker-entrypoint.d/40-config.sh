#!/bin/sh
set -eu
# Solo configuración pública. Nunca incluir claves de BD ni secretos aquí.
API_BASE_URL=${API_BASE_URL:-http://localhost:8080}
ALERTAS_WS_URL=${ALERTAS_WS_URL:-ws://localhost:8082/ws/alertas}
METRICAS_API_URL=${METRICAS_API_URL:-http://localhost:8081}
ALERTAS_API_URL=${ALERTAS_API_URL:-http://localhost:8082}
escape_js(){ printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'; }
printf 'window.APP_CONFIG = { API_BASE_URL: "%s", METRICAS_API_URL: "%s", ALERTAS_API_URL: "%s", ALERTAS_WS_URL: "%s" };\n' "$(escape_js "$API_BASE_URL")" "$(escape_js "$METRICAS_API_URL")" "$(escape_js "$ALERTAS_API_URL")" "$(escape_js "$ALERTAS_WS_URL")" > /usr/share/nginx/html/config.js
