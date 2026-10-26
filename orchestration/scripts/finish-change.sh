#!/usr/bin/env bash
# Closes a change: checks its PRs and the integration, cleans the worktree and branches and archives the
# change in OpenSpec (only when everything is merged and the platform E2E on main is green).
#
#   orchestration/scripts/finish-change.sh [--dry-run] <change>
#   orchestration/scripts/finish-change.sh --abandon <change>    # no checks: drop worktree + local/remote branches
#
# Checks (normal mode), for each repo where branch leonardo02lobo/<change> exists on the remote or had a PR:
#   1. its PR is MERGED;
#   2. for components: the submodule pointer on platform main contains that merge (bump PR merged);
#   3. the last `e2e` run on platform main is green.
# Then: removes $WT_ROOT/<change>, deletes local branches, runs `openspec archive <change> -y` in the main
# checkout (commit it through a PR) and adds a DONE entry to the board.
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib.sh
source "$HERE/lib.sh"

mode=finish
case "${1:-}" in
  --dry-run) mode=dry; shift ;;
  --abandon) mode=abandon; shift ;;
esac
change="${1:-}"; [[ -n "$change" ]] || die "usage: finish-change.sh [--dry-run|--abandon] <change>"
valid_change "$change"

MAIN="$(main_checkout "$HERE")"
WT_ROOT="${WT_ROOT:-$(dirname "$MAIN")}"
BOARD="${ORCH_BOARD:-$MAIN/orchestration/BOARD.md}"
branch="${BRANCH_PREFIX}${change}"
wt="$WT_ROOT/$change"

remote_has_branch() { gh api "repos/$(repo_slug "$1")/branches/${branch//\//%2F}" --silent 2>/dev/null; }

cleanup() { # <delete remote branches: 0|1>
  local r
  if [[ -d "$wt" ]]; then
    for r in "${COMPONENTS[@]}"; do
      if [[ -d "$wt/$r/.git" || -f "$wt/$r/.git" ]] && git -C "$wt/$r" rev-parse --verify --quiet "refs/heads/$branch" >/dev/null; then
        git -C "$wt/$r" checkout --quiet --detach
        git -C "$wt/$r" branch --quiet -D "$branch"
      fi
    done
    git -C "$MAIN" worktree remove --force "$wt"
    log "worktree $wt removed"
  fi
  git -C "$MAIN" worktree prune
  if git -C "$MAIN" show-ref --verify --quiet "refs/heads/$branch"; then
    git -C "$MAIN" branch --quiet -D "$branch"
    log "local branch $branch deleted (platform)"
  fi
  if [[ "$1" == "1" ]]; then
    for r in platform "${COMPONENTS[@]}"; do
      if remote_has_branch "$r"; then
        gh api --method DELETE "repos/$(repo_slug "$r")/git/refs/heads/$branch" --silent
        log "remote branch $branch deleted in $(repo_slug "$r")"
      fi
    done
  fi
}

if [[ "$mode" == "abandon" ]]; then
  cleanup 1
  board_append "$BOARD" "${ORCH_AGENT:-orchestrator}" ALL STATUS "Change \`$change\` abandonado: worktree y ramas \`$branch\` eliminados (sin archivar)."
  exit 0
fi

fail=0
for r in platform "${COMPONENTS[@]}"; do
  slug=$(repo_slug "$r")
  pr=$(gh pr list -R "$slug" --head "$branch" --state all --json number,state,mergeCommit,url -q 'sort_by(.number) | last // empty')
  if [[ -z "$pr" ]]; then
    if remote_has_branch "$r"; then log "$slug: branch $branch has no PR"; fail=1; fi
    continue
  fi
  state=$(jq -r .state <<<"$pr"); url=$(jq -r .url <<<"$pr")
  if [[ "$state" != "MERGED" ]]; then log "$slug: PR $url is $state"; fail=1; continue; fi
  log "$slug: PR $url merged"
  if [[ "$r" != "platform" ]]; then
    merge=$(jq -r '.mergeCommit.oid' <<<"$pr")
    pinned=$(gh api "repos/$ORCH_OWNER/ProConnect/contents/$r?ref=main" -q .sha)
    cmp=$(gh api "repos/$slug/compare/$merge...$pinned" -q .status 2>/dev/null || echo unknown)
    if [[ "$cmp" == "identical" || "$cmp" == "ahead" ]]; then
      log "$r: platform main pins ${pinned:0:7}, includes ${merge:0:7}"
    else
      log "$r: platform main pins ${pinned:0:7}, which does NOT include ${merge:0:7} (merge the bump PR)"; fail=1
    fi
  fi
done

e2e=$(gh run list -R "$ORCH_OWNER/ProConnect" -w e2e -b main -L 1 --json conclusion,headSha,url -q '.[0] // empty')
if [[ "$(jq -r '.conclusion // ""' <<<"${e2e:-{\}}")" == "success" ]]; then
  log "platform e2e on main: success ($(jq -r .url <<<"$e2e"))"
else
  log "platform e2e on main is not green: ${e2e:-no runs}"; fail=1
fi

if [[ $fail -ne 0 ]]; then die "change $change is not ready to finish"; fi
if [[ "$mode" == "dry" ]]; then log "dry run: ready to finish $change"; exit 0; fi

cleanup 0
if [[ -d "$MAIN/openspec/changes/$change" ]]; then
  (cd "$MAIN" && openspec archive "$change" -y)
  log "openspec change archived in $MAIN: commit it through a PR"
fi
board_append "$BOARD" "${ORCH_AGENT:-orchestrator}" ALL DONE "Change \`$change\` cerrado: PRs mergeados, submódulos en main y e2e verde. Worktree y ramas locales eliminados; archivado en OpenSpec."
