# Manual de despliegue — Gestión de Alquileres

Aplicación web Java multimódulo (Maven) sobre **Spring Boot 3.3 / Java 17** y **PostgreSQL 16**,
con login, aviso por correo de contraseñas caducadas y seis funcionalidades de gestión de
inquilinos y rentas.

---

## 1. Contenido del proyecto

```
gestion-alquileres/
├── pom.xml                     POM padre (agrupa los tres módulos)
├── core/                       Módulo 1: dominio, JPA, servicios, seguridad, correo
├── excel/                      Módulo 2: lectura y escritura de ficheros Excel (Apache POI)
├── web-front/                  Módulo 3: capa web (Thymeleaf + Spring Security). Ejecutable
├── db/                         Scripts SQL para instalación manual y datos de ejemplo
├── Dockerfile                  Imagen de la aplicación (build multietapa)
├── docker-compose.yml          Entorno completo: postgres + app + mailhog (+ adminer)
├── .env.example                Plantilla de variables de entorno
└── scripts/                    Atajos para las operaciones habituales
```

**Dependencias entre módulos:** `web-front` → `excel` → `core`.
Solo `web-front` genera un jar ejecutable (`web-front/target/gestion-alquileres.jar`).

---

## 2. Requisitos

| Escenario | Necesita |
|---|---|
| Despliegue con Docker (recomendado) | Docker Engine 24+ y Docker Compose v2 |
| Compilación / ejecución local | JDK 17, Maven 3.9+, PostgreSQL 16 |

Comprobación rápida:

```bash
docker --version
docker compose version
```

---

## 3. Despliegue con Docker (camino rápido)

### 3.1 Preparar las variables de entorno

```bash
cd gestion-alquileres
cp .env.example .env
```

Edite `.env` y cambie **como mínimo** `POSTGRES_PASSWORD`. Si va a usar un servidor
SMTP real, rellene también el bloque de correo (ver §7).

### 3.2 Construir y levantar todo

```bash
docker compose up -d --build
```

Esto arranca cuatro cosas:

| Servicio | Contenedor | Puerto | Para qué sirve |
|---|---|---|---|
| `postgres` | `alquileres-postgres` | 5432 | Base de datos |
| `app` | `alquileres-app` | 8080 | La aplicación |
| `mailhog` | `alquileres-mailhog` | 8025 | Bandeja de pruebas donde ver los correos |
| `adminer` | `alquileres-adminer` | 8081 | Cliente web de BBDD (perfil opcional) |

La primera construcción tarda varios minutos porque descarga las dependencias Maven.
Las siguientes son casi instantáneas gracias al cacheado de capas.

### 3.3 Comprobar que ha arrancado

```bash
docker compose ps
docker compose logs -f app
```

En el log debe aparecer:

```
Successfully applied 2 migrations to schema "public"
Usuario 'system' creado (password nunca caduca)
Started Application in X seconds
```

Salud de la aplicación:

```bash
curl http://localhost:8080/actuator/health      # {"status":"UP"}
```

### 3.4 Entrar

Abra **http://localhost:8080** y acceda con:

| Usuario | Contraseña | Rol | Caducidad |
|---|---|---|---|
| `system` | `system` | ROLE_ADMIN | **nunca caduca** |
| `admin` | `Admin2024` | ROLE_ADMIN | 90 días |
| `gestor` | `Gestor2024` | ROLE_USER | 90 días |

> La cuenta `system` se comprueba y se repara en **cada arranque**: si no existe se crea, y
> siempre queda activa y marcada como «la contraseña no caduca nunca».
> Para cambiar su contraseña, defina `SYSTEM_PASSWORD` en `.env` antes de arrancar.

### 3.5 Cargar los datos de ejemplo (opcional)

Debe hacerse **después** del primer arranque, cuando Flyway ya ha creado las tablas:

```bash
docker compose exec -T postgres psql -U alquileres -d alquileres < db/04_datos_ejemplo.sql
```

O con el atajo incluido:

```bash
./scripts/cargar-datos-ejemplo.sh
```

