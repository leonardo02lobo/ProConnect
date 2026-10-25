## Why

`backProconnect` y `gateway` corren sobre **Spring Boot 3.5.16**, cuyo soporte OSS terminó el **2026-06-30**: la línea 3.x ya no recibe parches gratuitos. `verify-web-security` aceptó como riesgo, hasta el 2027-01-31, varios CVE de Spring Framework 6.2 y Security 6.5 sin parche en 3.5.x, suponiendo que la línea seguía soportada. Esa suposición era incorrecta, así que el riesgo debe resolverse migrando y no esperando. Spring Boot 4.1 (GA 2026-06, última 4.1.2, soporte OSS hasta 2027-07-31) es el destino natural. La 4.0 vence el 2026-12-31.

Además, el análisis de Semgrep del PR #4 marcó 26 alertas de cadena de suministro: actions de GitHub referenciadas por tag mutable en lugar de SHA, y Dependabot sin `cooldown`. Al ser higiene de dependencias, entran en este mismo change.

## What Changes

- **BREAKING (interno)** `backProconnect` → **Spring Boot 4.1.x** (Spring Framework 7, Spring Security 7, Hibernate 7, Jakarta EE 11, Jackson 3 por defecto, starters modulares).
- `gateway` → Spring Boot 4.x + Spring Cloud compatible:
  - Si en el momento de aplicar hay un Spring Cloud **GA** que soporte Boot 4.1 (2026.0.x), ambos módulos van a 4.1.x.
  - Si no, el gateway va a **Boot 4.0.x + Spring Cloud 2025.1.x GA** y queda una tarea de seguimiento para subirlo a 4.1 cuando salga 2026.0 GA. No se usan versiones RC en producción.
- Dependencias alineadas: jjwt 0.13.x (o el módulo de Jackson compatible), Flyway, Bucket4j, Caffeine, spring-retry o el reemplazo nativo de Framework 7, datasource-proxy, Testcontainers, GreenMail, Lombok compatible con el JDK de build, plugins (Spotless, Checkstyle, compiler, surefire).
- Retirar las supresiones de SEC-24 de Dependency-Check que el upgrade deje sin efecto, y actualizar la auditoría de seguridad.
- Cadena de suministro en CI:
  - todas las `uses:` de `.github/workflows/*` fijadas a SHA de commit, con comentario de la versión;
  - `cooldown` en `.github/dependabot.yml`;
  - las 26 alertas de Semgrep cerradas.
- Java: se mantiene el target 17 (mínimo de Boot 4). Evaluar en el diseño si conviene pasar el runtime a 21 LTS; no es obligatorio.
- Sin cambios de comportamiento funcional: la suite E2E, los tests JUnit y los escaneos de seguridad son el contrato de "no regresión".

## Capabilities

### New Capabilities
- `platform-support`: la plataforma (frameworks y dependencias) debe estar en líneas con soporte de seguridad vigente, con verificación automatizada.
- `ci-supply-chain`: integridad de la cadena de suministro de CI (actions fijadas por SHA, cooldown de actualizaciones).

### Modified Capabilities
<!-- No hay specs archivadas en openspec/specs/. -->

## Impact

- `backProconnect/pom.xml`, `gateway/pom.xml`, código afectado por las APIs cambiadas: configuración de Spring Security 7, Jackson 3 (`tools.jackson.*`) en serialización y en jjwt, Hibernate 7 en queries nativas y proyecciones, rutas y predicados de Spring Cloud Gateway, starters de test, `@MockBean` si queda alguno.
- Tests de backend, gateway, E2E, conteo de queries de perf y escaneos de seguridad.
- `.github/workflows/*`, `.github/dependabot.yml`, `security/dependency-check-suppressions.xml`, `docs/security/audit-*.md`.
- **Depende de**: `restructure-models-performance` (fase 2) y `add-role-management`. Se aplica sobre la rama de perf después de mergear role-management, para tocar el pom y la seguridad una sola vez.
