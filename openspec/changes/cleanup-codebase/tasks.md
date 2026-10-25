## 1. Tooling de calidad

- [x] 1.1 Agregar `.editorconfig` raíz
- [x] 1.2 Configurar `spotless-maven-plugin` y `maven-checkstyle-plugin` (reglas de nombres) en `backProconnect` y `gateway`
- [x] 1.3 Configurar ESLint (flat config, typescript-eslint, astro, react-hooks, `no-console`) y Prettier en `FrontedProConnect` y `NodeTsPosts`
- [x] 1.4 Aplicar el formateo automático en un commit aislado y registrarlo en `.git-blame-ignore-revs`
- [x] 1.5 Añadir `spotless:check`, `checkstyle:check` y `npm run lint` al workflow `ci`

## 2. Manejo de errores

- [x] 2.1 Crear el paquete `exception/` con `NotFoundException`, `ConflictException`, `ValidationException` y `ForbiddenException`
- [x] 2.2 Implementar `GlobalExceptionHandler` (`@RestControllerAdvice`) con `ProblemDetail` y mapeo de `MethodArgumentNotValidException` a `errors[]`
- [x] 2.3 Agregar el filtro de `traceId` (MDC) y devolverlo en las respuestas de error
- [x] 2.4 Agregar Bean Validation (`@NotBlank`, `@Size`, `@Email`) y `@Valid` en todos los DTOs de entrada

## 3. Refactor por recurso (backend + frontend + E2E verdes en cada paso)

- [x] 3.1 Posts: rutas REST, `getFeed(Optional<Long>)` unificado, proyección `FeedRow`, services sin `String`/`catch` genérico, 201 + `Location`
- [x] 3.2 Likes: `POST`/`DELETE /api/posts/{postId}/likes` sin body, 409 en duplicado
- [x] 3.3 Comments: `/api/posts/{postId}/comments`, corregir el NPE de `convertToDto`, proyección tipada
- [x] 3.4 Users/Profile: `/api/users`, `/api/users/{id}/profile` con proyección tipada, sin `Object[]`
- [x] 3.5 Friends: `/api/friends` y `/api/friends/requests/{id}/accept|reject`
- [x] 3.6 Messages, Companies, Jobs, Postulations, WorkStations, ProfilePhotos: rutas REST y services sin `String`
- [x] 3.7 Renombrar métodos a camelCase y campos `Id` → `id` en entidades y DTOs, y eliminar los `Long.valueOf(Id) == null`
- [x] 3.8 Generar `docs/api-routes.md` con la tabla antes/después

## 4. Frontend

- [x] 4.1 Crear servicios por recurso (`posts.ts`, `comments.ts`, `likes.ts`, `users.ts`, `auth.ts`) sobre `apiFetch`
- [x] 4.2 Quitar los `fetch` directos de `Post.tsx`, `AddPost.tsx` y `HeaderProfile.tsx`
- [x] 4.3 Like y comentario con update optimista y rollback, sin `window.location.reload()`
- [x] 4.4 Crear `/login` y `/register` (con formulario de registro real), eliminar `SignIn.astro` y `SignUp.astro`, y actualizar redirecciones y links
- [x] 4.5 Unificar tipos en inglés (`totalComments`) alineados con los DTOs del backend
- [x] 4.6 Eliminar todos los `console.log`
- [x] 4.7 Arreglar el script `dev` de `NodeTsPosts` (`tsx watch index.ts`)

## 5. Correos de postulación y retiro del legacy

