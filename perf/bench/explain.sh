#!/usr/bin/env bash
# Captura EXPLAIN / EXPLAIN ANALYZE de las queries actuales (tarea 1.5).
# Uso: perf/bench/explain.sh [label]   -> perf/results/<label>-explain.txt
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
LABEL="${1:-baseline}"
PROJECT="${COMPOSE_PROJECT:-pc-perf}"
T="$(node "$HERE/targets.mjs")"
get() { node -e "console.log(JSON.parse(process.argv[1])['$1'])" "$T"; }
VIEWER=$(get viewerId); PROFILE=$(get profileUserId); POST=$(get commentsPostId)
OUT="$HERE/../results/${LABEL}-explain.txt"
{
  echo "-- targets: viewer=$VIEWER profile_user=$PROFILE post=$POST"
  { echo "SET @viewer=$VIEWER, @profile_user=$PROFILE, @post=$POST;"
    echo "SET @com=(SELECT MIN(id) FROM proconnect.comments WHERE post_id=$POST);"
    cat "$HERE/explain.sql"; } |
    docker exec -i "${PROJECT}-mysql-1" mysql -uroot -pperfroot proconnect --vertical 2>/dev/null
} | sed -e 's/\\n/\n/g' > "$OUT"
echo "Guardado $OUT"
