#!/usr/bin/env sh
set -eu

MAX_WAIT_SECONDS="${2:-120}"
SLEEP_SECONDS=2

if [ "$#" -ge 1 ] && [ -n "$1" ]; then
  CANDIDATE_URLS="$1"
else
  CANDIDATE_URLS="http://localhost:8848/nacos/actuator/health http://localhost:8848/nacos/v1/console/health"
fi

echo "[readiness-gate] Waiting for Nacos (nicos) readiness (timeout ${MAX_WAIT_SECONDS}s)"
echo "[readiness-gate] Candidate endpoints: ${CANDIDATE_URLS}"

elapsed=0
while [ "$elapsed" -lt "$MAX_WAIT_SECONDS" ]; do
  for url in $CANDIDATE_URLS; do
    response="$(curl -fsS "$url" 2>/dev/null || true)"
    if [ -n "$response" ] && (echo "$response" | grep -qi '"UP"\|"healthy"\|true\|ok'); then
      echo "[readiness-gate] Nacos is healthy and reachable via ${url}"
      exit 0
    fi
  done

  if command -v docker >/dev/null 2>&1; then
    nacos_status="$(docker compose ps --format json 2>/dev/null | grep '"Service":"nacos"' || true)"
    if [ -n "$nacos_status" ] && echo "$nacos_status" | grep -qi 'healthy'; then
      echo "[readiness-gate] Nacos container is healthy; waiting for HTTP endpoint..."
    fi
  fi

  sleep "$SLEEP_SECONDS"
  elapsed=$((elapsed + SLEEP_SECONDS))
done

echo "[readiness-gate] BLOCKER: Nacos is not healthy/reachable after ${MAX_WAIT_SECONDS}s"
echo "[readiness-gate] Diagnostics:"
for url in $CANDIDATE_URLS; do
  curl -i -s "$url" | sed -n '1,8p' || true
done
if command -v docker >/dev/null 2>&1; then
  docker compose ps || true
  docker compose logs nacos --tail 80 || true
fi
exit 1
