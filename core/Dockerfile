# =============================================================================
#  gestion-alquileres :: imagen de la aplicacion (build multi-etapa)
#
#  Etapa 1: compila los tres modulos Maven con JDK 17.
#  Etapa 2: imagen final solo con el JRE y el jar ejecutable de web-front.
# =============================================================================

# ------------------------------- Etapa 1: build ------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Primero solo los POM, para que la capa de dependencias se cachee y no haya
# que volver a descargarlas cada vez que cambia el codigo.
COPY pom.xml ./
COPY core/pom.xml       core/pom.xml
COPY excel/pom.xml      excel/pom.xml
COPY web-front/pom.xml  web-front/pom.xml
RUN mvn -B -ntp dependency:go-offline -DskipTests || true

COPY core/src       core/src
COPY excel/src      excel/src
COPY web-front/src  web-front/src
RUN mvn -B -ntp clean package -DskipTests

# ---------------------------- Etapa 2: ejecucion -----------------------------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

ENV TZ=Europe/Madrid \
    LANG=es_ES.UTF-8 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=Europe/Madrid"

# curl lo usa el HEALTHCHECK; la aplicacion no necesita root.
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system alquileres \
 && useradd --system --gid alquileres --home /app alquileres

COPY --from=build /build/web-front/target/gestion-alquileres.jar /app/aplicacion.jar
RUN chown -R alquileres:alquileres /app
USER alquileres

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
    CMD ["/bin/sh", "-c", "curl -fsS http://localhost:8080/actuator/health || exit 1"]

ENTRYPOINT ["/bin/sh", "-c", "exec java $JAVA_OPTS -jar /app/aplicacion.jar"]
