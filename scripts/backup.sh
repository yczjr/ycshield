#!/usr/bin/env bash
# 备份数据库 + 上传附件
set -e
cd "$(dirname "$0")/.."
STAMP=$(date +%Y%m%d-%H%M%S)
mkdir -p backups

PW=$(grep -E '^DB_PASSWORD=' .env | cut -d= -f2)
docker exec blued-mysql mysqldump -uroot -p"$PW" --single-transaction --routines blued \
  | gzip > "backups/blued-db-$STAMP.sql.gz"

tar -czf "backups/blued-upload-$STAMP.tar.gz" -C data upload 2>/dev/null || true

ls -lh backups | tail -3
echo "备份完成：backups/"
