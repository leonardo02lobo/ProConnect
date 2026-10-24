## Why

ProConnect no tiene autenticación real: el login devuelve `token: null`, el frontend escribe por su cuenta una cookie `user=` y todas las APIs confían en el `userId` que manda el cliente. Además, el login devuelve el hash del password, las credenciales de MySQL están commiteadas y el servicio Spring mezcla servlet (MVC) y reactive (Gateway) en el mismo proceso. Hay que resolver estos riesgos antes de hacer cualquier otra mejora (E2E, limpieza, performance), porque todas dependen de un sistema de identidad confiable y de una topología de servicios estable.

## What Changes

- **BREAKING** Autenticación con Spring Security + JWT. `POST /api/auth/login` emite una cookie `httpOnly`, `Secure` y `SameSite`. **Toda** la API (`/api/**`, incluidas las lecturas de feed, posts y perfiles) y las imágenes (`/upload/**`) requieren sesión iniciada, salvo login, registro y logout. La identidad se toma del token, no del body.
- El frontend redirige a `/login` ante cualquier 401. Las páginas SSR (`post/[postId]`, `user/[userId]`) reenvían la cookie de sesión al backend.
- **BREAKING** Ninguna respuesta de la API incluye `password` (ni hash ni texto plano). Se elimina el campo de los DTOs de salida y del tipo `UserDTO` del frontend.
- **BREAKING** El API Gateway se separa en su propio módulo (`gateway/`, puerto 8080), que enruta `/api/**` → `backProconnect` (8081) y `/upload/**` → `NodeTsPosts` (3001). Se quita `spring-cloud-starter-gateway` y `webflux` de la API.
- Los secretos y la configuración (DB, JWT secret, puertos, orígenes CORS, URL base del frontend) se leen de variables de entorno. Se agregan `.env.example` por servicio y se eliminan las contraseñas del código.
- CORS deja de usar `*` y pasa a una lista explícita de orígenes, compatible con `credentials`.
- El frontend usa una única variable `PUBLIC_API_URL` y `credentials: "include"`. Desaparecen las 15 URLs `localhost:8080` fijas y la cookie `user=` escrita por el cliente.
- Higiene del repo: se dejan de versionar `node_modules/` y `NodeTsPosts/upload/` y se agrega un `.gitignore` raíz.
- Los puertos se resuelven: `NodeTsPosts` pasa a 3001, configurable. El legacy `BackendProConnect` queda fuera del arranque por defecto (su retiro se hace en `cleanup-codebase`).

## Capabilities

### New Capabilities
- `user-auth`: login/registro, emisión y validación de JWT en cookie, logout, endpoint `me`, protección de endpoints y no exposición de credenciales.
- `api-gateway`: punto de entrada único que enruta a la API core y al servicio de media, con CORS centralizado.
- `runtime-config`: configuración y secretos externalizados por variables de entorno en todos los servicios, más reglas sobre qué se versiona en el repo.

### Modified Capabilities
<!-- No existen specs previas en openspec/specs/. -->

## Impact

- **backProconnect**: `pom.xml` (sale gateway/webflux, entran `spring-boot-starter-security` y `jjwt`), `SecurityConfig`, `AuthController`, `AuthService`, todos los controllers que reciben `userId` en body/path para mutaciones, DTOs (`UserDto`, `JwtResponse`, `LoginResponseDto`), `application.properties`/`application.yml`.
- **Nuevo módulo** `gateway/` (Spring Cloud Gateway).
- **NodeTsPosts**: puerto y directorio de uploads por env.
- **FrontedProConnect**: `Service/*`, `GetCookie.ts` (se reemplaza por `GET /api/auth/me`), `Header.tsx`, `SignUp.astro`, `Post.tsx`, `AddPost.tsx`, `HeaderProfile.tsx`, `types/Dtos.ts`, páginas dinámicas.
- **Repo**: `.gitignore`, se eliminan ~3.900 archivos versionados de `node_modules` y las imágenes subidas.
- **Operación**: hay que rotar la contraseña de MySQL y el app password de Gmail (escrito en `BackendProConnect/src/settings/configurateEmail.ts`), porque los dos ya están en el historial de git.
- Roles válidos del sistema: `USER`, `COMPANY_ADMIN`, `ADMIN`. Se incluyen en el JWT.
- Desbloquea `add-e2e-ai-ci` y `verify-web-security`.
