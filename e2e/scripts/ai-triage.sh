#!/usr/bin/env bash
# Local AI triage of a failed E2E run with Claude Code (`claude -p`).
# Same prompt as .github/workflows/ai-e2e-triage.yml (e2e/prompts/triage.md via build-prompt.sh).
#   e2e/scripts/ai-triage.sh [test-results dir] [artifacts dir] [output file]
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"
RESULTS_DIR="${1:-e2e/test-results}"
ARTIFACTS_DIR="${2:-e2e/artifacts}"
OUT="${3:-e2e/ai-triage.md}"
RESULTS_DIR="${RESULTS_DIR%/}"; ARTIFACTS_DIR="${ARTIFACTS_DIR%/}"

command -v claude >/dev/null || { echo "Claude Code CLI not found (npm i -g @anthropic-ai/claude-code)" >&2; exit 1; }
[[ -d "$RESULTS_DIR" ]] || { echo "No results dir $RESULTS_DIR — run the E2E suite first" >&2; exit 1; }
mkdir -p "$ARTIFACTS_DIR/traces"
rm -f "$OUT"

PROMPT="$("$ROOT/e2e/scripts/build-prompt.sh" triage "$RESULTS_DIR" "$ARTIFACTS_DIR" "$OUT")"

# Read-only toolset: it may read code/artifacts, unzip traces into the artifacts dir and
# write ONLY the output file. No Edit, no git, no network.
ALLOWED=(Read Glob Grep LS "Edit(${OUT})" "Bash(unzip:*)" "Bash(ls:*)" "Bash(grep:*)" "Bash(head:*)" "Bash(tail:*)" "Bash(wc:*)" "Bash(find:*)" "Bash(cat:*)")

echo "[ai-triage] running claude -p (model: ${CLAUDE_MODEL:-default}) ..." >&2
answer="$(claude -p "$PROMPT" \
  --allowedTools "${ALLOWED[@]}" \
  --disallowedTools "NotebookEdit" "WebFetch" "WebSearch" "Bash(git push:*)" "Bash(git commit:*)" \
  ${CLAUDE_MODEL:+--model "$CLAUDE_MODEL"} \
  --output-format text)"

# If the model answered on stdout instead of writing the file, persist it ourselves.
if [[ ! -s "$OUT" ]]; then printf '%s\n' "$answer" > "$OUT"; fi
echo
cat "$OUT"
echo
echo "[ai-triage] diagnosis saved to $OUT" >&2
