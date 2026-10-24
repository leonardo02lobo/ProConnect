#!/usr/bin/env bash
# Local AI healing of broken E2E tests with Claude Code (`claude -p`) + Playwright test MCP.
# Same prompt as .github/workflows/ai-e2e-heal.yml (e2e/prompts/heal.md via build-prompt.sh).
# It EDITS files in your working tree for you to review (git diff); it never commits or pushes.
#   e2e/scripts/ai-heal.sh [test-results dir] [artifacts dir] [output file]
# Env: E2E_SKIP_RUN=1 to reuse the existing results instead of running the suite first.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"
RESULTS_DIR="${1:-e2e/test-results}"
ARTIFACTS_DIR="${2:-e2e/artifacts}"
OUT="${3:-e2e/ai-heal.md}"

command -v claude >/dev/null || { echo "Claude Code CLI not found" >&2; exit 1; }

if [[ "${E2E_SKIP_RUN:-0}" != "1" ]]; then
  echo "[ai-heal] running the suite to collect current failures ..." >&2
  if E2E_SKIP_UP=1 "$ROOT/e2e/scripts/run-e2e.sh" >/dev/null 2>&1; then
    echo "[ai-heal] suite is green, nothing to heal" >&2; exit 0
  fi
fi
rm -f "$OUT"
PROMPT="$("$ROOT/e2e/scripts/build-prompt.sh" heal "$RESULTS_DIR" "$ARTIFACTS_DIR" "$OUT")"

ALLOWED=(Read Glob Grep LS "Edit(e2e/tests/**)" "Edit(e2e/fixtures/**)" "Edit(FrontedProConnect/src/**)" "Edit(${OUT})"
  "Bash(npx --prefix e2e playwright test:*)" "Bash(ls:*)" "Bash(grep:*)" "Bash(cat:*)" "Bash(git diff:*)" "Bash(git status:*)" mcp__playwright-test)

echo "[ai-heal] running claude -p ..." >&2
answer="$(claude -p "$PROMPT" \
  --mcp-config "$ROOT/e2e/mcp.heal.json" \
  --allowedTools "${ALLOWED[@]}" \
  --disallowedTools "WebFetch" "WebSearch" "Bash(git push:*)" "Bash(git commit:*)" "Bash(git reset:*)" "Bash(git checkout:*)" \
  ${CLAUDE_MODEL:+--model "$CLAUDE_MODEL"} \
  --output-format text)"
if [[ ! -s "$OUT" ]]; then printf '%s\n' "$answer" > "$OUT"; fi

# Guardrail check (same script as CI): no skip/fixme/only, no removed expects or specs.
if ! "$ROOT/e2e/scripts/check-guardrails.sh"; then
  echo "[ai-heal] GUARDRAIL VIOLATION — do not accept these changes as-is." >&2
  cat "$OUT"; exit 3
fi

cat "$OUT"
echo
git --no-pager diff --stat
echo "[ai-heal] review the changes with 'git diff' and open a PR manually if they are correct." >&2
