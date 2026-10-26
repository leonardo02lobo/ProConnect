# api-error-handling Specification

## Purpose
Errores de la API en formato application/problem+json con traceId.
## Requirements
### Requirement: Errores con código HTTP correcto
La API MUST responder los errores con el código HTTP que corresponde a su causa, y MUST NOT devolver 200 con un mensaje de error en el body.

#### Scenario: Recurso inexistente
- **WHEN** un cliente pide `GET /api/posts/999999` y el post no existe
- **THEN** la respuesta es 404

#### Scenario: Body inválido
- **WHEN** un cliente envía `POST /api/posts` sin `content`
- **THEN** la respuesta es 400 y lista el campo inválido

#### Scenario: Conflicto
- **WHEN** un usuario da like a un post al que ya le dio like
- **THEN** la respuesta es 409

### Requirement: Formato uniforme problem+json
Todas las respuestas de error MUST usar `Content-Type: application/problem+json`, con los campos `type`, `title`, `status`, `detail` e `instance`, más `errors` (lista de `{field, message}`) en los errores de validación.

#### Scenario: Error de validación
- **WHEN** una petición falla la validación de Bean Validation
- **THEN** el body contiene `status: 400` y un arreglo `errors` con un elemento por campo inválido

### Requirement: Sin filtración de detalles internos
Los errores no controlados MUST responder 500 con un mensaje genérico, y MUST NOT incluir stack traces, mensajes de excepción de JDBC/Hibernate ni SQL. El detalle completo MUST registrarse en el log del servidor con un identificador de correlación que también se devuelve en la respuesta.

#### Scenario: Excepción inesperada
- **WHEN** ocurre una excepción no prevista en un service
- **THEN** la respuesta es 500 con `detail` genérico y un `traceId`, y el log del servidor contiene el stack trace asociado a ese `traceId`

