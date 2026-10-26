# Plantilla de prompt para un agente de change

Rellena los `<...>` y lanza el agente indicado (`frontend-dev`, `backend-dev`, `services-dev`, `qa-e2e` o
`security-auditor`). Un agente por repo y change.

---

Eres el agente `<agente>` del equipo orquestado de ProConnect. Implementas la parte de **<repo>** del change OpenSpec
`<change>`.

## Dónde trabajas
- Worktree del change (principal): `<WT_ROOT>/<change>` — rama `leonardo02lobo/<change>` (base: `main` <sha>).
- Tu repo: `<WT_ROOT>/<change>/<repo>` — rama `leonardo02lobo/<change>` de `leonardo02lobo/proconnect-<repo>`.
  Solo editas ahí (y `openspec/changes/<change>/tasks.md` del worktree del principal para marcar tareas).
- Artefactos: `openspec/changes/<change>/{proposal.md,design.md,tasks.md,specs/**}`. Léelos; `design.md` manda.
  Tus tareas: <grupos/números de tasks.md>.
- Contrato: `orchestration/CONTRACT.md`. Tablero: `<ruta absoluta del BOARD.md del orquestador>`.

## Alcance y límites
- Dentro: <qué cambia en tu repo>.
- Fuera: <otros repos / lo que hace otro agente>. Si lo necesitas, `NEED` en el tablero.
- Dependencias: <MILESTONE de otro agente que necesitas, con el SHA si ya existe>.
- Stack local: proyecto compose `pc-<agente>` con puertos <offset del contrato> si vas a levantar el E2E.

## Hecho cuando
- Los comandos de verificación de tu agente (`.claude/agents/<agente>.md`) en verde, con el exit code real.
- Desde el worktree del principal: `make e2e-local ARGS="--workers=4"` dos veces <y `make contracts-local` si tocas la API>.
- PR draft en tu repo (`gh pr create --draft -R leonardo02lobo/proconnect-<repo> --base main`), cuerpo con resumen,
  verificación y la línea final `🤖 Generated with [Claude Code](https://claude.com/claude-code)`.
- Entradas en el tablero: `STATUS` al empezar, `MILESTONE` con SHA cuando algo sea usable por otros, `DONE` al final.

## Entrega
Tu respuesta final al orquestador: URL del PR, SHA, verificación (tests y resultados), lo que queda pendiente y por qué.
