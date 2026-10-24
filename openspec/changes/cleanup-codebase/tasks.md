## 1. Tooling de calidad

- [ ] 1.1 Agregar `.editorconfig` raíz
- [ ] 1.2 Configurar `spotless-maven-plugin` y `maven-checkstyle-plugin` (reglas de nombres) en `backProconnect` y `gateway`
- [ ] 1.3 Configurar ESLint (flat config, typescript-eslint, astro, react-hooks, `no-console`) y Prettier en `FrontedProConnect` y `NodeTsPosts`
- [ ] 1.4 Aplicar el formateo automático en un commit aislado y registrarlo en `.git-blame-ignore-revs`
- [ ] 1.5 Añadir `spotless:check`, `checkstyle:check` y `npm run lint` al workflow `ci`

## 2. Manejo de errores

- [ ] 2.1 Crear el paquete `exception/` con `NotFoundException`, `ConflictException`, `ValidationException` y `ForbiddenException`
- [ ] 2.2 Implementar `GlobalExceptionHandler` (`@RestControllerAdvice`) con `ProblemDetail` y mapeo de `MethodArgumentNotValidException` a `errors[]`
- [ ] 2.3 Agregar el filtro de `traceId` (MDC) y devolverlo en las respuestas de error
- [ ] 2.4 Agregar Bean Validation (`@NotBlank`, `@Size`, `@Email`) y `@Valid` en todos los DTOs de entrada

## 3. Refactor por recurso (backend + frontend + E2E verdes en cada paso)

- [ ] 3.1 Posts: rutas REST, `getFeed(Optional<Long>)` unificado, proyección `FeedRow`, services sin `String`/`catch` genérico, 201 + `Location`
- [ ] 3.2 Likes: `POST`/`DELETE /api/posts/{postId}/likes` sin body, 409 en duplicado
- [ ] 3.3 Comments: `/api/posts/{postId}/comments`, corregir el NPE de `convertToDto`, proyección tipada
- [ ] 3.4 Users/Profile: `/api/users`, `/api/users/{id}/profile` con proyección tipada, sin `Object[]`
- [ ] 3.5 Friends: `/api/friends` y `/api/friends/requests/{id}/accept|reject`
- [ ] 3.6 Messages, Companies, Jobs, Postulations, WorkStations, ProfilePhotos: rutas REST y services sin `String`
- [ ] 3.7 Renombrar métodos a camelCase y campos `Id` → `id` en entidades y DTOs, y eliminar los `Long.valueOf(Id) == null`
- [ ] 3.8 Generar `docs/api-routes.md` con la tabla antes/después

## 4. Frontend

- [ ] 4.1 Crear servicios por recurso (`posts.ts`, `comments.ts`, `likes.ts`, `users.ts`, `auth.ts`) sobre `apiFetch`
- [ ] 4.2 Quitar los `fetch` directos de `Post.tsx`, `AddPost.tsx` y `HeaderProfile.tsx`
- [ ] 4.3 Like y comentario con update optimista y rollback, sin `window.location.reload()`
- [ ] 4.4 Crear `/login` y `/register` (con formulario de registro real), eliminar `SignIn.astro` y `SignUp.astro`, y actualizar redirecciones y links
- [ ] 4.5 Unificar tipos en inglés (`totalComments`) alineados con los DTOs del backend
- [ ] 4.6 Eliminar todos los `console.log`
- [ ] 4.7 Arreglar el script `dev` de `NodeTsPosts` (`tsx watch index.ts`)

## 5. Correos de postulación y retiro del legacy

- [ ] 5.1 Agregar `PostulationStatus` y `Postulation.status` (default `PENDING`)
- [ ] 5.2 Endpoints `POST /api/postulations/{id}/accept|reject` con check de dueño de empresa o ADMIN (403) y 409 si ya está resuelta
- [ ] 5.3 Agregar `spring-boot-starter-mail` y `spring-retry`, y configurar SMTP por env (`MAIL_*`)
- [ ] 5.4 Plantillas `accepted.txt` y `rejected.txt` copiadas literal del legacy, y `ApplicationMailService`
- [ ] 5.5 `PostulationResolvedEvent` + `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` + `@Retryable` (3 intentos)
- [ ] 5.6 Agregar Mailpit al compose de dev y al de E2E
- [ ] 5.7 Tests: unitario de plantillas, integración (GreenMail o Mailpit) y un fallo de SMTP que no revierte el estado
- [ ] 5.8 E2E: el dueño acepta una postulación y Mailpit recibe el correo en el email del postulante
- [ ] 5.9 Inventariar en `docs/backlog-legacy.md` el resto de funcionalidad exclusiva de `BackendProConnect`
- [ ] 5.10 Crear el tag `legacy-express-backend` y eliminar `BackendProConnect/`

## 6. Verificación

- [ ] 6.1 Actualizar los tests E2E a las rutas `/login` y `/register` y a los endpoints nuevos
- [ ] 6.2 Confirmar `ci` (lint, format, build) y `e2e` en verde
- [ ] 6.3 Verificar con curl: 404 en un post inexistente, 400 con `errors[]`, 409 en like duplicado y 500 sin stack trace
