## ADDED Requirements

### Requirement: Formato y lint automáticos
Cada proyecto MUST tener formateo y lint configurados (Spotless + Checkstyle en Java; ESLint + Prettier en TypeScript/Astro), y el workflow de CI MUST fallar ante violaciones.

#### Scenario: Código mal formateado en un PR
- **WHEN** un PR introduce código Java que no cumple google-java-format
- **THEN** el paso `mvn spotless:check` falla y el check `ci` queda en rojo

#### Scenario: console.log en el frontend
- **WHEN** un PR agrega `console.log` en `FrontedProConnect/src`
- **THEN** ESLint (`no-console`, permitiendo `warn` y `error`) falla en CI

### Requirement: Convenciones de nombres Java
El código Java MUST usar camelCase para métodos y campos, PascalCase para tipos, y MUST NOT tener campos de entidad o DTO llamados `Id` en mayúscula.

#### Scenario: Checkstyle de nombres
- **WHEN** se ejecuta `mvn checkstyle:check`
- **THEN** no hay violaciones de `MethodName` ni `MemberName`

### Requirement: Sin backend duplicado
El repositorio MUST contener una única implementación del backend de dominio (Spring `backProconnect`). El backend Express legacy MUST eliminarse y quedar accesible solo mediante el tag git `legacy-express-backend`.

#### Scenario: Estructura del repositorio
- **WHEN** se lista la raíz del repositorio
- **THEN** no existe el directorio `BackendProConnect/` y `git tag -l legacy-express-backend` devuelve el tag

### Requirement: Rutas de autenticación con nombres correctos
El frontend MUST exponer la página de inicio de sesión en `/login` y la de registro en `/register`, cada una con la funcionalidad que indica su nombre.

#### Scenario: Navegar a login
- **WHEN** un usuario abre `/login`
- **THEN** ve un formulario de email y password que inicia sesión

#### Scenario: Navegar a registro
- **WHEN** un usuario abre `/register`
- **THEN** ve un formulario de creación de cuenta (nombre, userName, email, password) que registra al usuario

### Requirement: Actualizaciones de UI sin recarga completa
Las acciones de like y comentario en el frontend MUST actualizar el estado del componente sin `window.location.reload()`.

#### Scenario: Comentar
- **WHEN** un usuario envía un comentario válido
- **THEN** el comentario aparece en la lista y el contador se incrementa sin que se recargue la página
