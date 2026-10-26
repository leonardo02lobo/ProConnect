# data-model-integrity Specification

## Purpose
Integridad del modelo de datos: migraciones versionadas, restricciones e índices.
## Requirements
### Requirement: Esquema versionado con migraciones
El esquema de base de datos MUST gestionarse exclusivamente con migraciones Flyway versionadas, y la aplicación MUST arrancar con `spring.jpa.hibernate.ddl-auto=validate`.

#### Scenario: Base de datos vacía
- **WHEN** la API arranca contra un MySQL vacío
- **THEN** Flyway aplica todas las migraciones en orden y Hibernate valida el esquema sin errores

#### Scenario: Entidad desalineada con el esquema
- **WHEN** se agrega un campo a una entidad sin su migración correspondiente
- **THEN** la aplicación falla al arrancar con un error de validación de esquema

### Requirement: Integridad referencial
Toda columna que referencia a otra tabla MUST tener una clave foránea, con comportamiento de borrado explícito (`CASCADE` para likes, comentarios, fotos y postulaciones del recurso padre; `RESTRICT` donde se pierda información de negocio).

#### Scenario: Like a post inexistente
- **WHEN** se intenta insertar un like con un `post_id` que no existe
- **THEN** la base de datos rechaza la inserción y la API responde 404

#### Scenario: Borrar un post
- **WHEN** se elimina un post
- **THEN** se eliminan también sus likes y comentarios

### Requirement: Unicidad de relaciones
La base de datos MUST impedir duplicados en: like por (usuario, post), relación de amistad por (seguidor, seguido), postulación por (usuario, empleo) y foto de perfil por usuario.

#### Scenario: Doble like concurrente
- **WHEN** dos peticiones simultáneas del mismo usuario dan like al mismo post
- **THEN** solo se persiste un like, `like_count` se incrementa una sola vez y la segunda petición recibe 409

### Requirement: Índices para patrones de acceso
El esquema MUST incluir índices que cubran el feed (`posts(created_at, id)`), los posts por autor (`posts(user_id, created_at)`), los comentarios por post (`comments(post_id, created_at)`), las solicitudes de amistad pendientes (`friends(user_id_be_followed, status)`) y los mensajes por receptor (`messages(receiver_id, sent_at)`).

#### Scenario: Plan de ejecución del feed
- **WHEN** se ejecuta `EXPLAIN` sobre la query paginada del feed con el dataset de referencia
- **THEN** usa el índice de `posts(created_at, id)` y no hace full table scan sobre `posts`

### Requirement: Tipos de dominio adecuados
Las fechas MUST modelarse con `java.time` (`Instant` o `LocalDateTime`), y los estados y roles (`Friend.status`, `User.role`) MUST modelarse como enums persistidos como texto.

#### Scenario: Estado inválido de amistad
- **WHEN** se intenta persistir una amistad con un estado fuera de `PENDING`, `ACCEPTED` o `REJECTED`
- **THEN** la operación falla con un error de validación

