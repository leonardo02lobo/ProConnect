## ADDED Requirements

### Requirement: Repos por componente
El sistema MUST estar distribuido en cuatro repositorios privados del mismo propietario: `ProConnect` (principal), `proconnect-frontend`, `proconnect-backend` y `proconnect-services` (que contiene `gateway/` y `media/`).

#### Scenario: Inventario de repos
- **WHEN** se listan los repos del propietario
- **THEN** existen los cuatro, todos con visibilidad privada y rama por defecto `main`

### Requirement: Composición por submódulos
El repo principal MUST referenciar los tres repos hijos como submódulos git en `frontend/`, `backend/` y `services/`, y MUST NOT contener el código fuente de esos componentes fuera de los submódulos.

#### Scenario: Clon completo
- **WHEN** se ejecuta `git clone --recurse-submodules` del repo principal
- **THEN** quedan disponibles `frontend/`, `backend/` y `services/` en los commits fijados por el principal, y `make e2e-local` levanta el sistema completo

#### Scenario: Sin código duplicado
- **WHEN** se buscan en el árbol del principal (sin contar submódulos) archivos `pom.xml`, `astro.config.*` o el `index.ts` del servicio de media
- **THEN** no hay coincidencias

### Requirement: Historial conservado por componente
Cada repo hijo MUST conservar el historial de commits que afectó a su carpeta de origen, con las rutas reescritas a la raíz del repo (o a `gateway/` y `media/` en services), y los autores y fechas originales.

#### Scenario: git log del backend
- **WHEN** se ejecuta `git log --follow pom.xml` en `proconnect-backend`
- **THEN** aparecen los commits históricos que modificaron `backProconnect/pom.xml` en el monorepo

### Requirement: Secretos históricos depurados
Los repos hijos MUST NOT contener en ningún commit las credenciales históricas conocidas (la contraseña antigua de MySQL y las credenciales SMTP del legacy); sus valores MUST reemplazarse durante la reescritura del historial.

#### Scenario: gitleaks sobre el historial
- **WHEN** se ejecuta gitleaks con `--log-opts=--all` en cada repo hijo
- **THEN** no hay hallazgos, y sus `.gitleaksignore` no necesitan entradas heredadas

### Requirement: Monorepo histórico preservado
El repo principal MUST conservar su historial completo (incluido el tag `legacy-express-backend`), de modo que el estado previo a la división se pueda recuperar.

#### Scenario: Volver al estado monorepo
- **WHEN** se hace checkout del tag `pre-split-monorepo` en el principal
- **THEN** el árbol contiene el monorepo completo tal como estaba antes de la división
