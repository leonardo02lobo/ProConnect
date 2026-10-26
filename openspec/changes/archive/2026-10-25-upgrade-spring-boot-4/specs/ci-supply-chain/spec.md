## ADDED Requirements

### Requirement: Actions fijadas por SHA
Toda referencia `uses:` a una action de terceros en `.github/workflows/*` MUST apuntar a un SHA de commit completo (40 caracteres), con un comentario que indique la versión equivalente.

#### Scenario: Revisión de workflows
- **WHEN** se buscan referencias `uses:` con `@v` o con un nombre de rama
- **THEN** no hay coincidencias, salvo actions locales (`./`)

#### Scenario: Semgrep
- **WHEN** Semgrep analiza los workflows
- **THEN** no reporta `github-actions-mutable-action-tag`

### Requirement: Cooldown de actualizaciones automáticas
La configuración de Dependabot MUST definir un `cooldown` (mínimo 3 días, y 7 para actualizaciones mayores) en todos los ecosistemas, para no adoptar versiones recién publicadas que puedan estar comprometidas.

#### Scenario: Revisión de dependabot.yml
- **WHEN** se inspecciona `.github/dependabot.yml`
- **THEN** cada entrada de `updates` tiene `cooldown` y Semgrep no reporta `dependabot-missing-cooldown`

### Requirement: Dependabot mantiene los SHA fijados
Dependabot MUST seguir pudiendo proponer actualizaciones de actions fijadas por SHA (ecosistema `github-actions` habilitado y comentario de versión en formato reconocible).

#### Scenario: Nueva versión de una action
- **WHEN** se publica una versión nueva de una action usada
- **THEN** Dependabot abre un PR que actualiza el SHA y el comentario de versión
