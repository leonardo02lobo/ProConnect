# e2e-testing Specification

## Purpose
Suite E2E con Playwright sobre el stack completo en Docker Compose.
## Requirements
### Requirement: Stack E2E reproducible
El proyecto MUST proveer un `docker-compose.e2e.yml` que levante MySQL, API core, gateway, servicio de media y frontend con un solo comando, y que cargue datos semilla deterministas.

#### Scenario: Levantar el stack
- **WHEN** se ejecuta `docker compose -f docker-compose.e2e.yml up -d --wait`
- **THEN** todos los servicios quedan `healthy` y `GET <gateway>/api/posts` responde 200

#### Scenario: Datos semilla
- **WHEN** el stack termina de levantarse
- **THEN** existen al menos dos usuarios de prueba con credenciales conocidas y un post con un like y un comentario

### Requirement: Cobertura de flujos críticos
La suite E2E MUST cubrir como mínimo: registro, login, logout, carga del feed, creación de post con imagen, like y unlike, comentario, perfil propio, perfil ajeno y redirección a login sin sesión.

#### Scenario: Ejecución completa
- **WHEN** se ejecuta `npx playwright test` contra el stack E2E
- **THEN** existe al menos un test por cada flujo listado y todos pasan

#### Scenario: Acceso sin sesión
- **WHEN** un navegador sin cookie de sesión abre `/`, `/Profile` o `/user/{id}`
- **THEN** el test verifica la redirección a la página de login en todos los casos

#### Scenario: Ejecución local
- **WHEN** un desarrollador ejecuta `make e2e-local`
- **THEN** se levanta el stack, se ejecuta la suite y se muestra la ruta al reporte HTML

### Requirement: Aislamiento entre tests
Cada test MUST poder ejecutarse en paralelo y de forma independiente. Los datos que crea MUST ser únicos por test (sufijo aleatorio o worker index) y la autenticación MUST reutilizar `storageState` por usuario.

#### Scenario: Ejecución en paralelo
- **WHEN** se ejecuta la suite con `--workers=4` dos veces seguidas sin reiniciar el stack
- **THEN** ambas ejecuciones pasan sin colisiones de datos

### Requirement: Selectores estables
Los tests MUST localizar elementos por rol accesible o por `data-testid`, y MUST NOT usar clases de Tailwind ni XPath posicional.

#### Scenario: Revisión de selectores
- **WHEN** se busca en `e2e/tests` el uso de `locator('.` o `xpath=`
- **THEN** no hay coincidencias

### Requirement: Evidencia de fallos
Ante un fallo, la suite MUST conservar el reporte HTML, la traza de Playwright, una captura y un video del test fallido.

#### Scenario: Test fallido
- **WHEN** un test falla
- **THEN** en `e2e/playwright-report/` y `e2e/test-results/` existen la traza `.zip`, el screenshot y el video de ese test

