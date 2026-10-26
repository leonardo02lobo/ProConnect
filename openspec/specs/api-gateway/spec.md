# api-gateway Specification

## Purpose
API Gateway (Spring Cloud Gateway) como única entrada a la API y al servicio de media.
## Requirements
### Requirement: Punto de entrada único
El sistema MUST exponer al frontend un único origen (el gateway, puerto 8080 por defecto) que enruta `/api/**` a la API core y `/upload/**` al servicio de media.

#### Scenario: Ruta de API
- **WHEN** el frontend llama a `GET <gateway>/api/posts`
- **THEN** el gateway reenvía la petición a la API core y devuelve su respuesta sin alterar el path `/api/posts`

#### Scenario: Subida de imagen
- **WHEN** el frontend envía `POST <gateway>/upload/subirImagen` con un archivo multipart
- **THEN** el gateway reenvía la petición al servicio de media en `/subirImagen`

#### Scenario: Servir imagen
- **WHEN** el frontend pide `GET <gateway>/upload/uploads/<archivo>`
- **THEN** el gateway devuelve el archivo servido por el servicio de media

### Requirement: Media protegida por sesión en el gateway
El gateway MUST validar la firma y la expiración del JWT de la cookie de sesión en toda petición a `/upload/**` (subida y lectura de imágenes), y MUST responder 401 si falta o es inválido.

#### Scenario: Imagen sin sesión
- **WHEN** un cliente sin cookie pide `GET <gateway>/upload/uploads/<archivo>`
- **THEN** la respuesta es 401

#### Scenario: Imagen con sesión
- **WHEN** un navegador autenticado carga una `<img>` desde `<gateway>/upload/uploads/<archivo>`
- **THEN** la cookie viaja por ser el mismo site y la imagen se devuelve con 200

### Requirement: La API core no corre gateway en su mismo proceso
La API core MUST ejecutarse como aplicación servlet (Spring MVC) sin dependencias de Spring Cloud Gateway ni WebFlux.

#### Scenario: Arranque de la API core
- **WHEN** se inicia `backProconnect`
- **THEN** arranca sobre un contenedor servlet en su puerto configurado (8081 por defecto) y su classpath no contiene `spring-cloud-starter-gateway`

### Requirement: CORS centralizado con orígenes explícitos
El gateway MUST aplicar CORS con una lista de orígenes permitidos leída de configuración, MUST permitir credenciales y MUST NOT usar el comodín `*`.

#### Scenario: Origen permitido
- **WHEN** llega un preflight desde el origen configurado del frontend
- **THEN** la respuesta incluye `Access-Control-Allow-Origin` con ese origen exacto y `Access-Control-Allow-Credentials: true`

#### Scenario: Origen no permitido
- **WHEN** llega una petición desde un origen que no está en la lista
- **THEN** la respuesta no incluye `Access-Control-Allow-Origin`

