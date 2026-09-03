-- ============================================================================
--  gestion-alquileres :: V1 - Esquema inicial
--  Motor: PostgreSQL 16
-- ============================================================================

-- ---------------------------------------------------------------------------
-- USUARIO: credenciales de acceso, correo de aviso y politica de caducidad
-- ---------------------------------------------------------------------------
CREATE TABLE usuario (
    id                        BIGSERIAL     PRIMARY KEY,
    username                  VARCHAR(50)   NOT NULL,
    password                  VARCHAR(255)  NOT NULL,
    email                     VARCHAR(150)  NOT NULL,
    nombre_completo           VARCHAR(150),
    rol                       VARCHAR(30)   NOT NULL DEFAULT 'ROLE_USER',
    activo                    BOOLEAN       NOT NULL DEFAULT TRUE,
    password_nunca_caduca     BOOLEAN       NOT NULL DEFAULT FALSE,
    fecha_caducidad_password  DATE,
    fecha_ultimo_aviso        TIMESTAMP,
    fecha_alta                TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT uk_usuario_username UNIQUE (username)
);

COMMENT ON COLUMN usuario.email IS 'Correo al que se envia el aviso de contrasena caducada';
COMMENT ON COLUMN usuario.password_nunca_caduca IS 'TRUE para cuentas tecnicas (system): la contrasena no caduca nunca';
COMMENT ON COLUMN usuario.fecha_ultimo_aviso IS 'Evita reenviar el aviso de caducidad mas de una vez al dia';

-- ---------------------------------------------------------------------------
-- INMUEBLE: direccion completa. La combinacion de los 6 campos es unica
-- ---------------------------------------------------------------------------
CREATE TABLE inmueble (
    id            BIGSERIAL    PRIMARY KEY,
    nombre_calle  VARCHAR(200) NOT NULL,
    portal        VARCHAR(20)  NOT NULL DEFAULT '',
    piso          VARCHAR(20)  NOT NULL DEFAULT '',
    letra         VARCHAR(10)  NOT NULL DEFAULT '',
    municipio     VARCHAR(120) NOT NULL DEFAULT '',
    ciudad        VARCHAR(120) NOT NULL DEFAULT '',
    CONSTRAINT uk_inmueble_direccion UNIQUE (nombre_calle, portal, piso, letra, municipio, ciudad)
);

CREATE INDEX ix_inmueble_calle ON inmueble (nombre_calle);

-- ---------------------------------------------------------------------------
-- INQUILINO: datos personales. nombre_busqueda es el nombre completo
-- normalizado (mayusculas, sin acentos, sin dobles espacios) que se usa para
-- localizar al inquilino dentro del concepto del extracto bancario.
-- ---------------------------------------------------------------------------
CREATE TABLE inquilino (
    id               BIGSERIAL    PRIMARY KEY,
    nombre           VARCHAR(120) NOT NULL,
    apellidos        VARCHAR(200) NOT NULL,
    dni              VARCHAR(20)  NOT NULL,
    nombre_busqueda  VARCHAR(320) NOT NULL,
    CONSTRAINT uk_inquilino_dni UNIQUE (dni)
);

CREATE INDEX ix_inquilino_nombre    ON inquilino (upper(nombre));
CREATE INDEX ix_inquilino_apellidos ON inquilino (upper(apellidos));
CREATE INDEX ix_inquilino_busqueda  ON inquilino (nombre_busqueda);

