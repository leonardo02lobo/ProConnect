## ADDED Requirements

### Requirement: Build y tests por servicio en cada PR
El repositorio MUST tener un workflow `ci.yml` que, en cada pull request y push a `main`, compile y ejecute los tests de `backProconnect`, `gateway`, `NodeTsPosts` y `FrontedProConnect`.

#### Scenario: PR que rompe la compilación
- **WHEN** se abre un PR con código Java que no compila
- **THEN** el job de `backProconnect` falla y el check del PR queda en rojo

#### Scenario: Cache de dependencias
- **WHEN** el workflow corre por segunda vez sin cambios en `pom.xml` ni `package-lock.json`
- **THEN** restaura desde cache las dependencias de Maven y npm

### Requirement: E2E en cada PR
El repositorio MUST tener un workflow `e2e.yml` que en cada PR levante el stack E2E, ejecute Playwright y publique el reporte como artifact.

#### Scenario: E2E exitoso
- **WHEN** todos los tests E2E pasan
- **THEN** el check `e2e` queda en verde y el reporte HTML se publica como artifact

#### Scenario: E2E fallido
- **WHEN** algún test E2E falla
- **THEN** el check `e2e` queda en rojo y se publican como artifacts el reporte, las trazas, los videos y los logs de los contenedores

### Requirement: Workflows ejecutables en local
Los workflows `ci` y `e2e` MUST poder ejecutarse en local con `act` (`make ci-local`), y cada workflow nuevo MUST haberse ejecutado con éxito en local antes de su primer push.

#### Scenario: Ejecutar CI en local
- **WHEN** un desarrollador ejecuta `make ci-local`
- **THEN** `act` ejecuta los jobs de `ci.yml` en contenedores locales y termina con el mismo resultado que tendría en GitHub

#### Scenario: Verificación en GitHub tras validar en local
- **WHEN** la rama del change se sube después de pasar en local
- **THEN** los workflows `ci` y `e2e` corren en GitHub Actions y terminan en verde

### Requirement: Checks obligatorios para merge
La rama `main` MUST requerir que `ci` y `e2e` pasen antes de permitir el merge de un PR.

#### Scenario: Intento de merge con E2E en rojo
- **WHEN** un PR tiene el check `e2e` fallido
- **THEN** GitHub bloquea el botón de merge

### Requirement: Secretos de CI gestionados por GitHub
Los workflows MUST obtener credenciales (DB de test, `JWT_SECRET` de test, `ANTHROPIC_API_KEY`) desde GitHub Secrets o generarlas efímeramente, y MUST NOT tenerlas escritas en los YAML.

#### Scenario: Revisión de workflows
- **WHEN** se inspeccionan los archivos de `.github/workflows/`
- **THEN** todas las credenciales se referencian como `${{ secrets.* }}` o se generan en el propio job
