## ADDED Requirements

### Requirement: Recursos y verbos HTTP
Los endpoints MUST identificar recursos con sustantivos en plural y expresar la acción con el verbo HTTP. Las rutas MUST NOT contener verbos como `create`, `edit`, `delete`, `find` o `list`.

#### Scenario: CRUD de posts
- **WHEN** un cliente opera sobre posts
- **THEN** usa `GET /api/posts`, `GET /api/posts/{id}`, `POST /api/posts`, `PUT /api/posts/{id}` y `DELETE /api/posts/{id}`

#### Scenario: Recursos anidados
- **WHEN** un cliente gestiona likes y comentarios de un post
- **THEN** usa `POST /api/posts/{postId}/likes`, `DELETE /api/posts/{postId}/likes`, `GET /api/posts/{postId}/comments` y `POST /api/posts/{postId}/comments`

### Requirement: DELETE sin body
Las peticiones `DELETE` MUST identificar el recurso solo por la ruta y la sesión, y MUST NOT requerir body.

#### Scenario: Quitar like
- **WHEN** el usuario autenticado envía `DELETE /api/posts/42/likes` sin body
- **THEN** se elimina su like del post 42 y la respuesta es 204

### Requirement: Respuestas de éxito devuelven el recurso
Las operaciones de creación MUST responder 201 con el recurso creado y un header `Location`. Las actualizaciones MUST responder 200 con el recurso actualizado. Las eliminaciones MUST responder 204.

#### Scenario: Crear post
- **WHEN** un usuario autenticado envía `POST /api/posts` válido
- **THEN** la respuesta es 201, con `Location: /api/posts/{id}` y el JSON del post (incluido su `id`)
