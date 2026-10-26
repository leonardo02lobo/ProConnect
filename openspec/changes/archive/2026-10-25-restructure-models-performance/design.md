## Context

Modelo actual: 11 entidades sin relaciones JPA, FKs implícitas como `Long`, esquema gestionado por `ddl-auto=update` y solo dos restricciones `unique` (email y userName).

Costo actual del feed (`PostsService.getAllPosts` → `PostsRepository.GetAllPosts`):

```
1 query  posts ⋈ user ⟕ profile_photos  + 3 subconsultas COUNT(*) correlacionadas por fila
└─ por cada post (P):     commentsRepository.findByPostId          → P queries
   └─ por cada comentario (C): commentsRepository.findUserWithComment → C queries
Total = 1 + P + C        (P=100, C=1000 → 1.101 queries, sin paginar)
```

Perfil (`UserRepository.findUsersWithProfilesAndPosts`): `user ⟕ posts ⟕ comments` genera P×C filas, y por cada comentario se lanzan `userRepository.findById` + `profilePhotosRepository.findByUserId` (2C queries).

## Goals / Non-Goals

**Goals:**
- Feed y perfil con O(1) queries por página y latencia acotada.
- Integridad de datos garantizada por la base, no solo por la aplicación.
- Esquema reproducible y auditable (Flyway).
- Mejoras **medidas**, no supuestas: línea base antes y después.

**Non-Goals:**
- Cache distribuida (Redis) o CDN de imágenes. Se reevalúa con datos después de este change.
- Feed personalizado por amigos o ranking: sigue siendo cronológico global.
- Sharding, réplicas de lectura o búsqueda full-text.
- Mover imágenes a object storage.

## Decisions

### D1. Medir primero
- Generador: `CommandLineRunner` en el perfil `perf` con Datafaker y semilla fija, que inserta en lote vía `JdbcTemplate.batchUpdate` (rápido y sin pasar por los services).
- Conteo de queries: `spring.jpa.properties.hibernate.generate_statistics=true` en tests, más un helper `QueryCounter` con `datasource-proxy` para aserciones en tests de integración (Testcontainers MySQL).
- Latencia: script `k6` (o `oha`) contra el stack Compose de `add-e2e-ai-ci`, 30 s por endpoint, que guarda p50/p95.
- La medición se ejecuta **antes** de tocar el modelo (commit base) y al final.

### D2. Flyway con baseline del esquema actual
- `V1__baseline.sql` se genera con `mysqldump --no-data` del esquema actual, normalizado. En bases existentes se usa `baselineOnMigrate=true` y `baselineVersion=1`.
- `V2__constraints_and_indexes.sql`: limpia duplicados antes de crear los `UNIQUE` (se deja el like o la amistad más antiguo) y borra huérfanos antes de las FKs, después de un `SELECT` de auditoría que queda en el log de la migración.
- `V3__user_media_columns.sql`: `user.avatar_url`, `user.cover_url` ← `profile_photos`. La tabla se elimina en una migración posterior (V5), cuando el código ya no la lee (expand/contract).
- `V4__post_counters.sql`: `like_count` y `comment_count` + `UPDATE` inicial desde los `COUNT(*)`.
- **Alternativa**: Liquibase. Se descarta porque SQL plano es más legible para este equipo y el proyecto usa una sola base.

### D3. Relaciones JPA selectivas, todas LAZY
- `Post.author` (`@ManyToOne LAZY`), `Comment.post` / `Comment.author`, `Like.post` / `Like.user`, `Job.company`, `Postulation.job` / `Postulation.user`, `Friend.follower` / `Friend.followed`, `Message.sender` / `Message.receiver`.
- **No** se mapean colecciones `@OneToMany` en `User` ni en `Post`: evita cargas accidentales y bucles en la serialización. Las colecciones se piden por repositorio y paginadas.
- `open-in-view=false`, para que cualquier lazy fuera de transacción falle en dev y no genere N+1 silenciosos.

### D4. Feed en 3 queries
```
Q1  SELECT p.id, p.title, p.content, p.image_url, p.created_at, p.like_count, p.comment_count,
           u.id, u.name, u.user_name, u.avatar_url
    FROM posts p JOIN user u ON u.id = p.user_id
    WHERE (p.created_at, p.id) < (:cursorTs, :cursorId)
    ORDER BY p.created_at DESC, p.id DESC LIMIT :limit+1
Q2  SELECT post_id FROM likes WHERE user_id = :viewer AND post_id IN (:ids)       -- solo si hay viewer
Q3  SELECT * FROM (
      SELECT c.*, u.name, u.user_name, u.avatar_url,
             ROW_NUMBER() OVER (PARTITION BY c.post_id ORDER BY c.created_at DESC) rn
      FROM comments c JOIN user u ON u.id = c.user_id WHERE c.post_id IN (:ids)) t
    WHERE rn <= 3
```
- MySQL 8 soporta window functions y comparación de tuplas. La comparación de tuplas puede no usar el índice en algunas versiones, así que como alternativa se usa `created_at < :ts OR (created_at = :ts AND id < :id)`, que se valida con `EXPLAIN` (D1).
- El cursor es `base64("<epochMicros>:<id>")`, opaco para el cliente.
- Se pide `LIMIT limit+1` para saber si hay página siguiente sin `COUNT`.
- **Alternativa**: paginación por offset. Se degrada con páginas profundas y es inestable ante inserciones.
- **Alternativa**: `@EntityGraph` / `JOIN FETCH` de comentarios. No permite limitar a N por post y vuelve a traer el cartesiano.