- [x] 5.1 Agregar `PostulationStatus` y `Postulation.status` (default `PENDING`)
- [x] 5.2 Endpoints `POST /api/postulations/{id}/accept|reject` con check de dueño de empresa o ADMIN (403) y 409 si ya está resuelta
- [x] 5.3 Agregar `spring-boot-starter-mail` y `spring-retry`, y configurar SMTP por env (`MAIL_*`)
- [x] 5.4 Plantillas `accepted.txt` y `rejected.txt` copiadas literal del legacy, y `ApplicationMailService`
- [x] 5.5 `PostulationResolvedEvent` + `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` + `@Retryable` (3 intentos)
- [x] 5.6 Agregar Mailpit al compose de dev y al de E2E
- [x] 5.7 Tests: unitario de plantillas, integración (GreenMail o Mailpit) y un fallo de SMTP que no revierte el estado
- [x] 5.8 E2E: el dueño acepta una postulación y Mailpit recibe el correo en el email del postulante
- [x] 5.9 Inventariar en `docs/backlog-legacy.md` el resto de funcionalidad exclusiva de `BackendProConnect`
- [x] 5.10 Crear el tag `legacy-express-backend` y eliminar `BackendProConnect/`

## 7. Bugs heredados (FINDINGs de risks, perf, e2e y security en BOARD.md)

Cada uno con su test de regresión (`backProconnect/src/test/.../api/InheritedBugsTests.java` salvo indicación).

- [x] 7.1 Aceptar/rechazar/cancelar amistad fallaba siempre (check invertido) y `/pending` no devolvía el `id` de la solicitud → `friendRequestsCanBeAcceptedRejectedAndCancelled`
- [x] 7.2 `DELETE` de comentario respondía 401 aunque borraba → `deletingACommentAnswers204`
- [x] 7.3 La actualización de una postulación no persistía; `PostulationDto` sin constructor vacío (ahora record) → `postulationUpdatePersists`
- [x] 7.4 `GET /api/profilePhoto/{id}` roto por el nombre del `@PathVariable` → `profilePhotosCanBeReadById`
- [x] 7.5 `GET /api/posts` devolvía `null` en vez de `[]`, sin `ORDER BY`, y duplicaba posts con varias filas de `profile_photos` → `feedIsAnOrderedListWithoutDuplicates`
- [x] 7.6 Perfil 500 (NPE en `findUsersWithProfilesAndPosts`) con posts sin comentarios; `userLiked` del perfil usaba al dueño en vez del viewer → `profileWorksForPostsWithoutCommentsAndUsesTheViewer`
- [x] 7.7 `user.name` UNIQUE impedía nombres repetidos (quitado de la entidad; la migración del índice la hace Flyway en restructure-models-performance) → `twoUsersCanShareTheSameName`
- [x] 7.8 Like y comentario recargaban la página → optimista con rollback; E2E `like.spec.ts` / `comment.spec.ts` comprueban que no hay recarga completa
- [x] 7.9 La búsqueda del header no hacía nada al pulsar Enter → página `/search`; E2E `search.spec.ts`
- [x] 7.10 Avatar por defecto desde CDN externo (flaticon, además bloqueado por la CSP) → `public/assets/avatar-default.svg`; cubierto por `feed.spec.ts` (`post-author-photo` visible)
- [x] 7.11 `Profile.astro` con `client:only` sin framework → `client:only="react"`; E2E `profile.spec.ts`
- [x] 7.13 `imageUrl`/`photoUrl` sin validación de formato (pendiente de verify-web-security): solo nombres de archivo de media (`<uuid>.<jpg|png|webp|gif>`), sin rutas ni URLs → `ApiErrorHandlingTests.imageReferencesMustBeMediaFileNames`
- [x] 7.12 Lockfile del frontend incompatible con el npm 10 de `node:22-alpine` (heredado del upgrade a Astro 7) → regenerado; lo valida el build de la imagen web en E2E/CI

## 6. Verificación

- [x] 6.1 Actualizar los tests E2E a las rutas `/login` y `/register` y a los endpoints nuevos
- [ ] 6.2 Confirmar `ci` (lint, format, build) y `e2e` en verde
- [x] 6.3 Verificar con curl: 404 en un post inexistente, 400 con `errors[]`, 409 en like duplicado y 500 sin stack trace
