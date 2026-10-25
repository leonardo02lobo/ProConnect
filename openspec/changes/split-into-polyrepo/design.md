## Context

- `main` (6bd3440) contiene todo integrado: `FrontedProConnect/`, `backProconnect/`, `gateway/` y `NodeTsPosts/`, más en la raíz `.claude/`, `openspec/`, `e2e/`, `perf/`, `security/`, `docs/`, los compose, el `Makefile` y 7 workflows.
- Decisiones del usuario: submódulos git; dividir después de mergear la cadena (ya hecho); repos **privados** reutilizando `ProConnect` como principal; historial conservado con `git filter-repo`.
- La cuenta permite branch protection en repos privados (comprobado) pero **no Code Scanning**, porque no tiene GitHub Code Security.
- La orquestación de agentes vive hoy fuera del repo, en `~/orca/workspaces/ProConnect/.orchestration`.
- Secretos históricos conocidos (README, checklist de rotación): la contraseña de MySQL de `application.properties` y las credenciales SMTP y el JWT del legacy. Los hallazgos de gitleaks están triados en `.gitleaksignore`.

## Goals / Non-Goals

**Goals:**
- Un repo por componente, con historial y CI propios.
- Un repo principal que componga versiones concretas de los componentes y pruebe el sistema completo.
- El gestor de IA en la raíz del principal, capaz de orquestar changes que cruzan repos.

**Non-Goals:**
- Publicar artefactos versionados (imágenes en un registro, releases semver): por ahora se compone por SHA de submódulo.
- Separar `gateway` y `media` en repos distintos (el usuario pidió un repo de microservicios).
- Despliegue continuo.

## Decisions

### D1. División con git filter-repo y depuración de secretos
```
frontend : git filter-repo --subdirectory-filter FrontedProConnect
backend  : git filter-repo --subdirectory-filter backProconnect
services : git filter-repo --path gateway/ --path NodeTsPosts/ --path-rename NodeTsPosts/:media/
todos    : --replace-text secrets.txt   (valores literales históricos → ***REMOVED***)
```
- `secrets.txt` se construye a partir de los hallazgos de gitleaks sobre el historial del monorepo (`gitleaks detect --log-opts=--all --report-format json`), extrayendo **solo** los valores de las reglas ya triadas. El archivo vive en un directorio temporal fuera de cualquier repo y se borra al terminar; nunca se commitea ni se muestra.
- Cada repo se reescribe sobre un **clon fresco y espejo** (`git clone --no-local`), nunca sobre el worktree de trabajo.
- Después se verifica con gitleaks (`--log-opts=--all`) sobre cada repo nuevo: 0 hallazgos.
- **Alternativa**: `git subtree split`. Es más lento, no reescribe secretos y deja un historial menos limpio.

### D2. Repo principal tras la división
```
ProConnect/
├── CLAUDE.md                 ← protocolo de orquestación (nuevo)
├── .claude/{commands,skills,agents}/
├── openspec/                 ← specs del sistema + changes multi-repo
├── orchestration/            ← CONTRACT.md, BOARD.md, templates/, scripts/start-change.sh, finish-change.sh
├── frontend/   (submódulo → proconnect-frontend)
├── backend/    (submódulo → proconnect-backend)
├── services/   (submódulo → proconnect-services: gateway/, media/)
├── e2e/  perf/  security/dast/  docs/
├── docker-compose.{dev,e2e,sec}.yml   Makefile   README.md   SECURITY.md
└── .github/workflows/{e2e,dast,ai-e2e-triage,ai-e2e-heal,bump-submodules,contracts}.yml
```
- Antes de quitar las carpetas se crea el tag `pre-split-monorepo` en `main`.
- Submódulos con `branch = main` en `.gitmodules` (para `git submodule update --remote`) y URLs HTTPS (`https://github.com/leonardo02lobo/proconnect-*.git`), que sirven tanto en local con `gh auth` como en Actions con token.
- Los `.gitignore`, `.editorconfig`, `.gitleaks.toml` y `.gitleaksignore` se reparten: cada hijo lleva los suyos y el principal se queda con lo de la raíz.

### D3. Workflows por repo
| Repo | Workflows |
|---|---|
| frontend | `ci.yml` (astro check, ESLint, Prettier, build), `security.yml` (Semgrep, gitleaks, npm audit, CodeQL JS/TS sin upload), `notify-platform.yml` |
| backend | `ci.yml` (mvn verify con ITs de Testcontainers, Spotless, Checkstyle), `security.yml` (CodeQL Java sin upload, Semgrep, gitleaks, Dependency-Check con `NVD_API_KEY`), `eol-check.yml`, `notify-platform.yml` |
| services | `ci.yml` (matriz gateway Maven + media npm test/lint), `security.yml` (CodeQL Java y JS/TS, Semgrep, gitleaks, SCA Maven y npm), `eol-check.yml`, `notify-platform.yml` |
| principal | `e2e.yml`, `dast.yml`, `contracts.yml`, `ai-e2e-triage.yml`, `ai-e2e-heal.yml`, `bump-submodules.yml`, `security.yml` (gitleaks y Semgrep de lo que queda en la raíz) |

- **CodeQL sin Code Scanning**: `github/codeql-action/analyze` con `upload: never` y `output: sarif-results`, seguido de un paso que falla si el SARIF tiene resultados de `security-severity` ≥ 7 (con `jq`) y sube el SARIF como artifact. **Semgrep**: `semgrep ci --error` (o `semgrep scan --error --config ...`) sin `--sarif` upload.
- Todas las actions fijadas por SHA y Dependabot con cooldown de 7 días en cada repo. En el principal se añade además el ecosistema `gitsubmodule`, como respaldo del bump.

