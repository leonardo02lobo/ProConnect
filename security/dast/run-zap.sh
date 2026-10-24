#!/usr/bin/env bash
# DAST autenticado con OWASP ZAP baseline contra el stack E2E (verify-web-security, tarea 1.3).
#
#   security/dast/run-zap.sh            (stack ya levantado; ver dast.yml o README)
#
# Env: WEB_URL (http://localhost:14321), API_URL (http://localhost:18080), OUT_DIR (security/dast/out),
#      ZAP_IMAGE (ghcr.io/zaproxy/zaproxy:stable), DAST_USER_PASSWORD (generado si falta).
# Crea/inicia sesión con un usuario DAST por la API, inyecta la cookie pc_session en cada petición de ZAP
# (replacer) y falla si hay alertas de riesgo alto o reglas marcadas FAIL en rules.tsv.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
WEB_URL="${WEB_URL:-http://localhost:14321}"
API_URL="${API_URL:-http://localhost:18080}"
OUT_DIR="${OUT_DIR:-$ROOT/security/dast/out}"
ZAP_IMAGE="${ZAP_IMAGE:-ghcr.io/zaproxy/zaproxy:stable}"
EMAIL="dast.$(date +%s)@proconnect.test"
PASSWORD="${DAST_USER_PASSWORD:-$(openssl rand -hex 16)}"
[[ -n "${GITHUB_ACTIONS:-}" ]] && echo "::add-mask::$PASSWORD"

mkdir -p "$OUT_DIR"; chmod 777 "$OUT_DIR"
cp "$ROOT/security/dast/rules.tsv" "$OUT_DIR/rules.tsv"

curl -fsS -o /dev/null -H 'Content-Type: application/json' -H "Origin: $WEB_URL" \
  -d "{\"name\":\"DAST $EMAIL\",\"userName\":\"$EMAIL\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" \
  "$API_URL/api/auth/register"
TOKEN=$(curl -fsS -D - -o /dev/null -H 'Content-Type: application/json' -H "Origin: $WEB_URL" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" "$API_URL/api/auth/login" \
  | sed -n 's/^[Ss]et-[Cc]ookie: pc_session=\([^;]*\).*/\1/p' | tr -d '\r')
[[ -n "$TOKEN" ]] || { echo "login DAST falló" >&2; exit 2; }
[[ -n "${GITHUB_ACTIONS:-}" ]] && echo "::add-mask::$TOKEN"

AUTH="-config replacer.full_list(0).description=auth -config replacer.full_list(0).enabled=true \
-config replacer.full_list(0).matchtype=REQ_HEADER -config replacer.full_list(0).matchstr=Cookie \
-config replacer.full_list(0).regex=false -config replacer.full_list(0).replacement=pc_session=$TOKEN"

status=0
scan() { # $1 = nombre, $2 = URL objetivo
  echo "== ZAP baseline: $1 ($2)"
  local rc=0
  docker run --rm --network host -v "$OUT_DIR":/zap/wrk:rw "$ZAP_IMAGE" zap-baseline.py \
    -t "$2" -m 2 -c rules.tsv -I -J "zap-$1.json" -r "zap-$1.html" -w "zap-$1.md" -z "$AUTH" || rc=$?
  # -I: los WARN no fallan; rc=1 => alguna regla FAIL; rc>=3 => error de ZAP.
  if [[ $rc -ne 0 ]]; then status=1; fi
  if python3 - "$OUT_DIR/zap-$1.json" <<'PY'
import json, sys
d = json.load(open(sys.argv[1]))
high = [a["name"] for s in d.get("site", []) for a in s.get("alerts", []) if a.get("riskcode") == "3"]
print("Alertas de riesgo alto:", high or "ninguna")
sys.exit(1 if high else 0)
PY
  then :; else status=1; fi
}

scan web "$WEB_URL/"
scan api "$API_URL/api/posts"
# El token del usuario DAST se revoca al terminar.
curl -fsS -o /dev/null -X POST -H "Origin: $WEB_URL" -b "pc_session=$TOKEN" "$API_URL/api/auth/logout" || true
exit $status
