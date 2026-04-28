#!/bin/bash
set -euo pipefail

echo "=== Iniciando deploy backend ==="

cd /opt/ecommerce
git fetch origin main
git reset --hard origin/main

docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

echo ">> Aguardando healthcheck..."
for attempt in $(seq 1 18); do
  status=$(curl -s -o /dev/null -w "%{http_code}" https://mtpersonalizados.store/api/health || true)
  echo "Health attempt $attempt/18: $status"

  if [ "$status" -ge 200 ] && [ "$status" -lt 400 ]; then
    echo "=== Deploy backend concluido! ==="
    exit 0
  fi

  sleep 5
done

echo "Healthcheck falhou"
exit 1