### D4. Integración entre repos
```
hijo: push a main ──notify-platform.yml──► repository_dispatch(type=submodule-updated, {component, sha})
principal: bump-submodules.yml ──► checkout(submodules) → git -C <dir> checkout <sha> → PR "bump <component> → <sha7>" (rama bump/<component>)
principal: PR ──► e2e.yml + dast.yml + contracts.yml (checks requeridos) ──► merge
```
- Credencial: una **GitHub App propia** (recomendado) o un **token fine-grained**, guardada como `PLATFORM_TOKEN`. Permisos:
  - en el principal: Contents R/W y Pull requests R/W (para crear el PR de bump);
  - en los hijos: Contents R (para el checkout de submódulos) e Issues R/W (para que el triage abra issues).
  - Los hijos guardan `PLATFORM_DISPATCH_TOKEN`, con permiso solo de dispatch al principal (Contents R/W en el principal, que es lo que requiere el endpoint `dispatches`).
  - Crear la App o el token es **acción del usuario**; los workflows quedan preparados y, si falta el secret, se saltan con un aviso claro.
- El PR de bump lo crea el token de la App/PAT, no `GITHUB_TOKEN`, para que sí dispare los workflows del PR.

### D5. Gestor de IA en la raíz
- `CLAUDE.md` raíz: describe el sistema, los repos y submódulos y el protocolo, y fija los roles: orquestador, agentes por repo, QA y seguridad. Recoge también las reglas de git (ramas `leonardo02lobo/<change>`, PRs draft, nunca push a main) y el flujo OpenSpec multi-repo.
- `.claude/agents/`: `frontend-dev.md`, `backend-dev.md`, `services-dev.md`, `qa-e2e.md` y `security-auditor.md`. El frontmatter define `tools`, y el cuerpo, el alcance (directorio), los comandos de build y test (Docker con temurin-17, npm), los puertos y los criterios de "hecho".
- `orchestration/`:
  - `CONTRACT.md`: migrado y actualizado a las rutas nuevas.
  - `BOARD.md`: tablero nuevo, con el histórico archivado en `orchestration/archive/2026-10-board.md`.
  - `templates/agent-prompt.md`.
  - `scripts/start-change.sh <change> <repos...>`: crea la rama en el principal y en cada submódulo afectado, con worktrees en `../<change>/<repo>`, y añade la entrada al tablero.
  - `scripts/finish-change.sh <change>`: comprueba los PRs, limpia los worktrees y archiva el change en OpenSpec cuando la integración está verde.
- Las proposals de OpenSpec llevan una sección `## Repos` (frontend/backend/services/platform). Se añade como regla en `openspec/config.yaml`.
- El triage con IA (`e2e/prompts/triage.md`) gana un paso: mapear el test fallido a un componente y, con `PLATFORM_TOKEN`, abrir un issue en el repo hijo (`gh issue create -R`).

### D6. Orden de ejecución
1. Archivar las 7 changes de OpenSpec en `main` (consolidar `openspec/specs/`) y crear el tag `pre-split-monorepo`.
2. Crear los 3 repos privados vacíos.
3. Reescribir el historial de cada hijo en clones espejo (D1), verificar con gitleaks, añadir sus workflows, `CLAUDE.md` mínimo, `README.md` y `.gitignore`, y hacer push de `main`.
4. Comprobar que cada hijo pasa su CI en GitHub (salvo los jobs que dependen de secrets aún ausentes, que se saltan con aviso).
5. Principal: quitar las carpetas, añadir los submódulos, actualizar rutas (compose, Makefile, e2e, perf, dast, docs), los workflows, el gestor de IA y el README. Verificar en local `make e2e-local`, `make ci-local` y `make lint-workflows`.
6. PR en el principal → integración verde → merge.
7. Hacer privado `ProConnect` y aplicar branch protection en los 4 repos con los nombres reales de los checks.
8. Comunicar al usuario las acciones pendientes: `PLATFORM_TOKEN`, `PLATFORM_DISPATCH_TOKEN`, `NVD_API_KEY` en backend y services, y `CLAUDE_CODE_OAUTH_TOKEN` en el principal.

## Risks / Trade-offs

- [Pasar a privado elimina estrellas y watchers y desactiva Code Scanning] → Lo eligió el usuario. Los escaneos siguen bloqueando por job (D3).
- [Sin `PLATFORM_TOKEN` los workflows de E2E del principal no pueden clonar submódulos privados] → Hasta que exista, el E2E del principal queda en rojo o saltado. Se documenta como primera acción del usuario y se valida en local mientras tanto.
- [La reescritura de historial pierde algo] → Se trabaja sobre clones espejo, el monorepo se conserva entero con el tag `pre-split-monorepo` y se comparan los árboles (el `HEAD` de cada hijo debe ser igual a su carpeta en `main`).
- [Los submódulos complican el día a día] → `make bootstrap` (`git submodule update --init --recursive`), los scripts de orquestación y una guía en el README y en `CLAUDE.md`.
- [Cambios cruzados requieren varios PRs] → `start-change.sh` y el flujo de bump automatizan la coordinación; el orquestador de IA la gestiona.

## Migration Plan

Lo marca D6. Rollback: borrar los 3 repos nuevos y revertir el PR del principal; el monorepo sigue intacto en el historial y en el tag `pre-split-monorepo`. El cambio de visibilidad se puede revertir (aunque las estrellas no se recuperan).
