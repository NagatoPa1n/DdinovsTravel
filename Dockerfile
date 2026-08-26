# ---------------------------------------------------------------- build stage
# JDK 17 still runs annotation processors found on the classpath, so Lombok is
# picked up and the build avoids the "cannot find symbol" failure the README
# describes for JDK 23+.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Dependencies resolve in a layer of their own, so editing sources does not
# re-download the world on every rebuild.
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

# ------------------------------------------------------------------ run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# curl is only here for the container healthcheck below.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home --uid 1001 app

# MediaService creates and writes UPLOAD_DIR at startup, so the unprivileged
# user must own it. A named volume mounted here inherits this ownership.
RUN mkdir -p /app/uploads && chown -R app:app /app

COPY --from=build --chown=app:app /build/target/*.jar /app/app.jar

USER app

ENV SERVER_PORT=8080 \
    UPLOAD_DIR=/app/uploads \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

EXPOSE 8080

# /v3/api-docs is permitted without a token (SecurityConfig.PUBLIC_ANY), so the
# probe needs no credentials. start-period covers Hibernate's schema update and
# the admin seeding on first boot.
