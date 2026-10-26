# perf/ — medición de rendimiento (restructure-models-performance)

Herramientas para medir **queries SQL por petición** y **latencia p50/p95** de los endpoints
calientes del backend (`backend/`) sobre un dataset sintético determinista. Se usan para la línea base
(fase 1, sobre el código actual) y se reutilizan tal cual al final del change para la comparación.

Nada de esto modifica código de la app: la API se compila desde el submódulo `backend/` tal como está en
el commit, la configuración se sobrescribe por variables de entorno y el conteo de sentencias se hace
desde MySQL (`performance_schema`), sin instrumentar la aplicación.

Mediciones guardadas (`results/<label>-*`, informes en `docs/perf/`):

| Label | Código | Notas |
|---|---|---|
| `baseline` | `03a4ba7` (rutas antiguas, sin auth) | fase 1, `docs/perf/2026-10-24-baseline.md` |
| `pre` | `4163ca1` (tras risks/security/cleanup, sin índices) | rutas REST + login; mismo dataset |
| `after-v2` | `2f79dbe` (mismo código + Flyway V1/V2: solo índices/FKs) | V2 aplicada sobre la base `pre` existente |
| `after` | fase 2 completa (V2.1–V5, feed en 3 queries, paginación) | `docs/perf/2026-10-25-after.md` |

Desde la fase 2 toda la API exige sesión: `count-queries.mjs` y k6 hacen login (`/api/auth/login`) con los
usuarios sembrados (`PerfPass#2026`) y envían la cookie `pc_session`; el compose sube el rate limit de login.

```
perf/
├── docker-compose.perf.yml   # stack pc-perf: mysql:8.4 (23306) + api (28080)
├── docker/api.Dockerfile     # build del backend con maven:3.9-eclipse-temurin-17
├── mysql/perf.cnf            # performance_schema + buffer pool
├── package.json              # deps Node (mysql2, @faker-js/faker, bcryptjs) con versiones fijas
├── seed/seed.mjs             # generador determinista del dataset de referencia
├── bench/
│   ├── targets.mjs           # elige (por SQL, de forma determinista) los ids a medir
│   ├── count-queries.mjs     # sentencias SQL / tiempo BD / tamaño por petición (performance_schema)
│   ├── k6/hot-endpoints.js   # benchmark de latencia k6
│   ├── run-k6.sh             # ejecuta k6 en Docker dentro de la red del stack
│   ├── explain.sh            # EXPLAIN/EXPLAIN ANALYZE: explain.sql (fase 1), explain-pre.sql, explain-after.sql
│   ├── run-all.sh            # medición completa
│   └── report.mjs            # tabla Markdown (y comparación entre dos labels)
└── results/                  # JSON / txt de cada medición (versionados)
```

## Requisitos

- Docker + `docker-compose` (v2), Node 22.
- Puertos de host libres: 23306 (MySQL) y 28080 (API). Compose project: `pc-perf`.
- ~2,5 GB de RAM libres (MySQL 1,5 GB + API 1,25 GB como límite).

## Reproducir la medición (comandos exactos, desde la raíz del repo)

```bash
# 1. Levantar el stack (compila la API actual en Docker; la primera vez tarda ~2 min).
#    --wait espera al healthcheck: Flyway crea/migra el esquema al arrancar la API.
PERF_API_TAG=after docker-compose -p pc-perf -f perf/docker-compose.perf.yml up -d --build --wait

# 3. Dependencias Node y dataset de referencia (semilla fija; vacía y re-siembra las tablas)
(cd perf && npm ci && node seed/seed.mjs | tee results/<label>-seed.json)

# 4. Medición completa (~50 min con el código actual: cada petición de feed tarda ~4 min)
perf/bench/run-all.sh <label>          # p. ej. after (EXPLAIN_SQL=explain-after.sql)
#   pre-fase-2 (feed sin paginar, minutos por petición):
#   FEED_ENDPOINTS=feed FEED_LOAD_VUS=3 EXPLAIN_SQL=explain-pre.sql perf/bench/run-all.sh pre

# 5. Tabla resumen / comparación
node perf/bench/report.mjs after --compare baseline   # mapea las rutas antiguas de la línea base

# 6. Bajar el stack y liberar recursos
docker-compose -p pc-perf -f perf/docker-compose.perf.yml down -v
```

Pasos sueltos:

```bash
node perf/bench/targets.mjs                                   # ids que se miden
node perf/bench/count-queries.mjs --label x --only profile,comments,likeCreate --repeat 10 --warmup 5
perf/bench/run-k6.sh comments serial x 1 30s                  # <endpoint> <serial|load> [label] [vus] [duration]
perf/bench/run-k6.sh like load x 10 30s
perf/bench/explain.sh x
node perf/seed/seed.mjs --checksum-only                       # conteos + checksums del dataset
```

