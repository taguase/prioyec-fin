-- ============================================================================
--  gestion-alquileres :: 01 - Creacion del rol y de la base de datos
--
--  Ejecutar CONECTADO COMO SUPERUSUARIO (postgres) y a la base 'postgres':
--      psql -h localhost -U postgres -f db/01_crear_base_datos.sql
--
--  Con Docker esto NO hace falta: la imagen de postgres crea la base y el
--  usuario a partir de las variables POSTGRES_DB / POSTGRES_USER / POSTGRES_PASSWORD.
-- ============================================================================

-- Rol de la aplicacion. Cambie la contrasena antes de usarlo en produccion.
DO $$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'alquileres') THEN
      CREATE ROLE alquileres WITH LOGIN PASSWORD 'alquileres';
   END IF;
END
$$;

-- CREATE DATABASE no puede ir dentro de un bloque DO ni de una transaccion.
-- Si la base ya existe, psql avisara con un error que puede ignorarse.
CREATE DATABASE alquileres
    WITH OWNER = alquileres
         ENCODING = 'UTF8'
         LC_COLLATE = 'es_ES.UTF-8'
         LC_CTYPE = 'es_ES.UTF-8'
         TEMPLATE = template0;

COMMENT ON DATABASE alquileres IS 'Gestion de alquileres de inmuebles';
