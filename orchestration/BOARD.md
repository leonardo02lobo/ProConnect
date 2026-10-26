# Tablero de coordinación (append-only)

Formato de cada entrada (añadir al final, nunca editar entradas ajenas):

```
## [YYYY-MM-DD HH:MM] <agente> → <destino|ALL> · <TIPO>
<mensaje breve; repo, rutas y commits concretos>
```

TIPOS: `STATUS` (avance), `MILESTONE` (algo usable por otros: indicar repo y commit SHA), `NEED` (bloqueo que otro debe
resolver), `DECISION` (decisión local que afecta a otros), `CONTRACT-CHANGE` (propuesta de cambio a `CONTRACT.md`),
`FINDING` (hallazgo útil para otro change), `DONE`.

Agentes: `orchestrator`, `frontend-dev`, `backend-dev`, `services-dev`, `qa-e2e`, `security-auditor` (con el change entre
paréntesis cuando haya varios en paralelo, p. ej. `backend-dev (add-notifications)`).

El tablero vivo es este archivo en el checkout principal del orquestador; los agentes escriben en él por ruta absoluta
(se la da su prompt) y el orquestador lo commitea al cerrar cada change. Revísalo al empezar y al terminar cada grupo
de tareas. Histórico anterior a la división en repos: [`archive/2026-10-board.md`](archive/2026-10-board.md).

---

## [2026-10-26] orchestrator → ALL · STATUS
Tablero versionado en el repo principal (split-into-polyrepo). Topología: ProConnect (principal) + proconnect-frontend,
proconnect-backend y proconnect-services como submódulos en `frontend/`, `backend/` y `services/`. Contrato en
`orchestration/CONTRACT.md`; protocolo en `CLAUDE.md`.
