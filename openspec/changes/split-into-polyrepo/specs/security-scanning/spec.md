## MODIFIED Requirements

### Requirement: Análisis estático de código
El CI de cada repo MUST ejecutar CodeQL (Java y/o JavaScript/TypeScript según el repo) y Semgrep (reglas OWASP) en cada PR. En los repos privados sin GitHub Code Security, CodeQL MUST ejecutarse con `upload: never`, fallar el job ante resultados con `security-severity` ≥ 7 y conservar el SARIF como artifact; Semgrep MUST fallar el job ante hallazgos de severidad `ERROR` sin subir SARIF.

#### Scenario: Inyección SQL introducida
- **WHEN** un PR concatena input del usuario en una query nativa
- **THEN** el job de CodeQL o de Semgrep falla y su log o artifact muestra el hallazgo
