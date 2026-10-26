# role-management Specification

## Purpose
Gestión de roles (USER, COMPANY_ADMIN, ADMIN) con auditoría.
## Requirements
### Requirement: Asignación de roles por un ADMIN
El sistema MUST permitir que un usuario con rol `ADMIN` asigne a cualquier otro usuario uno de los roles `USER`, `COMPANY_ADMIN` o `ADMIN` mediante `PUT /api/admin/users/{id}/role` con body `{"role": "<ROL>"}`. Cualquier otro rol MUST recibir 403.

#### Scenario: Promover a COMPANY_ADMIN
- **WHEN** un ADMIN envía `PUT /api/admin/users/{id}/role` con `{"role":"COMPANY_ADMIN"}` para un usuario `USER`
- **THEN** la respuesta es 200 con el usuario y su rol nuevo, y el usuario puede crear empresas

#### Scenario: Usuario sin permisos
- **WHEN** un usuario `USER` o `COMPANY_ADMIN` envía la misma petición
- **THEN** la respuesta es 403 y el rol no cambia

#### Scenario: Rol inválido
- **WHEN** un ADMIN envía `{"role":"SUPERUSER"}`
- **THEN** la respuesta es 400 en formato problem+json

### Requirement: Listado de usuarios para administración
El sistema MUST exponer `GET /api/admin/users`, solo para ADMIN, paginado (máximo 100 por página) y con filtro `q` por nombre, userName o email, que devuelva id, name, userName, email y role.

#### Scenario: Buscar usuario
- **WHEN** un ADMIN pide `GET /api/admin/users?q=ana`
- **THEN** recibe los usuarios que coinciden, con su rol, y metadatos de paginación

### Requirement: Siempre existe al menos un ADMIN
El sistema MUST rechazar con 409 cualquier cambio que deje el sistema sin usuarios `ADMIN`, y MUST impedir que un ADMIN se quite a sí mismo el rol `ADMIN`.

#### Scenario: Auto-degradación
- **WHEN** un ADMIN intenta cambiar su propio rol a `USER`
- **THEN** la respuesta es 409 y su rol sigue siendo `ADMIN`

#### Scenario: Último ADMIN
- **WHEN** solo hay un ADMIN y otro ADMIN, ya degradado concurrentemente, intenta degradarlo
- **THEN** la operación falla con 409 y queda al menos un ADMIN

### Requirement: Auditoría de cambios de rol
Cada cambio de rol MUST registrarse con el actor, el usuario afectado, el rol anterior, el rol nuevo y la fecha. Ese registro MUST NOT poder modificarse por la API.

#### Scenario: Registro de auditoría
- **WHEN** un ADMIN promueve a un usuario
- **THEN** existe una fila en `role_changes` con actor, usuario, `USER` → `COMPANY_ADMIN` y timestamp

### Requirement: Efecto inmediato en sesiones abiertas
El rol efectivo de una petición autenticada MUST ser el rol vigente del usuario en la base de datos (cache de hasta 30 s que se invalida al cambiar el rol), y no solo el claim del JWT.

#### Scenario: Degradación con sesión abierta
- **WHEN** un ADMIN degrada a `USER` a un `COMPANY_ADMIN` que tiene sesión abierta
- **THEN** la siguiente petición de ese usuario para crear una empresa recibe 403, sin que tenga que cerrar sesión

### Requirement: Interfaz de administración
El frontend MUST ofrecer la página `/admin/users`, accesible solo para ADMIN, para buscar usuarios y cambiar su rol. Para el resto de usuarios MUST NOT mostrarse el enlace, y la página MUST redirigir o responder 403.

#### Scenario: ADMIN cambia un rol desde la UI
- **WHEN** un ADMIN busca a un usuario en `/admin/users` y selecciona `COMPANY_ADMIN`
- **THEN** la UI muestra el rol nuevo tras la confirmación del backend

#### Scenario: Usuario normal intenta entrar
- **WHEN** un `USER` abre `/admin/users`
- **THEN** no ve la lista de usuarios y recibe un 403 o una redirección a `/`

### Requirement: Flujo de empresa de punta a punta
Con la gestión de roles, el flujo completo MUST funcionar: el ADMIN promueve a un usuario a `COMPANY_ADMIN`, ese usuario crea una empresa y un empleo, otro usuario se postula y el dueño acepta, lo que envía el correo al postulante.

#### Scenario: E2E de empresa
- **WHEN** se ejecuta el test E2E del flujo de empresa
- **THEN** todos los pasos responden con éxito y Mailpit contiene el correo de aceptación para el postulante

