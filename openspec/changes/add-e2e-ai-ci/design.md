## Context

- No hay tests ni CI. Hay un workflow `jekyll-gh-pages.yml` dentro de `FrontedProConnect/.github/`, que GitHub ignora porque no está en la raíz.
- Después de `fix-critical-risks` el stack queda en 4 servicios más MySQL, con configuración por entorno y auth por cookie, lo que permite levantarlo en CI.
- El frontend es Astro + islas React (`client:load`, `client:only`), así que los tests tienen que esperar a la hidratación.

## Goals / Non-Goals

**Goals:**
- Una suite E2E confiable y rápida (< 5 min en CI) para los flujos que ya existen.
- Que la IA reduzca el costo de escribir y mantener tests, sin quitar el control humano.
- Pipeline de PR con gates claros.

**Non-Goals:**
- Tests unitarios exhaustivos de backend: se agregan los mínimos para que `ci` tenga sentido. La cobertura unitaria crece en los changes siguientes.
- Tests de carga y de performance (`restructure-models-performance`).
- Escaneos de seguridad (`verify-web-security`, que reutiliza este pipeline).
- Deploy continuo.

## Decisions

### D1. Playwright como framework E2E
- Trae soporte nativo de trazas, video, `storageState` y paralelismo, y es el único con agentes de test oficiales (planner/generator/healer) integrables con Claude Code mediante `npx playwright init-agents --loop=claude`.
- **Alternativas**: Cypress (sin multi-tab, paralelismo de pago) y Selenium (más verboso, sin agentes).

### D2. Stack E2E con Docker Compose
```
docker-compose.e2e.yml
 ├─ mysql:8      (healthcheck mysqladmin ping, seed vía /docker-entrypoint-initdb.d o job seed)
 ├─ api          (build backProconnect, depends_on mysql healthy)
 ├─ media        (build NodeTsPosts, volumen tmpfs para uploads)
 ├─ gateway      (build gateway, depends_on api+media)
 └─ web          (astro build + preview, PUBLIC_API_URL=http://localhost:8080)
```
- Dockerfiles multi-stage: Maven → JRE 17 y Node 22 → runtime.
- **Seed**: se hace por API (registro y creación de posts desde un `globalSetup` de Playwright) en vez de SQL crudo. Así sobrevive a los cambios de esquema de `restructure-models-performance` y además ejercita los endpoints.
- **Alternativa**: Testcontainers desde Playwright. Es más complejo y añade poco valor aquí.

### D3. Estructura de `e2e/`
```
e2e/
 ├─ playwright.config.ts   (baseURL web, trace: 'retain-on-failure', video: 'retain-on-failure', retries: CI?2:0)
 ├─ global-setup.ts        (seed + login → .auth/<user>.json)
 ├─ fixtures/              (users, api client, page objects mínimos)
 ├─ specs/                 (planes Markdown — entrada/salida de agentes IA)
 ├─ tests/                 (auth, feed, post, like, comment, profile)
 └─ .github/ (no) — los agentes viven en .claude/agents/ generados por init-agents
```

### D4. IA en tres niveles
| Nivel | Cuándo | Herramienta | Escribe en repo |
|---|---|---|---|
| Autoría | Local, a demanda | Playwright Test Agents + Claude Code | Sí, vía PR humano |
| Triage | `e2e` falla en un PR | `anthropics/claude-code-action@v1` con prompt de triage y artifacts descargados | No (solo comenta) |
| Healing | `workflow_dispatch` | Claude Code action + agente healer | Abre PR separado |

- **Local primero**: la lógica de IA vive en `e2e/scripts/ai-triage.sh` / `ai-heal.sh` + `e2e/prompts/*.md`. En local se ejecuta con `claude -p --allowedTools ... < prompt` (auth de Claude Code del desarrollador). En CI, `claude-code-action` recibe `prompt_file: e2e/prompts/triage.md`. Mismo prompt, mismo formato de salida.
- **Workflows en local con `act`**: se instala con `gh extension install nektos/gh-act`. `make ci-local` → `gh act pull_request -W .github/workflows/ci.yml`. El E2E con `act` necesita Docker-in-Docker; si no funciona de forma fiable, `make e2e-local` ejecuta los mismos pasos del job directamente en el host (mismo script `e2e/scripts/run-e2e.sh`, que también llama el workflow).
- **Orden**: local verde → push de la rama del change → PR en draft → Actions verde. Los secrets de GitHub (`ANTHROPIC_API_KEY` o `CLAUDE_CODE_OAUTH_TOKEN` generado con `claude setup-token`) los configura el usuario.
- El triage se dispara con `workflow_run` sobre `e2e` con `conclusion == failure` y descarga los artifacts (reporte, trazas, logs de Compose). El prompt obliga a seguir el formato: causa → evidencia → archivo → sugerencia.
- Guardarraíles en el prompt y en `CLAUDE.md` de `e2e/`: prohibido `test.skip` y `fixme` y prohibido relajar `expect`. Ante un bug real, se reporta.
- Permisos mínimos por job (`permissions:` explícitos). La autenticación es con el secret `ANTHROPIC_API_KEY` o con la Claude GitHub App.

### D5. Workflows
- `ci.yml`: matrix por servicio (`java` con `setup-java` + cache Maven, `node` con `setup-node` + cache npm). Tests mínimos: `contextLoads` con H2 o Testcontainers MySQL, y `astro check`.
- `e2e.yml`: `docker compose up --wait`, `npx playwright install --with-deps chromium`, `npx playwright test`, `upload-artifact` (`if: always()`), `docker compose logs > logs.txt` si falla.
- Solo Chromium en PR. Firefox/WebKit van en un cron nocturno opcional.

### D6. `data-testid`
- Convención `kebab-case` por componente: `post-card`, `post-like-button`, `post-comment-input`, `add-post-submit`, `header-user-menu`, `login-email`, etc.
- Se priorizan `getByRole`/`getByLabel`. `data-testid` queda para lo que no tiene rol accesible claro. De paso esto mejora la accesibilidad, porque hoy los inputs de SignUp no tienen `for` y `id` coherentes.

## Risks / Trade-offs

- [Tests flaky por hidratación de las islas React] → Se espera a elementos interactivos (`toBeEnabled`), nunca `waitForTimeout`. `retries: 2` solo en CI y los flaky se reportan en el resumen.
- [Costo de tokens de los workflows IA] → El triage solo corre ante fallos. Healing es manual. Se limita `max_turns` y los artifacts que se pasan al modelo.
- [La IA propone cambios incorrectos] → Nunca hace push directo a la rama del PR, el healing va en un PR separado y siempre hay revisión humana.
- [Tiempo de build de Docker en CI] → Cache de capas con `docker/build-push-action` + `cache-from: type=gha`.
- [Exposición de secretos en logs que lee la IA] → Los logs de Compose se filtran (`JWT_SECRET`, `DB_PASSWORD`) antes de subirlos como artifact.

## Migration Plan

1. Dockerfiles y Compose, levantados en local.
2. `e2e/` con 2 tests (login y feed) más CI básico.
3. Completar flujos y `data-testid`.
4. Workflows de IA (triage y luego heal).
5. Activar branch protection en `main`.

## Resolved Decisions

- IA: primero en local con Claude Code (scripts y prompts versionados), luego los mismos prompts en GitHub Actions. El secret de CI lo configura el usuario.
- Toda la app requiere login: el `globalSetup` siempre autentica y los tests sin sesión solo verifican redirecciones y 401.
- El workflow de GitHub Pages (Jekyll) se elimina: no aplica a Astro SSR.
