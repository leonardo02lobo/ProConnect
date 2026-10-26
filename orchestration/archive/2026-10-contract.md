# Contrato del monorepo (histórico)

Copia literal de `~/orca/workspaces/ProConnect/.orchestration/CONTRACT.md` antes de split-into-polyrepo. El vigente es `../CONTRACT.md`.

---

# Contrato compartido entre changes (fuente de verdad para todos los agentes)

Si un agente necesita cambiar algo de este contrato, lo propone en BOARD.md con `[CONTRACT-CHANGE]`
y espera el OK del orquestador. Nadie edita este archivo salvo el orquestador.

## Ramas y worktrees
| Change | Rama | Worktree | Estado |
|---|---|---|---|
| fix-critical-risks | leonardo02lobo/fix-critical-risks | /home/leonardo/orca/workspaces/ProConnect/fix-critical-risks | DONE · PR draft #1 → main (b43a886) |
| add-e2e-ai-ci | leonardo02lobo/add-e2e-ai-ci | /home/leonardo/orca/workspaces/ProConnect/add-e2e-ai-ci | DONE · PR draft #2 apilado sobre #1 (611ae5f) |
| restructure-models-performance | leonardo02lobo/restructure-models-performance | /home/leonardo/orca/workspaces/ProConnect/restructure-models-performance | DONE · PR draft #6 apilado sobre #4 (979b256) |
| cleanup-codebase | leonardo02lobo/cleanup-codebase | /home/leonardo/orca/workspaces/ProConnect/cleanup-codebase | DONE · PR draft #4 apilado sobre #2 (5832bca; incluye security) |
| verify-web-security | leonardo02lobo/verify-web-security | /home/leonardo/orca/workspaces/ProConnect/verify-web-security | DONE · PR draft #3 apilado sobre #1 (0dd7d0c) |
| add-role-management | leonardo02lobo/add-role-management | /home/leonardo/orca/workspaces/ProConnect/add-role-management | DONE · PR draft #5 apilado sobre #4 |
| upgrade-spring-boot-4 | leonardo02lobo/upgrade-spring-boot-4 | /home/leonardo/orca/workspaces/ProConnect/upgrade-spring-boot-4 | DONE · PR draft #7 apilado sobre #6 (277e254; integra #5) |

Base común: `leonardo02lobo/main` @ 03a4ba7. Las ramas comparten el repo: `git merge leonardo02lobo/<otra>` funciona en local sin push.
Orden de PRs: risks → main; e2e → PR apilado sobre la rama de risks (base `leonardo02lobo/fix-critical-risks`).

## Topología y puertos (defaults del código)
| Servicio | Dir | Puerto interno | Env |
|---|---|---|---|
| gateway | gateway/ | 8080 | PORT, API_URI, MEDIA_URI, CORS_ORIGINS, JWT_SECRET |
| api core | backProconnect/ | 8081 | SERVER_PORT, DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET, JWT_TTL (PT24H), COOKIE_SECURE, CORS_ORIGINS |
| media | NodeTsPosts/ | 3001 | PORT, UPLOAD_DIR |
| web | FrontedProConnect/ | 4321 | PUBLIC_API_URL (= URL del gateway, build-time), API_INTERNAL_URL (gateway visto desde SSR, runtime, opcional; default PUBLIC_API_URL), HOST, PORT |
| mysql | - | 3306 | DB name `proconnect` |
| mailpit (cleanup) | - | 1025 smtp / 8025 ui | MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM |

## Puertos de host por agente (para correr stacks en paralelo sin chocar)
- risks: defaults (3306, 8081, 3001, 8080, 4321). Compose project `pc-risks`.
- e2e: offset +10000 (13306, 18081, 13001, 18080, 14321, 18025). Compose project `pc-e2e`.
- security: offset +30000 (33306, 38081, 33001, 38080, 34321). Compose project `pc-sec`.
- cleanup: offset +40000 (43306, 48081, 43001, 48080, 44321, mailpit 48025). Compose project `pc-cleanup` (o reutiliza docker-compose.e2e.yml con override de puertos).
- roles: offset +50000 (53306, 58081, 53001, 58080, 54321, mailpit 58025). Compose project `pc-roles`.
- boot4: offset +60000 (63306, 63081, 63001, 63080 — NO 6x080 si choca, usa 63180 —, 64321, mailpit 63025). Compose project `pc-boot4`. Ojo: ProtonVPN ocupa 58080 en el host.
- perf: offset +20000 (23306, 28081/28080, ...). Compose project `pc-perf`.
Nunca toques contenedores que no sean de tu proyecto compose (hay contenedores ajenos `finanzas-*` corriendo).

## Auth
- Cookie: `pc_session`. HttpOnly; Path=/; SameSite=Lax; Secure salvo COOKIE_SECURE=false.
- JWT HS256, claims: `sub` = userId (string), `role` ∈ {USER, COMPANY_ADMIN, ADMIN}, `name`, `exp`.
- Públicos: SOLO `POST /api/auth/login`, `POST /api/auth/register`, `POST /api/auth/logout`, `GET /actuator/health`.
- `GET /api/auth/me` → `UserPublicDto { id, name, userName, email, role, photoUrl, wallPhotoUrl }` (sin password).
- Gateway valida JWT en TODO `/upload/**` (401 si falta).
- Frontend: `/login`, `/register` ya existen desde M3 (/SignUp→/login, /SignIn→/register con 301). Astro `output: 'server'` + @astrojs/node standalone.

## Rutas API vigentes durante risks (cleanup las renombra luego)
Se mantienen las rutas actuales (`/api/posts/create`, `/api/like/create`, `/api/comment/create`, `/api/user/{id}/profiles-posts`, ...).
Cambios de risks: `GET /api/posts/{userId}` puede seguir existiendo pero el viewer sale del token; añadir `GET /api/auth/me` y `POST /api/auth/logout`.

## Toolchain local
- Solo hay JDK 25 y no hay `mvn`. Compilar/testear Java con Docker: `docker run --rm -v "$PWD":/app -v "$HOME/.m2":/root/.m2 -w /app maven:3.9-eclipse-temurin-17 mvn -B ...` (o `./mvnw` si se ajusta Lombok ≥ 1.18.40 para JDK 25; preferir Docker = paridad con CI).
- Compose: binario `docker-compose` (v2.40), no `docker compose`.
- Node 22, gh autenticado como leonardo02lobo, Claude Code CLI disponible (`claude -p`). `act` NO instalado (e2e lo instala: `gh extension install nektos/gh-act`).

## Reglas de git
- Solo commits en TU rama/worktree. Commits pequeños por grupo de tareas, mensaje con línea final:
  `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`
- Permitido: `git push -u origin <tu-rama>` y abrir PR en **draft** (`gh pr create --draft`) cuando el change esté verde en local.
- Prohibido: push a main / leonardo02lobo/main, force-push, merge de PRs, configurar secrets o branch protection, reescribir historial, `git stash` sin tag.
- Marcar tareas `- [x]` en `openspec/changes/<change>/tasks.md` de tu rama a medida que se completan.

## Rutas API vigentes (desde cleanup)
REST sin verbos: ver docs/api-routes.md en la rama de cleanup. Errores application/problem+json con traceId.
