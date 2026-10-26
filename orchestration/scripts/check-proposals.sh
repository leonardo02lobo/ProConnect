#!/usr/bin/env bash
# Every active OpenSpec change must declare the repos it touches (openspec/config.yaml rule):
# a "## Repos" section in proposal.md listing any of: frontend, backend, services, platform.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
fail=0
for p in "$ROOT"/openspec/changes/*/proposal.md; do
  [[ -e "$p" ]] || continue
  [[ "$p" == */archive/* ]] && continue
  change=$(basename "$(dirname "$p")")
  section=$(awk '/^## Repos[[:space:]]*$/{f=1;next} /^## /{f=0} f' "$p")
  if [[ -z "$section" ]]; then
    echo "::error file=${p#"$ROOT"/}::$change: falta la sección '## Repos' (frontend, backend, services, platform)"
    fail=1; continue
  fi
  if ! grep -qE '\b(frontend|backend|services|platform)\b' <<<"$section"; then
    echo "::error file=${p#"$ROOT"/}::$change: '## Repos' no nombra ningún repo (frontend, backend, services, platform)"
    fail=1; continue
  fi
  echo "$change: repos → $(grep -oE '\b(frontend|backend|services|platform)\b' <<<"$section" | sort -u | paste -sd, -)"
done
exit $fail
