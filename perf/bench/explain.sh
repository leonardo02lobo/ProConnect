#!/usr/bin/env bash
# Captura EXPLAIN / EXPLAIN ANALYZE de las queries actuales (tarea 1.5).
# Uso: perf/bench/explain.sh [label]   -> perf/results/<label>-explain.txt
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
LABEL="${1:-baseline}"
# SQL a explicar: explain.sql (fase 1, queries de 03a4ba7), explain-pre.sql (post-cleanup)
# o explain-after.sql (fase 2: feed en 3 queries, perfil, comentarios paginados).
SQL="${2:-}"; SQL="${SQL:-explain.sql}"
PROJECT="${COMPOSE_PROJECT:-pc-perf}"
T="$(node "$HERE/targets.mjs")"
get() { node -e "console.log(JSON.parse(process.argv[1])['$1'])" "$T"; }
VIEWER=$(get viewerId); PROFILE=$(get profileUserId); POST=$(get commentsPostId)
DEEP_CURSOR=$(get feedDeepCursor)
DEEP=$(node -e "const [m,i]=Buffer.from(process.argv[1],'base64url').toString().split(':');console.log(m+' '+i)" "$DEEP_CURSOR")
DEEP_MICROS=${DEEP% *}; DEEP_ID=${DEEP#* }
OUT="$HERE/../results/${LABEL}-explain.txt"
{
  echo "-- targets: viewer=$VIEWER profile_user=$PROFILE post=$POST"
  { echo "SET time_zone='+00:00';"
    echo "SET @viewer=$VIEWER, @profile_user=$PROFILE, @post=$POST, @deep_id=$DEEP_ID;"
    echo "SET @deep_ts=FROM_UNIXTIME($DEEP_MICROS / 1000000);"
    echo "SET @com=(SELECT MIN(id) FROM proconnect.comments WHERE post_id=$POST);"
    cat "$HERE/$SQL"; } |
    docker exec -i "${PROJECT}-mysql-1" mysql -uroot -pperfroot proconnect --vertical 2>/dev/null
} | sed -e 's/\\n/\n/g' > "$OUT"
echo "Guardado $OUT"
