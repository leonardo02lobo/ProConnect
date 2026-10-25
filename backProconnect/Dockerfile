# syntax=docker/dockerfile:1.7
# Multi-stage build: Maven (JDK 17, target 17) -> JRE 21 LTS (alpine) en runtime (upgrade-spring-boot-4, D4).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline -DskipTests || true
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests \
 && cp target/*.jar /app/app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/app.jar /app/app.jar
USER app
ENV JAVA_OPTS="-XX:+UseSerialGC -XX:MaxRAMPercentage=75 -Xss512k"
EXPOSE 8081
HEALTHCHECK --interval=5s --timeout=3s --start-period=60s --retries=30 \
  CMD wget -qO- http://localhost:${SERVER_PORT:-${PORT:-8081}}/actuator/health | grep -q '"UP"' || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