### 3.6 Operaciones habituales

```bash
docker compose logs -f app                # ver el log de la aplicación
docker compose restart app                # reiniciar solo la aplicación
docker compose up -d --build app          # recompilar tras cambiar código
docker compose down                       # parar (los datos se conservan)
docker compose down -v                    # parar y BORRAR la base de datos
docker compose --profile herramientas up -d adminer   # levantar Adminer
```

---

## 4. Instalación de la base de datos

Hay dos caminos. **Con Docker no hay que hacer nada**: la base la crea el contenedor y el
esquema lo aplica Flyway al arrancar la aplicación.

### 4.1 Automático (Flyway) — el que usa Docker

Al arrancar, la aplicación ejecuta en orden las migraciones que viven en
`core/src/main/resources/db/migration/`:

| Migración | Qué hace |
|---|---|
| `V1__esquema_inicial.sql` | Crea las 7 tablas, índices y restricciones |
| `V2__usuarios_iniciales.sql` | Inserta `system`, `admin` y `gestor` |

Flyway lleva su propio control en la tabla `flyway_schema_history`; una migración ya
aplicada nunca se repite. Para añadir cambios al modelo, cree `V3__...sql` — **no** edite
una migración ya aplicada.

### 4.2 Manual (PostgreSQL instalado a mano)

```bash
# 1) Rol y base de datos (como superusuario 'postgres')
psql -h localhost -U postgres -f db/01_crear_base_datos.sql

# 2) Esquema (como usuario de la aplicación)
psql -h localhost -U alquileres -d alquileres -f db/02_esquema.sql

# 3) Usuarios iniciales (incluye 'system', que nunca caduca)
psql -h localhost -U alquileres -d alquileres -f db/03_usuarios_iniciales.sql

# 4) Datos de ejemplo (opcional)
psql -h localhost -U alquileres -d alquileres -f db/04_datos_ejemplo.sql
```

Si instala el esquema a mano, arranque después la aplicación con `FLYWAY_ENABLED=false`
para que no intente aplicar las migraciones sobre una base ya creada.

### 4.3 Modelo de datos

```
inmueble  1 ──< contrato >── 1  inquilino
                   │
                   └──< renta_mensual >── carga_fichero >──< movimiento_fichero

usuario   (independiente: credenciales y correo de aviso)
```

| Tabla | Qué guarda |
|---|---|
| `usuario` | Credenciales, correo de avisos y política de caducidad |
| `inmueble` | Calle, portal, piso, letra, municipio y ciudad (los 6 campos son la clave única) |
| `inquilino` | Nombre, apellidos, DNI y `nombre_busqueda` normalizado |
| `contrato` | Une inquilino e inmueble: fechas, renta y marca de *inquilino actual* |
| `carga_fichero` | Traza de cada Excel procesado (quién, cuándo, cuántas líneas) |
| `renta_mensual` | Importe cobrado por contrato y mes (uno por contrato/año/mes) |
| `movimiento_fichero` | Cada línea del fichero mensual con el resultado de su casado |

Reglas del modelo que conviene conocer:

- **Un solo inquilino actual por inmueble**: lo garantiza el índice único parcial
  `uk_contrato_actual_por_inmueble ON contrato(inmueble_id) WHERE actual`.
- **Una sola renta por contrato y periodo**: `uk_renta_contrato_periodo`. Si el fichero
  mensual trae varios movimientos del mismo inquilino, los importes se **suman** y las
  fechas se **concatenan** con `" | "` en `renta_mensual.fechas_pago`.
- `inquilino.nombre_busqueda` guarda `"APELLIDOS NOMBRE"` en mayúsculas y sin acentos: es
  el texto que se busca dentro del concepto bancario del fichero mensual.

### 4.4 Inserts de todas las tablas

`db/04_datos_ejemplo.sql` contiene al menos un `INSERT` por cada tabla del modelo
(`usuario`, `inmueble`, `inquilino`, `contrato`, `carga_fichero`, `renta_mensual`,
`movimiento_fichero`), usa identificadores explícitos para que el juego de datos sea
reproducible y reposiciona las secuencias al final. Incluye a propósito:

