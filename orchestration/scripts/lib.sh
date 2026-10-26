#!/usr/bin/env bash
# Shared helpers for the orchestration scripts (sourced, not executed).
# shellcheck disable=SC2034  # variables used by the sourcing scripts

ORCH_OWNER="${ORCH_OWNER:-leonardo02lobo}"
BRANCH_PREFIX="${BRANCH_PREFIX:-leonardo02lobo/}"
COMPONENTS=(frontend backend services)

# Main checkout of the platform repo (the one that owns .git), even when called from a change worktree.
main_checkout() { # <any dir inside the platform repo>
  local common
  common=$(git -C "$1" rev-parse --path-format=absolute --git-common-dir)
  dirname "$common"
}

repo_slug() {
  case "$1" in
    platform) echo "$ORCH_OWNER/ProConnect" ;;
    frontend | backend | services) echo "$ORCH_OWNER/proconnect-$1" ;;
    *) return 1 ;;
  esac
}

is_component() {
  local c
  for c in "${COMPONENTS[@]}"; do [[ "$c" == "$1" ]] && return 0; done
  return 1
}

die() { printf '\033[1;31m[orch]\033[0m %s\n' "$*" >&2; exit 1; }
log() { printf '\033[1;34m[orch]\033[0m %s\n' "$*" >&2; }

valid_change() { [[ "$1" =~ ^[a-z0-9]+(-[a-z0-9]+)*$ ]] || die "change name must be kebab-case: $1"; }

board_append() { # <board file> <agent> <dest> <type> <message...>
  local board=$1 agent=$2 dest=$3 type=$4; shift 4
  printf '\n## [%s] %s → %s · %s\n%s\n' "$(date '+%Y-%m-%d %H:%M')" "$agent" "$dest" "$type" "$*" >>"$board"
}
