#!/usr/bin/env bash
# Medición completa (línea base o "after") sobre el stack pc-perf ya levantado y sembrado.
# Uso: perf/bench/run-all.sh [label]
#   FAST_ENDPOINTS  (profile comments like)   endpoints rápidos: serial 30 s + load 10 VUs 30 s
#   FEED_ENDPOINTS  (feed feedDeep)           feed: serial 30 s + load FEED_LOAD_VUS VUs
#                   (antes del feed paginado, solo `feed`: feedDeep devolvería el feed completo)
#   FEED_LOAD_VUS   (10) en la línea base 3: una petición de feed tardaba minutos.
#   FEED_REPEAT     (1)  repeticiones del conteo de queries del feed (cada una tarda minutos)
#   LOAD_VUS        (10)
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
LABEL="${1:-baseline}"
FAST_ENDPOINTS="${FAST_ENDPOINTS:-profile comments like}"
FEED_ENDPOINTS="${FEED_ENDPOINTS:-feed feedDeep}"
FEED_LOAD_VUS="${FEED_LOAD_VUS:-10}"
FEED_REPEAT="${FEED_REPEAT:-1}"
LOAD_VUS="${LOAD_VUS:-10}"
log() { echo "[$(date -u +%H:%M:%S)] $*"; }

log "1/5 EXPLAIN"
"$HERE/explain.sh" "$LABEL" "${EXPLAIN_SQL:-}"

log "2/5 Conteo de queries (endpoints rápidos)"
node "$HERE/count-queries.mjs" --label "$LABEL" --only profile,comments,likeCreate --repeat 10 --warmup 5

log "3/5 k6 endpoints rápidos (serial 1 VU 30 s + load $LOAD_VUS VUs 30 s)"
for ep in $FAST_ENDPOINTS; do
  "$HERE/run-k6.sh" "$ep" serial "$LABEL" 1 30s
  "$HERE/run-k6.sh" "$ep" load "$LABEL" "$LOAD_VUS" 30s
done

log "4/5 Feed: k6 serial 30 s (también calienta) + conteo de queries"
for ep in $FEED_ENDPOINTS; do
  "$HERE/run-k6.sh" "$ep" serial "$LABEL" 1 30s
done
FEED_ONLY="$(echo $FEED_ENDPOINTS | tr ' ' ',')"
node "$HERE/count-queries.mjs" --label "$LABEL" --only "$FEED_ONLY" --repeat "$FEED_REPEAT" --warmup "${FEED_WARMUP:-0}"

log "5/5 Feed: k6 load $FEED_LOAD_VUS VUs 30 s"
for ep in $FEED_ENDPOINTS; do
  "$HERE/run-k6.sh" "$ep" load "$LABEL" "$FEED_LOAD_VUS" 30s
done

log "Informe"
node "$HERE/report.mjs" "$LABEL"
