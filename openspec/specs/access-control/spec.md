# access-control Specification

## Purpose
Autorización por propiedad y rol en la capa de servicio de la API.
## Requirements
### Requirement: Solo el autor modifica su contenido
La API MUST permitir editar o eliminar un post o comentario solo a su autor o a un usuario con rol `ADMIN`. Cualquier otro usuario autenticado MUST recibir 403.

#### Scenario: Editar post ajeno
- **WHEN** el usuario A envía `PUT /api/posts/{id}` sobre un post del usuario B
- **THEN** la respuesta es 403 y el post no cambia

#### Scenario: Borrar comentario propio
- **WHEN** el usuario A elimina un comentario suyo
- **THEN** la respuesta es 204 y el comentario deja de existir

#### Scenario: Admin modera
- **WHEN** un usuario con rol `ADMIN` elimina un post de otro usuario
- **THEN** la respuesta es 204

### Requirement: Permisos por rol
El sistema MUST aplicar exactamente tres roles: `USER` (usar la red y postularse), `COMPANY_ADMIN` (además, crear y gestionar sus empresas, empleos y postulaciones) y `ADMIN` (moderar cualquier recurso). Solo un `COMPANY_ADMIN` o un `ADMIN` MUST poder crear empresas.

#### Scenario: USER crea empresa
- **WHEN** un usuario con rol `USER` intenta crear una empresa
- **THEN** la respuesta es 403

#### Scenario: COMPANY_ADMIN crea empresa
- **WHEN** un usuario con rol `COMPANY_ADMIN` crea una empresa
- **THEN** la respuesta es 201 y queda como propietario

### Requirement: Gestión de empresas, empleos y postulaciones por propietario
Solo el usuario propietario de una empresa MUST poder editarla o eliminarla, crear o borrar sus empleos, y ver o resolver las postulaciones a esos empleos. Un postulante MUST poder ver y retirar solo sus propias postulaciones.

#### Scenario: Ver postulaciones de empleo ajeno
- **WHEN** el usuario A pide las postulaciones de un empleo de una empresa de B
- **THEN** la respuesta es 403

#### Scenario: Crear empleo en empresa ajena
- **WHEN** el usuario A envía la creación de un empleo con `companyId` de una empresa de B
- **THEN** la respuesta es 403

### Requirement: Privacidad de mensajes
Los mensajes MUST ser visibles solo para su emisor y su receptor.

#### Scenario: Leer conversación ajena
- **WHEN** el usuario C pide los mensajes entre A y B
- **THEN** la respuesta es 403, o devuelve solo los mensajes en los que C participa

### Requirement: Gestión de cuenta propia
La actualización y la eliminación de un usuario y de sus fotos MUST estar permitidas solo al propio usuario o a un `ADMIN`. Las solicitudes de amistad MUST aceptarse o rechazarse solo por el destinatario.

#### Scenario: Actualizar otro usuario
- **WHEN** el usuario A envía `PUT /api/users/{idDeB}`
- **THEN** la respuesta es 403

#### Scenario: Aceptar solicitud dirigida a otro
- **WHEN** el usuario A intenta aceptar una solicitud de amistad cuyo destinatario es B
- **THEN** la respuesta es 403

### Requirement: Enumeración no revela recursos privados
Para recursos privados (mensajes, postulaciones), la API MUST responder igual ante un recurso inexistente que ante uno ajeno (404), para no revelar su existencia.

#### Scenario: Postulación ajena
- **WHEN** el usuario A pide `GET /api/postulations/{id}` de una postulación de B
- **THEN** la respuesta es 404, idéntica a la de un id inexistente