- un inmueble con **dos contratos** (uno histórico y otro actual) para probar el botón de histórico;
- un contrato con **dos movimientos** en el mismo mes, para ver la suma de importes y la concatenación de fechas;
- un movimiento **no casado**, que es el que aparece en el bloque final del Excel de conciliación;
- un usuario `caducado` con la contraseña ya vencida, para probar el envío del correo de aviso.

---

## 5. Compilación y ejecución sin Docker

```bash
# Compilar los tres módulos y ejecutar los tests
mvn clean install

# Arrancar solo el módulo web (perfil de desarrollo: correo simulado en el log)
mvn -pl web-front spring-boot:run -Dspring-boot.run.profiles=dev

# O bien el jar ya empaquetado
java -jar web-front/target/gestion-alquileres.jar
```

Comandos útiles durante el desarrollo:

```bash
mvn -pl core test                                   # tests de un solo módulo
mvn -pl excel test -Dtest=LectoresExcelTest         # una sola clase de test
mvn -pl excel test -Dtest=LectoresExcelTest#leeInquilinos   # un solo test
mvn -pl core,excel -am install -DskipTests          # módulo + sus dependencias
```

La conexión por defecto es `jdbc:postgresql://localhost:5432/alquileres` con usuario y
contraseña `alquileres`. Se puede cambiar sin tocar código con las variables de entorno de §6.

---

## 6. Variables de entorno

Todas tienen valor por defecto en `web-front/src/main/resources/application.yml`.

### Base de datos

| Variable | Por defecto | Descripción |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/alquileres` | URL JDBC |
| `SPRING_DATASOURCE_USERNAME` | `alquileres` | Usuario |
| `SPRING_DATASOURCE_PASSWORD` | `alquileres` | Contraseña |
| `DB_POOL_SIZE` | `10` | Tamaño máximo del pool HikariCP |
| `FLYWAY_ENABLED` | `true` | Aplicar migraciones al arrancar |
| `JPA_DDL_AUTO` | `validate` | Hibernate solo valida; el esquema lo crea Flyway |

### Aplicación

| Variable | Por defecto | Descripción |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto HTTP |
| `MAX_FILE_SIZE` | `20MB` | Tamaño máximo del Excel subido |
| `SYSTEM_PASSWORD` | `system` | Contraseña de la cuenta técnica |
| `SYSTEM_EMAIL` | `system@gestion-alquileres.local` | Correo de la cuenta técnica |
| `PASSWORD_DIAS_VALIDEZ` | `90` | Días de validez de una contraseña nueva |
| `PASSWORD_DIAS_PREAVISO` | `7` | Días de antelación del preaviso |
| `PASSWORD_CRON` | `0 0 7 * * *` | Cuándo se revisan las caducidades (a diario a las 07:00) |
| `LOG_LEVEL_APP` | `INFO` | Nivel de log de la aplicación |

### Correo

| Variable | Por defecto | Descripción |
|---|---|---|
| `MAIL_HOST` / `MAIL_PORT` | `mailhog` / `1025` | Servidor SMTP |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | vacías | Credenciales SMTP |
| `MAIL_SMTP_AUTH` | `false` | Autenticación SMTP |
| `MAIL_SMTP_STARTTLS` | `false` | STARTTLS |
| `CORREO_HABILITADO` | `true` | Si es `false`, los avisos solo se escriben en el log |
| `CORREO_REMITENTE` | `no-reply@gestion-alquileres.local` | Remitente |
| `CORREO_COPIA_ADMIN` | vacía | Dirección en copia de todos los avisos |

---

## 7. Login y aviso de contraseña caducada

**Cómo funciona.** Cada usuario tiene grabado un correo en `usuario.email`. Cuando su
contraseña caduca se le manda un aviso a ese buzón por dos vías:

1. **Al intentar entrar.** Si las credenciales son correctas pero la contraseña ha vencido,
   Spring Security rechaza el acceso, se envía el correo y el usuario ve el mensaje
   *«Su contraseña ha caducado…»* en la pantalla de login.
2. **Tarea diaria.** A la hora indicada en `PASSWORD_CRON` se recorren las cuentas
   caducadas (aviso de caducidad) y las que caducan dentro de `PASSWORD_DIAS_PREAVISO`
   (preaviso). Nunca se manda más de un correo por usuario y día.

La cuenta `system` queda **fuera de todo esto**: tiene `password_nunca_caduca = true`, y
tanto las consultas del planificador como la comprobación de login la excluyen.

### Probar el envío con MailHog

MailHog viene incluido en `docker-compose.yml` y hace de servidor SMTP falso.

```bash
# 1) Cargar los datos de ejemplo: crean el usuario 'caducado'
./scripts/cargar-datos-ejemplo.sh

