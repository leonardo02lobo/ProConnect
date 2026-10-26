## ADDED Requirements

### Requirement: Gestor de IA en la raíz del repo principal
La raíz del repo principal MUST contener el gestor de IA del sistema: `CLAUDE.md` con el protocolo de orquestación, `.claude/` (comandos y skills de OpenSpec y definiciones de agentes), `openspec/` (specs y changes de todo el sistema) y `orchestration/` (contrato, tablero y plantillas). Los repos hijos MUST NOT duplicar OpenSpec ni el protocolo; solo incluyen un `CLAUDE.md` mínimo que remite al principal.

#### Scenario: Abrir Claude Code en la raíz
- **WHEN** se abre Claude Code en la raíz del principal
- **THEN** están disponibles los comandos `/opsx:*`, los agentes por repo y las instrucciones de orquestación, sin configuración adicional

### Requirement: Agentes por repo
`.claude/agents/` MUST definir al menos un agente para cada repo hijo (frontend, backend, services) y agentes transversales para QA/E2E y seguridad, cada uno limitado a su submódulo o área, con las convenciones de ese repo (toolchain, comandos de test y puertos).

#### Scenario: Asignar una tarea de backend
- **WHEN** el orquestador delega una tarea de API al agente de backend
- **THEN** el agente trabaja solo dentro de `backend/` (o su worktree), usa los comandos de build y test del backend y reporta en el tablero

### Requirement: Changes multi-repo
Cada change de OpenSpec MUST declarar en su proposal los repos afectados. Un script del principal MUST crear, para un change, una rama con el mismo nombre en cada submódulo afectado (y un worktree aislado por repo), y registrar el change en el tablero.

#### Scenario: Iniciar un change que toca frontend y backend
- **WHEN** se ejecuta `orchestration/scripts/start-change.sh add-notifications frontend backend`
- **THEN** existen las ramas `leonardo02lobo/add-notifications` en `proconnect-frontend` y `proconnect-backend` y en el principal, con worktrees listos, y una entrada nueva en `orchestration/BOARD.md`

#### Scenario: Cierre del change
- **WHEN** se mergean los PRs del change en los repos hijos
- **THEN** el principal recibe los PRs de bump y el change se archiva en OpenSpec solo cuando el E2E de integración está verde

### Requirement: Protocolo de coordinación versionado
El contrato (puertos, variables de entorno, auth, convenciones de git) y el tablero de coordinación MUST vivir versionados en `orchestration/` del principal, con el mismo formato de entradas usado hasta ahora (STATUS, MILESTONE, NEED, DECISION, CONTRACT-CHANGE, FINDING, DONE).

#### Scenario: Agente consulta el contrato
- **WHEN** un agente necesita el puerto del gateway o el nombre de la cookie de sesión
- **THEN** lo encuentra en `orchestration/CONTRACT.md` del principal

### Requirement: Triage de IA dirigido al repo responsable
Cuando el E2E de integración falla, el triage con IA del principal MUST identificar el componente responsable y MUST poder abrir un issue en el repo hijo correspondiente con el diagnóstico, sin hacer push de código.

#### Scenario: Fallo causado por el backend
- **WHEN** un bump de `backend/` rompe el E2E
- **THEN** el triage comenta el diagnóstico en el PR del principal y abre un issue en `proconnect-backend` con el test fallido, la evidencia y el archivo probable
