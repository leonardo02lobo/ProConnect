## ADDED Requirements

### Requirement: Headers de seguridad
Todas las respuestas que pasan por el gateway MUST incluir `Content-Security-Policy` (sin `unsafe-eval`; `script-src` limitado a `self`), `X-Content-Type-Options: nosniff`, `Referrer-Policy: strict-origin-when-cross-origin`, `Permissions-Policy` restrictiva y `frame-ancestors 'none'`. Fuera de desarrollo MUST incluir además `Strict-Transport-Security` con `max-age` ≥ 15552000.

#### Scenario: Revisar headers
- **WHEN** un cliente pide cualquier ruta a través del gateway
- **THEN** la respuesta incluye todos los headers listados

#### Scenario: Embebido en iframe
- **WHEN** un sitio de terceros intenta cargar ProConnect en un iframe
- **THEN** el navegador lo bloquea por `frame-ancestors 'none'`

### Requirement: Protección CSRF en mutaciones
La API MUST rechazar con 403 las peticiones `POST`, `PUT`, `PATCH` y `DELETE` autenticadas por cookie cuyo header `Origin` (o `Referer` en su ausencia) no pertenezca a la lista de orígenes permitidos.

#### Scenario: Formulario desde otro sitio
- **WHEN** un sitio malicioso envía un `POST /api/posts` con la cookie del usuario y `Origin: https://evil.example`
- **THEN** la respuesta es 403 y no se crea el post

### Requirement: Rate limiting de autenticación
Los endpoints de login y registro MUST limitar los intentos por IP (máximo 10 por minuto) y por cuenta (máximo 5 fallos consecutivos), con bloqueo temporal de 15 minutos. Al superarse el límite MUST responder 429 con `Retry-After`.

#### Scenario: Fuerza bruta sobre una cuenta
- **WHEN** se envían 6 intentos de login fallidos seguidos para el mismo email
- **THEN** el sexto y los siguientes durante 15 minutos reciben 429 aunque el password sea correcto

#### Scenario: Ráfaga desde una IP
- **WHEN** una IP envía 11 peticiones de login en menos de un minuto
- **THEN** la undécima recibe 429 con header `Retry-After`

### Requirement: Cookie de sesión endurecida
La cookie de sesión MUST tener `HttpOnly`, `SameSite=Lax` o `Strict`, `Path=/` y, fuera de desarrollo, `Secure`. Su valor MUST NOT ser legible desde JavaScript.

#### Scenario: Leer la cookie desde el navegador
- **WHEN** se evalúa `document.cookie` en la página después de iniciar sesión
- **THEN** el resultado no contiene la cookie de sesión

### Requirement: Datos sensibles fuera de las respuestas y logs
Las respuestas de la API MUST NOT contener `password`, hashes, tokens ni emails de terceros en vistas públicas. Los logs MUST NOT registrar passwords ni tokens.

#### Scenario: Perfil público de otro usuario
- **WHEN** el usuario A consulta el perfil de B
- **THEN** la respuesta no incluye el email de B ni ningún campo de credenciales

#### Scenario: Log de login fallido
- **WHEN** falla un login
- **THEN** el log registra el email (o un hash del email) y la IP, pero no el password
