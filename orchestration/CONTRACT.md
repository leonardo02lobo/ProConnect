# Contrato compartido entre agentes y changes (fuente de verdad)

Si un agente necesita cambiar algo de este contrato, lo propone en `BOARD.md` con `[CONTRACT-CHANGE]` y espera el OK
del orquestador. Nadie edita este archivo salvo el orquestador (y siempre vía PR en el principal).

Migrado de `~/orca/workspaces/ProConnect/.orchestration/CONTRACT.md` en `split-into-polyrepo` (rutas actualizadas a la
topología de submódulos). El histórico de ramas y PRs de los changes anteriores está en `archive/2026-10-board.md`.

## Repos y ramas

| Repo | Directorio en el principal | Rama por defecto | Ramas de change |
|---|---|---|---|
| `leonardo02lobo/ProConnect` (principal) | raíz | `main` | `leonardo02lobo/<change>` |
| `leonardo02lobo/proconnect-frontend` | `frontend/` | `main` | `leonardo02lobo/<change>` |
| `leonardo02lobo/proconnect-backend` | `backend/` | `main` | `leonardo02lobo/<change>` |
| `leonardo02lobo/proconnect-services` | `services/` (`gateway/`, `media/`) | `main` | `leonardo02lobo/<change>` |

- Un change usa el **mismo nombre de rama** en el principal y en cada repo afectado
  (`orchestration/scripts/start-change.sh <change> <repos...>`).
- Worktree del change: `<WT_ROOT>/<change>/` (principal) con los submódulos afectados en `<WT_ROOT>/<change>/<repo>/`
  sobre la rama del change. `WT_ROOT` por defecto = directorio padre del checkout principal del repo (el que tiene
  `.git`); en la máquina del mantenedor exporta `WT_ROOT=/home/leonardo/orca/workspaces/ProConnect` y
  `ORCH_BOARD=<checkout del orquestador>/orchestration/BOARD.md`.
- Changes activos: ver `BOARD.md` (entradas `STATUS` de `start-change.sh`) y `openspec list`.

## Topología y puertos (defaults del código)

