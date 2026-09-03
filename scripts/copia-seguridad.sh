#!/usr/bin/env bash
# Volcado comprimido de la base de datos en copias/.
set -euo pipefail

cd "$(dirname "$0")/.."
mkdir -p copias

USUARIO="${POSTGRES_USER:-alquileres}"
BASE="${POSTGRES_DB:-alquileres}"
DESTINO="copias/alquileres-$(date +%Y%m%d-%H%M%S).sql.gz"

docker compose exec -T postgres pg_dump -U "$USUARIO" "$BASE" | gzip > "$DESTINO"
echo "Copia generada: $DESTINO"
