# admin-bootstrap Specification

## Purpose
Creación segura del primer usuario ADMIN al arrancar la API.
## Requirements
### Requirement: Bootstrap opcional e idempotente
Si se configuran un email y un password de bootstrap, el sistema MUST crear al arrancar un usuario `ADMIN` con ese email solo cuando no existe ya. MUST NOT modificar nunca un usuario existente, y sin configuración MUST NOT hacer nada.

#### Scenario: Arranque sin configuración
- **WHEN** la API arranca sin las variables `BOOTSTRAP_ADMIN_*`
- **THEN** no se crea ni se modifica ningún usuario

#### Scenario: Usuario ya existente
- **WHEN** la API arranca con un `BOOTSTRAP_ADMIN_EMAIL` que ya existe con rol `USER`
- **THEN** el usuario conserva su rol y su password

### Requirement: Password desde archivo
El password del bootstrap MUST poder leerse desde un archivo indicado por `BOOTSTRAP_ADMIN_PASSWORD_FILE` (compatible con Docker secrets). Si se definen tanto `BOOTSTRAP_ADMIN_PASSWORD` como `BOOTSTRAP_ADMIN_PASSWORD_FILE`, el arranque MUST fallar con un mensaje claro.

#### Scenario: Docker secret
- **WHEN** se define `BOOTSTRAP_ADMIN_PASSWORD_FILE=/run/secrets/admin_password`
- **THEN** el ADMIN se crea con el contenido del archivo, sin espacios ni saltos de línea finales

#### Scenario: Configuración ambigua
- **WHEN** se definen ambas variables
- **THEN** la API no arranca e indica que debe usarse solo una

### Requirement: Política de password del bootstrap
El password del bootstrap MUST tener al menos 12 caracteres y contener al menos tres de estas cuatro clases: minúsculas, mayúsculas, dígitos y símbolos. MUST NOT registrarse nunca en el log.

#### Scenario: Password débil
- **WHEN** se configura `BOOTSTRAP_ADMIN_PASSWORD=adminadmin12`
- **THEN** la API no arranca e indica la política requerida, sin mostrar el password

### Requirement: userName sin colisiones
El `userName` del ADMIN MUST tomarse de `BOOTSTRAP_ADMIN_USERNAME` si está definido. En caso contrario MUST derivarse del email y, si ya existe, añadir un sufijo numérico creciente. Una colisión MUST NOT impedir el arranque.

#### Scenario: userName derivado ocupado
- **WHEN** existe un usuario `admin` y se configura `BOOTSTRAP_ADMIN_EMAIL=admin@empresa.com` sin `BOOTSTRAP_ADMIN_USERNAME`
- **THEN** se crea el ADMIN con `userName` `admin1` y la API arranca normalmente

#### Scenario: userName explícito ocupado
- **WHEN** `BOOTSTRAP_ADMIN_USERNAME` coincide con un userName existente
- **THEN** la API no arranca e indica el conflicto, porque fue una elección explícita del operador

