# syntax=docker/dockerfile:1.7
# Imagen del backProconnect ACTUAL tal cual está en el commit (sin modificar código).
# Contexto de build: raíz del repo.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY backProconnect/pom.xml backProconnect/pom.xml
COPY backProconnect/src backProconnect/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -f backProconnect/pom.xml -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/backProconnect/target/backProconnect-0.0.1-SNAPSHOT.jar /app/app.jar
ENV JAVA_OPTS="-Xms512m -Xmx768m"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
