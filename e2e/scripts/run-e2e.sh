#!/usr/bin/env bash
# Single entry point for the E2E run, shared by `make e2e-local` and .github/workflows/e2e.yml.
#
#   e2e/scripts/run-e2e.sh [playwright args...]
#
# Env:
#   E2E_SKIP_UP=1        do not (re)build/start the stack (assume it is already up)
#   E2E_NO_BUILD=1       start the stack without building images (CI builds them beforehand)
#   E2E_KEEP_STACK=1     leave the stack running at the end (default: keep locally, down in CI)
#   E2E_JWT_SECRET / E2E_DB_PASSWORD   generated per run when unset
#   COMPOSE             compose binary (default: docker-compose, falls back to "docker compose")
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
E2E_DIR="$ROOT/e2e"
ART_DIR="$E2E_DIR/artifacts"
PROJECT="${E2E_COMPOSE_PROJECT:-pc-e2e}"
COMPOSE_FILE="$ROOT/docker-compose.e2e.yml"

if [[ -z "${COMPOSE:-}" ]]; then
  if command -v docker-compose >/dev/null 2>&1; then COMPOSE="docker-compose"; else COMPOSE="docker compose"; fi
fi
dc() { $COMPOSE -p "$PROJECT" -f "$COMPOSE_FILE" "$@"; }

log() { printf '\n\033[1;34m[run-e2e]\033[0m %s\n' "$*"; }

# Ephemeral test secrets (never committed, never printed).
export E2E_JWT_SECRET="${E2E_JWT_SECRET:-$(openssl rand -hex 32)}"
export E2E_DB_PASSWORD="${E2E_DB_PASSWORD:-$(openssl rand -hex 16)}"
if [[ -n "${GITHUB_ACTIONS:-}" ]]; then
  echo "::add-mask::$E2E_JWT_SECRET"
  echo "::add-mask::$E2E_DB_PASSWORD"
fi

collect_logs() {
  mkdir -p "$ART_DIR"
  log "Collecting compose logs (secrets redacted) → $ART_DIR/compose-logs.txt"
  dc ps -a > "$ART_DIR/compose-ps.txt" 2>&1 || true
  dc logs --no-color --timestamps 2>&1 \
    | sed -e "s/${E2E_JWT_SECRET}/***JWT_SECRET***/g" -e "s/${E2E_DB_PASSWORD}/***DB_PASSWORD***/g" \
          -E -e 's/(JWT_SECRET|DB_PASSWORD|MYSQL_PASSWORD|MYSQL_ROOT_PASSWORD|pc_session)([=:"[:space:]]+)[^[:space:]";,]+/\1\2***/g' \
    > "$ART_DIR/compose-logs.txt" || true
}

if [[ "${E2E_SKIP_UP:-0}" != "1" ]]; then
  # Secrets are generated per run, so a stack left over from a previous run would have
  # different credentials: always recreate it.
  log "Starting stack ($PROJECT)"
  dc down -v --remove-orphans >/dev/null 2>&1 || true
  build_flag="--build"; [[ "${E2E_NO_BUILD:-0}" == "1" ]] && build_flag="--no-build"
  if ! dc up -d $build_flag --wait --wait-timeout "${E2E_WAIT_TIMEOUT:-300}"; then
    log "Stack failed to become healthy"
    collect_logs
    dc ps -a
    exit 2
  fi
fi

cd "$E2E_DIR"
if [[ ! -d node_modules ]] || [[ package-lock.json -nt node_modules/.package-lock.json ]]; then
  log "Installing e2e dependencies"
  npm ci --no-audit --no-fund
fi
if [[ -n "${CI:-}" ]]; then
  npx playwright install --with-deps chromium
else
  npx playwright install chromium >/dev/null
fi

log "Running Playwright $*"
npx playwright test "$@"
status=$?

if [[ $status -ne 0 || -n "${CI:-}" ]]; then collect_logs; fi

keep_default=1; [[ -n "${CI:-}" ]] && keep_default=0
if [[ "${E2E_KEEP_STACK:-$keep_default}" != "1" && "${E2E_SKIP_UP:-0}" != "1" ]]; then
  log "Stopping stack"
  dc down -v --remove-orphans >/dev/null 2>&1 || true
fi

log "HTML report: $E2E_DIR/playwright-report/index.html  (npx playwright show-report)"
if [[ $status -ne 0 ]]; then
  log "E2E FAILED (exit $status). Artifacts: e2e/test-results, e2e/playwright-report, e2e/artifacts. Triage: e2e/scripts/ai-triage.sh"
fi
exit $status
