# Carpeta de inicializacion de PostgreSQL

Los ficheros `.sql` que se dejen aqui los ejecuta la imagen oficial de PostgreSQL
**solo la primera vez que se crea el volumen de datos**.

Esta carpeta se deja vacia a proposito: el esquema lo crea **Flyway** al arrancar
la aplicacion (migraciones `V1__esquema_inicial.sql` y `V2__usuarios_iniciales.sql`
dentro del modulo `core`). Si se copiara aqui el esquema, Flyway encontraria la
base ya poblada y tendria que hacer un *baseline*, con el riesgo de saltarse
migraciones.

Los datos de ejemplo (`db/04_datos_ejemplo.sql`) deben cargarse **despues** de que
la aplicacion haya arrancado al menos una vez:

```bash
docker compose exec -T postgres psql -U alquileres -d alquileres < db/04_datos_ejemplo.sql
```
