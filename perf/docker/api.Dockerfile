# syntax=docker/dockerfile:1.7
# Imagen del backend (submódulo backend/) tal cual está en el commit (sin modificar código), sin root.
# Contexto de build: raíz del repo.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -f backend/pom.xml -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app --no-create-home app
COPY --from=build /src/backend/target/backProconnect-0.0.1-SNAPSHOT.jar /app/app.jar
ENV JAVA_OPTS="-Xms512m -Xmx768m"
EXPOSE 8080
USER app
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