-- ---------------------------------------------------------------------------
-- CONTRATO: relaciona inquilino <-> inmueble con fechas e importe de renta.
-- 'actual' marca el inquilino vigente del inmueble; el resto es historico.
-- ---------------------------------------------------------------------------
CREATE TABLE contrato (
    id             BIGSERIAL      PRIMARY KEY,
    inquilino_id   BIGINT         NOT NULL,
    inmueble_id    BIGINT         NOT NULL,
    fecha_inicio   DATE           NOT NULL,
    fecha_fin      DATE,
    importe_renta  NUMERIC(12,2)  NOT NULL DEFAULT 0,
    actual         BOOLEAN        NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_contrato_inquilino FOREIGN KEY (inquilino_id) REFERENCES inquilino (id),
    CONSTRAINT fk_contrato_inmueble  FOREIGN KEY (inmueble_id)  REFERENCES inmueble (id),
    CONSTRAINT ck_contrato_fechas    CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE INDEX ix_contrato_inmueble  ON contrato (inmueble_id);
CREATE INDEX ix_contrato_inquilino ON contrato (inquilino_id);

-- Solo puede haber un contrato marcado como actual por inmueble
CREATE UNIQUE INDEX uk_contrato_actual_por_inmueble
    ON contrato (inmueble_id) WHERE actual;

-- ---------------------------------------------------------------------------
-- CARGA_FICHERO: traza de cada fichero Excel procesado
-- ---------------------------------------------------------------------------
CREATE TABLE carga_fichero (
    id                BIGSERIAL     PRIMARY KEY,
    tipo              VARCHAR(30)   NOT NULL,
    nombre_fichero    VARCHAR(255)  NOT NULL,
    fecha_carga       TIMESTAMP     NOT NULL DEFAULT now(),
    usuario           VARCHAR(50),
    anio              INTEGER,
    mes               INTEGER,
    registros_leidos  INTEGER       NOT NULL DEFAULT 0,
    registros_ok      INTEGER       NOT NULL DEFAULT 0,
    registros_ko      INTEGER       NOT NULL DEFAULT 0,
    observaciones     TEXT,
    CONSTRAINT ck_carga_tipo CHECK (tipo IN ('INQUILINOS','MENSUAL')),
    CONSTRAINT ck_carga_mes  CHECK (mes IS NULL OR (mes BETWEEN 1 AND 12))
);

CREATE INDEX ix_carga_fecha ON carga_fichero (fecha_carga DESC);

-- ---------------------------------------------------------------------------
-- RENTA_MENSUAL: importe cobrado por contrato y mes. Un unico registro por
-- contrato/anio/mes; si el fichero trae varios movimientos se acumulan en
-- 'importe' y sus fechas se concatenan en 'fechas_pago'.
-- ---------------------------------------------------------------------------
CREATE TABLE renta_mensual (
    id             BIGSERIAL      PRIMARY KEY,
    contrato_id    BIGINT         NOT NULL,
    anio           INTEGER        NOT NULL,
    mes            INTEGER        NOT NULL,
    importe        NUMERIC(12,2)  NOT NULL DEFAULT 0,
    fecha_pago     DATE,
    fechas_pago    VARCHAR(255),
    num_movimientos INTEGER       NOT NULL DEFAULT 0,
    origen         VARCHAR(20)    NOT NULL DEFAULT 'MANUAL',
    carga_id       BIGINT,
    fecha_registro TIMESTAMP      NOT NULL DEFAULT now(),
    CONSTRAINT fk_renta_contrato FOREIGN KEY (contrato_id) REFERENCES contrato (id),
    CONSTRAINT fk_renta_carga    FOREIGN KEY (carga_id)    REFERENCES carga_fichero (id),
    CONSTRAINT uk_renta_contrato_periodo UNIQUE (contrato_id, anio, mes),
    CONSTRAINT ck_renta_mes    CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT ck_renta_origen CHECK (origen IN ('MANUAL','FICHERO'))
);

CREATE INDEX ix_renta_periodo ON renta_mensual (anio, mes);

COMMENT ON COLUMN renta_mensual.fechas_pago IS 'Fechas de todos los movimientos casados, separadas por " | "';

-- ---------------------------------------------------------------------------
-- MOVIMIENTO_FICHERO: cada linea leida del Excel mensual, casada o no.
-- Las lineas con estado NO_CASADO son las que se listan al final del informe.
-- ---------------------------------------------------------------------------
CREATE TABLE movimiento_fichero (
    id                BIGSERIAL      PRIMARY KEY,
    carga_id          BIGINT         NOT NULL,
    fila              INTEGER,
    concepto          TEXT,
    importe           NUMERIC(12,2),
    fecha_movimiento  DATE,
    contrato_id       BIGINT,
    estado            VARCHAR(20)    NOT NULL,
    motivo            VARCHAR(200),
    CONSTRAINT fk_movimiento_carga    FOREIGN KEY (carga_id)    REFERENCES carga_fichero (id) ON DELETE CASCADE,
    CONSTRAINT fk_movimiento_contrato FOREIGN KEY (contrato_id) REFERENCES contrato (id),
    CONSTRAINT ck_movimiento_estado   CHECK (estado IN ('CASADO','NO_CASADO','AMBIGUO'))
);

CREATE INDEX ix_movimiento_carga  ON movimiento_fichero (carga_id);
CREATE INDEX ix_movimiento_estado ON movimiento_fichero (estado);
