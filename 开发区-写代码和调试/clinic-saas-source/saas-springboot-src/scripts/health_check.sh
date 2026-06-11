#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
INDEX_RESPONSE="$(curl -fsS "$BASE_URL/")"

if ! printf '%s' "$INDEX_RESPONSE" | grep -q "<div id=\"app\"></div>"; then
  echo "[HEALTH] root page response missing app mount node" >&2
  exit 1
fi

LOGIN_STATUS="$(curl -fsS -o /dev/null -w '%{http_code}' "$BASE_URL/login1")"
if [ "$LOGIN_STATUS" != "200" ]; then
  echo "[HEALTH] /login1 returned $LOGIN_STATUS" >&2
  exit 1
fi

PID_LINE="$(pgrep -af 'java -jar /root/newsystem-backend-cloud.jar' | head -n 1 || true)"
if [ -z "$PID_LINE" ]; then
  echo "[HEALTH] runtime jar process not found" >&2
  exit 1
fi

echo "[HEALTH] root page OK"
echo "[HEALTH] login page OK"
echo "[HEALTH] process: $PID_LINE"
