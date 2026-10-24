## 1. Contenerización del stack

- [x] 1.1 Dockerfile multi-stage para `backProconnect` (Maven → JRE 17) con healthcheck en `/actuator/health` (agregar `spring-boot-starter-actuator`)
- [x] 1.2 Dockerfile multi-stage para `gateway`
- [x] 1.3 Dockerfile para `NodeTsPosts` con `UPLOAD_DIR` en volumen
- [x] 1.4 Dockerfile para `FrontedProConnect` (build con `PUBLIC_API_URL` como build-arg + `astro preview` o adapter Node)
- [x] 1.5 Crear `docker-compose.e2e.yml` con healthchecks y `depends_on: condition: service_healthy`
- [x] 1.6 Verificar `docker compose -f docker-compose.e2e.yml up -d --wait` en local

## 2. Proyecto Playwright

- [x] 2.1 Inicializar `e2e/` (`npm init playwright@latest`, TypeScript, solo Chromium por defecto)
- [x] 2.2 Configurar `playwright.config.ts` (baseURL desde env, `trace`/`video`/`screenshot` retain-on-failure, `retries` en CI, reporter html + github)
- [x] 2.3 Implementar `global-setup.ts`: seed por API (usuarios A y B, post con like y comentario) y `storageState` por usuario
- [x] 2.4 Crear fixtures: usuarios, cliente API y helpers de datos únicos por worker
- [x] 2.5 Agregar `e2e/CLAUDE.md` con convenciones de selectores y guardarraíles (sin skip, sin relajar expects)

## 3. Selectores en el frontend

- [x] 3.1 Agregar `data-testid` y labels accesibles en los formularios de login y registro
- [x] 3.2 Agregar `data-testid` en `Header` (menú de usuario, logout), `Post` (card, like, contador, input y submit de comentario), `AddPost` y `HeaderProfile`

## 4. Tests de flujos críticos

- [x] 4.1 `auth.spec.ts`: registro, login válido, login inválido, logout, redirección sin sesión desde `/`, `/Profile` y `/user/{id}`
- [x] 4.2 `feed.spec.ts`: el feed carga los posts del seed con autor, foto y contadores
- [x] 4.3 `post.spec.ts`: crear post con imagen y verificar que aparece en el feed y en el perfil
- [x] 4.4 `like.spec.ts`: like/unlike actualiza el contador y persiste tras recargar
- [x] 4.5 `comment.spec.ts`: comentar y ver el comentario con su autor
- [x] 4.6 `profile.spec.ts`: perfil propio y perfil ajeno (`/user/[id]`)
- [x] 4.7 Ejecutar la suite 2 veces con `--workers=4` sin reiniciar el stack y confirmar 0 colisiones

## 5. GitHub Actions

- [x] 5.1 Eliminar `FrontedProConnect/.github/workflows/jekyll-gh-pages.yml`
- [x] 5.2 Crear `.github/workflows/ci.yml` (matrix Java/Node, caches, build y test por servicio)
- [x] 5.3 Agregar un test mínimo de arranque a `backProconnect` y `gateway`, y `astro check` al frontend, para que `ci` valide algo real
- [x] 5.4 Crear `.github/workflows/e2e.yml` (Compose con cache de capas, Playwright, artifacts `if: always()`, logs filtrados de secretos)
- [x] 5.5 Configurar secrets de test (`E2E_JWT_SECRET`, `E2E_DB_PASSWORD`) o generarlos dentro del job

## 6. IA en local

- [x] 6.1 Ejecutar `npx playwright init-agents --loop=claude` y commitear las definiciones de agentes generadas
- [ ] 6.2 Generar con los agentes (en local) un test adicional (p. ej. búsqueda de usuarios) como prueba del flujo, y revisarlo a mano
- [x] 6.3 Crear `e2e/prompts/triage.md` y `e2e/scripts/ai-triage.sh` (`claude -p` sobre reporte, trazas y logs → `e2e/ai-triage.md`)
- [x] 6.4 Crear `e2e/prompts/heal.md` y `e2e/scripts/ai-heal.sh` (healer sobre los tests fallidos, sin skip ni expects relajados)
- [x] 6.5 Provocar un fallo intencional en local, ejecutar `ai-triage.sh` y validar que el diagnóstico es correcto
- [ ] 6.6 Romper un selector en local, ejecutar `ai-heal.sh` y validar que lo repara sin debilitar aserciones
- [x] 6.7 Documentar en `e2e/README.md` el flujo local (planner → generator → healer, triage y heal)

## 7. Workflows en local con act

- [x] 7.1 Instalar `act` (`gh extension install nektos/gh-act`) y crear `Makefile` con `e2e-local` y `ci-local`
- [x] 7.2 Extraer los pasos de E2E a `e2e/scripts/run-e2e.sh` (lo usan `make e2e-local` y `e2e.yml`)
- [ ] 7.3 Ejecutar `make ci-local` hasta verde
- [ ] 7.4 Ejecutar `e2e.yml` con act (o `run-e2e.sh` en el host si DinD no es viable) hasta verde
- [x] 7.5 Crear `ai-e2e-triage.yml` y `ai-e2e-heal.yml` reutilizando `e2e/prompts/*` con `anthropics/claude-code-action@v1` y permisos mínimos, y validar su sintaxis con `act -n` / `actionlint`

## 8. Subida a GitHub Actions

- [ ] 8.1 Push de la rama del change y apertura de un PR en draft hacia `main`
- [ ] 8.2 Verificar `ci` y `e2e` en verde en GitHub Actions
- [ ] 8.3 (Usuario) configurar el secret `ANTHROPIC_API_KEY` o `CLAUDE_CODE_OAUTH_TOKEN`
- [ ] 8.4 Provocar un fallo intencional en un commit temporal del PR, verificar que el bot comenta el diagnóstico y revertir el commit
- [ ] 8.5 (Usuario) activar branch protection en `main` con `ci` y `e2e` como checks requeridos
- [ ] 8.6 Documentar en el README raíz cómo correr todo en local y en CI
