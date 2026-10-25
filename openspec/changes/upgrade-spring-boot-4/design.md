## Context

- Ambos módulos Java están en Spring Boot 3.5.16 (soporte OSS terminado el 2026-06-30). El gateway usa Spring Cloud 2025.0.3.
- Versiones publicadas a 2026-10-25 (Maven Central): Boot 4.0.9 (OSS hasta 2026-12-31), **4.1.2** (OSS hasta 2027-07-31) y 4.2.0-RC1. Spring Cloud GA más reciente: **2025.1.4** (tren para Boot 4.0); **2026.0.0 solo en RC1**. Otras: jjwt 0.13.0, Flyway 13.11.1, Bucket4j 8.21.0.
- Red de seguridad existente: E2E (≈ 27+ tests, más los de role-management), JUnit (backend ≈ 60+, gateway 11), media 9, tests de conteo de queries (perf), CodeQL, Semgrep, gitleaks, Dependency-Check, npm audit y ZAP. Hay 16 checks requeridos en `main`.

## Goals / Non-Goals

**Goals:**
- Salir de una línea sin soporte con el mínimo cambio de comportamiento.
- Que el gateway quede en una combinación Boot + Cloud **GA** compatible.
- Cerrar las alertas de cadena de suministro de CI.
- Que no vuelva a pasar desapercibido un fin de soporte.

**Non-Goals:**
- Reescribir a programación reactiva, virtual threads obligatorios o GraalVM native.
- Subir el frontend o Node: ya los actualizó verify-web-security (Astro 7).
- Cambios funcionales o de API.

## Decisions

### D1. Destino por módulo
| Módulo | Destino | Condición |
|---|---|---|
| backProconnect | Boot **4.1.x** (último patch) | Siempre |
| gateway | Boot **4.1.x** + Spring Cloud **2026.0.x GA** | Solo si 2026.0 está en GA al aplicar |
| gateway (fallback) | Boot **4.0.x** + Spring Cloud **2025.1.x GA** | Si 2026.0 sigue en RC. Se abre la tarea de seguimiento "gateway → 4.1 al salir 2026.0 GA" antes de 2026-12-31 |

- La compatibilidad Boot ↔ Cloud se comprueba en la tabla oficial de spring.io (o en `spring-cloud-dependencies-parent` / `spring-cloud-build`) en el momento de aplicar. No se asume.
- **Alternativa**: Boot 4.0 en los dos módulos. Vence en 2 meses y obliga a migrar otra vez enseguida.
- **Alternativa**: Spring Cloud RC en el gateway. Es inaceptable en producción.

### D2. Estrategia: OpenRewrite primero, ajustes manuales después
- Ejecutar la receta oficial `org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_x` (rewrite-spring) con el plugin `rewrite-maven-plugin` en modo `dryRun`. Se revisa el diff y luego se aplica. No queda como dependencia del proyecto.
- Puntos de atención conocidos de Boot 4 (se verifican en las release notes oficiales durante la implementación):
  - **Starters modulares**: `spring-boot-starter-web` → `spring-boot-starter-webmvc` (o equivalente), y starters de test por tecnología.
  - **Jackson 3** (`tools.jackson.*`) por defecto. Hay que revisar los `ObjectMapper` propios, los `@JsonProperty`/`@JsonInclude` y `jjwt-jackson`. Si jjwt no soporta Jackson 3, se usa el módulo de compatibilidad de Jackson 2 que ofrezca Boot 4, o `jjwt-gson`, con el formato del token sin cambios.
  - **Spring Security 7**: DSL de lambda obligatorio y APIs deprecadas eliminadas. Revisar `SecurityConfig`, `OriginCheckFilter`, `JwtAuthFilter`, `AuthRateLimitFilter` y el entry point problem+json.
  - **Hibernate 7**: revisar las queries nativas con alias y proyecciones (perf), los tipos `Instant`, el dialecto MySQL y `ddl-auto=validate` con Flyway.
  - **Spring Cloud Gateway** (servidor WebFlux): nombres de propiedades `spring.cloud.gateway.server.webflux.*` y API del `GlobalFilter`.
  - **spring-retry**: confirmar que sigue siendo compatible o migrar a las capacidades de resiliencia nativas de Framework 7 (`@Retryable` del core), manteniendo 3 intentos con backoff.
  - Lombok y el plugin de compilación con el JDK del contenedor de build (temurin-17).

### D3. Orden de trabajo
1. Rama sobre perf (ya con role-management mergeado). Capturar el **snapshot de contratos JSON** (feed, perfil, `/me`, errores) con un script (`perf/bench/targets.mjs` o un test) para compararlo después.
2. backProconnect: OpenRewrite → compilar → corregir → `mvn verify` verde.
3. gateway: decidir D1 → migrar → tests verdes.
4. Stack completo: E2E, conteo de queries, contratos JSON, ZAP.
5. Escaneos: Dependency-Check sin las supresiones de SEC-24 obsoletas, CodeQL y Semgrep.
6. Cadena de suministro de CI: SHA pinning (con `pinact` o a mano consultando `gh api repos/<owner>/<action>/git/ref/tags/<tag>`), `cooldown` en Dependabot.
7. Workflow `eol-check.yml` semanal (endoflife.date API → falla si quedan menos de 60 días).

### D4. Java 17 frente a 21
- Se mantiene el **target 17** para no ampliar el alcance. Las imágenes Docker de runtime pueden pasar a JRE 21 LTS si todo pasa igual. Si genera algún problema se queda en 17; se decide durante la implementación y se documenta.

## Risks / Trade-offs

- [Jackson 3 cambia la serialización de fechas o nulls y rompe el frontend] → Snapshot de contratos JSON antes y después, más los E2E.
- [jjwt sin soporte de Jackson 3] → Usar el serializador alternativo con el mismo formato de token. Test de que un token emitido antes de la migración sigue validando (misma clave y algoritmo).
- [Spring Cloud 2026.0 no está en GA] → Fallback de D1 con fecha de seguimiento.
- [Conflictos con perf y role-management] → Este change se aplica el último, sobre la rama integrada.
- [Los SHA fijados dificultan las actualizaciones] → Dependabot `github-actions` las mantiene (requirement de ci-supply-chain).

## Migration Plan

Commits pequeños por paso de D3. Rollback: revertir la serie (no hay migraciones de datos en este change). PR draft apilado sobre `leonardo02lobo/restructure-models-performance`.
