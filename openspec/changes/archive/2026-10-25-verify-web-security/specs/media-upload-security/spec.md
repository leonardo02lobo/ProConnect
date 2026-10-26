## ADDED Requirements

### Requirement: Subida solo autenticada
La subida de imágenes MUST requerir una sesión válida. El gateway MUST rechazar con 401 las peticiones a `/upload/**` de escritura sin un JWT válido.

#### Scenario: Subida anónima
- **WHEN** un cliente sin cookie de sesión envía `POST /upload/subirImagen`
- **THEN** la respuesta es 401 y no se escribe ningún archivo

### Requirement: Allowlist de tipos por contenido
El servicio de media MUST aceptar solo JPEG, PNG, WebP y GIF, MUST validar el tipo por magic bytes (no por extensión ni por el `Content-Type` del cliente) y MUST rechazar SVG, HTML y ejecutables.

#### Scenario: HTML disfrazado de imagen
- **WHEN** se sube un archivo `ataque.png` cuyo contenido es HTML con `<script>`
- **THEN** la respuesta es 415 y el archivo no se almacena

#### Scenario: SVG
- **WHEN** se sube un archivo SVG válido
- **THEN** la respuesta es 415

### Requirement: Límite de tamaño
El servicio de media MUST rechazar archivos de más de 5 MB y MUST aceptar un solo archivo por petición.

#### Scenario: Archivo grande
- **WHEN** se sube una imagen de 12 MB
- **THEN** la respuesta es 413 y no queda ningún archivo parcial en disco

### Requirement: Nombres generados en el servidor
El nombre del archivo almacenado MUST generarse en el servidor (UUID + extensión derivada del tipo detectado) y MUST NOT incluir ninguna parte del nombre original.

#### Scenario: Path traversal en el nombre
- **WHEN** se sube un archivo con nombre original `../../index.ts`
- **THEN** el archivo se guarda dentro de `UPLOAD_DIR` con un nombre UUID y no se escribe fuera de ese directorio

### Requirement: Metadatos eliminados
Las imágenes almacenadas MUST NOT conservar metadatos EXIF (incluida la geolocalización).

#### Scenario: Foto con GPS
- **WHEN** se sube un JPEG con metadatos de geolocalización
- **THEN** el archivo servido no contiene bloques EXIF

### Requirement: Servido seguro
Al servir archivos, el servicio MUST enviar el `Content-Type` del tipo detectado y `X-Content-Type-Options: nosniff`, y MUST NOT listar directorios.

#### Scenario: Pedir el directorio
- **WHEN** un cliente pide `GET /upload/uploads/`
- **THEN** la respuesta es 404, sin listado de archivos

#### Scenario: Headers de imagen
- **WHEN** un cliente pide una imagen almacenada
- **THEN** la respuesta incluye `X-Content-Type-Options: nosniff` y un `Content-Type` `image/*`
