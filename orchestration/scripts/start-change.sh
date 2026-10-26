#!/usr/bin/env bash
# Starts a (multi-repo) change: same branch name in the platform repo and in every affected component,
# one isolated worktree for the change and a board entry.
#
#   orchestration/scripts/start-change.sh [--no-push] [--base <ref>] <change> [frontend] [backend] [services]
#
# Result (WT_ROOT defaults to the parent directory of the platform main checkout):
#   $WT_ROOT/<change>/            worktree of ProConnect on leonardo02lobo/<change> (from --base, default origin/main)
#   $WT_ROOT/<change>/<repo>/     the submodule, on leonardo02lobo/<change> (from its origin/main)
# Unaffected submodules stay at the commit pinned by main, so `make e2e-local` in the change worktree tests
# exactly "main + this change". Branches are pushed (-u) unless --no-push.
# Env: WT_ROOT, ORCH_BOARD (default: orchestration/BOARD.md of the main checkout), ORCH_AGENT (default: orchestrator).
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib.sh
source "$HERE/lib.sh"

push=1
base=origin/main
while [[ "${1:-}" == --* ]]; do
  case "$1" in
    --no-push) push=0; shift ;;
    --base) base="${2:?--base needs a ref}"; shift 2 ;;
    *) die "unknown option $1" ;;
  esac
done
change="${1:-}"; [[ -n "$change" ]] || die "usage: start-change.sh [--no-push] [--base <ref>] <change> [frontend] [backend] [services]"
shift
valid_change "$change"
repos=("$@")
for r in "${repos[@]}"; do is_component "$r" || die "unknown component '$r' (frontend, backend, services)"; done

MAIN="$(main_checkout "$HERE")"
WT_ROOT="${WT_ROOT:-$(dirname "$MAIN")}"
BOARD="${ORCH_BOARD:-$MAIN/orchestration/BOARD.md}"
branch="${BRANCH_PREFIX}${change}"
wt="$WT_ROOT/$change"

[[ -e "$wt" ]] && die "$wt already exists"
if [[ ! -d "$MAIN/openspec/changes/$change" ]]; then
  log "warning: openspec/changes/$change does not exist yet in $MAIN (create the proposal with /opsx:propose)"
fi

git -C "$MAIN" fetch --quiet origin main
git -C "$MAIN" rev-parse --verify --quiet "$base^{commit}" >/dev/null || die "unknown base ref $base"
git -C "$MAIN" show-ref --verify --quiet "refs/heads/$branch" && die "branch $branch already exists in the platform repo"
for r in "${repos[@]}"; do
  [[ "$(git -C "$MAIN" ls-tree "$base" -- "$r" | awk '{print $2}')" == "commit" ]] \
    || die "$base has no submodule '$r' (is the platform split merged? use --base <ref>)"
done
log "platform: $branch from $base → $wt"
git -C "$MAIN" worktree add --quiet --no-track -b "$branch" "$wt" "$base"
git -C "$wt" submodule update --init --recursive --quiet

for r in "${repos[@]}"; do
  log "$r: $branch from origin/main → $wt/$r"
  git -C "$wt/$r" fetch --quiet origin main
  git -C "$wt/$r" checkout --quiet -b "$branch" origin/main
  git -C "$wt/$r" branch --quiet --unset-upstream 2>/dev/null || true
done

if [[ $push -eq 1 ]]; then
  git -C "$wt" push --quiet -u origin "$branch"
  for r in "${repos[@]}"; do git -C "$wt/$r" push --quiet -u origin "$branch"; done
  log "branches pushed"
fi

list="platform${repos[*]:+ ${repos[*]}}"
board_append "$BOARD" "${ORCH_AGENT:-orchestrator}" ALL STATUS \
  "Change \`$change\` iniciado: rama \`$branch\` en ${list// /, }. Worktree \`$wt\` (submódulos afectados en \`$wt/<repo>\`)."
log "board entry added to $BOARD"
printf '%s\n' "$wt"
