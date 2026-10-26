#!/usr/bin/env bash
# Opens (or updates) an issue in the component repo responsible for each APP_BUG of an AI triage.
# Reads the `<!-- triage-components: a,b -->` marker that e2e/prompts/triage.md asks for at the end of
# the diagnosis. Never pushes code: it only creates/comments issues with the diagnosis.
#
#   e2e/scripts/open-component-issues.sh <ai-triage.md> [context line]
#
# Env:
#   GH_TOKEN          token with Issues R/W on the component repos (CI: secrets.PLATFORM_TOKEN; local: gh auth)
#   DRY_RUN=1         print what would be created, do not call GitHub
#   REPO_OWNER        owner of the component repos (default: leonardo02lobo)
#   TRIAGE_KEY        dedup key in the issue title (default: short HEAD sha); one open issue per component+key
set -euo pipefail

FILE="${1:?usage: open-component-issues.sh <ai-triage.md> [context]}"
CONTEXT="${2:-}"
OWNER="${REPO_OWNER:-leonardo02lobo}"
KEY="${TRIAGE_KEY:-$(git rev-parse --short HEAD 2>/dev/null || echo local)}"

[[ -s "$FILE" ]] || { echo "[issues] $FILE is empty, nothing to do" >&2; exit 0; }

marker=$(grep -oE '<!--[[:space:]]*triage-components:[^>]*-->' "$FILE" | tail -n 1 || true)
if [[ -z "$marker" ]]; then
  echo "[issues] no triage-components marker in $FILE, nothing to do" >&2
  exit 0
fi
list=$(sed -E 's/<!--[[:space:]]*triage-components:[[:space:]]*//; s/[[:space:]]*-->//' <<<"$marker" | tr '[:upper:]' '[:lower:]' | tr -d ' ')

repo_for() {
  case "$1" in
    frontend) echo "$OWNER/proconnect-frontend" ;;
    backend) echo "$OWNER/proconnect-backend" ;;
    services | gateway | media) echo "$OWNER/proconnect-services" ;;
    *) echo "" ;;
  esac
}

declare -A seen=()
opened=0
IFS=',' read -r -a comps <<<"$list"
for comp in "${comps[@]}"; do
  [[ -z "$comp" || "$comp" == "none" || "$comp" == "platform" ]] && continue
  repo=$(repo_for "$comp")
  if [[ -z "$repo" ]]; then echo "[issues] unknown component '$comp', skipped" >&2; continue; fi
  [[ -n "${seen[$repo]:-}" ]] && continue
  seen[$repo]=1

  title="E2E: fallo de integración atribuido a ${comp} (${KEY})"
  body=$(
    printf '> Issue abierto por el triage con IA del repo principal (ProConnect). Diagnóstico automático: requiere revisión humana.\n'
    [[ -n "$CONTEXT" ]] && printf '> %s\n' "$CONTEXT"
    printf '\n'
    cat "$FILE"
  )

  if [[ "${DRY_RUN:-0}" == "1" ]]; then
    printf '[issues] DRY_RUN → %s\n  title: %s\n  body: %s líneas\n' "$repo" "$title" "$(wc -l <<<"$body")"
    opened=$((opened + 1))
    continue
  fi

  existing=$(gh issue list -R "$repo" --state open --search "\"$title\" in:title" --json number -q '.[0].number' 2>/dev/null || true)
  if [[ -n "$existing" ]]; then
    gh issue comment "$existing" -R "$repo" --body "$body" >/dev/null
    echo "[issues] commented $repo#$existing"
  else
    url=$(gh issue create -R "$repo" --title "$title" --body "$body")
    echo "[issues] created $url"
  fi
  opened=$((opened + 1))
done

echo "[issues] components with issues: $opened" >&2
