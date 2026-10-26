## MODIFIED Requirements

### Requirement: Build y tests por servicio en cada PR
Cada repo de componente MUST tener un workflow `ci.yml` que, en cada pull request y push a `main`, compile y ejecute los tests de su código: `proconnect-backend` (API core), `proconnect-services` (`gateway/` y `media/`) y `proconnect-frontend`. El repo principal MUST tener un `ci.yml` propio para lo que vive en su raíz (typecheck de `e2e/`, actionlint, shellcheck y validación de OpenSpec), sin clonar los submódulos.

#### Scenario: PR que rompe la compilación
- **WHEN** se abre en `proconnect-backend` un PR con código Java que no compila
- **THEN** el job `java (backend)` falla y el check del PR queda en rojo

#### Scenario: Cache de dependencias
- **WHEN** el workflow corre por segunda vez sin cambios en `pom.xml` ni `package-lock.json`
- **THEN** restaura desde cache las dependencias de Maven y npm
