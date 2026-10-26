## Context

Medición sobre `backProconnect` y el frontend (al 2026-10-24):

| Smell | Cantidad | Ejemplo |
|---|---|---|
| `return "Error: ..."` en services | 22 | `FriendService` (5) |
| `catch (Exception e)` genérico | 13 | `PostsService.AddPost` |
| Métodos públicos PascalCase | 40 | `AddPost`, `GetAllPosts` |
| `Long.valueOf(Id) == null` (siempre false) | 14 | `PostsService`, `UserService` |
| Mapeo manual `Object[]` | 3 services / 3 repos | `row[13]`, `row[15]` |
| `console.*` en frontend | 18 | `LikeApi.ts` (6) |
| `fetch` directo en componentes | 4 | `Post.tsx`, `AddPost.tsx` |

Además:
- `PostsService.convertToDto` hace `user = userList.get(0)` aunque la lista esté vacía (NPE).
- Las páginas `SignUp.astro` (que hace login) y `SignIn.astro` ("hola") están invertidas.
- El script `dev` de `NodeTsPosts` ejecuta `node ./index.ts`.

## Goals / Non-Goals

**Goals:**
- Un contrato de API predecible: códigos HTTP, errores y rutas.
- Convenciones verificadas por herramientas, no por memoria.
- Un único backend.
- Mantener el comportamiento funcional: los E2E siguen en verde, solo con rutas y URLs actualizadas.

**Non-Goals:**
- Optimizar queries, agregar relaciones JPA o índices (`restructure-models-performance`). Aquí solo se sustituye `Object[]` por proyecciones tipadas con las **mismas** queries.
- Migrar los correos de postulación desde Express (backlog).
- Rediseño visual del frontend.

## Decisions

### D1. `@RestControllerAdvice` + `ProblemDetail` nativo de Spring 6
- Spring Boot 3 trae `ProblemDetail` y `spring.mvc.problemdetails.enabled=true`, así que no hace falta una librería extra.
- Excepciones de dominio en `exception/` → handler central. `MethodArgumentNotValidException` se convierte en 400 con `errors[]`.
- Un `traceId` (MDC, filtro `OncePerRequestFilter`) va en el log y en la respuesta.

### D2. Rutas REST, con migración en un solo paso
- No hay clientes externos: el único consumidor es el frontend propio, que se migra en el mismo change. No se versiona `/api/v1` ni se mantienen alias.
- Tabla de mapeo (extracto):

| Antes | Después |
|---|---|
| `POST /api/posts/create` | `POST /api/posts` |
| `PUT /api/posts/edit/{Id}` | `PUT /api/posts/{id}` |
| `GET /api/posts/{userId}` (feed con userId) | `GET /api/posts` (usa sesión si existe) |
| `GET /api/posts/find/{Id}` | `GET /api/posts/{id}` |
| `POST /api/like/create` + `DELETE /api/like/delete` (body) | `POST`/`DELETE /api/posts/{postId}/likes` |
| `POST /api/comment/create`, `GET /api/comment/find/{postId}` | `POST`/`GET /api/posts/{postId}/comments` |
| `GET /api/user/{userId}/profiles-posts` | `GET /api/users/{id}/profile` |
| `PUT /api/friend/accept/{x}` | `POST /api/friends/requests/{id}/accept` |

- La tabla completa se genera durante la implementación y se deja en `docs/api-routes.md`.
- **Alternativa**: mantener las rutas y solo arreglar códigos. Se descarta porque perpetúa `DELETE` con body, que algunos proxies descartan.

### D3. Proyecciones tipadas en vez de `Object[]`
- Interfaces de proyección de Spring Data (`interface FeedRow { Long getId(); ... }`) con alias en la SQL nativa. Las queries no cambian; solo deja de existir la indexación posicional `row[n]`.
- Se unifican los dos `GetAllPosts` en `getFeed(Optional<Long> viewerId)`, con `user_liked` calculado como `0` si no hay viewer.

