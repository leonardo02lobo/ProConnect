# user-auth Specification

## Purpose
Autenticación con sesión JWT en cookie HttpOnly.
## Requirements
### Requirement: Login emite sesión firmada en cookie httpOnly
El sistema MUST autenticar con email y password contra el hash BCrypt almacenado. Si el login es exitoso, MUST emitir un JWT firmado dentro de una cookie `httpOnly`, `SameSite=Lax` (o `Strict`) y `Secure` fuera de desarrollo, con una expiración configurable.

#### Scenario: Credenciales válidas
- **WHEN** un cliente envía `POST /api/auth/login` con email y password correctos
- **THEN** la respuesta es 200, incluye un header `Set-Cookie` con el JWT marcado `HttpOnly` y el body contiene id, name, userName, email y role del usuario

#### Scenario: Credenciales inválidas
- **WHEN** un cliente envía `POST /api/auth/login` con un password incorrecto o un email inexistente
- **THEN** la respuesta es 401 con un mensaje genérico que no revela si el email existe, y no se emite cookie

### Requirement: Las respuestas nunca exponen credenciales
El sistema MUST NOT incluir el campo `password` (ni hash ni texto plano) en ninguna respuesta HTTP de la API.

#### Scenario: Respuesta de login
- **WHEN** el login es exitoso
- **THEN** el JSON de respuesta no contiene la clave `password`

#### Scenario: Endpoints de usuario y perfil
- **WHEN** un cliente consulta `GET /api/user`, `GET /api/user/search/{id}`, `GET /api/user/{userId}/profiles-posts`, el feed de posts o los comentarios
- **THEN** ningún objeto de usuario anidado contiene la clave `password`

### Requirement: Toda la API requiere sesión iniciada
El sistema MUST rechazar con 401 cualquier petición (lectura o escritura) a `/api/**` que no traiga un JWT válido, salvo `POST /api/auth/login`, `POST /api/auth/register` y `POST /api/auth/logout`.

#### Scenario: Crear post sin sesión
- **WHEN** un cliente sin cookie de sesión envía `POST /api/posts/create`
- **THEN** la respuesta es 401 y no se crea ningún registro

#### Scenario: Leer el feed sin sesión
- **WHEN** un cliente sin cookie de sesión pide `GET /api/posts`
- **THEN** la respuesta es 401 y no incluye ningún post

#### Scenario: Leer un perfil sin sesión
- **WHEN** un cliente sin cookie de sesión pide el perfil de cualquier usuario
- **THEN** la respuesta es 401

#### Scenario: Token expirado o manipulado
- **WHEN** un cliente envía una cookie con un JWT expirado o con firma inválida
- **THEN** la respuesta es 401

### Requirement: La identidad proviene del token, no del cliente
El sistema MUST derivar el usuario actor de una mutación a partir del JWT, e ignorar cualquier `userId` que venga en el body o en query params para fijar la autoría.

#### Scenario: Suplantación en creación de post
- **WHEN** el usuario autenticado A envía `POST /api/posts/create` con `userId` del usuario B en el body
- **THEN** el post se crea con autor A

#### Scenario: Like en nombre de otro
- **WHEN** el usuario autenticado A envía `POST /api/like/create` con `userId` de B
- **THEN** el like queda registrado a nombre de A

### Requirement: Sesión actual y logout
El sistema MUST exponer `GET /api/auth/me`, que devuelve el usuario de la sesión actual, y `POST /api/auth/logout`, que invalida la cookie.

#### Scenario: Consultar sesión
- **WHEN** un cliente con sesión válida llama a `GET /api/auth/me`
- **THEN** recibe 200 con los datos públicos del usuario

#### Scenario: Sin sesión
- **WHEN** un cliente sin cookie llama a `GET /api/auth/me`
- **THEN** recibe 401

#### Scenario: Logout
- **WHEN** un cliente autenticado llama a `POST /api/auth/logout`
- **THEN** la respuesta incluye un `Set-Cookie` que expira la cookie de sesión, y las llamadas siguientes a `/api/auth/me` devuelven 401

### Requirement: El frontend solo es navegable con sesión
Todas las páginas del frontend, salvo `/login` y `/register`, MUST redirigir a `/login` si no hay sesión válida. Las páginas renderizadas en servidor MUST reenviar la cookie de sesión del navegador al backend.

#### Scenario: Abrir el feed sin sesión
- **WHEN** un navegador sin cookie de sesión abre `/`
- **THEN** termina en `/login` sin haber mostrado ningún post

#### Scenario: Abrir un perfil por SSR con sesión
- **WHEN** un usuario autenticado abre `/user/{id}`
- **THEN** la página se renderiza con los datos del perfil, porque la cookie se reenvía al backend

### Requirement: Roles del sistema
El sistema MUST reconocer exactamente los roles `USER`, `COMPANY_ADMIN` y `ADMIN`, MUST asignar `USER` por defecto al registrarse y MUST incluir el rol en el JWT.

#### Scenario: Registro por defecto
- **WHEN** un usuario se registra sin especificar rol
- **THEN** se guarda con rol `USER`, aunque el body intente enviar `ADMIN`

### Requirement: Registro valida y hashea
El sistema MUST validar en el registro el formato del email, la unicidad de email y userName y una longitud mínima de password de 8 caracteres, y MUST guardar el password solo como hash BCrypt.

#### Scenario: Email duplicado
- **WHEN** se registra un usuario con un email que ya existe
- **THEN** la respuesta es 409 y no se crea un usuario nuevo

#### Scenario: Registro válido
- **WHEN** se registra un usuario con datos válidos
- **THEN** la respuesta es 201 y la columna `password` en base de datos contiene un hash BCrypt, no el texto plano

