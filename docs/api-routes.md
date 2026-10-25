# Rutas de la API (cleanup-codebase)

Todas las rutas van bajo el gateway (`/api/**` → API core). Salvo `POST /api/auth/{login,register,logout}` y
`GET /actuator/health`, todas exigen la cookie de sesión `pc_session`. La autoría de cualquier mutación sale
siempre de la sesión, nunca del body. Los errores responden `application/problem+json` (RFC 7807) con
`type`, `title`, `status`, `detail`, `instance`, `traceId` y, en validación, `errors[] {field, message}`.

Convenciones: crear → `201` + `Location` + recurso; actualizar → `200` + recurso; borrar → `204`;
`DELETE` sin body. Las rutas antiguas se eliminaron sin alias (el único consumidor es el frontend propio,
migrado en el mismo change).

## Auth (sin cambios de ruta)

| Antes | Después | Notas |
|---|---|---|
| `POST /api/auth/register` | `POST /api/auth/register` | 201 + `Location: /api/users/{id}`; 400 con `errors[]`; 409 duplicado |
| `POST /api/auth/login` | `POST /api/auth/login` | 401 problem+json genérico |
| `GET /api/auth/me` | `GET /api/auth/me` | |
| `POST /api/auth/logout` | `POST /api/auth/logout` | 204 |

## Posts, likes y comentarios

| Antes | Después | Notas |
|---|---|---|
| `GET /api/posts` · `GET /api/posts/{userId}` (feed) | `GET /api/posts` | viewer de la sesión; `[]` si no hay posts (antes `null`); orden más nuevo primero; `totalComentarios` → `totalComments` |
| `GET /api/posts/find/{Id}` | `GET /api/posts/{id}` | 404 si no existe (antes 200 vacío) |
| `POST /api/posts/create` | `POST /api/posts` | 201 + `Location`; `title` y `content` obligatorios (≤255) |
| `PUT /api/posts/edit/{Id}` | `PUT /api/posts/{id}` | 200 + post |
| `DELETE /api/posts/delete/{Id}` | `DELETE /api/posts/{id}` | 204; borra también sus likes y comentarios |
| `GET /api/like/list/{postId}` | `GET /api/posts/{postId}/likes` | |
| `POST /api/like/create` (body `{postId}`) | `POST /api/posts/{postId}/likes` | sin body; 201; 409 si ya existía |
| `DELETE /api/like/delete` (body `{postId}`) | `DELETE /api/posts/{postId}/likes` | sin body; 204; 404 si no había like |
| `GET /api/comment/find/{postId}` | `GET /api/posts/{postId}/comments` | con autor; 404 si el post no existe |
| `POST /api/comment/create` (body `{postId, content}`) | `POST /api/posts/{postId}/comments` | body `{content}`; 201 con el comentario y su autor |
| `DELETE /api/comment/delete/{Id}` | `DELETE /api/comments/{id}` | 204 (antes 401 aunque borraba) |

## Usuarios, fotos y amistades

| Antes | Después | Notas |
|---|---|---|
| `GET /api/user` | `GET /api/users` | |
| `GET /api/user/search/name/{name}` | `GET /api/users?q={texto}` | busca en nombre **y** userName |
| `GET /api/user/search/{id}` | `GET /api/users/{id}` | 404 si no existe |
| `PUT /api/user/update/{id}` | `PUT /api/users/{id}` | 200 + usuario; 409 si email/userName son de otro |
| `DELETE /api/user/delete/{id}` | `DELETE /api/users/{id}` | 204 |
| `POST /api/user/create` | — (eliminada) | duplicaba `POST /api/auth/register` |
| `GET /api/user/{userId}/profiles-posts` | `GET /api/users/{id}/profile` | 404 si no existe; `userLiked` del viewer |
| `POST /api/profilePhoto/create` | `PUT /api/users/{id}/media` | restructure-models-performance (V3): avatar/portada son columnas de `user`; antes `POST /api/profile-photos` |
| `GET /api/profilePhoto/{id}` | `GET /api/users/{id}` (`avatarUrl`, `coverUrl`) | antes `GET /api/profile-photos/{id}` |
| `POST /api/profilePhoto/updateProfilePhoto` · `POST /api/profilePhoto/updateWallPhoto` (id en body) | `PUT /api/users/{id}/media` | parcial: los campos `null` no cambian |
| `POST /api/friend/create?userIdBeFollowed=` | `POST /api/friends/requests` | body `{userId}`; 201; 400 a uno mismo; 409 si ya hay solicitud o amistad |
| `GET /api/friend/pending/{userId}` | `GET /api/friends/requests` | recibidas por la sesión; ahora incluyen el `id` (antes no) |
| `PUT /api/friend/accept/{x}` | `POST /api/friends/requests/{id}/accept` | solo el destinatario; 409 si ya resuelta |
| `PUT /api/friend/reject/{x}` | `POST /api/friends/requests/{id}/reject` | solo el destinatario |
| `DELETE /api/friend/cancel?userIdBeFollowed=` | `DELETE /api/friends/requests/{id}` | solo el emisor, solo pendientes |
| `DELETE /api/friend/delete?userIdBeFollowed=` | `DELETE /api/friends/{userId}` | 204; 404 si no sois amigos |
| `GET /api/friend/all/{userId}` | `GET /api/users/{id}/friends` | `[]` en vez de 204 |
| `GET /api/friend/find?userId=&otherUserId=` | `GET /api/users/{id}/friendship` | relación entre la sesión y `{id}`; 404 si no hay |

## Mensajes