### D4. Herramientas de calidad
- Java: `spotless-maven-plugin` (google-java-format, AOSP 4 espacios para no reindentar todo el estilo actual) + `maven-checkstyle-plugin` con reglas de nombres. Se aplica `spotless:apply` en un commit aislado de solo formato, para que el `git blame` siga siendo legible (se registra en `.git-blame-ignore-revs`).
- TS: ESLint flat config con `typescript-eslint`, `eslint-plugin-astro`, `eslint-plugin-react-hooks` y `no-console`; Prettier con `prettier-plugin-astro`.

### D5. Frontend
- Páginas `/login` y `/register`. `SignUp.astro` y `SignIn.astro` se eliminan (no hay usuarios externos con enlaces guardados).
- `Service/` por recurso (`posts.ts`, `comments.ts`, `likes.ts`, `users.ts`, `auth.ts`) sobre el `apiFetch` de `fix-critical-risks`. Los componentes solo llaman a servicios.
- Like y comentario con update optimista + rollback si falla.
- Tipos en inglés consistente (`totalComments`), alineados con el DTO de backend.

### D6. Correos de postulación
- `Postulation.status` (enum `PostulationStatus`). Como Flyway llega en `restructure-models-performance`, aquí la columna la crea `ddl-auto=update` con default `PENDING`, y el baseline V1 posterior la captura.
- `PostulationService.accept/reject(id)`: verifica que el actor es el dueño de la empresa del empleo (`company.userId == actor`) o `ADMIN`, verifica que la postulación está `PENDING` (409 si no) y cambia el estado. Luego publica un `PostulationResolvedEvent`.
- `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async` → `ApplicationMailService.send(...)` con `JavaMailSender`, y `@Retryable(maxAttempts = 3, backoff = @Backoff(2000, multiplier = 2))` de spring-retry.
- Plantillas: el texto de `MensajePositivo` y `MensajeNegativo` se copia literal en `resources/templates/mail/accepted.txt` y `rejected.txt`, con placeholders `{nombre}`, `{puesto}` y `{empresa}`. Se mantiene texto plano, igual que hoy.
- Mailpit (`axllent/mailpit`) se añade al `docker-compose.e2e.yml` y al compose de dev. El E2E consulta `GET /api/v1/messages` de Mailpit.
- **Alternativa**: mantener el microservicio Node solo para correo. Se descarta porque añade otro servicio y el destinatario seguiría llegando por red.

### D7. Retiro de `BackendProConnect`
- Solo después de D6 con E2E en verde: `git tag legacy-express-backend` y luego `git rm -r BackendProConnect`.
- Inventario del resto de funcionalidad exclusiva (búsqueda de empleo por nombre y por puesto, que Spring cubre con `/api/job/search`) en `docs/backlog-legacy.md`.

## Risks / Trade-offs

- [Un refactor grande rompe flujos sin que se note] → Los E2E de `add-e2e-ai-ci` son el gate. Se trabaja en commits pequeños por recurso (posts → likes → comments → users → friends → jobs …), con E2E verdes en cada paso.
- [El commit masivo de formato genera conflictos con ramas abiertas] → Coordinarlo: se aplica en un momento sin PRs abiertos y se usa `.git-blame-ignore-revs`.
- [Se pierde funcionalidad del legacy] → Tag, más inventario en backlog.
- [Los cambios de rutas rompen bookmarks] → No aplica, la app no está en producción.

## Migration Plan

1. Tooling (Spotless, ESLint) en modo reporte → commit de formato → modo estricto en CI.
2. Excepciones y advice global.
3. Por recurso: rutas REST + services sin String + proyecciones + frontend + E2E.
4. Páginas `/login` y `/register`.
5. Retiro de legacy.

## Resolved Decisions

- Los correos de postulación son requisito: se pasan a Spring (D6) antes de retirar el legacy.

## Open Questions

- ¿Idioma del dominio y de la UI? El código queda en inglés; la UI hoy mezcla inglés y español. Las plantillas de correo se mantienen en español.
