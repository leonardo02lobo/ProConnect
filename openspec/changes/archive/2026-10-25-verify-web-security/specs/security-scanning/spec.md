## ADDED Requirements

### Requirement: Análisis de dependencias
El CI MUST analizar en cada PR las dependencias de Maven y npm de todos los servicios, y MUST fallar ante vulnerabilidades de severidad alta o crítica que no tengan una excepción documentada. Dependabot MUST estar configurado para Maven, npm, Docker y GitHub Actions.

#### Scenario: Dependencia vulnerable
- **WHEN** un PR agrega una dependencia con un CVE crítico conocido
- **THEN** el job de SCA falla e identifica el paquete y el CVE

### Requirement: Análisis estático de código
El CI MUST ejecutar CodeQL (Java y JavaScript/TypeScript) y Semgrep (reglas OWASP) en cada PR, y publicar los hallazgos en GitHub Code Scanning.

#### Scenario: Inyección SQL introducida
- **WHEN** un PR concatena input del usuario en una query nativa
- **THEN** CodeQL o Semgrep reporta el hallazgo en el PR

### Requirement: Detección de secretos
El CI MUST ejecutar gitleaks sobre los commits de cada PR y, semanalmente, sobre todo el historial. Los secretos detectados en el historial MUST constar como rotados en la auditoría.

#### Scenario: Secreto en un PR
- **WHEN** un PR agrega una línea con una clave API o una contraseña literal
- **THEN** el job de secretos falla e indica archivo y línea

#### Scenario: Historial
- **WHEN** corre el escaneo semanal del historial
- **THEN** cualquier hallazgo conocido (p. ej. la contraseña de MySQL anterior) aparece en la allowlist con referencia a su rotación, y no hay hallazgos nuevos sin triage

### Requirement: Escaneo dinámico autenticado
El CI MUST ejecutar OWASP ZAP baseline contra el stack E2E, autenticado con el usuario semilla, al menos en cada push a `main` y semanalmente, y MUST fallar ante alertas de riesgo alto.

#### Scenario: Header faltante
- **WHEN** se elimina el header CSP del gateway
- **THEN** el escaneo ZAP reporta la alerta en el reporte publicado como artifact

### Requirement: Auditoría documentada y suite de regresión
El proyecto MUST mantener `docs/security/audit-<fecha>.md`, con cada hallazgo clasificado (severidad, categoría OWASP, evidencia, remediación, estado) y MUST incluir tests automatizados que reproduzcan cada hallazgo corregido.

#### Scenario: Hallazgo corregido
- **WHEN** se corrige un IDOR descubierto en la auditoría
- **THEN** existe un test que intenta el acceso indebido y espera 403, y la auditoría marca el hallazgo como resuelto con referencia a ese test
