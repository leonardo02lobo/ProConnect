# Migraciones de base de datos (Flyway)

Desde `restructure-models-performance` el esquema de MySQL lo gestiona **solo Flyway**
(`backProconnect/src/main/resources/db/migration`). La API arranca con
`spring.jpa.hibernate.ddl-auto=validate`: si una entidad no coincide con el esquema migrado, no arranca.

| Versión | Script | Qué hace | Destructiva |
|---|---|---|---|
| V1 | `V1__baseline.sql` | Esquema que generaba Hibernate tras cleanup-codebase (sin UNIQUE en `user.name`, `postulations.status` DEFAULT `PENDING`) | No |
| V2 | `V2__constraints_and_indexes.sql` | Auditoría → borra huérfanos y duplicados → quita UNIQUE de `user.name` → UNIQUE, índices y FKs | **Sí** (solo filas inválidas) |
| V2.1 | `V2_1__types_and_enums.sql` | `date`→`created_at`, `messages.timestamp`→`sent_at` (DATETIME(6) UTC, NOT NULL); normaliza `friends.status`, `user.role`, `postulations.status` + CHECK | No (normaliza valores) |

## Bases nuevas y existentes

- **Base vacía**: Flyway aplica V1, V2, … en orden.
- **Base creada antes por Hibernate** (`ddl-auto=update`, sin `flyway_schema_history`): con
  `baseline-on-migrate=true` y `baseline-version=1` Flyway la marca como V1 (no ejecuta V1) y aplica V2+.
  V2 está escrito para ambos casos (p. ej. busca por `information_schema` el índice UNIQUE de `user.name`
  que creó Hibernate con nombre aleatorio).
- Variables: `FLYWAY_ENABLED` (true), `JPA_DDL_AUTO` (validate). No cambiarlas en producción.

## Procedimiento en una base con datos (obligatorio para V2 y siguientes)

1. **Backup** (con la app parada o en modo mantenimiento):
   ```bash
   mysqldump --single-transaction --routines --triggers -h <host> -u <user> -p proconnect \
     > proconnect-$(date +%F)-pre-flyway.sql
   ```
2. **Ensayo sobre una copia**: restaurar el dump en una base `proconnect_copy`, arrancar la versión nueva de
   la API apuntando a la copia (`DB_URL=.../proconnect_copy`) y revisar el log de Flyway y la auditoría:
   ```sql
   SELECT version, check_name, affected_rows FROM migration_audit WHERE affected_rows > 0;
   ```
   Si los recuentos de huérfanos/duplicados no son los esperados, **parar** y revisar los datos antes de
   migrar la base real.
3. Arrancar la versión nueva contra la base real (Flyway migra al arrancar).
4. Verificar `SELECT * FROM flyway_schema_history ORDER BY installed_rank;` (todas `success = 1`).

La auditoría (`migration_audit`) queda en la base: es la constancia de qué se borró y cuántas filas.

## Rollback (manual)

Flyway Community no ejecuta *undo*. Los scripts de vuelta atrás están en
`backProconnect/src/main/resources/db/rollback/` (fuera de `db/migration`, nunca los aplica Flyway) y se
ejecutan a mano, en orden inverso, con la versión anterior de la app desplegada:

```bash
mysql proconnect < backProconnect/src/main/resources/db/rollback/U2__constraints_and_indexes.sql
mysql proconnect -e "DELETE FROM flyway_schema_history WHERE version = '2'"
```

Las filas borradas por V2 (huérfanos y duplicados) **no** se recuperan con el rollback: para eso está el
backup del paso 1.

## Tests

- `*Tests` (surefire, H2): suite rápida; el esquema sale de las entidades y Flyway está desactivado.
- `*IT` (failsafe, `mvn verify`): MySQL 8.4 real con Testcontainers, Flyway + `validate`:
  - `FlywaySchemaIT`: base vacía → todas las migraciones + validación + FKs/UNIQUE presentes.
  - `LegacyDatabaseMigrationIT`: base creada por Hibernate con datos sucios → baseline V1 + V2+,
    auditoría, limpieza y datos válidos conservados.
  - `IntegrityIT`: doble like concurrente (un registro, 201 + 409), cascadas, FKs.
- Docker es necesario para los `*IT` (en GitHub Actions `ubuntu-latest` lo tiene). Sin Docker:
  `mvn verify -DskipITs`, o apuntar a un MySQL ya levantado (base vacía `proconnect`):
  ```bash
  docker-compose -f docker-compose.dev.yml up -d mysql
  IT_DB_URL=jdbc:mysql://localhost:3306/proconnect IT_DB_USER=root IT_DB_PASSWORD=... mvn verify
  ```
  (`LegacyDatabaseMigrationIT` se omite en ese modo porque necesita crear su propia base).
- Maven en Docker con Testcontainers (sin JDK local):
  ```bash
  docker run --rm --network host --user "$(id -u):$(id -g)" --group-add "$(stat -c %g /var/run/docker.sock)" \
    -v /var/run/docker.sock:/var/run/docker.sock -e TESTCONTAINERS_RYUK_DISABLED=false \
    -e MAVEN_CONFIG=/tmp/.m2 -v "$HOME/.m2":/tmp/.m2 -v "$PWD":/app -w /app \
    maven:3.9-eclipse-temurin-17 mvn -B -Dmaven.repo.local=/tmp/.m2/repository verify
  ```
