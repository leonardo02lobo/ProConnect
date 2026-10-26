# ProConnect — gestor de IA (repo principal)

ProConnect es una red social profesional cerrada (estilo LinkedIn; toda la app requiere login). Este repo es el
**principal (plataforma)**: compone los componentes como submódulos git, ejecuta la integración (E2E, DAST,
contratos) y aloja el gestor de IA que coordina el trabajo entre repos. Artefactos y comunicación en español;
código e identificadores en inglés.

## Mapa de repos

| Directorio | Repo (privado) | Contenido | Agente |
|---|---|---|---|
| `frontend/` | `leonardo02lobo/proconnect-frontend` | Astro 5 SSR + React 19 + Tailwind 4 (web, :4321) | `frontend-dev` |
| `backend/` | `leonardo02lobo/proconnect-backend` | API core Spring Boot 4 / Java 17, JPA + MySQL + Flyway (:8081) | `backend-dev` |
| `services/` | `leonardo02lobo/proconnect-services` | `gateway/` Spring Cloud Gateway (:8080) y `media/` Express + multer (:3001) | `services-dev` |
| raíz | `leonardo02lobo/ProConnect` | `e2e/`, `perf/`, `security/dast/`, `docs/`, compose, `Makefile`, workflows de integración, `openspec/`, `orchestration/`, `.claude/` | `qa-e2e`, `security-auditor`, orquestador |

- Los submódulos apuntan a commits concretos (`git submodule status`). `make bootstrap` los inicializa;
  `make submodules-status` compara con su `main`. Nunca edites código de un componente desde la raíz sin estar
  en una rama del change dentro del submódulo.
- Al mergear en `main` de un hijo, su `notify-platform.yml` avisa al principal y `bump-submodules.yml` abre el PR
  `bump <component> → <sha7>`, sobre el que corren `e2e`, `dast` y `contracts`.
- Contrato compartido (puertos, variables, auth, toolchain, reglas de git): [`orchestration/CONTRACT.md`](orchestration/CONTRACT.md).
- Tablero de coordinación: [`orchestration/BOARD.md`](orchestration/BOARD.md) (append-only).

## Roles

- **Orquestador** (la sesión principal de Claude Code en la raíz): convierte las peticiones en changes de OpenSpec,
  decide qué repos tocan, arranca el change, reparte tareas a los agentes, revisa sus PRs y el tablero, y es el
  único que edita `CONTRACT.md`, mergea (con el OK del usuario) y cierra changes.
- **Agentes por repo** (`.claude/agents/frontend-dev.md`, `backend-dev.md`, `services-dev.md`): trabajan solo
  dentro de su submódulo (o su worktree del change), con la toolchain y los comandos de su repo.
- **QA/E2E** (`qa-e2e`): suite Playwright, stack de integración, contratos JSON, triage y healing con IA.
- **Seguridad** (`security-auditor`): revisiones de seguridad, escaneos (gitleaks, CodeQL, Semgrep, SCA, ZAP) y
  `docs/security/`. Solo lectura sobre el código de los componentes: reporta hallazgos al agente del repo.

## Protocolo de un change multi-repo

1. **Proponer** con `/opsx:propose` en la raíz. La proposal DEBE tener `## Repos` (frontend, backend, services,
   platform): lo exige `openspec/config.yaml` y lo valida `orchestration/scripts/check-proposals.sh` en `ci`.
   Las tareas se agrupan por repo.
2. **Arrancar**: `orchestration/scripts/start-change.sh <change> <repos...>` crea la rama
   `leonardo02lobo/<change>` en el principal y en cada repo afectado, el worktree `../<change>/` (con los
   submódulos afectados en `../<change>/<repo>/` sobre la rama del change) y la entrada en el tablero.
3. **Delegar**: un agente por repo con el prompt de [`orchestration/templates/agent-prompt.md`](orchestration/templates/agent-prompt.md)
   (change, alcance, worktree, puertos, criterios de hecho). Cada agente marca `- [x]` en `tasks.md` (en el worktree
   del principal) y publica `STATUS`/`MILESTONE`/`NEED`/`DONE` en el tablero.
4. **Integrar en local**: en el worktree del change, `make e2e-local` levanta el sistema con los submódulos en las
   ramas del change (suite completa dos veces, `--workers=4`) y `make contracts-local` compara los contratos JSON.
5. **PRs**: uno draft por repo hijo (`gh pr create --draft -R leonardo02lobo/proconnect-<repo>`) y, si el principal
   cambia, otro en `ProConnect`. Orden de merge: hijos primero (con su CI verde); después los PRs de bump del principal
   con `e2e`/`dast`/`contracts` verdes; por último el PR del principal del change.
6. **Cerrar**: `orchestration/scripts/finish-change.sh <change>` comprueba PRs mergeados, que `main` del principal
   fija los commits mergeados y que `e2e` en `main` está verde; entonces limpia worktree y ramas y archiva el change
   en OpenSpec (commit del archivo vía PR). `--dry-run` solo comprueba; `--abandon` descarta un change.

## Reglas de git (todos los agentes)

- Ramas `leonardo02lobo/<change>`; commits pequeños por grupo de tareas con la línea final
  `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.
- Permitido: `git push -u origin <rama-del-change>` y PRs en **draft**.
- Prohibido: push o force-push a `main`, mergear PRs, cambiar visibilidad, branch protection o secrets, reescribir
  historial publicado, `git stash` sin etiqueta, tocar worktrees o contenedores ajenos.
- Un cambio en un submódulo solo llega al principal por un PR (bump), nunca con un commit directo en `main`.

## Comandos clave

```bash
git clone --recurse-submodules https://github.com/leonardo02lobo/ProConnect.git && make bootstrap
make e2e-local            # stack Compose (pc-e2e, puertos +10000) + Playwright; ARGS="--workers=4"
make contracts-local      # contratos JSON contra docs/upgrade/contracts-boot4.json (stack levantado)
make ci-local             # ci.yml con act (typecheck e2e, actionlint, shellcheck, openspec)
make lint-workflows       # actionlint
make ai-triage            # diagnóstico con claude -p del último fallo E2E (ISSUES=1 abre issue en el repo hijo)
```

Java siempre en Docker (`maven:3.9-eclipse-temurin-17`, con tu UID); compose con el binario `docker-compose`.
Detalle de puertos por agente y toolchain en `orchestration/CONTRACT.md`.
