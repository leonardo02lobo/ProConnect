# job-application-notifications Specification

## Purpose
Notificaciones por correo de las postulaciones a empleos.
## Requirements
### Requirement: Estado de la postulación
Cada postulación MUST tener un estado `PENDING`, `ACCEPTED` o `REJECTED`, que es `PENDING` al crearse.

#### Scenario: Nueva postulación
- **WHEN** un usuario se postula a un empleo
- **THEN** la postulación se crea con estado `PENDING`

### Requirement: Resolución de postulaciones por la empresa
Solo el propietario (`COMPANY_ADMIN` dueño de la empresa del empleo) o un `ADMIN` MUST poder resolver una postulación, mediante `POST /api/postulations/{id}/accept` o `POST /api/postulations/{id}/reject`. Una postulación ya resuelta MUST NOT volver a resolverse.

#### Scenario: Aceptar como propietario
- **WHEN** el dueño de la empresa acepta una postulación `PENDING`
- **THEN** la respuesta es 200 y la postulación queda `ACCEPTED`

#### Scenario: Resolver sin ser propietario
- **WHEN** un usuario que no es dueño de la empresa intenta aceptar o rechazar
- **THEN** la respuesta es 403 y no se envía ningún correo

#### Scenario: Doble resolución
- **WHEN** se intenta rechazar una postulación ya `ACCEPTED`
- **THEN** la respuesta es 409

### Requirement: Correo al postulante al resolver
Al aceptar o rechazar una postulación, el sistema MUST enviar al email registrado del postulante un correo con asunto `Respuesta hacia el puesto de: <título del empleo>` y el cuerpo correspondiente (mensaje positivo o negativo, que conserva el contenido de las plantillas del backend legacy), personalizado con el nombre del postulante, el puesto y el nombre de la empresa. El destinatario MUST obtenerse de la base de datos, nunca del body de la petición.

#### Scenario: Correo de aceptación
- **WHEN** se acepta la postulación de Ana al puesto "Backend Dev" en "Acme"
- **THEN** se envía un correo a su email registrado con el asunto `Respuesta hacia el puesto de: Backend Dev` y un cuerpo que contiene "Hola Ana", "Backend Dev" y "Acme" con el texto de felicitación

#### Scenario: Correo de rechazo
- **WHEN** se rechaza una postulación
- **THEN** se envía el correo con la plantilla negativa

#### Scenario: Destinatario no manipulable
- **WHEN** la petición de resolución incluye un campo `correoDestino` o `email` en el body
- **THEN** el campo se ignora y el correo va solo al email registrado del postulante

### Requirement: Envío asíncrono y tolerante a fallos
El correo MUST enviarse después del commit de la transacción y de forma asíncrona. Un fallo de SMTP MUST NOT revertir el cambio de estado, MUST registrarse en el log y MUST reintentarse hasta 3 veces con backoff.

#### Scenario: SMTP caído
- **WHEN** el servidor SMTP no responde al aceptar una postulación
- **THEN** la postulación queda `ACCEPTED`, la API responde 200 y el log registra los reintentos fallidos

### Requirement: Configuración SMTP por entorno
Host, puerto, usuario, contraseña, TLS y remitente del correo MUST configurarse por variables de entorno (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`). En desarrollo y E2E MUST usarse un servidor de captura local (Mailpit).

#### Scenario: Verificación en E2E
- **WHEN** un test E2E acepta una postulación con el stack E2E
- **THEN** la API de Mailpit muestra el correo recibido para el email del postulante

