#!/bin/bash
BACKUP_DIR=/opt/backups/postgres
mkdir -p $BACKUP_DIR
docker exec ecommerce-postgres pg_dump -U ecommerce_user ecommerce_db | gzip > $BACKUP_DIR/backup_$(date +%Y%m%d_%H%M%S).sql.gz
find $BACKUP_DIR -mtime +7 -delete
echo "Backup concluído: $(date)"
