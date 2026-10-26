## Why

Hoy ProConnect es un monorepo con frontend, API, gateway, servicio de media, E2E, OpenSpec y la orquestación de agentes. Queremos que cada pieza tenga su propio repositorio, ciclo de CI y propiedad, manteniendo un **repo principal** que componga el sistema completo y aloje en la raíz el **gestor de IA**: OpenSpec, los agentes de Claude Code, el protocolo de orquestación (contrato y tablero) y el triage/healing de E2E con IA. Ese gestor coordina el trabajo entre los repos.

## What Changes

- **Repos nuevos (privados)**, cada uno con el historial de su carpeta conservado con `git filter-repo` y los secretos históricos conocidos reemplazados:
  - `proconnect-frontend` ← `FrontedProConnect/`
  - `proconnect-backend` ← `backProconnect/`
  - `proconnect-services` ← `gateway/` + `NodeTsPosts/` (como `gateway/` y `media/`)
- **BREAKING (estructura)** `ProConnect` pasa a ser el **repo principal (plataforma)** y se vuelve **privado**:
  - las carpetas de código se sustituyen por **submódulos git** en `frontend/`, `backend/` y `services/`;
  - conserva en la raíz: `.claude/`, `openspec/`, `orchestration/` (contrato, tablero y plantillas de agentes), `e2e/`, `perf/`, `security/` (DAST), `docs/`, los `docker-compose.*.yml`, el `Makefile` y los workflows de integración.
- **Gestor de IA en la raíz**:
  - `CLAUDE.md` raíz con el protocolo de orquestación;
  - `.claude/agents/` con un agente por repo (frontend, backend, services) más QA/E2E y seguridad;
  - `orchestration/` (versionado) que sustituye a `~/orca/.../.orchestration`;
  - un script que crea una rama y un worktree con el mismo nombre de change en cada submódulo afectado;
  - las changes de OpenSpec declaran qué repos tocan;
  - el triage con IA del E2E puede abrir issues en el repo hijo responsable.
- **CI por repo**:
  - cada hijo trae su build, tests, lint, escaneo de seguridad (SAST, SCA, secretos), Dependabot con cooldown, actions fijadas por SHA y `eol-check` (los repos Java);
  - el principal ejecuta E2E, DAST, triage/heal con IA, `bump-submodules` y la verificación de contratos.
- **Integración continua entre repos**:
  - al mergear en `main` de un hijo, este notifica al principal (`repository_dispatch`);
  - el principal abre un PR que mueve el puntero del submódulo y ejecuta el E2E completo sobre esa combinación.
- **Repos privados sin code scanning** (necesita GitHub Code Security, de pago): CodeQL y Semgrep se ejecutan sin subir SARIF y **fallan el job** ante hallazgos. Branch protection en `main` de los 4 repos.
- **OpenSpec**: se archivan las 7 changes completadas para consolidar `openspec/specs/` como fuente de verdad antes de dividir.

## Repos

- **platform** (`ProConnect`): submódulos, rutas, workflows de integración, gestor de IA en la raíz y documentación.
- **frontend** (`proconnect-frontend`, nuevo): historial de `FrontedProConnect/` y su CI propio.
- **backend** (`proconnect-backend`, nuevo): historial de `backProconnect/` y su CI propio.
- **services** (`proconnect-services`, nuevo): historial de `gateway/` y `NodeTsPosts/` (→ `media/`) y su CI propio.

## Capabilities

### New Capabilities
- `repository-topology`: un repo principal más 3 repos hijos, relación por submódulos, historial conservado y secretos depurados.
- `cross-repo-ci`: CI por repo, integración en el principal, bump automático de submódulos y gates.
- `ai-orchestration`: gestor de IA en la raíz del principal (agentes por repo, protocolo, scripts y OpenSpec multi-repo).

### Modified Capabilities
- `ci-pipeline`: el build y los tests por servicio pasan al `ci.yml` de cada repo hijo; el principal conserva un `ci.yml` de raíz.
- `security-scanning`: CodeQL y Semgrep sin subir SARIF (repos privados) y con fallo del job por severidad.
- `platform-support`: rutas de los `pom.xml` en los repos hijos.

## Impact

- GitHub: se crean 3 repos privados, `ProConnect` pasa a privado (**se pierden estrellas y watchers**), 4 conjuntos de branch protection y secrets nuevos.
- Rutas: `docker-compose.*.yml`, `Makefile`, `e2e/scripts/*`, `perf/*`, `security/dast/*`, workflows, README y docs pasan a `frontend/`, `backend/`, `services/gateway/` y `services/media/`.
- Desarrollo local: `git clone --recurse-submodules` y `make bootstrap`.
- **Acción del usuario**: crear un token (GitHub App o PAT fine-grained) con acceso a los 4 repos para que Actions clone submódulos privados y envíe y reciba eventos entre repos; volver a crear los secrets (`NVD_API_KEY` y `CLAUDE_CODE_OAUTH_TOKEN`) donde se usen.
