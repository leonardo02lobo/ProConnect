## 1. Preparación

- [x] 1.1 Mergear en la rama el estado final de perf (fase 2) y de add-role-management; suite completa en verde como punto de partida (anotar el número de tests)
- [x] 1.2 Script de snapshot de contratos JSON (feed, perfil, `/api/auth/me`, error 400/404/409 problem+json) y snapshot "antes"
- [x] 1.3 Comprobar en la fuente oficial la compatibilidad Spring Cloud ↔ Boot 4.1 y decidir el destino del gateway (D1), registrándolo en design.md

## 2. backProconnect → Spring Boot 4.1.x

- [x] 2.1 OpenRewrite `UpgradeSpringBoot_4_x` en dry-run, revisar el diff y aplicar
- [x] 2.2 Ajustar starters modulares y dependencias (jjwt, Flyway, Bucket4j, Caffeine, retry, datasource-proxy, Testcontainers, GreenMail, Lombok, plugins)
- [x] 2.3 Jackson 3: revisar la configuración y las anotaciones; jjwt con un serializador compatible; test de que un token emitido con la versión anterior sigue validando
- [x] 2.4 Spring Security 7: `SecurityConfig` y filtros (JWT, Origin, rate limit, entry point problem+json)
- [x] 2.5 Hibernate 7: queries nativas, proyecciones e `Instant`, con `ddl-auto=validate` y Flyway
- [x] 2.6 Retry de correos (spring-retry o el nativo de Framework 7) manteniendo 3 intentos con backoff
- [x] 2.7 `mvn verify` verde (Spotless y Checkstyle incluidos)

## 3. gateway

- [x] 3.1 Migrar a la combinación Boot + Spring Cloud GA decidida en 1.3 (propiedades `spring.cloud.gateway.server.webflux.*`, `GlobalFilter` JWT, headers de seguridad, CORS)
- [x] 3.2 Tests del gateway en verde
- [x] 3.3 Si se usó el fallback 4.0: tarea de seguimiento en BOARD y en tasks.md con fecha límite 2026-12-31 — **No aplica**: Spring Cloud 2025.1.4 GA soporta Boot 4.1.x (design.md, D1 resolución), el gateway ya está en 4.1.2

## 4. Verificación integral

- [x] 4.1 Stack completo: suite E2E verde dos veces (`--workers=4`)
- [x] 4.2 Tests de conteo de queries (perf) en verde con los mismos umbrales
- [x] 4.3 Snapshot de contratos JSON "después" idéntico en forma al "antes"
- [x] 4.4 ZAP baseline autenticado sin alertas altas

## 5. Seguridad y dependencias

- [x] 5.1 Dependency-Check: eliminar las supresiones de SEC-24 obsoletas; 0 hallazgos ≥ 7 sin justificar
- [x] 5.2 Actualizar `docs/security/audit-*.md` (SEC-24 resuelto por migración)
- [ ] 5.3 Fijar por SHA (con comentario de versión) todas las `uses:` de `.github/workflows/*`
- [ ] 5.4 `cooldown` en todas las entradas de `.github/dependabot.yml`
- [ ] 5.5 Workflow semanal `eol-check.yml` (falla a menos de 60 días del fin de soporte OSS de la línea de Boot usada)
- [ ] 5.6 Semgrep sin alertas `mutable-action-tag` ni `missing-cooldown`

## 6. Entrega

- [ ] 6.1 `make ci-local`, `make lint-workflows` y `actionlint` verdes
- [ ] 6.2 Push y PR draft apilado sobre `leonardo02lobo/restructure-models-performance`; los 16 checks requeridos en verde en GitHub (salvo `sca maven` si aún falta `NVD_API_KEY`)
