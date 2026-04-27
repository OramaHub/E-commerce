#!/bin/bash
set -euo pipefail

echo "=== Iniciando deploy backend ==="

cd /opt/ecommerce
git fetch origin main
git reset --hard origin/main

docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

echo ">> Aguardando healthcheck..."
sleep 5
status=$(curl -s -o /dev/null -w "%{http_code}" https://mtpersonalizados.store/api/health || true)
echo "Health: $status"
if [ "$status" -lt 200 ] || [ "$status" -ge 400 ]; then
  echo "Healthcheck falhou"
  exit 1
fi

echo "=== Deploy backend concluido! ==="
