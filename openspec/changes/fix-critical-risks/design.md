## Context

Estado actual:

```
Frontend (Astro :4321) ──► backProconnect :8080
                            ├─ @RestController /api/*  (MVC + JPA, bloqueante)
                            └─ Spring Cloud Gateway /upload/** ──► NodeTsPosts :3000
                               (web-application-type: reactive)
BackendProConnect (Express :3000, legacy) ── conflicto de puerto con NodeTsPosts
```

- Solo hay `spring-security-crypto` para BCrypt. No hay filtros de seguridad.
- `AuthController.login` devuelve `JwtResponse(null, UserDto(..., password, ...))`.
- El frontend guarda en `cookieStore.set("user", ...)` el JSON del usuario y lo lee con `getUserFromCookie()` para mandar `userId` en cada mutación.
- `application.properties` contiene la contraseña de MySQL. `node_modules` (~3.900 archivos) y las imágenes subidas están versionados.

## Goals / Non-Goals

**Goals:**
- Que la identidad del usuario sea verificable en el servidor (JWT) y que la autoría de cada mutación salga del token.
- Eliminar la exposición de hashes de password.
- Una topología de servicios coherente, sin mezclar MVC y WebFlux y sin choques de puerto.
- Configuración por entorno, lista para CI (E2E) y despliegue.

**Non-Goals:**
- Autorización fina por recurso (p. ej. "solo el autor puede editar su post"): se verifica y se completa en `verify-web-security`. Aquí solo se garantiza que el actor sale del token.
- Refresh tokens, OAuth o MFA.
- Renombrar endpoints o limpiar estilo (`cleanup-codebase`).
- Cambios en el modelo de datos (`restructure-models-performance`).

## Decisions

### D1. Gateway en módulo propio
Topología objetivo:
```
Frontend :4321 ──► gateway :8080 ──┬─► /api/**    → backProconnect :8081 (MVC)
                                   └─► /upload/** → NodeTsPosts    :3001
```
- `gateway/` es un proyecto Spring Boot nuevo con solo `spring-cloud-starter-gateway`. Las rutas se definen en `application.yml` y las URIs salen de env (`API_URI`, `MEDIA_URI`). Para `/upload/**` se mantiene `StripPrefix=1`; `/api/**` no lleva strip.
- **Alternativa descartada**: quitar el gateway y que el frontend hable con dos orígenes. Duplica CORS, complica las cookies cross-origin y obliga a reescribir las URLs de imágenes.
- **Alternativa descartada**: dejar todo en un solo proceso con WebFlux y R2DBC. Obliga a reescribir toda la capa JPA.

### D2. JWT en cookie httpOnly, validado en la API core
- Dependencias: `spring-boot-starter-security` e `io.jsonwebtoken:jjwt-api/impl/jackson`.
- `SecurityFilterChain` stateless: `permitAll` **solo** en `POST /api/auth/login`, `/register` y `/logout` (y `/actuator/health` para healthchecks); `authenticated` en todo lo demás, incluidas las lecturas (la red es cerrada).
- Gateway: un `GlobalFilter` para `/upload/**` valida la firma y la expiración del JWT con el mismo `JWT_SECRET` (biblioteca jjwt), sin consultar la base de datos. Así las imágenes también quedan detrás de la sesión.
- Frontend: las páginas SSR (`post/[postId].astro`, `user/[userId].astro`) leen `Astro.request.headers.get("cookie")` y lo reenvían en el `fetch`. Un middleware de Astro (`src/middleware.ts`) redirige a `/login` si no hay cookie `pc_session`, y una respuesta 401 del backend también redirige.
- Un `JwtAuthFilter` lee la cookie `pc_session`, valida firma y expiración (HS256, `JWT_SECRET` ≥ 32 bytes) y carga el `principal` con `userId` y `role`.
- La cookie se emite como `HttpOnly; Path=/; SameSite=Lax; Secure` (`Secure` se desactiva solo con `COOKIE_SECURE=false` en dev) y `Max-Age` sale de `JWT_TTL`, por defecto 24 h.
- La validación se hace en la API y no en el gateway, porque la API es quien necesita el principal. El gateway solo enruta.
- **Alternativa descartada**: token en `localStorage` con header `Authorization`. Queda expuesto a XSS.
- **CSRF**: con `SameSite=Lax` y sin endpoints de mutación por `GET`, el riesgo baja. Los `GET` que mutan del legacy no existen en Spring. Se deja anotado para `verify-web-security`.

