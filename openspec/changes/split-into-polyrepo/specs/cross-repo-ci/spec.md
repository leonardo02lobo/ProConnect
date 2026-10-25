## ADDED Requirements

### Requirement: CI propio en cada repo hijo
Cada repo hijo MUST ejecutar en sus PRs y en `main`: build, tests unitarios y de integración propios, lint y formato, SAST, SCA, detección de secretos y, en los repos Java, `eol-check`. Las actions MUST estar fijadas por SHA y Dependabot MUST tener cooldown.

#### Scenario: PR que rompe tests del backend
- **WHEN** se abre en `proconnect-backend` un PR con un test fallido
- **THEN** su CI falla y la branch protection de `main` bloquea el merge

### Requirement: Escaneos sin code scanning en repos privados
En los repos privados, CodeQL y Semgrep MUST ejecutarse sin depender de subir SARIF a GitHub Code Scanning y MUST fallar el job ante hallazgos de severidad alta o crítica (o de nivel `error` en Semgrep).

#### Scenario: Hallazgo de CodeQL en repo privado
- **WHEN** un PR introduce una vulnerabilidad detectable por CodeQL en un repo privado
- **THEN** el job de CodeQL falla y su log o artifact muestra el hallazgo, aunque Code Scanning no esté habilitado

### Requirement: Integración en el repo principal
El repo principal MUST ejecutar la suite E2E completa, el DAST autenticado y la comparación de contratos JSON contra los commits de submódulos fijados en cada PR, y MUST requerir esos checks para mergear en `main`.

#### Scenario: PR de bump de submódulo
- **WHEN** se abre en el principal un PR que mueve el puntero de `backend/`
- **THEN** se ejecutan E2E, DAST y contratos sobre esa combinación, y el PR no se puede mergear si fallan

### Requirement: Bump automático de submódulos
Cuando un repo hijo integra cambios en su `main`, el principal MUST recibir una notificación y abrir (o actualizar) automáticamente un PR que mueva el submódulo correspondiente a ese commit.

#### Scenario: Merge en frontend
- **WHEN** se mergea un PR en `main` de `proconnect-frontend`
- **THEN** en pocos minutos existe en `ProConnect` un PR `bump frontend → <sha>` con su CI de integración en ejecución

### Requirement: Acceso a repos privados desde Actions
Los workflows MUST clonar submódulos privados y enviar eventos entre repos usando una credencial dedicada guardada como secret (GitHub App o token fine-grained con permisos mínimos), y MUST NOT usar tokens personales clásicos con alcance amplio.

#### Scenario: Checkout con submódulos
- **WHEN** corre el workflow E2E del principal
- **THEN** el checkout con `submodules: recursive` funciona usando el secret dedicado, y los permisos del token se limitan a lo que necesita (contents read en los hijos y lo justo para dispatch/PR)
