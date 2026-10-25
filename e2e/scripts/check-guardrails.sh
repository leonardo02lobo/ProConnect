#!/usr/bin/env bash
# Fails if the working-tree diff (or diff against $1) weakens the E2E suite:
#  - adds test.skip / test.fixme / test.only (or describe/it variants)
#  - deletes a spec file
#  - removes more expect() calls than it adds
# Used by ai-heal.sh (local) and ai-e2e-heal.yml (CI).
#   check-guardrails.sh [base-ref]
set -euo pipefail
BASE="${1:-}"
diff_cmd=(git diff -U0)
[[ -n "$BASE" ]] && diff_cmd+=("$BASE")
fail=0

if "${diff_cmd[@]}" -- e2e/tests e2e/fixtures | grep -nE '^\+.*\b(test|it|describe)(\.describe)?\.(skip|fixme|only)\b'; then
  echo "::error::guardrail: skip/fixme/only added to the E2E suite" ; fail=1
fi
deleted=$("${diff_cmd[@]}" --diff-filter=D --name-only -- e2e/tests || true)
if [[ -n "$deleted" ]]; then echo "::error::guardrail: spec files deleted: $deleted"; fail=1; fi
removed=$("${diff_cmd[@]}" -- e2e/tests | grep -cE '^-[^-].*\bexpect(\.poll)?\(' || true)
added=$("${diff_cmd[@]}" -- e2e/tests | grep -cE '^\+[^+].*\bexpect(\.poll)?\(' || true)
if (( removed > added )); then echo "::error::guardrail: $((removed-added)) expect() call(s) removed"; fail=1; fi
if "${diff_cmd[@]}" -- e2e/tests | grep -nE '^\+.*waitForTimeout'; then echo "::error::guardrail: waitForTimeout added"; fail=1; fi

(( fail == 0 )) && echo "guardrails OK (expect: -$removed/+$added)"
exit $fail
