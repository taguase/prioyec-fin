-- ============================================================================
--  gestion-alquileres :: 04 - Datos de ejemplo
--
--  Contiene al menos un INSERT por cada tabla del modelo:
--      usuario, inmueble, inquilino, contrato,
--      carga_fichero, renta_mensual, movimiento_fichero
--
--  Ejecutar DESPUES de que exista el esquema (Flyway al arrancar la app, o
--  bien db/02_esquema.sql + db/03_usuarios_iniciales.sql):
--      psql -h localhost -U alquileres -d alquileres -f db/04_datos_ejemplo.sql
--  Con Docker:
--      docker compose exec -T postgres psql -U alquileres -d alquileres < db/04_datos_ejemplo.sql
--
--  Se usan identificadores explicitos para que el juego de datos sea
--  reproducible; al final se reposicionan las secuencias.
-- ============================================================================

BEGIN;

-- ---------------------------------------------------------------------------
-- 1) USUARIO
--    'system' y 'admin' ya los crea la migracion V2. Aqui se anade un usuario
--    de gestion adicional y uno con la contrasena YA CADUCADA, util para
--    probar el envio del correo de aviso.
--    Contrasenas (BCrypt fuerza 10):
--       system  -> system      (nunca caduca)
--       admin   -> Admin2024
--       gestor  -> Gestor2024
--       caducado-> Gestor2024
-- ---------------------------------------------------------------------------
INSERT INTO usuario (username, password, email, nombre_completo, rol, activo,
                     password_nunca_caduca, fecha_caducidad_password)
VALUES ('caducado',
        '$2y$10$mPsjyUhgGHFYus1I0WrlBOBKKCi51QT59g0ZAigETFnlHQQxFDMLi',
        'usuario.caducado@example.com',
        'Usuario con contrasena caducada',
        'ROLE_USER', TRUE, FALSE, CURRENT_DATE - INTERVAL '5 day')
ON CONFLICT (username) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 2) INMUEBLE
-- ---------------------------------------------------------------------------
INSERT INTO inmueble (id, nombre_calle, portal, piso, letra, municipio, ciudad) VALUES
  (1, 'Calle Mayor 10',                '2', '1', 'A', 'Alcala de Henares', 'Madrid'),
  (2, 'Calle Mayor 10',                '2', '1', 'B', 'Alcala de Henares', 'Madrid'),
  (3, 'Calle Mayor 10',                '2', '2', 'A', 'Alcala de Henares', 'Madrid'),
  (4, 'Avenida de la Constitucion 45', '1', '3', 'C', 'Madrid',            'Madrid'),
  (5, 'Calle del Sol 7',               '',  'Bajo', 'A', 'Getafe',         'Madrid')
ON CONFLICT ON CONSTRAINT uk_inmueble_direccion DO NOTHING;

-- ---------------------------------------------------------------------------
-- 3) INQUILINO
--    nombre_busqueda = "APELLIDOS NOMBRE" normalizado (mayusculas, sin acentos).
--    Es el texto que se busca dentro del concepto del fichero mensual.
-- ---------------------------------------------------------------------------
INSERT INTO inquilino (id, nombre, apellidos, dni, nombre_busqueda) VALUES
  (1, 'Maria',  'Lopez Garcia',      '12345678Z', 'LOPEZ GARCIA MARIA'),
  (2, 'Juan',   'Perez Sanz',        '23456789S', 'PEREZ SANZ JUAN'),
  (3, 'Lucia',  'Fernandez Ruiz',    '34567890T', 'FERNANDEZ RUIZ LUCIA'),
  (4, 'Carlos', 'Martin Gomez',      '45678901R', 'MARTIN GOMEZ CARLOS'),
  (5, 'Ana',    'Diaz Moreno',       '56789012W', 'DIAZ MORENO ANA'),
  (6, 'Pedro',  'Navarro Iglesias',  '67890123A', 'NAVARRO IGLESIAS PEDRO')
ON CONFLICT (dni) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 4) CONTRATO
--    Un contrato actual por inmueble (lo garantiza el indice unico parcial)
--    mas contratos historicos para poder probar el boton de historico.
-- ---------------------------------------------------------------------------
INSERT INTO contrato (id, inquilino_id, inmueble_id, fecha_inicio, fecha_fin, importe_renta, actual) VALUES
  -- Calle Mayor 10, 2, 1 A : Pedro (historico) -> Maria (actual)
  (1, 6, 1, DATE '2019-01-01', DATE '2022-12-31', 620.00, FALSE),
  (2, 1, 1, DATE '2023-01-01', DATE '2028-12-31', 750.00, TRUE),
  -- Calle Mayor 10, 2, 1 B : Juan (actual)
  (3, 2, 2, DATE '2022-06-01', DATE '2027-05-31', 690.00, TRUE),
  -- Calle Mayor 10, 2, 2 A : Lucia (actual)
  (4, 3, 3, DATE '2024-03-01', NULL,              810.00, TRUE),
  -- Avenida de la Constitucion 45, 1, 3 C : Carlos (actual)
  (5, 4, 4, DATE '2021-09-15', DATE '2026-09-14', 980.00, TRUE),
  -- Calle del Sol 7, Bajo A : Ana (actual)
  (6, 5, 5, DATE '2023-11-01', NULL,              540.00, TRUE)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 5) CARGA_FICHERO
