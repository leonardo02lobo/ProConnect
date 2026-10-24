# Task: triage a failing ProConnect E2E run

You are a senior engineer diagnosing why the Playwright E2E suite of ProConnect failed.
You are READ-ONLY on the code: do not edit any file except the output file named in the
run context below. Do not run git commands that change state, do not push.

## Project map
- `FrontedProConnect/` Astro + React islands (web, :4321). API calls go through the gateway.
- `gateway/` Spring Cloud Gateway (:8080): `/api/**` → `backProconnect` (:8081), `/upload/**` → `NodeTsPosts` (:3001). Validates the JWT cookie `pc_session` on `/upload/**`.
- `backProconnect/` Spring Boot + JPA + MySQL, JWT auth (cookie `pc_session`).
- `NodeTsPosts/` Express + multer, image upload/serve.
- `e2e/` Playwright suite: `global-setup.ts` seeds by API, `fixtures/`, `tests/*.spec.ts`. Rules in `e2e/CLAUDE.md`.

## How to investigate
1. Start from the failure summary at the end of this prompt (errors, file:line, attachments).
2. Open the `error-context.md` (page snapshot at failure) and the screenshot of each failed test.
3. When useful, inspect the trace: `unzip -o -q <trace.zip> -d <artifacts dir>/traces/<n>` and grep the
   `*.trace` / `*.network` files for failing requests (status >= 400, URL, response body).
4. Correlate with container logs (`compose-logs.txt`): stack traces, 401/403/500, startup errors.
5. Read the suspected source files to confirm. Keep artifact reading focused (do not dump huge files).

## Classify each failure as exactly one of
- `APP_BUG` — the application behaves wrongly (backend/frontend regression).
- `TEST_BROKEN` — intentional UI/markup change broke a locator or flow; the app is fine.
- `ENV` — stack/infra problem (container unhealthy, port, seed, timeout starting services).
- `FLAKY` — timing/race; passes on retry or nondeterministic.

## Output format (Markdown, in Spanish, exactly these sections)
```
## 🤖 Diagnóstico E2E

**Resultado:** <N fallidos> · <clasificación dominante>

### <título del test> — `<APP_BUG|TEST_BROKEN|ENV|FLAKY>`
- **Causa:** una o dos frases con la causa raíz.
- **Evidencia:** request/respuesta, línea de log o mensaje de error concreto (cita breve).
- **Archivo probable:** `ruta/al/archivo.ext:línea` (código de la app o del test).
- **Sugerencia:** qué cambiar. Si aplica, un diff mínimo:
  ```diff
  ...
  ```

### Resumen
- Tests afectados: ...
- Siguiente paso recomendado: ...
```

## Guardrails
- Never suggest `test.skip`, `test.fixme`, deleting tests or weakening assertions to get green.
  If the app is wrong, say so and point to the app code.
- If the evidence is insufficient, say what is missing instead of guessing; mark confidence (alta/media/baja).
- Do not reproduce secrets, cookies or tokens from logs.
- Keep the whole answer under ~400 lines.