### D5. Contadores denormalizados
- Se actualizan con `UPDATE posts SET like_count = like_count + 1 WHERE id = ?` en la misma transacción que el `INSERT` del like, lo que es atómico a nivel de fila. Ante una violación del `UNIQUE` se hace rollback y se devuelve 409, y el contador no cambia.
- Un job de reconciliación opcional (`@Scheduled`, desactivado por defecto) recalcula y loguea discrepancias.
- **Alternativa**: triggers de MySQL. Ocultan lógica fuera del código y complican los tests.
- **Alternativa**: mantener `COUNT(*)` con índice `likes(post_id)`. Es aceptable a esta escala, pero escala peor y cuesta 2 subconsultas por fila.

### D6. Tipos y enums
- `Date` → `Instant` con columnas `DATETIME(6)` en UTC (`hibernate.jdbc.time_zone=UTC`).
- `FriendStatus { PENDING, ACCEPTED, REJECTED }` y `Role { USER, COMPANY_ADMIN, ADMIN }` con `@Enumerated(STRING)` y migración que normaliza los valores existentes.

### D7. Configuración de runtime
- Hikari: `maximumPoolSize` 10 (ajustable por env) y `connectionTimeout` 3 s.
- `hibernate.default_batch_fetch_size=50`, `jdbc.batch_size=50`, `order_inserts=true`.
- `show-sql` solo en el perfil `dev`.

## Risks / Trade-offs

- [Migraciones destructivas (borrar duplicados o huérfanos) sobre datos reales] → Auditoría previa con `SELECT` en la migración, backup obligatorio (`mysqldump`) antes de aplicar y prueba en una copia.
- [Los contadores denormalizados pueden divergir] → Actualización en la misma transacción, test de consistencia en la migración y job de reconciliación.
- [El cambio de contrato del feed rompe el frontend] → Se migra el frontend en el mismo change (botón "cargar más" o IntersectionObserver) y los E2E lo cubren.
- [Window functions o comparación de tuplas con planes malos] → Validar con `EXPLAIN` sobre el dataset de referencia y usar la alternativa con `OR` si hace falta.
- [Umbral de p95 dependiente del hardware de CI] → El umbral bloqueante es el de número de queries, que es determinista. La latencia se reporta y solo alerta.

## Migration Plan

1. Generador de dataset + benchmark → informe base (`docs/perf/…-baseline.md`). **Se ejecuta ya, sobre el código actual de `main`**, en paralelo con `fix-critical-risks`, para capturar la línea base real antes de cualquier refactor. Como el generador y el benchmark solo añaden código en el perfil `perf` y en `perf/`, conflictúan poco con el resto de changes.
2. Flyway V1 baseline, con `ddl-auto=validate` y la app sin cambios funcionales.
3. V2 restricciones e índices (con backup).
4. Relaciones JPA + enums + `Instant`.
5. V3/V4 columnas de media y contadores. El código lee las columnas nuevas.
6. Feed, perfil y listados paginados + frontend + E2E.
7. V5 contract: eliminar `profile_photos`.
8. Benchmark final y comparación.
Rollback: hasta el paso 6 cada migración tiene su script `U*` manual documentado. V5 solo se aplica cuando todo lo anterior está estable en todos los entornos.

## Resolved Decisions

- **Feed global** cronológico: el índice principal es `posts(created_at, id)`. No hace falta fan-out por seguidos.
- Toda lectura requiere login, así que siempre hay un viewer: Q2 (likes del viewer) se ejecuta siempre y el feed queda en exactamente 3 queries.
- Roles: enum `Role { USER, COMPANY_ADMIN, ADMIN }` (introducido en `fix-critical-risks`); aquí solo se normalizan los valores existentes en la migración.
- `Postulation.status` ya existe (`cleanup-codebase`): el baseline V1 lo captura e incluye el índice `postulations(job_id, status)`.

## Open Questions

- ¿Volumen real esperado? El dataset de referencia asume una escala pequeña o mediana; si se esperan órdenes de magnitud más, hay que reconsiderar la cache.
- ¿Hace falta conservar `ProfilePhotos` como historial de fotos? Por defecto se colapsa en `User`.
