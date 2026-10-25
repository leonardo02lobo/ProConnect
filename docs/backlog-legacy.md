# Backlog: funcionalidad del backend Express legacy

`BackendProConnect/` (Express + mysql2, puerto 3000) se retiró en `cleanup-codebase`. El código sigue
accesible en el tag git **`legacy-express-backend`** (`git show legacy-express-backend:BackendProConnect/...`).
Ningún cliente lo usaba: el frontend habla solo con el gateway → API Spring (`backProconnect`).

## Ya cubierto por la API Spring

| Legacy (`/api/...`) | Spring | Notas |
|---|---|---|
| `Correos/EnviarMensajePositivo` · `Correos/EnviarMensajeNegativo` | `POST /api/postulations/{id}/accept\|reject` | Migrado (requisito de producto). Mismas plantillas y asunto; el destinatario sale de la BD (el legacy lo tomaba del body: relay abierto) |
| `usuario/crearUsuario`, `usuario/loginUser`, `usuario/logout`, `usuario/getCookie` | `/api/auth/register\|login\|logout\|me` | JWT HS256 en cookie `pc_session` |
| `usuario/`, `usuario/Filtrar/:id`, `usuario/Buscador/:n`, `usuario/FiltrarNombre/:n`, `usuario/FindUser` | `GET /api/users`, `/api/users/{id}`, `/api/users?q=` | |
| `usuario/ValidarNombreUsuario`, `usuario/ValidarEmail` | `POST /api/auth/register` (409 con el campo duplicado) | No hay endpoint de "¿está libre?" |
| `usuario/ActualizarFotoUsuario` | `POST /upload/subirImagen` + `PUT /api/users/{id}/media` | |
| `Empresas/`, `Empresas/CrearEmpresa` | `GET\|POST /api/companies` | Spring exige COMPANY_ADMIN/ADMIN |
| `Empleo/crearEmpleo`, `Empleo/ObtenerTodosLosEmpleos`, `Empleo/ObtenerByID/:id` | `POST /api/jobs`, `GET /api/jobs`, `GET /api/jobs/{id}` | |
| `Empleo/BuscarEmpleoPorNombre/:n` | `GET /api/jobs?q=` | título o descripción |
| `Postulaciones/CrearPostulacion`, `Postulaciones/ObtenerSolicitudes/:id`, `Postulaciones/EliminarPostulacion` | `POST /api/postulations`, `GET /api/jobs/{id}/postulations`, `DELETE /api/postulations/{id}` | |
| `Publicacion/`, `Publicacion/Publicacion/:id`, `Publicacion/CrearPublicacion`, `Publicacion/TodasLasPublicaciones` | `GET /api/posts`, `GET\|POST /api/posts[/{id}]` | |
| `Publicacion/Publicacion/DarLike/:id`, `EliminarLike/:id`, `BuscarLikes/:id` | `POST\|DELETE\|GET /api/posts/{id}/likes` | el legacy mutaba con GET |
| `Comentarios/CrearComentario`, `Comentarios/ObtenerComentariosPorPublicacion/:id` | `POST\|GET /api/posts/{id}/comments` | |
| `Amigos/AgregarAmigos`, `AceptarSolicitud`, `RechazarSolicitud`, `BuscarSolicitudes`, `BuscarTodosLosAmigos/:id`, `EliminarSolicitud/:id` | `/api/friends/requests[...]`, `/api/users/{id}/friends`, `DELETE /api/friends/{userId}` | el legacy borraba con GET |
| `upload` (multer local) | servicio de media `NodeTsPosts` (`/upload/**` en el gateway) | |

## Pendiente (solo existía en el legacy)

| Funcionalidad | Legacy | Propuesta |
|---|---|---|
| Búsqueda de empleo por puesto exacto | `GET Empleo/ObtenerEmpleoByPuesto/:puesto` (solo el primer resultado) | Filtro `GET /api/jobs?workStation=` (el `q` actual busca en título y descripción, no en el puesto) |
| Contadores de seguidores / seguidos | `GET Amigos/BuscarNumeroSeguidores/:id`, `BuscarNumeroSeguidos/:id` (ambas con la misma query: bug) | Incluir `totalFriends` en `GET /api/users/{id}/profile` (el perfil del frontend muestra hoy "8 amigos" fijo) |
| Seguidores con datos de usuario | `GET Amigos/BuscarSeguidores/:id`, `BuscarSeguidoresByID/:id` | `GET /api/users/{id}/friends` devuelve ids; añadir vista con `UserPublicDto` |
| "¿Me postulé a este empleo?" | `POST Postulaciones/RevisarSolicitud` (usuario/empleo en el body) | `GET /api/postulations?jobId=` filtrando por la sesión (hoy `POST` duplicado da 409) |
| Restablecer contraseña | `POST usuario/SetPassword` (sin token ni verificación: cualquiera cambiaba cualquier password) | Flujo de reset con token de un solo uso por correo (reutiliza `ApplicationMailService`) |
| Cambiar la imagen de un post existente | `POST Publicacion/ActualizarFoto` (multipart) | Subir con `/upload/subirImagen` y `PUT /api/posts/{id}` con el nuevo `imageUrl` (ya posible por API; falta UI) |
| UI de empresas, empleos y postulaciones | — (el legacy tampoco tenía UI en este repo) | Páginas en el frontend; hoy solo hay API |