--    Traza de las dos cargas de ejemplo: la de inquilinos y la del mes.
-- ---------------------------------------------------------------------------
INSERT INTO carga_fichero (id, tipo, nombre_fichero, fecha_carga, usuario, anio, mes,
                           registros_leidos, registros_ok, registros_ko, observaciones) VALUES
  (1, 'INQUILINOS', 'inquilinos-alta-inicial.xlsx', TIMESTAMP '2026-01-08 09:12:00', 'admin',
      NULL, NULL, 6, 6, 0, NULL),
  (2, 'MENSUAL',    'cobros-2026-01.xlsx',          TIMESTAMP '2026-02-02 08:40:00', 'gestor',
      2026, 1, 7, 6, 1, 'Un movimiento sin inquilino identificable')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 6) RENTA_MENSUAL
--    Enero 2026 conciliado desde el fichero y febrero 2026 metido a mano.
--    El contrato 3 cobra en dos plazos: importes sumados y fechas concatenadas.
-- ---------------------------------------------------------------------------
INSERT INTO renta_mensual (id, contrato_id, anio, mes, importe, fecha_pago, fechas_pago,
                           num_movimientos, origen, carga_id) VALUES
  (1, 2, 2026, 1, 750.00, DATE '2026-01-05', '05/01/2026',               1, 'FICHERO', 2),
  (2, 3, 2026, 1, 690.00, DATE '2026-01-03', '03/01/2026 | 12/01/2026',  2, 'FICHERO', 2),
  (3, 4, 2026, 1, 810.00, DATE '2026-01-07', '07/01/2026',               1, 'FICHERO', 2),
  (4, 5, 2026, 1, 980.00, DATE '2026-01-02', '02/01/2026',               1, 'FICHERO', 2),
  (5, 6, 2026, 1, 540.00, DATE '2026-01-04', '04/01/2026',               1, 'FICHERO', 2),
  (6, 2, 2026, 2, 750.00, DATE '2026-02-05', '05/02/2026',               1, 'MANUAL',  NULL),
  (7, 3, 2026, 2, 690.00, DATE '2026-02-04', '04/02/2026',               1, 'MANUAL',  NULL)
ON CONFLICT ON CONSTRAINT uk_renta_contrato_periodo DO NOTHING;

-- ---------------------------------------------------------------------------
-- 7) MOVIMIENTO_FICHERO
--    Las siete lineas leidas del fichero de enero: seis casadas y una que no.
-- ---------------------------------------------------------------------------
INSERT INTO movimiento_fichero (id, carga_id, fila, concepto, importe, fecha_movimiento,
                                contrato_id, estado, motivo) VALUES
  (1, 2, 2, 'TRANSFERENCIA DE MARIA LOPEZ GARCIA CONCEPTO ALQUILER ENERO',      750.00, DATE '2026-01-05', 2, 'CASADO', NULL),
  (2, 2, 3, 'TRASPASO JUAN PEREZ SANZ ALQUILER PRIMER PLAZO',                   400.00, DATE '2026-01-03', 3, 'CASADO', NULL),
  (3, 2, 4, 'TRASPASO JUAN PEREZ SANZ ALQUILER SEGUNDO PLAZO',                  290.00, DATE '2026-01-12', 3, 'CASADO', NULL),
  (4, 2, 5, 'INGRESO LUCIA FERNANDEZ RUIZ RENTA MENSUAL',                       810.00, DATE '2026-01-07', 4, 'CASADO', NULL),
  (5, 2, 6, 'RECIBO CARLOS MARTIN GOMEZ ALQUILER',                              980.00, DATE '2026-01-02', 5, 'CASADO', NULL),
  (6, 2, 7, 'BIZUM ANA DIAZ MORENO ALQUILER ENE',                               540.00, DATE '2026-01-04', 6, 'CASADO', NULL),
  (7, 2, 8, 'TRANSFERENCIA VARIOS CONCEPTOS SIN IDENTIFICAR',                   300.00, DATE '2026-01-15', NULL, 'NO_CASADO',
      'No se ha localizado ningun inquilino en el concepto')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Reposicionamiento de las secuencias tras los INSERT con id explicito
-- ---------------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('usuario', 'id'),            COALESCE((SELECT MAX(id) FROM usuario), 1));
SELECT setval(pg_get_serial_sequence('inmueble', 'id'),           COALESCE((SELECT MAX(id) FROM inmueble), 1));
SELECT setval(pg_get_serial_sequence('inquilino', 'id'),          COALESCE((SELECT MAX(id) FROM inquilino), 1));
SELECT setval(pg_get_serial_sequence('contrato', 'id'),           COALESCE((SELECT MAX(id) FROM contrato), 1));
SELECT setval(pg_get_serial_sequence('carga_fichero', 'id'),      COALESCE((SELECT MAX(id) FROM carga_fichero), 1));
SELECT setval(pg_get_serial_sequence('renta_mensual', 'id'),      COALESCE((SELECT MAX(id) FROM renta_mensual), 1));
SELECT setval(pg_get_serial_sequence('movimiento_fichero', 'id'), COALESCE((SELECT MAX(id) FROM movimiento_fichero), 1));

COMMIT;

-- ---------------------------------------------------------------------------
-- Comprobacion rapida
-- ---------------------------------------------------------------------------
-- SELECT 'usuario' t, count(*) FROM usuario
-- UNION ALL SELECT 'inmueble',           count(*) FROM inmueble
-- UNION ALL SELECT 'inquilino',          count(*) FROM inquilino
-- UNION ALL SELECT 'contrato',           count(*) FROM contrato
-- UNION ALL SELECT 'carga_fichero',      count(*) FROM carga_fichero
-- UNION ALL SELECT 'renta_mensual',      count(*) FROM renta_mensual
-- UNION ALL SELECT 'movimiento_fichero', count(*) FROM movimiento_fichero;
