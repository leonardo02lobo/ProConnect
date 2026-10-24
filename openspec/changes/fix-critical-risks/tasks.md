## 1. Higiene del repositorio

- [ ] 1.1 Crear `.gitignore` raíz (`node_modules/`, `dist/`, `target/`, `.env`, `NodeTsPosts/upload/*` con `!.gitkeep`)
- [ ] 1.2 `git rm -r --cached` de todos los `node_modules/` y de `NodeTsPosts/upload/*`, y agregar `NodeTsPosts/upload/.gitkeep`
- [ ] 1.3 Verificar que `git ls-files | grep -E 'node_modules/|/upload/'` solo devuelve `.gitkeep`

## 2. Configuración por entorno

- [ ] 2.1 Unificar la config de la API en `application.yml` con `${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}`, `${JWT_SECRET}`, `${JWT_TTL:PT24H}`, `${SERVER_PORT:8081}`, `${CORS_ORIGINS}` y eliminar `application.properties`
- [ ] 2.2 `NodeTsPosts`: leer `PORT` (default 3001) y `UPLOAD_DIR`
- [ ] 2.3 Crear `.env.example` en `backProconnect/`, `gateway/`, `NodeTsPosts/` y `FrontedProConnect/`
- [ ] 2.4 Rotar la contraseña de MySQL de la instancia de desarrollo y el app password de Gmail (hoy escrito en el código legacy), y documentarlo en el README raíz (la rotación la hace el usuario; el agente solo deja la checklist)

## 3. Gateway separado

- [ ] 3.1 Crear el módulo `gateway/` (Spring Boot 3.3, Java 17, `spring-cloud-starter-gateway`) con su Maven wrapper
- [ ] 3.2 Configurar rutas `/api/**` → `${API_URI}` (sin strip) y `/upload/**` → `${MEDIA_URI}` con `StripPrefix=1`, en puerto `${PORT:8080}`
- [ ] 3.3 Configurar `globalcors` con `allowedOrigins` desde `${CORS_ORIGINS}` y `allowCredentials: true`
- [ ] 3.4 Quitar `spring-cloud-starter-gateway`, `spring-boot-starter-webflux` y `web-application-type: reactive` de `backProconnect`
- [ ] 3.5 `GlobalFilter` JWT en el gateway para todo `/upload/**` (401 sin cookie válida)
- [ ] 3.6 Verificar con curl que `GET :8080/api/posts` y `GET :8080/upload/uploads/<file>` responden vía gateway

## 4. Autenticación JWT

- [ ] 4.1 Agregar `spring-boot-starter-security` y `jjwt` (api, impl, jackson) al `pom.xml`
- [ ] 4.2 Implementar `JwtService` (firmar y validar, HS256, el arranque falla si `JWT_SECRET` falta o tiene menos de 32 bytes)
- [ ] 4.3 Implementar `JwtAuthFilter`, que lee la cookie `pc_session` y llena el `SecurityContext` con `userId` y `role`
- [ ] 4.4 Configurar `SecurityFilterChain` stateless: `permitAll` solo en login/register/logout y `/actuator/health`, `authenticated` en todo lo demás (lecturas incluidas), 401 JSON en entry point
- [ ] 4.4b Enum `Role { USER, COMPANY_ADMIN, ADMIN }`, `USER` forzado en el registro y rol en los claims del JWT
- [ ] 4.5 Reescribir `AuthController.login` para emitir la cookie (`HttpOnly`, `SameSite=Lax`, `Secure` según `COOKIE_SECURE`) y devolver `UserPublicDto`
- [ ] 4.6 Agregar `GET /api/auth/me` y `POST /api/auth/logout`
- [ ] 4.7 Registro: validar email, unicidad (409) y password ≥ 8; responder 201
- [ ] 4.8 Corregir `UserService.UpdateUserById` para hashear el password nuevo
- [ ] 4.9 Quitar `@CrossOrigin(origins = "*")` de los controllers (CORS lo maneja el gateway)

## 5. Actor desde el token y DTOs sin password

- [ ] 5.1 Crear el helper `CurrentUser` / usar `@AuthenticationPrincipal` para obtener el `userId` autenticado
- [ ] 5.2 Usar el actor del token en las mutaciones de Posts, Comments, Likes, Friends, Messages, Postulations, ProfilePhotos y WorkStation, sobrescribiendo el `userId` del body
- [ ] 5.3 Crear `UserPublicDto` y reemplazar los usos de `UserDto` en respuestas (`AuthController`, `UserService`, `PostsService`, `CommentsService`, `UserProfileDTO`)
- [ ] 5.4 Marcar `password` como `@JsonProperty(access = WRITE_ONLY)` en `UserDto`
- [ ] 5.5 Verificar con curl que ninguna respuesta de `/api/user*`, `/api/posts*` ni `/api/comment*` contiene `"password"`

## 6. Frontend

- [ ] 6.1 Crear `src/Service/http.ts` con `apiFetch()` (base `import.meta.env.PUBLIC_API_URL`, `credentials: "include"`) y `mediaUrl()`
- [ ] 6.2 Migrar `AuthApi`, `PostApi`, `LikeApi`, `Post.tsx`, `AddPost.tsx`, `HeaderProfile.tsx`, `post/[postId].astro` y `user/[userId].astro` a `apiFetch` / `mediaUrl`
- [ ] 6.3 Reemplazar `getUserFromCookie` por un store (nanostores) cargado desde `GET /api/auth/me`, y dejar de escribir la cookie `user=`
- [ ] 6.4 Hacer que el logout del `Header` llame a `POST /api/auth/logout`
- [ ] 6.5 Quitar `password` de `UserDTO` y `UserProfileDTO` en `types/Dtos.ts`
- [ ] 6.6 Crear `src/middleware.ts` que redirige a `/login` sin cookie `pc_session`, y hacer que `apiFetch` redirija ante un 401
- [ ] 6.7 Reenviar la cookie del request en el `fetch` SSR de `post/[postId].astro` y `user/[userId].astro`
- [ ] 6.8 Verificar que `grep -r "localhost:8080" FrontedProConnect/src` está vacío

## 7. Verificación manual del flujo

- [ ] 7.1 Levantar el stack (MySQL, API :8081, media :3001, gateway :8080, front :4321) sin conflictos de puerto
- [ ] 7.2 Recorrer registro → login → feed → like → comentar → subir post con imagen → perfil → logout
- [ ] 7.3 Confirmar 401 sin cookie en `POST /api/posts/create`, `GET /api/posts`, el perfil y `GET /upload/uploads/<file>`, y que un `userId` ajeno en el body se ignora
