# feed-performance Specification

## Purpose
Rendimiento del feed y de los listados paginados (consultas acotadas).
## Requirements
### Requirement: Feed paginado por cursor
`GET /api/posts` MUST devolver los posts ordenados por `(createdAt DESC, id DESC)` en páginas de `limit` elementos (por defecto 20, máximo 50), junto con un `nextCursor` opaco que permite pedir la página siguiente.

#### Scenario: Primera página
- **WHEN** un cliente pide `GET /api/posts?limit=20` con al menos 25 posts existentes
- **THEN** recibe 20 posts ordenados del más reciente al más antiguo y un `nextCursor` no nulo

#### Scenario: Última página
- **WHEN** un cliente pide la página con el último `nextCursor` y quedan menos de `limit` posts
- **THEN** recibe los posts restantes y `nextCursor: null`

#### Scenario: Estabilidad ante inserciones
- **WHEN** se crea un post nuevo mientras un cliente pagina
- **THEN** la página siguiente no repite ni omite posts de la secuencia original

#### Scenario: Límite excesivo
- **WHEN** un cliente pide `limit=1000`
- **THEN** recibe como máximo 50 posts

### Requirement: Número de queries acotado
Atender una página del feed MUST ejecutar un número constante de queries SQL (como máximo 4), independiente del número de posts, comentarios y likes de la página.

#### Scenario: Página con muchos comentarios
- **WHEN** se pide una página de 20 posts que en total tienen 500 comentarios
- **THEN** las estadísticas de Hibernate registran como máximo 4 sentencias para esa petición

### Requirement: Contadores precalculados
Cada post MUST exponer `likeCount` y `commentCount` leídos de columnas denormalizadas, que se actualizan en la misma transacción que crea o elimina el like o comentario.

#### Scenario: Like y unlike
- **WHEN** un usuario da like y luego lo quita
- **THEN** `likeCount` sube en 1 y vuelve a su valor original, y coincide con `COUNT(*)` de `likes` para ese post

#### Scenario: Consistencia tras la migración
- **WHEN** se ejecuta la migración que agrega los contadores
- **THEN** para cada post existente `like_count` y `comment_count` son iguales a los `COUNT(*)` reales

### Requirement: Preview de comentarios acotado
Cada post del feed MUST incluir como máximo los 3 comentarios más recientes con su autor. La lista completa MUST obtenerse paginada en `GET /api/posts/{id}/comments`.

#### Scenario: Post con 40 comentarios
- **WHEN** el feed incluye un post con 40 comentarios
- **THEN** ese post trae 3 comentarios en `commentsPreview` y `commentCount: 40`

### Requirement: Perfil sin producto cartesiano
`GET /api/users/{id}/profile` MUST devolver los datos públicos del usuario y la primera página de sus posts, sin unir posts con comentarios en la misma consulta. Las páginas siguientes MUST pedirse con cursor.

#### Scenario: Usuario con 200 posts
- **WHEN** se pide el perfil de un usuario con 200 posts
- **THEN** la respuesta incluye 20 posts y un `nextCursor`, y se ejecutan como máximo 4 sentencias SQL

### Requirement: Listados paginados
Todos los endpoints que listan colecciones (usuarios, empresas, empleos, postulaciones, puestos, mensajes, amigos) MUST aceptar `page` y `size` (máximo 100) y MUST NOT devolver la tabla completa.

#### Scenario: Listado de usuarios
- **WHEN** un cliente pide `GET /api/users?size=500`
- **THEN** recibe como máximo 100 usuarios y metadatos de paginación (`totalElements` o `hasNext`)