| Antes | Después | Notas |
|---|---|---|
| `POST /api/message/create` | `POST /api/messages` | 201; 404 si el destinatario no existe |
| `GET /api/message/list/{userId}` | `GET /api/messages` | enviados y recibidos por la sesión |
| `DELETE /api/message/delete/{Id}` | `DELETE /api/messages/{id}` | 204 (antes 200 con texto) |

## Empresas, empleos y postulaciones

| Antes | Después | Notas |
|---|---|---|
| `GET /api/company` | `GET /api/companies` | |
| — | `GET /api/companies/{id}` | nuevo |
| `POST /api/company/create` | `POST /api/companies` | 201; solo COMPANY_ADMIN o ADMIN |
| `PUT /api/company/edit/{Id}` | `PUT /api/companies/{id}` | 200 + empresa |
| `DELETE /api/company/delete/{Id}` | `DELETE /api/companies/{id}` | 204 |
| `GET /api/job` | `GET /api/jobs` | |
| `GET /api/job/search?keyword=` | `GET /api/jobs?q={texto}` | título o descripción |
| — | `GET /api/jobs/{id}` | nuevo |
| `POST /api/job/create` | `POST /api/jobs` | 201; solo el dueño de la empresa o ADMIN |
| `DELETE /api/job/delete/{Id}` | `DELETE /api/jobs/{id}` | 204 |
| `GET /api/postulation/job/{jobId}` | `GET /api/jobs/{id}/postulations` | solo el dueño de la empresa o ADMIN |
| `POST /api/postulation/create` | `POST /api/postulations` | body `{jobId}`; 201 con `status: PENDING`; 409 si ya te postulaste |
| `GET /api/postulation` | `GET /api/postulations` | propias + recibidas en mis empresas (ADMIN: todas) |
| `GET /api/postulation/id/{id}` | `GET /api/postulations/{id}` | 404 si no es tuya ni de tu empresa |
| `PUT /api/postulation/update/{id}` | `PUT /api/postulations/{id}` | ahora persiste; 409 si ya está resuelta |
| `DELETE /api/postulation/delete/{id}` | `DELETE /api/postulations/{id}` | 204 |
| `POST /api/Correos/EnviarMensajePositivo` (Express) | `POST /api/postulations/{id}/accept` | dueño de la empresa o ADMIN (postulante → 403; ajeno → 404); 409 si ya resuelta; correo al email registrado del postulante |
| `POST /api/Correos/EnviarMensajeNegativo` (Express) | `POST /api/postulations/{id}/reject` | ídem con la plantilla negativa |

## Puestos de trabajo (workstations)

| Antes | Después | Notas |
|---|---|---|
| `GET /api/workstation` | `GET /api/workstations` | `[]` en vez de 204 |
| `GET /api/workstation/search/workStation/{text}` | `GET /api/workstations?q={texto}` | |
| `GET /api/workstation/search/{id}` | `GET /api/workstations/{id}` | 404 si no existe |
| `POST /api/workstation/create` | `POST /api/workstations` | 201 |
| `PUT /api/workstation/update/{id}` | `PUT /api/workstations/{id}` | 200 + recurso |
| `DELETE /api/workstation/delete/{id}` | `DELETE /api/workstations/{id}` | 204 |

## Paginación y forma de las respuestas (restructure-models-performance)

**BREAKING** (frontend y E2E migrados en el mismo change). Dos formas de paginar:

- **Por cursor** (listas cronológicas que crecen por arriba; estable ante inserciones):
  `?limit=` (20 por defecto, máx. 50) y `?cursor=` = `nextCursor` de la página anterior (opaco; inválido → 400).
  Respuesta `{ "items": [...], "nextCursor": "…" | null }`.
  | Ruta | Orden | Notas |
  |---|---|---|
  | `GET /api/posts` | más nuevo primero | 3 sentencias SQL por página |
  | `GET /api/users/{id}/posts` | más nuevo primero | páginas siguientes del perfil |
  | `GET /api/posts/{postId}/comments` | más antiguo primero | 1 sentencia por página |
- **Por página** (listados): `?page=` (0-based) y `?size=` (20 por defecto, máx. 100).
  Respuesta `{ "items": [...], "page": 0, "size": 20, "hasNext": true }` (sin `COUNT(*)` de la tabla).
  Aplica a `GET /api/users[?q=]`, `/api/companies`, `/api/jobs[?q=]`, `/api/jobs/{id}/postulations`,
  `/api/workstations[?q=]`, `/api/messages`, `/api/friends/requests`, `/api/users/{id}/friends`, `/api/postulations`,
  `/api/posts/{postId}/likes`.

Cambios de campos:

| Recurso | Antes | Después |
|---|---|---|
| Post del feed/perfil | `totalLikes`, `totalComments`, `comments[]` (todos) | `likeCount`, `commentCount` (contadores denormalizados), `commentsPreview[]` (los 3 más recientes, en orden cronológico) |
| Usuario público (`/me`, autores, búsqueda, perfil) | `photoUrl`, `wallPhotoUrl` | `avatarUrl`, `coverUrl` |
| Perfil `GET /api/users/{id}/profile` | `posts[]` (todos) | `posts[]` (primera página, 20) + `nextCursor` |
| Fotos | `/api/profile-photos[/{id}]` | `PUT /api/users/{id}/media` `{avatarUrl?, coverUrl?}`: parcial (`null` = sin cambio, `""` = borrar); propietario o ADMIN (403) |

Like y comentario mantienen sus rutas; un doble like (también concurrente) responde 409 y no altera `likeCount`.

