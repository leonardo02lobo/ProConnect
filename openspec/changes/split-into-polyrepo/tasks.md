## 1. Preparación en el monorepo

- [x] 1.1 Archivar las 7 changes completadas con `openspec archive` (anotando en cada una las tareas que eran del usuario) y validar `openspec/specs/`
- [x] 1.2 Crear y subir el tag `pre-split-monorepo` sobre `main`
- [x] 1.3 Instalar `git-filter-repo` y generar fuera del repo la lista de reemplazo de secretos a partir de gitleaks (sin mostrar ni commitear los valores)

## 2. Repos hijos

- [x] 2.1 Crear los repos privados `proconnect-frontend`, `proconnect-backend` y `proconnect-services` (vacíos, rama `main`)
- [x] 2.2 Frontend: clon espejo → `filter-repo --subdirectory-filter FrontedProConnect` + `--replace-text` → verificar que el árbol es igual a `FrontedProConnect/` de `main` y que gitleaks da 0 en el historial
- [x] 2.3 Backend: igual con `backProconnect/`
- [x] 2.4 Services: `--path gateway/ --path NodeTsPosts/ --path-rename NodeTsPosts/:media/` + `--replace-text`, con las mismas verificaciones
- [x] 2.5 En cada hijo: `README.md`, `CLAUDE.md` mínimo que remite al principal, `.gitignore`, `.editorconfig`, configuración de gitleaks y `dependabot.yml` con cooldown
- [x] 2.6 En cada hijo: `ci.yml`, `security.yml` (CodeQL con `upload: never` + fallo por severidad, Semgrep `--error`, gitleaks, SCA), `eol-check.yml` en los Java y `notify-platform.yml`, todo fijado por SHA; validar con actionlint y `gh act` cuando sea viable
- [ ] 2.7 Push de `main` de cada hijo y comprobar su CI en GitHub (los jobs que dependen de secrets ausentes se saltan con aviso)

## 3. Repo principal

- [x] 3.1 Quitar `FrontedProConnect/`, `backProconnect/`, `gateway/` y `NodeTsPosts/`, y añadir los submódulos `frontend/`, `backend/` y `services/` (HTTPS, `branch = main`)
- [x] 3.2 Actualizar rutas en `docker-compose.*.yml`, `Makefile` (más el objetivo `bootstrap`), `e2e/scripts/*`, `e2e/prompts/*`, `perf/*`, `security/dast/*`, `docs/*`, `README.md`, `SECURITY.md`, `.editorconfig`, `.gitignore` y la configuración de gitleaks
- [x] 3.3 Workflows del principal: `e2e.yml`, `dast.yml` y `contracts.yml` con checkout de submódulos usando `PLATFORM_TOKEN`; `bump-submodules.yml` (`repository_dispatch`, programado y manual); `security.yml` reducido; quitar los jobs que pasaron a los hijos; `dependabot.yml` con `gitsubmodule`
- [ ] 3.4 Verificar en local `make bootstrap`, `make e2e-local` (suite completa dos veces), `make ci-local` y `make lint-workflows`

## 4. Gestor de IA en la raíz

- [ ] 4.1 `CLAUDE.md` raíz con el protocolo de orquestación multi-repo
- [ ] 4.2 `.claude/agents/`: `frontend-dev`, `backend-dev`, `services-dev`, `qa-e2e` y `security-auditor`
- [ ] 4.3 `orchestration/`: `CONTRACT.md` actualizado, `BOARD.md` nuevo con el histórico en `archive/`, `templates/agent-prompt.md` y los scripts `start-change.sh` y `finish-change.sh` (probados con un change ficticio que luego se limpia)
- [ ] 4.4 Añadir a `openspec/config.yaml` la regla de declarar `## Repos` en cada proposal
- [ ] 4.5 Triage con IA: mapeo de tests a componentes y apertura de issue en el repo hijo con `PLATFORM_TOKEN` (validado en local con `claude -p` sobre un fallo provocado)

## 5. Publicación

- [ ] 5.1 PR en el principal con la división; checks verdes salvo los que dependen de `PLATFORM_TOKEN`, documentados → merge
- [ ] 5.2 Hacer privado `ProConnect` (`gh repo edit --visibility private --accept-visibility-change-consequences`)
- [ ] 5.3 Branch protection en `main` de los 4 repos con los nombres reales de los checks
- [ ] 5.4 Mover la orquestación externa (`~/orca/workspaces/ProConnect/.orchestration`) al histórico del repo y documentar en el README la lista de acciones del usuario (`PLATFORM_TOKEN`, `PLATFORM_DISPATCH_TOKEN`, `NVD_API_KEY`, `CLAUDE_CODE_OAUTH_TOKEN`)
