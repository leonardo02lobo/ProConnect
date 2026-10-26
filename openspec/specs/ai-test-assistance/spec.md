# ai-test-assistance Specification

## Purpose
Triage y healing de fallos E2E asistidos por IA (Claude Code), con revisión humana.
## Requirements
### Requirement: Generación de tests asistida por IA
El proyecto MUST incluir la configuración de los Playwright Test Agents (planner, generator, healer) para Claude Code, de modo que un desarrollador pueda pasar de un plan en Markdown a un test ejecutable.

#### Scenario: Generar un test nuevo
- **WHEN** un desarrollador pide al agente planner un plan para un flujo y luego al generator que lo implemente
- **THEN** se crean `e2e/specs/<flujo>.md` y `e2e/tests/<flujo>.spec.ts`, el test pasa contra el stack E2E y cumple las reglas de selectores estables

### Requirement: Flujo de IA validado en local antes de CI
Cada automatización con IA (triage y healing) MUST implementarse como un script versionado (`e2e/scripts/ai-triage.sh`, `e2e/scripts/ai-heal.sh`) con su prompt en `e2e/prompts/`, ejecutable en local con Claude Code (`claude -p`). Los workflows de GitHub Actions MUST invocar esos mismos prompts, sin duplicarlos.

#### Scenario: Triage local
- **WHEN** un desarrollador provoca un fallo E2E en local y ejecuta `e2e/scripts/ai-triage.sh e2e/test-results`
- **THEN** obtiene en consola (y en `e2e/ai-triage.md`) el mismo formato de diagnóstico que publicaría el bot en el PR

#### Scenario: Paridad local/CI
- **WHEN** se compara el prompt usado por `ai-e2e-triage.yml` con el del script local
- **THEN** ambos leen el mismo archivo de `e2e/prompts/`

### Requirement: Triage automático de fallos E2E
Cuando el workflow `e2e` falla en un PR, el sistema MUST ejecutar un agente de IA que analice reporte, trazas y logs, y publique en el PR un comentario con la causa probable, los tests afectados y, si aplica, un diff sugerido.

#### Scenario: Fallo por regresión de backend
- **WHEN** un PR rompe un endpoint y el E2E falla
- **THEN** aparece en el PR un comentario del bot que identifica el test fallido, el request con error y el archivo de código probablemente responsable

#### Scenario: El agente no puede modificar la rama
- **WHEN** corre el workflow de triage
- **THEN** sus permisos se limitan a `contents: read`, `pull-requests: write` y `actions: read`, y no hace push de commits

### Requirement: Reparación de tests bajo revisión humana
El sistema MUST ofrecer un workflow manual que ejecute el agente healer sobre los tests fallidos y abra un PR separado con las correcciones. Ese PR MUST NOT mergearse automáticamente.

#### Scenario: Cambio de UI rompe selectores
- **WHEN** un mantenedor dispara `ai-e2e-heal` después de un rediseño de la UI
- **THEN** se abre un PR con los tests corregidos, una descripción de cada cambio y el resultado de la suite, y queda pendiente de aprobación humana

### Requirement: La IA no debilita las aserciones
Los cambios propuestos por IA MUST NOT eliminar tests, marcarlos como `skip` o `fixme`, ni relajar aserciones sin dejar una justificación explícita en la descripción del PR.

#### Scenario: Healer frente a un bug real
- **WHEN** el test falla porque la aplicación tiene un bug real, no por un cambio de UI
- **THEN** el agente reporta el bug en el comentario o PR y no modifica la aserción para que pase

