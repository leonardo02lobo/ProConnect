#!/usr/bin/env bash
# Ejecuta k6 (Docker) contra la API del stack pc-perf.
# Uso: perf/bench/run-k6.sh <endpoint> <serial|load> [label] [vus] [duration]
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
EP="$1"; MODE="$2"; LABEL="${3:-baseline}"
VUS="${4:-$([ "$MODE" = serial ] && echo 1 || echo 10)}"; DURATION="${5:-30s}"
PROJECT="${COMPOSE_PROJECT:-pc-perf}"
T="$(node "$HERE/targets.mjs")"
get() { node -e "console.log(JSON.parse(process.argv[1])['$1'])" "$T"; }
mkdir -p "$HERE/../results"
docker run --rm --network "${PROJECT}_default" --user "$(id -u):$(id -g)" \
  -v "$HERE/k6:/scripts:ro" -v "$HERE/../results:/results" \
  -e BASE_URL=http://api:8080 -e ENDPOINT="$EP" -e MODE="$MODE" -e VUS="$VUS" -e DURATION="$DURATION" -e LABEL="$LABEL" \
  -e VIEWER_ID="$(get viewerId)" -e PROFILE_USER_ID="$(get profileUserId)" \
  -e COMMENTS_POST_ID="$(get commentsPostId)" -e LIKE_POST_ID="$(get likePostId)" \
  -e FEED_DEEP_CURSOR="$(get feedDeepCursor)" \
  -e PERF_PASSWORD="$(cd "$HERE/.." && node -e "import('./dataset.mjs').then(m => console.log(m.PERF_PASSWORD))")" \
  grafana/k6:0.54.0 run --quiet /scripts/hot-endpoints.js | tail -1
