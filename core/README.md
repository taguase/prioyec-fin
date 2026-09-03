# Gestión de Alquileres

Aplicación web para la gestión de inquilinos, contratos y rentas de inmuebles en alquiler.

**Stack:** Java 17 · Spring Boot 3.3 · Thymeleaf · Spring Security · Spring Data JPA ·
Flyway · Apache POI · PostgreSQL 16 · Docker.

## Arranque rápido

```bash
cp .env.example .env          # y cambie POSTGRES_PASSWORD
docker compose up -d --build
./scripts/cargar-datos-ejemplo.sh   # opcional: datos de prueba
```

| Recurso | URL |
|---|---|
| Aplicación | http://localhost:8080 |
| Bandeja de correo de pruebas (MailHog) | http://localhost:8025 |
| Estado de la aplicación | http://localhost:8080/actuator/health |

Usuarios iniciales: `system` / `system` (**no caduca nunca**), `admin` / `Admin2024`,
`gestor` / `Gestor2024`.

> El manual completo de despliegue, instalación de la base de datos, variables de
> entorno y resolución de problemas está en **[MANUAL-DESPLIEGUE.md](MANUAL-DESPLIEGUE.md)**.

## Módulos Maven

| Módulo | Contenido |
|---|---|
| `core` | Dominio y JPA, repositorios, servicios de negocio, seguridad y envío de correo |
| `excel` | Lectura y escritura de ficheros Excel con Apache POI |
| `web-front` | Controladores, plantillas Thymeleaf y configuración de seguridad. Módulo ejecutable |

Dependencias: `web-front` → `excel` → `core`.

## Funcionalidades

| # | Pantalla | Qué hace |
|---|---|---|
| 1 | `/inquilinos/carga` | Carga masiva desde Excel de inquilinos, inmuebles y contratos |
| 2 | `/inquilinos/buscar` | Búsqueda por domicilio (combos de portal y piso/letra) o por nombre/DNI, con histórico |
| 3 | `/rentas/carga-mensual` | Concilia el Excel de cobros del mes y genera el Excel de resultados |
| 4 | `/rentas/alta` | Alta manual del importe y la fecha de la renta de un contrato |
| 5 | `/informes` | Excel mensual: inmueble, inquilino, importe del mes y fecha |
| 6 | `/informes` | Excel anual: inmueble, inquilinos y los doce importes mensuales |

## Desarrollo

```bash
mvn clean install                                       # compila los tres módulos y pasa los tests
mvn -pl web-front spring-boot:run -Dspring-boot.run.profiles=dev
mvn -pl excel test -Dtest=LectoresExcelTest#leeInquilinos
```
