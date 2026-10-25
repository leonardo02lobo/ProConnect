## Why

ProConnect no tiene tests: solo el `contextLoads` que genera Spring por defecto. Tampoco hay pipeline que valide los PRs. Los próximos changes (`cleanup-codebase`, `restructure-models-performance`) tocan la mayor parte del backend y del frontend, así que hace falta una red de seguridad E2E que verifique los flujos de usuario reales antes de refactorizar. Queremos además apoyarnos en IA para generar y mantener esos tests (planner/generator/healer) y para diagnosticar fallos en CI, de modo que la suite crezca al ritmo del producto sin un costo manual alto.

## What Changes

- Nuevo proyecto `e2e/` con **Playwright** (TypeScript) que cubre los flujos críticos: registro, login/logout, feed, crear post con imagen, like/unlike, comentar, ver perfil propio y ajeno, y acceso denegado sin sesión.
- Stack reproducible para tests con **Docker Compose** (`docker-compose.e2e.yml`): MySQL, API core, gateway, servicio de media y frontend compilado, más datos semilla deterministas.
- **Agentes de test con IA**: se configuran los Playwright Test Agents (planner, generator, healer) para Claude Code. Planes en `e2e/specs/*.md` y tests generados en `e2e/tests/`, siempre revisados por una persona antes del merge.
- **Local primero, luego CI**: cada pieza (stack, E2E, workflows, triage y healing con IA) se ejecuta y valida en local antes de subirla.
  - `make e2e-local` levanta el stack y corre Playwright.
  - `make ci-local` ejecuta los workflows con `act`.
  - `e2e/scripts/ai-triage.sh` corre el mismo triage en local con `claude -p` sobre los artifacts de un fallo.
  - Los workflows de GitHub reutilizan exactamente esos scripts y prompts, así lo que pasa en local es lo que pasa en CI.
  - Solo después se hace push de la rama del change y se verifica la ejecución real en GitHub Actions.
- **GitHub Actions**:
  - `ci.yml`: build y tests unitarios de cada servicio (Maven, npm) en cada PR y push a `main`.
  - `e2e.yml`: levanta el stack con Compose, corre Playwright y publica el reporte HTML, las trazas y los videos como artifacts.
  - `ai-e2e-triage.yml`: si E2E falla en un PR, se invoca `anthropics/claude-code-action`, que analiza trazas y logs y comenta en el PR un diagnóstico y, si aplica, un parche propuesto. Nunca hace push directo.
  - `ai-e2e-heal.yml` (manual, `workflow_dispatch`): ejecuta el agente healer sobre tests rotos por cambios de UI y abre un PR con las correcciones.
- Atributos `data-testid` estables en los componentes del frontend que usan los tests.

## Capabilities

### New Capabilities
- `e2e-testing`: suite E2E de Playwright sobre el stack completo, con datos semilla y selectores estables.
- `ci-pipeline`: workflows de GitHub Actions para build, tests y E2E, con gates sobre PRs.
- `ai-test-assistance`: generación, reparación y triage de tests E2E asistidos por IA, con revisión humana obligatoria.

### Modified Capabilities
<!-- Sin specs previas. -->

## Impact

- **Nuevo**: `e2e/` (Playwright, fixtures, seeds), `docker-compose.e2e.yml`, Dockerfiles en `backProconnect/`, `gateway/`, `NodeTsPosts/` y `FrontedProConnect/`, y `.github/workflows/*`.
- **Se mueve**: `FrontedProConnect/.github/workflows/jekyll-gh-pages.yml` (no aplica a Astro, porque Actions solo lee `.github/` en la raíz) se elimina o se reemplaza.
- **Frontend**: agregar `data-testid` en `Header`, `Post`, `AddPost`, `HeaderProfile` y en los formularios de auth.
- **Secrets de GitHub**: `ANTHROPIC_API_KEY` (o login OAuth de la Claude GitHub App) para los workflows de IA.
- **Depende de** `fix-critical-risks`: auth real, puertos y configuración por entorno.
