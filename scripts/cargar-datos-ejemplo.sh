#!/usr/bin/env bash
# Carga los datos de ejemplo en la base de datos que corre en Docker.
# Debe ejecutarse DESPUES del primer arranque de la aplicacion, cuando Flyway
# ya ha creado las tablas.
set -euo pipefail

cd "$(dirname "$0")/.."

USUARIO="${POSTGRES_USER:-alquileres}"
BASE="${POSTGRES_DB:-alquileres}"

if ! docker compose ps --status running postgres | grep -q postgres; then
    echo "El contenedor de PostgreSQL no esta levantado. Ejecute antes: docker compose up -d" >&2
    exit 1
fi

echo "Cargando db/04_datos_ejemplo.sql en ${BASE}..."
docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U "$USUARIO" -d "$BASE" < db/04_datos_ejemplo.sql

echo
echo "Filas por tabla:"
docker compose exec -T postgres psql -U "$USUARIO" -d "$BASE" -c "
SELECT 'usuario' AS tabla, count(*) FROM usuario
UNION ALL SELECT 'inmueble',           count(*) FROM inmueble
UNION ALL SELECT 'inquilino',          count(*) FROM inquilino
UNION ALL SELECT 'contrato',           count(*) FROM contrato
UNION ALL SELECT 'carga_fichero',      count(*) FROM carga_fichero
UNION ALL SELECT 'renta_mensual',      count(*) FROM renta_mensual
UNION ALL SELECT 'movimiento_fichero', count(*) FROM movimiento_fichero
ORDER BY 1;"