# 2) Entrar en http://localhost:8080 con  caducado / Gestor2024
#    -> el acceso se rechaza y se envía el aviso

# 3) Leer el correo en la bandeja de MailHog
open http://localhost:8025
```

### Configurar un SMTP real

En `.env`:

```dotenv
MAIL_HOST=smtp.midominio.com
MAIL_PORT=587
MAIL_USERNAME=avisos@midominio.com
MAIL_PASSWORD=contrasena-de-aplicacion
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
CORREO_REMITENTE=avisos@midominio.com
```

Después: `docker compose up -d app`.

Si el servidor de correo no responde, **la aplicación no falla**: registra el problema en
el log y sigue funcionando.

---

## 8. Las seis funcionalidades

### 1 · Carga de inquilinos — `/inquilinos/carga`

Sube un Excel y da de alta inmuebles, inquilinos y contratos. Columnas esperadas
(la primera fila es la cabecera y se ignora):

| Col | Campo | Col | Campo |
|---|---|---|---|
| A | Nombre | H | Piso |
| B | Apellidos | I | Letra |
| C | DNI | J | Municipio |
| D | Fecha inicio contrato | K | Ciudad |
| E | Fecha fin contrato | L | Importe renta actual |
| F | Nombre de la calle | M | Inquilino actual (S/N) |
| G | Portal | | |

Hay una **plantilla descargable** desde la propia pantalla. La carga es *idempotente*:
volver a subir el mismo fichero actualiza, no duplica. Las fechas se aceptan como fecha de
Excel o como texto (`dd/MM/yyyy`, `yyyy-MM-dd`…) y los importes en formato español
(`1.250,50`) o anglosajón (`1250.50`). Cada fila inválida se reporta con su número de fila
y el motivo, sin abortar el resto de la carga.

### 2 · Búsqueda de inquilinos — `/inquilinos/buscar`

Un *radio button* elige el modo:

- **Por domicilio**: un combo con las direcciones dadas de alta (a nivel de portal) y otro,
  dependiente, con el piso y la letra. Muestra el **inquilino actual** con nombre,
  apellidos, DNI, dirección completa y renta, y dos botones: *Ver histórico completo*
  (todos los inquilinos del inmueble) y *Ver inquilinos anteriores* (solo los pasados).
- **Por inquilino**: búsqueda `LIKE` sobre nombre, apellidos o DNI, con las mismas columnas.

### 3 · Carga del fichero del mes — `/rentas/carga-mensual`

El Excel de entrada trae en la **columna B** el concepto donde hay que localizar el nombre
del inquilino, en la **C** el importe y en la **D** la fecha.

El proceso normaliza el concepto (mayúsculas, sin acentos, sin puntuación) y busca dentro
de él las variantes del nombre de cada inquilino (`APELLIDOS NOMBRE`, `NOMBRE APELLIDOS`,
`NOMBRE PRIMER-APELLIDO`); si ninguna encaja, comprueba que todos los tokens
significativos del nombre estén presentes.

El Excel de salida (descargable desde la pantalla) contiene:

1. El listado de **todos los inmuebles e inquilinos** con el importe encontrado, las fechas
   y la diferencia respecto a la renta del contrato. Si un inquilino aparece en varios
   movimientos, los importes se suman y las fechas se concatenan.
2. A continuación, el bloque **«MOVIMIENTOS DEL FICHERO NO LOCALIZADOS»** con las líneas que
   no han casado con ningún inquilino y el motivo.

Todo queda grabado en `carga_fichero`, `movimiento_fichero` y `renta_mensual`.

### 4 · Insertar la renta del mes — `/rentas/alta`

Misma mecánica de búsqueda que la funcionalidad 2, con dos campos añadidos por fila:
**importe** y **fecha**. El periodo (mes y año) se deduce de la fecha. Si ya había un
registro para ese contrato y periodo, se actualiza.

### 5 · Excel mensual — `/informes`

Una fila por inmueble con la dirección, el inquilino, el importe de la renta del mes y la
fecha en la que se hizo, más una fila de totales. Incluye también los contratos sin cobro
registrado, con importe cero.

### 6 · Excel anual — `/informes`

Una fila por inmueble con la dirección, el listado de inquilinos (nombre y DNI) y los doce
importes mensuales en columnas consecutivas, con total por mes y total del año.

---

## 9. Diagnóstico de problemas

| Síntoma | Causa habitual | Qué hacer |
|---|---|---|
| `Connection refused` al arrancar la app | Postgres aún no está listo | `docker compose ps`; el `depends_on: service_healthy` ya lo espera, revise `docker compose logs postgres` |
| `Validation failed ... table [x] missing` | El esquema no coincide con las entidades | `docker compose down -v && docker compose up -d --build` para partir de cero |
| `Migration checksum mismatch` | Se editó una migración ya aplicada | Cree una migración nueva `V3__...sql` en vez de modificar la anterior |
| El login siempre falla | Hashes BCrypt sembrados a mano que no corresponden | Cambie la contraseña desde *Usuarios*, o arranque con `SYSTEM_PASSWORD` y entre como `system` |
| No llegan los correos | SMTP mal configurado | Revise el log: si aparece `[CORREO SIMULADO]`, `CORREO_HABILITADO` está a `false` o no hay `JavaMailSender` |
| El fichero mensual no casa a nadie | El concepto no está en la columna B | Descargue la plantilla desde la pantalla y compare el layout |
| `Maximum upload size exceeded` | Excel mayor de 20 MB | Suba `MAX_FILE_SIZE` y `MAX_REQUEST_SIZE` |
| `ports are not available: ... 5432: address already in use` | Ya hay un PostgreSQL en la máquina | Ponga otro puerto en `.env`: `POSTGRES_PORT=55432` (solo cambia el puerto **del host**; dentro de la red de Docker la app sigue conectando al 5432) |
| Lo mismo con el 8080 o el 8025 | Puerto ocupado | `APP_PORT=8090` / `MAILHOG_UI_PORT=8026` en `.env` |

Comandos de diagnóstico:

```bash
docker compose logs --tail=200 app
docker compose exec postgres psql -U alquileres -d alquileres -c '\dt'
docker compose exec postgres psql -U alquileres -d alquileres -c 'SELECT * FROM flyway_schema_history;'
docker compose exec postgres psql -U alquileres -d alquileres -c 'SELECT username, activo, password_nunca_caduca, fecha_caducidad_password FROM usuario;'
```

---

## 10. Antes de pasar a producción

- [ ] Cambiar `POSTGRES_PASSWORD` y la contraseña de `system` (`SYSTEM_PASSWORD`).
- [ ] Cambiar las contraseñas de `admin` y `gestor`, o borrar esas cuentas si no se usan.
- [ ] Configurar un SMTP real y quitar el servicio `mailhog` del compose.
- [ ] No exponer el puerto 5432 fuera del host (quitar el mapeo `ports` de `postgres`).
- [ ] Poner la aplicación detrás de un proxy inverso con HTTPS.
- [ ] Programar copias de seguridad:
      `docker compose exec -T postgres pg_dump -U alquileres alquileres | gzip > copia-$(date +%F).sql.gz`
- [ ] Revisar `PASSWORD_DIAS_VALIDEZ` y `PASSWORD_CRON` según la política de la organización.
