## 1. Línea base

- [x] 1.1 Agregar Testcontainers MySQL y `datasource-proxy` a los tests, y crear el helper `QueryCounter` — fase 2: `it/MySqlIntegrationTest` (MySQL 8.4 por Testcontainers o `IT_DB_URL`, Flyway + `validate`), `it/QueryCounter` (DML como `count-queries.mjs`); los `*IT` corren con failsafe en `mvn verify`
- [x] 1.2 Implementar el generador de dataset en el perfil `perf` (Datafaker, semilla fija, `JdbcTemplate.batchUpdate`) — implementado fuera de la app para no tocar su código: `perf/seed/seed.mjs` (Node + @faker-js/faker con semilla fija, inserts por lotes con mysql2)
- [x] 1.3 Escribir el script de benchmark (k6 u oha) para feed, perfil, comentarios de un post y like
- [x] 1.4 Ejecutar la medición sobre el código actual y guardar `docs/perf/<fecha>-baseline.md` (queries, p50, p95)
- [x] 1.5 Capturar el `EXPLAIN` de las queries actuales del feed y del perfil

## 2. Flyway

- [x] 2.1 Agregar `flyway-core` y `flyway-mysql`, y configurar `baselineOnMigrate`
- [x] 2.2 Generar `V1__baseline.sql` desde el esquema actual
- [x] 2.3 Cambiar a `ddl-auto=validate` y verificar el arranque contra una base vacía y una existente

## 3. Integridad e índices

- [x] 3.1 `V2__constraints_and_indexes.sql`: auditar y limpiar duplicados y huérfanos, y crear FKs (CASCADE/RESTRICT según el diseño), `UNIQUE` e índices
- [x] 3.2 Probar la migración V2 sobre una copia con el dataset de referencia, con backup previo documentado — stack pc-perf con la base "pre" creada por Hibernate (10k posts): `mysqldump` previo, baseline V1 + V2 en 1,9 s, auditoría sin huérfanos/duplicados (`perf/results/after-v2-run.log`); datos sucios cubiertos por `LegacyDatabaseMigrationIT`
- [x] 3.3 Test: un doble like concurrente produce un solo registro y un 409

## 4. Entidades

- [x] 4.1 Agregar relaciones `@ManyToOne(fetch = LAZY)` según D3, sin `@OneToMany` en `User`/`Post`
- [x] 4.2 Migrar `Date` → `Instant` (`DATETIME(6)`, UTC) en todas las entidades — `V2_1__types_and_enums.sql` renombra `date`→`created_at` (`messages.timestamp`→`sent_at`), NOT NULL; JSON conserva `date`/`timestamp`
- [x] 4.3 Crear los enums `FriendStatus` y `Role` y la migración de normalización de valores
- [x] 4.4 Poner `open-in-view=false`, el batch fetch size y la configuración de Hikari; `show-sql` solo en `dev`

## 5. Media en User y contadores

- [ ] 5.1 `V3__user_media_columns.sql`: `avatar_url` y `cover_url` en `user` + copia desde `profile_photos`
- [ ] 5.2 Adaptar código y DTOs para leer y escribir `avatarUrl`/`coverUrl`
- [ ] 5.3 `V4__post_counters.sql`: `like_count`, `comment_count` + backfill y test de consistencia
- [ ] 5.4 Actualizar contadores en las transacciones de like, unlike, comentar y borrar comentario

## 6. Queries paginadas

- [ ] 6.1 Implementar el feed en 3 queries (posts+autor, likes del viewer, preview de 3 comentarios con `ROW_NUMBER`) con cursor opaco
- [ ] 6.2 Validar con `EXPLAIN` el uso del índice `posts(created_at, id)` y ajustar el predicado si hace falta
- [ ] 6.3 `GET /api/posts/{id}/comments` paginado por cursor
- [ ] 6.4 Perfil: datos de usuario + primera página de posts por cursor, sin JOIN con comments
- [ ] 6.5 Paginación `Pageable` (máx. 100) en usuarios, empresas, empleos, postulaciones, puestos, mensajes y amigos
- [ ] 6.6 Tests de integración con `QueryCounter`: feed ≤ 4 y perfil ≤ 4 sentencias

## 7. Frontend y E2E

- [ ] 7.1 Feed y perfil con "cargar más" o scroll infinito usando `nextCursor`
- [ ] 7.2 Modal o página de comentarios completos paginados
- [ ] 7.3 Usar `avatarUrl`/`coverUrl` en los componentes
- [ ] 7.4 Actualizar y ampliar los E2E (paginación del feed y contadores tras like/unlike)

## 8. Contract y cierre

- [ ] 8.1 `V5__drop_profile_photos.sql` y eliminar la entidad y el repositorio `ProfilePhotos`
- [ ] 8.2 Benchmark final → `docs/perf/<fecha>-after.md` con la comparación contra la línea base
- [ ] 8.3 Confirmar los umbrales: ≤ 4 queries y p95 ≤ 150 ms en feed y perfil (reportando el hardware)
