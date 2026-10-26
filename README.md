# ProConnect

Red profesional: frontend Astro + React, API core en Spring Boot, servicio de media en Express y un API Gateway en Spring Cloud Gateway.

Este es el **repo principal (plataforma)**: compone los componentes como submódulos git, ejecuta la integración
(E2E, DAST, contratos JSON) y aloja el gestor de IA (`CLAUDE.md`, `.claude/agents/`, `openspec/`, `orchestration/`).

| Directorio | Repo (privado) | Contenido |
|---|---|---|
| `frontend/` | [`proconnect-frontend`](https://github.com/leonardo02lobo/proconnect-frontend) | web Astro + React |
| `backend/` | [`proconnect-backend`](https://github.com/leonardo02lobo/proconnect-backend) | API core Spring Boot |
| `services/` | [`proconnect-services`](https://github.com/leonardo02lobo/proconnect-services) | `gateway/` (Spring Cloud Gateway) y `media/` (Express) |

```bash
git clone --recurse-submodules https://github.com/leonardo02lobo/ProConnect.git
cd ProConnect && make bootstrap        # tras cada pull: inicializa/actualiza los submódulos a los commits fijados
make submodules-status                 # commit fijado vs main de cada componente
```

Cada componente tiene su propio CI (build, tests, lint, CodeQL/Semgrep/gitleaks/SCA). Al mergear en su `main`, avisa a
este repo (`notify-platform` → `repository_dispatch`) y `bump-submodules` abre el PR `bump <component> → <sha>`, que
solo se mergea con `e2e`, `dast` y `contracts` en verde. El monorepo anterior a la división está en el tag
`pre-split-monorepo` (y el backend Express legacy en `legacy-express-backend`).

## Topología

```
Navegador ──► web (Astro SSR) :4321
          └─► gateway :8080 ──┬─► /api/**    → backend        :8081 (Spring MVC + JPA + Security)
                              └─► /upload/** → services/media :3001 (Express + multer)
MySQL :3306 (DB `proconnect`)
```

| Servicio | Directorio | Puerto | Variables |
|---|---|---|---|
| gateway | `services/gateway/` | 8080 | `PORT`, `API_URI`, `MEDIA_URI`, `CORS_ORIGINS`, `JWT_SECRET` |
| API core | `backend/` | 8081 | `SERVER_PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_TTL`, `COOKIE_SECURE`, `CORS_ORIGINS` |
| media | `services/media/` | 3001 | `PORT`, `UPLOAD_DIR` |
| web | `frontend/` | 4321 | `PUBLIC_API_URL` (build), `API_INTERNAL_URL` (runtime, opcional) |

Cada servicio tiene un `.env.example` con todas sus variables. Copia los valores a tu entorno (o a un `.env` local, que no se versiona). El backend Express legacy (`BackendProConnect/`) se retiró en `cleanup-codebase`: queda en el tag git `legacy-express-backend` y su funcionalidad pendiente está en [`docs/backlog-legacy.md`](docs/backlog-legacy.md).

## Autenticación

- `POST /api/auth/login` valida email y password (BCrypt) y emite la cookie `pc_session` (JWT HS256, `HttpOnly; Path=/; SameSite=Lax; Secure`). `Secure` solo se desactiva con `COOKIE_SECURE=false` en desarrollo sobre http.
- Toda la API y todas las imágenes (`/upload/**`) requieren sesión, salvo `POST /api/auth/{login,register,logout}` y `GET /actuator/health`.
- `GET /api/auth/me` devuelve el usuario de la sesión y `POST /api/auth/logout` expira la cookie.
- El autor de cada mutación sale del token: los `userId` del body se ignoran.
- `JWT_SECRET` es obligatorio (32 bytes como mínimo) en la API y en el gateway, y tiene que ser el mismo en los dos. Si falta, ninguno de los dos arranca.

## Desarrollo local

Requisitos: Docker (`docker-compose`), Node 22, y Java 17 o Docker para compilar.

```bash
# 1. MySQL + Mailpit (captura los correos de postulación: SMTP :1025, UI http://localhost:8025)
docker-compose -p pc-risks -f docker-compose.dev.yml up -d

# 2. Variables comunes (ejemplo de desarrollo)
export JWT_SECRET="$(openssl rand -base64 48)" COOKIE_SECURE=false CORS_ORIGINS=http://localhost:4321
export DB_URL=jdbc:mysql://localhost:3306/proconnect DB_USER=proconnect DB_PASSWORD=proconnect-dev

# 3. API core (:8081) y gateway (:8080)
(cd backend && ./mvnw spring-boot:run)
(cd services/gateway && ./mvnw spring-boot:run)

# 4. Media (:3001)
(cd services/media && npm ci && npm start)

# 5. Web (:4321)
(cd frontend && npm ci && PUBLIC_API_URL=http://localhost:8080 npm run dev)
#   producción: PUBLIC_API_URL=... npm run build && npm start
```

Abre `http://localhost:4321` usando `localhost`, no `127.0.0.1`, para que la cookie de sesión se comparta entre `:4321` y `:8080`.

Los correos de postulación salen por SMTP (`MAIL_HOST`, `MAIL_PORT`; por defecto Mailpit en `localhost:1025`). Las rutas de la API están en [`docs/api-routes.md`](docs/api-routes.md), y los errores son `application/problem+json` con `traceId`.

### Roles y primer ADMIN

Los roles son `USER` (el registro siempre lo asigna), `COMPANY_ADMIN` (crea empresas y empleos y resuelve postulaciones) y `ADMIN`. Un ADMIN asigna roles desde `/admin/users` (o `PUT /api/admin/users/{id}/role`). Un ADMIN no puede quitarse su propio rol y siempre queda al menos uno. Cada cambio queda auditado en la tabla `role_changes` y se aplica a las sesiones abiertas en la siguiente petición.

El primer ADMIN se crea al arrancar la API con `BOOTSTRAP_ADMIN_EMAIL` y uno de estos dos (ver `backend/.env.example`):
- `BOOTSTRAP_ADMIN_PASSWORD`;
- `BOOTSTRAP_ADMIN_PASSWORD_FILE`, una ruta a un archivo, por ejemplo un Docker secret (`/run/secrets/admin_password`). Recomendado, porque el password no queda en el entorno del proceso.

Definir los dos es un error de arranque. El password debe tener al menos 12 caracteres y 3 de 4 clases (minúsculas, mayúsculas, dígitos, símbolos). `BOOTSTRAP_ADMIN_USERNAME` es opcional: si falta, se deriva del email, con sufijo numérico si ya existe. El bootstrap nunca modifica un usuario existente. **Retira las variables `BOOTSTRAP_ADMIN_*` (y el secret) después del primer arranque**: el ADMIN ya existe y los demás se promueven desde la UI.

Calidad (la CI de cada repo la exige): `mvn spotless:apply` / `mvn spotless:check checkstyle:check` en `backend/` y `services/gateway/`, y `npm run lint`, `npm run format` / `npm run format:check` en `frontend/` y `services/media/`. El commit de formato masivo está en `.git-blame-ignore-revs` de cada repo (`git config blame.ignoreRevsFile .git-blame-ignore-revs`).

Para compilar y testear sin Maven local: `docker run --rm -v "$PWD":/app -v "$HOME/.m2":/root/.m2 -w /app maven:3.9-eclipse-temurin-17 mvn -B verify`, desde `backend/` o `services/gateway/`. Los tests de la API usan H2 en memoria y no necesitan MySQL.

## Tests E2E y CI (local primero, luego GitHub Actions)

Todo se ejecuta primero en local con los mismos scripts que usan los workflows. Detalle en [`e2e/README.md`](e2e/README.md).

```bash
make help              # todos los objetivos
make e2e-local         # levanta el stack pc-e2e (puertos +10000), siembra datos y corre Playwright
make e2e-test ARGS="tests/like.spec.ts --headed"   # repetir contra el stack ya levantado
make e2e-down          # parar el stack
make bootstrap         # submódulos a los commits fijados
make contracts-local   # contratos JSON del stack levantado vs docs/upgrade/contracts-boot4.json
make ci-local          # ci.yml del principal con act (typecheck e2e, actionlint, shellcheck, openspec)
make ci-local-e2e      # e2e.yml con act (usa el Docker del host y tu `gh auth token` como PLATFORM_TOKEN)
make lint-workflows    # actionlint
make ai-triage         # diagnóstico con Claude Code (claude -p) del último fallo E2E
make ai-heal           # healer con Claude Code sobre los tests rotos (deja cambios para revisar)
```

Requisitos locales: Docker + `docker-compose`, Node 22, `gh` con la extensión `nektos/gh-act`, `actionlint` y el CLI de Claude Code para los objetivos de IA.

| Workflow | Cuándo | Qué hace |
|---|---|---|
| `ci` | cada PR (cualquier base) y push a `main` | lo de la raíz: typecheck de `e2e/`, actionlint, shellcheck, OpenSpec (`## Repos` en cada proposal) |
| `e2e` | cada PR y push a `main` | stack Compose con los submódulos fijados + Playwright; artifact `e2e-report` |
| `contracts` | cada PR y push a `main` | forma de las respuestas JSON vs `docs/upgrade/contracts-boot4.json` |
| `dast` | push a `main`, cron, PRs que mueven `frontend`/`services` | ZAP baseline autenticado |
| `security` | cada PR, push, cron | gitleaks (todo el historial en cron/push), Semgrep y npm audit de la raíz |
| `bump-submodules` | `repository_dispatch` de los hijos, cron diario, manual | PR `bump <component> → <sha>` |
| `ai-e2e-triage` | cuando `e2e` falla en un PR | Claude comenta un diagnóstico y abre issue en el repo hijo responsable (solo lectura, nunca hace push) |
| `ai-e2e-heal` | manual | healer + guardarraíles y abre un PR draft separado para revisión humana |

Workflows de cada componente (en su repo): `ci`, `security` (CodeQL con `upload: never` + fallo con `security-severity` ≥ 7, Semgrep `ERROR`, gitleaks, SCA), `eol-check` (Java) y `notify-platform`.

## Acciones del mantenedor (pendientes tras split-into-polyrepo)

1. **Facturación de Actions**: los repos privados consumen minutos de Actions. Hoy los jobs de los 3 repos hijos no
   arrancan ("recent account payments have failed or your spending limit needs to be increased"): revisar
   *Settings → Billing and plans* (método de pago / límite de gasto de Actions). Al pasar `ProConnect` a privado le
   pasará lo mismo.
2. **`PLATFORM_TOKEN`** (secret en `ProConnect`): GitHub App propia (recomendado) o token *fine-grained* con acceso
   solo a los 4 repos:
   - `ProConnect`: Contents R/W, Pull requests R/W (bump PRs), Workflows no.
   - `proconnect-frontend`, `proconnect-backend`, `proconnect-services`: Contents Read (checkout de submódulos) e
     Issues R/W (issues del triage).
   Sin él, `e2e`, `contracts`, `dast` y `ai-e2e-heal` fallan con un error que lo indica y `bump-submodules` se salta.
3. **`PLATFORM_DISPATCH_TOKEN`** (secret en cada repo hijo): fine-grained con acceso solo a `ProConnect`,
   Contents R/W (lo exige el endpoint `dispatches`). Sin él, `notify-platform` se salta con aviso.
4. **`NVD_API_KEY`** en `proconnect-backend` y `proconnect-services` (gratuita: https://nvd.nist.gov/developers/request-an-api-key).
   Sin ella, Dependency-Check se salta con aviso.
5. **`ANTHROPIC_API_KEY` o `CLAUDE_CODE_OAUTH_TOKEN`** (`claude setup-token`) en `ProConnect` para `ai-e2e-triage` /
   `ai-e2e-heal`; y "Allow GitHub Actions to create and approve pull requests" para `ai-e2e-heal`.
6. **Dependabot y submódulos privados**: en *Settings → Code security → Dependabot* de `ProConnect`, dar acceso a los
   repos hijos (ecosistema `gitsubmodule`).
7. Pasar `ProConnect` a privado y branch protection en `main` de los 4 repos (lo hace el orquestador con los nombres
   reales de los checks: hijos `ci`/`security`; principal `ci`, `e2e`, `contracts`, `dast`, `security`).

## Checklist de rotación de credenciales (acción manual requerida)

Estos secretos estuvieron commiteados y **siguen en el historial de git de este repo** (que no se reescribe, para no romper clones ni worktrees). En los repos hijos se sustituyeron por `***REMOVED***` al extraerlos con `git filter-repo`, pero la única mitigación real es rotarlos:

- [ ] **MySQL**: cambiar la contraseña del usuario `root` de la instancia de desarrollo (antes estaba en `backProconnect/src/main/resources/application.properties` del monorepo y como fallback en `BackendProConnect/src/models/database.ts`, hoy solo en el historial y en el tag `legacy-express-backend`). Crear un usuario de aplicación con permisos solo sobre `proconnect` y usarlo vía `DB_USER`/`DB_PASSWORD`.
- [ ] **Gmail**: revocar el *app password* de la cuenta que aparecía en `BackendProConnect/src/settings/configurateEmail.ts` (Cuenta de Google → Seguridad → Contraseñas de aplicaciones) y generar uno nuevo solo si se sigue usando. Ahora se lee de `MAIL_USERNAME`/`MAIL_PASSWORD`.
- [ ] **JWT legacy**: el secreto por defecto de `BackendProConnect` (fallback hardcodeado, ya eliminado) no debe reutilizarse en ningún sitio. Generar `JWT_SECRET` nuevo con `openssl rand -base64 48`.
- [x] Revisar el historial con `gitleaks`: automatizado en `.github/workflows/security.yml` de cada repo (cron semanal sobre todo el historial). Los hallazgos de esta lista están triados en `.gitleaksignore`; cualquier secreto nuevo hace fallar el job.
- [ ] Usuarios con password sin hashear: la actualización antigua de usuario guardaba el password en texto plano. Esas cuentas ya no pueden iniciar sesión (el login solo acepta hashes BCrypt). Para detectarlas y obligar a resetear el password:

  ```sql
  SELECT id, email FROM proconnect.user WHERE password NOT LIKE '$2%';
  ```

## Seguridad

- Política de reporte: [`SECURITY.md`](SECURITY.md). Auditoría y estado de cada hallazgo: [`docs/security/audit-2026-10-24.md`](docs/security/audit-2026-10-24.md).
- Autorización: los checks de propiedad/rol viven en los services (`OwnershipGuard`). Recursos públicos ajenos → 403; privados (mensajes, postulaciones) → 404. Solo `COMPANY_ADMIN`/`ADMIN` crean empresas; `ADMIN` modera todo.
- CSRF: la API rechaza mutaciones con `Origin`/`Referer` fuera de `CORS_ORIGINS`.
- Fuerza bruta: `RATE_LIMIT_AUTH_PER_MINUTE` (10), `RATE_LIMIT_MAX_FAILURES` (5), `RATE_LIMIT_LOCKOUT` (`PT15M`); detrás del gateway define `TRUSTED_PROXIES` con su red para respetar `X-Forwarded-For`.
- Headers: el gateway envía CSP, `X-Frame-Options`, `nosniff`, `Referrer-Policy`, `Permissions-Policy`; HSTS con `SPRING_PROFILES_ACTIVE=prod` (o `HSTS_ENABLED=true`). La web usa la CSP con hashes de Astro (`security.csp`).
- Escaneos locales (en cada repo, con su `.gitleaks.toml`): `docker run --rm -v "$PWD":/repo -w /repo ghcr.io/gitleaks/gitleaks:v8.30.1 git /repo --config /repo/.gitleaks.toml --gitleaks-ignore-path /repo/.gitleaksignore --log-opts=--all`; DAST: levanta `docker-compose -p pc-sec -f docker-compose.e2e.yml -f docker-compose.sec.yml up -d --build --wait` y ejecuta `security/dast/run-zap.sh`.
