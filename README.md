# ProConnect

Red profesional: frontend Astro + React, API core en Spring Boot, servicio de media en Express y un API Gateway en Spring Cloud Gateway.

## Topología

```
Navegador ──► web (Astro SSR) :4321
          └─► gateway :8080 ──┬─► /api/**    → backProconnect :8081 (Spring MVC + JPA + Security)
                              └─► /upload/** → NodeTsPosts    :3001 (Express + multer)
MySQL :3306 (DB `proconnect`)
```

| Servicio | Directorio | Puerto | Variables |
|---|---|---|---|
| gateway | `gateway/` | 8080 | `PORT`, `API_URI`, `MEDIA_URI`, `CORS_ORIGINS`, `JWT_SECRET` |
| API core | `backProconnect/` | 8081 | `SERVER_PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_TTL`, `COOKIE_SECURE`, `CORS_ORIGINS` |
| media | `NodeTsPosts/` | 3001 | `PORT`, `UPLOAD_DIR` |
| web | `FrontedProConnect/` | 4321 | `PUBLIC_API_URL` (build), `API_INTERNAL_URL` (runtime, opcional) |

Cada servicio tiene un `.env.example` con todas sus variables. Copia los valores a tu entorno (o a un `.env` local, que no se versiona). `BackendProConnect/` es el backend legacy: no forma parte del arranque por defecto y se retira en `cleanup-codebase`.

## Autenticación

- `POST /api/auth/login` valida email y password (BCrypt) y emite la cookie `pc_session` (JWT HS256, `HttpOnly; Path=/; SameSite=Lax; Secure`). `Secure` solo se desactiva con `COOKIE_SECURE=false` en desarrollo sobre http.
- Toda la API y todas las imágenes (`/upload/**`) requieren sesión, salvo `POST /api/auth/{login,register,logout}` y `GET /actuator/health`.
- `GET /api/auth/me` devuelve el usuario de la sesión y `POST /api/auth/logout` expira la cookie.
- El autor de cada mutación sale del token: los `userId` del body se ignoran.
- `JWT_SECRET` es obligatorio (32 bytes como mínimo) en la API y en el gateway, y tiene que ser el mismo en los dos. Si falta, ninguno de los dos arranca.

## Desarrollo local

Requisitos: Docker (`docker-compose`), Node 22, y Java 17 o Docker para compilar.

```bash
# 1. MySQL
docker-compose -p pc-risks -f docker-compose.dev.yml up -d

# 2. Variables comunes (ejemplo de desarrollo)
export JWT_SECRET="$(openssl rand -base64 48)" COOKIE_SECURE=false CORS_ORIGINS=http://localhost:4321
export DB_URL=jdbc:mysql://localhost:3306/proconnect DB_USER=proconnect DB_PASSWORD=proconnect-dev

# 3. API core (:8081) y gateway (:8080)
(cd backProconnect && ./mvnw spring-boot:run)
(cd gateway && ./mvnw spring-boot:run)

# 4. Media (:3001)
(cd NodeTsPosts && npm ci && npm start)

# 5. Web (:4321)
(cd FrontedProConnect && npm ci && PUBLIC_API_URL=http://localhost:8080 npm run dev)
#   producción: PUBLIC_API_URL=... npm run build && npm start
```

Abre `http://localhost:4321` usando `localhost`, no `127.0.0.1`, para que la cookie de sesión se comparta entre `:4321` y `:8080`.

Para compilar y testear sin Maven local: `docker run --rm -v "$PWD":/app -v "$HOME/.m2":/root/.m2 -w /app maven:3.9-eclipse-temurin-17 mvn -B verify`, desde `backProconnect/` o `gateway/`. Los tests de la API usan H2 en memoria y no necesitan MySQL.

## Tests E2E y CI (local primero, luego GitHub Actions)

Todo se ejecuta primero en local con los mismos scripts que usan los workflows. Detalle en [`e2e/README.md`](e2e/README.md).

```bash
make help              # todos los objetivos
make e2e-local         # levanta el stack pc-e2e (puertos +10000), siembra datos y corre Playwright
make e2e-test ARGS="tests/like.spec.ts --headed"   # repetir contra el stack ya levantado
make e2e-down          # parar el stack
make ci-local          # ci.yml con act (Maven verify, smoke de media, astro check + build, typecheck e2e)
make ci-local-e2e      # e2e.yml con act (usa el Docker del host)
make lint-workflows    # actionlint
make ai-triage         # diagnóstico con Claude Code (claude -p) del último fallo E2E
make ai-heal           # healer con Claude Code sobre los tests rotos (deja cambios para revisar)
```

Requisitos locales: Docker + `docker-compose`, Node 22, `gh` con la extensión `nektos/gh-act`, `actionlint` y el CLI de Claude Code para los objetivos de IA.

| Workflow | Cuándo | Qué hace |
|---|---|---|
| `ci` | cada PR (cualquier base) y push a `main` | build y tests por servicio |
| `e2e` | cada PR y push a `main` | stack Compose + Playwright; artifact `e2e-report` (reporte, trazas, vídeos, logs redactados) |
| `ai-e2e-triage` | cuando `e2e` falla en un PR | Claude comenta en el PR un diagnóstico (solo lectura, nunca hace push) |
| `ai-e2e-heal` | manual | healer + guardarraíles y abre un PR draft separado para revisión humana |

Configuración que hace el mantenedor: secret `ANTHROPIC_API_KEY` o `CLAUDE_CODE_OAUTH_TOKEN` (`claude setup-token`) para los workflows de IA; "Allow GitHub Actions to create and approve pull requests" para `ai-e2e-heal`; branch protection en `main` exigiendo `ci` y `e2e`.

## Checklist de rotación de credenciales (acción manual requerida)

Estos secretos estuvieron commiteados y **siguen en el historial de git**. El historial no se reescribe, para no romper clones ni worktrees, así que la única mitigación real es rotarlos:

- [ ] **MySQL**: cambiar la contraseña del usuario `root` de la instancia de desarrollo (antes estaba en `backProconnect/src/main/resources/application.properties` y como fallback en `BackendProConnect/src/models/database.ts`). Crear un usuario de aplicación con permisos solo sobre `proconnect` y usarlo vía `DB_USER`/`DB_PASSWORD`.
- [ ] **Gmail**: revocar el *app password* de la cuenta que aparecía en `BackendProConnect/src/settings/configurateEmail.ts` (Cuenta de Google → Seguridad → Contraseñas de aplicaciones) y generar uno nuevo solo si se sigue usando. Ahora se lee de `MAIL_USERNAME`/`MAIL_PASSWORD`.
- [ ] **JWT legacy**: el secreto por defecto de `BackendProConnect` (fallback hardcodeado, ya eliminado) no debe reutilizarse en ningún sitio. Generar `JWT_SECRET` nuevo con `openssl rand -base64 48`.
- [ ] Revisar el historial con `gitleaks` (se automatiza en `verify-web-security`).
- [ ] Usuarios con password sin hashear: la actualización antigua de usuario guardaba el password en texto plano. Esas cuentas ya no pueden iniciar sesión (el login solo acepta hashes BCrypt). Para detectarlas y obligar a resetear el password:

  ```sql
  SELECT id, email FROM proconnect.user WHERE password NOT LIKE '$2%';
  ```
