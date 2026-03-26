#!/usr/bin/env sh
set -eu

if [ "$#" -gt 0 ]; then
  exec "$@"
fi

echo "Waiting for Nacos..."
NACOS_ENDPOINTS="http://nacos:8848/nacos/actuator/health http://nacos:8848/nacos/v1/console/health"

for i in $(seq 1 60); do
  for endpoint in $NACOS_ENDPOINTS; do
    if curl -sf "$endpoint" >/dev/null; then
      echo "Nacos ready via $endpoint"
      echo "Starting application..."
      exec java -jar /app/app.jar
    fi
  done
  sleep 2
done

echo "Nacos did not become healthy in time, starting application anyway"

echo "Starting application..."
exec java -jar /app/app.jar