### D3. Actor desde el token
- Un helper `CurrentUser.id()` (o `@AuthenticationPrincipal`) se usa en los services de Posts, Comments, Likes, Friends, Messages, Postulations, ProfilePhotos y WorkStation.
- Los DTOs de entrada conservan `userId` por compatibilidad, pero el servicio lo sobrescribe. Se elimina en `cleanup-codebase`.

### D4. DTOs de salida sin password
- Se separan `UserDto` (entrada de registro/actualización, con `password` write-only) y `UserPublicDto` (salida). Hay que tocar todos los `new UserDto(..., password, ...)` en `AuthController`, `UserService`, `PostsService` y `CommentsService`.
- Como red de seguridad, se pone `@JsonProperty(access = WRITE_ONLY)` en `password`.

### D5. Configuración por entorno
- API: `application.yml` con `${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}`, `${JWT_SECRET}`, `${SERVER_PORT:8081}` y `${CORS_ORIGINS}`. Se elimina el `application.properties` duplicado.
- Media: `PORT` (3001) y `UPLOAD_DIR`.
- Frontend: `PUBLIC_API_URL` y un módulo `src/Service/http.ts` con `apiFetch()` que pone `credentials: "include"` y la base URL. Las URLs de imágenes salen de un helper `mediaUrl(path)`.
- `.env.example` por servicio, con valores de ejemplo y sin secretos reales.

### D6. Higiene de git
- `.gitignore` raíz: `node_modules/`, `dist/`, `target/`, `.env`, `NodeTsPosts/upload/*` (con `.gitkeep`).
- `git rm -r --cached` de `node_modules` y uploads. **No** se reescribe el historial: la contraseña filtrada se rota, en vez de usar `filter-repo`, para no romper clones ni worktrees.

## Risks / Trade-offs

- [Cambio breaking en el frontend: todas las llamadas cambian] → Se migra en el mismo change. `apiFetch` centraliza el cambio y se verifica a mano con el flujo login → feed → like → comentar → subir post.
- [Las cookies en dev son cross-port (4321 → 8080)] → `localhost` comparte cookies entre puertos y `SameSite=Lax` lo permite en el mismo site. Se documenta usar `localhost`, no `127.0.0.1`.
- [La contraseña de MySQL sigue en el historial] → Se rota en la instancia. Se deja anotado como hallazgo para `verify-web-security` (gitleaks sobre el historial).
- [Los usuarios existentes pueden tener passwords sin hashear (actualización vía `UpdateUserById` guarda texto plano)] → Se corrige `UpdateUserById` para hashear. Un script de una sola vez detecta filas sin prefijo `$2` y obliga a resetear el password.
- [El gateway duplica la validación de JWT para `/upload/**`] → Solo valida firma y expiración con la misma clave. La autorización fina sigue en la API.
- [Hay credenciales de Gmail en el código legacy] → Se rotan aquí. El código legacy se elimina en `cleanup-codebase`, que pasa los correos a Spring con SMTP por env.

## Migration Plan

1. Crear `gateway/`, mover la configuración de rutas y verificar el proxy con la API todavía sin seguridad.
2. Mover la API a 8081 y sacar sus dependencias de gateway y webflux.
3. Agregar Security y JWT. Probar con curl el login, `/me` y un 401 en una mutación.
4. Migrar el frontend a `apiFetch` y quitar la cookie cliente.
5. Higiene del repo y rotación de credenciales.
Rollback: cada paso va en un commit separado; para volver atrás se revierte la serie completa del change.

## Resolved Decisions

- Feed, posts y perfiles **requieren login**: la red es cerrada y no hay ningún `GET` público.
- Roles: solo `USER`, `COMPANY_ADMIN` y `ADMIN`. El registro siempre asigna `USER`. La aplicación de permisos por rol se hace en `verify-web-security`.