| Servicio | Dir | Puerto interno | Env |
|---|---|---|---|
| gateway | `services/gateway/` | 8080 | `PORT`, `API_URI`, `MEDIA_URI`, `CORS_ORIGINS`, `JWT_SECRET` |
| api core | `backend/` | 8081 | `SERVER_PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_TTL` (PT24H), `COOKIE_SECURE`, `CORS_ORIGINS`, `MAIL_*`, `BOOTSTRAP_ADMIN_*`, `RATE_LIMIT_*`, `TRUSTED_PROXIES` |
| media | `services/media/` | 3001 | `PORT`, `UPLOAD_DIR`, `UPLOAD_RATE_LIMIT_PER_MINUTE` |
| web | `frontend/` | 4321 | `PUBLIC_API_URL` (= URL del gateway, build-time), `API_INTERNAL_URL` (gateway visto desde SSR, runtime, opcional; default `PUBLIC_API_URL`), `HOST`, `PORT` |
| mysql | - | 3306 | DB `proconnect` |
| mailpit | - | 1025 smtp / 8025 ui | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` |

## Puertos de host por stack (para correr en paralelo sin chocar)

| Stack | Proyecto compose | Puertos (mysql, api, media, gateway, web, mailpit) |
|---|---|---|
| desarrollo local | `pc-risks` (`docker-compose.dev.yml`) | 3306, 8081, 3001, 8080, 4321, 8025 |
| E2E (`make e2e-local`, CI) | `pc-e2e` | 13306, 18081, 13001, 18080, 14321, 18025 |
| perf | `pc-perf` | 23306, 28081/28080, … (`perf/docker-compose.perf.yml`) |
| DAST / seguridad | `pc-sec` | +30000 (33306, 38081, 33001, 38080, 34321) |
| agente adicional N | `pc-<agente>` | offset +40000 / +50000 / +60000 (máx. 65535: si no cabe, elige otro offset libre) |

- Un agente que levanta su propio stack E2E: `E2E_IMAGE_PREFIX=pc-<agente> make e2e-local E2E_PROJECT=pc-<agente>` y, si
  hay otro stack en marcha, `E2E_*_PORT` + `E2E_WEB_URL`/`E2E_API_URL`/`E2E_MAILPIT_URL` con su offset.
- ProtonVPN ocupa 58080 en el host del mantenedor: no lo uses.
- Nunca toques contenedores que no sean de tu proyecto compose (hay contenedores ajenos `finanzas-*`, `portainer`, …).

## Auth

- Cookie `pc_session`: HttpOnly; Path=/; SameSite=Lax; Secure salvo `COOKIE_SECURE=false`.
- JWT HS256, claims: `sub` = userId (string), `role` ∈ {USER, COMPANY_ADMIN, ADMIN}, `name`, `exp`. `JWT_SECRET` igual en
  API y gateway (≥ 32 bytes).
- Públicos: SOLO `POST /api/auth/login`, `POST /api/auth/register`, `POST /api/auth/logout`, `GET /actuator/health`.
- `GET /api/auth/me` → `UserPublicDto { id, name, userName, email, role, photoUrl, wallPhotoUrl }` (sin password).
- El gateway valida el JWT en TODO `/upload/**` (401 si falta).
- Frontend: `/login`, `/register`; Astro `output: 'server'` + `@astrojs/node` standalone.

## API y contratos

- REST sin verbos, errores `application/problem+json` con `traceId`: `docs/api-routes.md`.
- Listados paginados `{items,page,size,hasNext}`; `GET /api/admin/users` mantiene `{content,page,size,totalElements,totalPages}`.
- La forma de las respuestas está fijada en `docs/upgrade/contracts-boot4.json`; el workflow `contracts` (y
  `make contracts-local`) falla ante cualquier diferencia. Cambiar un contrato = `CONTRACT-CHANGE` + actualizar la línea
  base en el PR del principal.

## Integración entre repos (CI)

- Hijo: `ci`, `security` (CodeQL `upload: never` + `sarif-gate.sh` ≥ 7, Semgrep `ERROR`, gitleaks, SCA), `eol-check`
  (Java) y `notify-platform` (tras `ci` verde en `main` → `repository_dispatch` `submodule-updated` al principal).
- Principal: `ci` (raíz), `security` (raíz), `e2e`, `dast`, `contracts` (requieren `PLATFORM_TOKEN` para los
  submódulos privados), `bump-submodules` (PR `bump/<component>`), `ai-e2e-triage` (comenta en el PR y abre issue en el
  repo hijo responsable con `PLATFORM_TOKEN`) y `ai-e2e-heal` (manual).
- Secrets: `PLATFORM_TOKEN` (principal), `PLATFORM_DISPATCH_TOKEN` (hijos), `NVD_API_KEY` (backend y services),
  `ANTHROPIC_API_KEY` o `CLAUDE_CODE_OAUTH_TOKEN` (principal). Los crea el mantenedor (README → acciones).

## Toolchain local

- No hay `mvn` ni JDK 17 en el host (solo JDK 25). Java con Docker y tu UID, capturando el exit code real:
  `docker run --rm --user "$(id -u):$(id -g)" -e MAVEN_CONFIG=/tmp/.m2 -v "$HOME/.m2":/tmp/.m2 -v "$PWD":/app -w /app maven:3.9-eclipse-temurin-17 mvn -B -Dmaven.repo.local=/tmp/.m2/repository ...`
  (los `*IT` del backend necesitan además `-v /var/run/docker.sock:/var/run/docker.sock`).
- Compose: binario `docker-compose` (v2), no `docker compose`.
- Node 22, `gh` autenticado como `leonardo02lobo`, Claude Code CLI (`claude -p`), `gh act` (nektos/gh-act) y `actionlint`
  en `~/.local/bin`, `openspec` 1.6, `git-filter-repo`.

## Reglas de git

- Solo commits en TU rama/worktree. Commits pequeños por grupo de tareas, mensaje con la línea final:
  `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`
- Permitido: `git push -u origin <tu-rama>` y abrir PR en **draft** (`gh pr create --draft`, con `-R` en los hijos) cuando
  el change esté verde en local.
- Prohibido: push a `main`, force-push, merge de PRs, configurar secrets, visibilidad o branch protection, reescribir
  historial publicado, `git stash` sin etiqueta, tocar worktrees o contenedores ajenos.
- Marcar tareas `- [x]` en `openspec/changes/<change>/tasks.md` del worktree del principal a medida que se completan.