## Dataset de referencia (`seed/seed.mjs`)

| Tabla | Filas | Notas |
|---|---|---|
| `user` | 1.000 | email `perf.user<N>@proconnect.test`, password **`PerfPass#2026`** (BCrypt cost 12, salt fijo), role `USER` |
| `profile_photos` | 922 | ~90 % de usuarios con avatar, ~60 % de ellos con portada |
| `posts` | 10.000 | autor aleatorio, fechas crecientes desde 2025-01-01, ~30 % con imagen |
| `likes` | 50.000 | pares (user, post) únicos |
| `comments` | 30.000 | post y autor aleatorios (hay posts sin comentarios, como en la realidad) |
| `friends` | 5.000 | pares dirigidos únicos; 70 % ACCEPTED, 20 % PENDING, 10 % REJECTED |

- **Determinismo**: semilla `PERF_SEED=20261024`, ids explícitos y fechas calculadas desde una
  fecha fija (nunca desde `now`). El script imprime conteos y un checksum por tabla
  (`BIT_XOR(CRC32(fila))`), independiente del orden físico: dos ejecuciones sobre bases vacías dan
  el mismo JSON (ver `results/baseline-seed*.json`).
- El script **no crea tablas**: siembra el esquema de la versión bajo prueba (arrancar la API antes).
  Detecta las columnas (`date`/`created_at`, `profile_photos` o `user.avatar_url/cover_url`, contadores
  `like_count/comment_count`, que recalcula) sin cambiar la secuencia de `faker`: el dataset lógico es
  idéntico y los checksums de `user`, `posts`, `likes`, `comments` y `friends` coinciden con la línea base.
- Variables: `DB_HOST` `DB_PORT` `DB_USER` `DB_PASSWORD` `DB_NAME` `PERF_SEED`.

## Cómo se mide

**Sentencias SQL por petición** (`bench/count-queries.mjs`): la API se conecta a MySQL con el
usuario `app`; el medidor usa `root`. Antes de cada petición se hace `TRUNCATE` de
`performance_schema.events_statements_summary_by_user_by_event_name` (y `..._by_digest`) y después
se suman las sentencias `statement/sql/%` del usuario `app`. Se reportan:
- **DML** = `SELECT + INSERT + UPDATE + DELETE` (la cifra comparable con los umbrales del spec,
  equivalente a lo que contaría Hibernate statistics / datasource-proxy).
- **total** = DML + `SET autocommit`, `SET SESSION TRANSACTION READ ONLY`, `COMMIT`… que el driver
  y Spring emiten alrededor de cada transacción (cada llamada a repositorio sin `@Transactional`
  envolvente abre su propia transacción).
- tiempo total en BD, filas examinadas y las sentencias más repetidas (digest) para ver el N+1.

**Latencia** (`bench/k6/hot-endpoints.js`, `grafana/k6:0.54.0` en Docker, dentro de la red del
stack para no medir el NAT del host):
- `serial`: 1 VU haciendo peticiones secuenciales durante 30 s → distribución de la petición
  individual en caliente (la métrica del umbral "p95 ≤ 150 ms por petición individual").
- `load`: 10 VUs concurrentes durante 30 s → p50/p95 y rps bajo carga.
- `like`: cada iteración hace `POST /api/like/create` + `DELETE /api/like/delete` del mismo par
  (usuario = nº de VU, post sin likes de los usuarios 1..64), así la BD queda como estaba.
- El feed actual tarda minutos por petición: `run-all.sh` lo mide con 1 VU (deja terminar la
  petición en curso, `gracefulStop` 20 min) y con `FEED_LOAD_VUS=3` en vez de 10.

**Req. única en caliente**: `count-queries.mjs` hace `--warmup` peticiones descartadas y mide
`--repeat` peticiones secuenciales con `fetch` desde el host (incluye serializar/transferir el JSON).

**EXPLAIN** (`bench/explain.sh`): `EXPLAIN` y `EXPLAIN ANALYZE` de las queries nativas de
`PostsRepository` y `UserRepository` y de las derivadas que forman el N+1, más el listado de índices.

## Entorno controlado

- MySQL 8.4 limitado a 2 CPU / 1,5 GB (buffer pool 512 MB); API limitada a 2 CPU / 1,25 GB
  (`-Xmx768m`). Así la medición es comparable entre ejecuciones en la misma máquina.
- `SPRING_JPA_SHOW_SQL=false` para no medir el coste de imprimir cada sentencia (la app trae
  `show-sql=true`; en fase 2 sale de prod de todos modos).
- `SPRING_DEVTOOLS_RESTART_ENABLED=false` (el jar ya excluye devtools al empaquetar, por si acaso).
- No se ejecuta nada más pesado en la máquina durante la medición.
