## Why

El modelo de datos no escala:
- **Sin integridad**: las entidades se relacionan por `Long` sueltos, sin claves foráneas ni índices fuera de los `unique` de email y userName.
- **Sin control de esquema**: `ddl-auto=update` decide el esquema.
- **Feed con N+1 en cascada**: 1 query de posts, 1 por post para comentarios y 1 por comentario para su autor. Además no tiene paginación y calcula 3 subconsultas `COUNT(*)` correlacionadas por fila.
- **Perfil sin límites**: hace un `JOIN` posts × comments que multiplica filas y luego, por cada comentario, 2 queries más.
- **Listados sin paginar**: `findAll()` en Users, Jobs, Companies, Postulations y WorkStations.
- **Sin restricciones de unicidad**: nada impide likes duplicados ni solicitudes de amistad repetidas.

Con 100 posts y 10 comentarios por post, una sola carga del feed ya ejecuta del orden de 1.100 queries.

## What Changes

- **Línea base de rendimiento**: dataset sintético reproducible (p. ej. 1k usuarios, 10k posts, 50k likes, 30k comentarios), conteo de queries por endpoint (Hibernate statistics) y latencia p50/p95 de los endpoints calientes, medidos **antes y después**.
- **Migraciones versionadas con Flyway**: `V1__baseline.sql` refleja el esquema actual y se pasa a `ddl-auto=validate`.
- **Integridad**: claves foráneas con `ON DELETE` definido y restricciones únicas (`likes(user_id, post_id)`, `friends(user_id_follow, user_id_be_followed)`, `postulations(user_id, job_id)`, `profile_photos(user_id)`).
- **Índices** para los patrones de acceso reales: `posts(created_at, id)`, `posts(user_id, created_at)`, `comments(post_id, created_at)`, `likes(post_id)`, `friends(user_id_be_followed, status)`, `messages(receiver_id, sent_at)`, `jobs(company_id)`, etc.
- **Relaciones JPA** (`@ManyToOne(fetch = LAZY)`) donde aportan navegación, manteniendo la FK como fuente de verdad. `ProfilePhotos` se integra en `User` como columnas (`avatar_url`, `cover_url`), porque es 1:1 y se lee en cada consulta.
- **Contadores denormalizados**: `posts.like_count` y `posts.comment_count`, que se mantienen en la misma transacción que crea o borra el like o comentario. Sustituyen a las subconsultas `COUNT(*)`.
- **BREAKING** **Feed paginado por cursor** (`GET /api/posts?cursor=<createdAt,id>&limit=20`). Devuelve los últimos N comentarios por post (preview, N=3) en **una sola** query batch; la lista completa queda en `GET /api/posts/{id}/comments` paginado.
- **BREAKING** **Perfil paginado**: datos del usuario y primera página de sus posts, sin el `JOIN` cartesiano con comentarios.
- **Paginación** (`Pageable`, límite máximo 100) en todos los listados que hoy usan `findAll()`.
- **Tipos**: `java.util.Date` → `Instant`/`LocalDateTime` con columnas `DATETIME(6)` o `TIMESTAMP`. `Friend.status` y `User.role` pasan a enums.
- **Pool y JPA**: ajustar Hikari, `spring.jpa.open-in-view=false`, `hibernate.default_batch_fetch_size` y `show-sql=false` fuera de dev.

## Capabilities

### New Capabilities
- `data-model-integrity`: esquema versionado con Flyway, claves foráneas, restricciones únicas, índices y tipos adecuados.
- `feed-performance`: feed y perfil paginados, con número de queries acotado e independiente del tamaño de la página, y contadores precalculados.
- `performance-baseline`: dataset sintético y medición reproducible de queries y latencia, con umbrales verificables.

### Modified Capabilities
<!-- Sin specs previas. Si `rest-api-conventions` (cleanup-codebase) ya está archivada, el feed paginado se añade como delta MODIFIED de esa capability. -->

## Impact

- **backProconnect**: todas las entidades, repositorios (fuera las nativas con `Object[]`, entran JPQL/proyecciones con `JOIN FETCH` o queries batch), `PostsService`, `UserService`, `CommentsService`, `LikeService`, `FriendService`, `pom.xml` (`flyway-core`, `flyway-mysql`), `application.yml`.
- **Base de datos**: migraciones `V1…Vn`. Hace falta un script de datos para pasar `profile_photos` a `user` y para calcular los contadores iniciales.
- **Frontend**: scroll infinito o "cargar más" en el feed y el perfil, y nuevos campos (`avatarUrl`, `coverUrl`).
- **E2E**: ajustar los tests a la paginación. El seed por API (decisión de `add-e2e-ai-ci`) sigue funcionando.
- **Depende de** `cleanup-codebase`: rutas REST y proyecciones tipadas.
