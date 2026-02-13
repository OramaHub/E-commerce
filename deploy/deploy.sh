#!/bin/bash
set -e

echo "=== Iniciando deploy ==="

echo ">> Atualizando backend..."
cd /opt/ecommerce
git fetch origin main
git reset --hard origin/main
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

echo ">> Atualizando frontend..."
cd /var/www/ecommerce
git fetch origin main
git reset --hard origin/main
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
npm install
npm run build

echo ">> Recarregando Nginx..."
sudo systemctl reload nginx

echo "=== Deploy concluído! ==="
