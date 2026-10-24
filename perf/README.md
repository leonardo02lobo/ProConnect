# perf/ — medición de rendimiento (restructure-models-performance)

Herramientas para medir **queries SQL por petición** y **latencia p50/p95** de los endpoints
calientes de `backProconnect` sobre un dataset sintético determinista. Se usan para la línea base
(fase 1, sobre el código actual) y se reutilizan tal cual al final del change para la comparación.

Nada de esto modifica código de la app: la API se compila desde `backProconnect/` tal como está en
el commit, la configuración se sobrescribe por variables de entorno de Spring y el conteo de
sentencias se hace desde MySQL (`performance_schema`), sin instrumentar la aplicación.

```
perf/
├── docker-compose.perf.yml   # stack pc-perf: mysql:8.4 (23306) + api (28080)
├── docker/api.Dockerfile     # build de backProconnect con maven:3.9-eclipse-temurin-17
├── mysql/perf.cnf            # performance_schema + buffer pool
├── package.json              # deps Node (mysql2, @faker-js/faker, bcryptjs) con versiones fijas
├── seed/seed.mjs             # generador determinista del dataset de referencia
├── bench/
│   ├── targets.mjs           # elige (por SQL, de forma determinista) los ids a medir
│   ├── count-queries.mjs     # sentencias SQL / tiempo BD / tamaño por petición (performance_schema)
│   ├── k6/hot-endpoints.js   # benchmark de latencia k6
│   ├── run-k6.sh             # ejecuta k6 en Docker dentro de la red del stack
│   ├── explain.sql / .sh     # EXPLAIN y EXPLAIN ANALYZE de las queries nativas
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
# 1. Levantar el stack (compila la API actual en Docker; la primera vez tarda ~2 min)
docker-compose -p pc-perf -f perf/docker-compose.perf.yml up -d --build

# 2. Esperar a que la API arranque: Hibernate (ddl-auto=update) crea el esquema
until curl -s -o /dev/null http://127.0.0.1:28080/api/posts; do sleep 2; done

# 3. Dependencias Node y dataset de referencia (semilla fija; vacía y re-siembra las tablas)
(cd perf && npm ci && node seed/seed.mjs | tee results/<label>-seed.json)

# 4. Medición completa (~50 min con el código actual: cada petición de feed tarda ~4 min)
perf/bench/run-all.sh <label>          # p. ej. baseline | after

# 5. Tabla resumen / comparación
node perf/bench/report.mjs after --compare baseline

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
- El script **no crea tablas**: siembra el esquema que genera la versión bajo prueba (arrancar la
  API antes). Si en fase 2 cambian columnas (Flyway, `avatar_url`, contadores…), hay que ajustar
  los `INSERT` de `seed.mjs` manteniendo la misma secuencia de `faker` para conservar el dataset.
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
