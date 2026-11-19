#!/usr/bin/env bash
# 恢复数据库： bash scripts/restore.sh backups/blued-db-xxx.sql.gz
set -e
cd "$(dirname "$0")/.."
FILE="${1:?用法: bash scripts/restore.sh <备份文件.sql.gz>}"
PW=$(grep -E '^DB_PASSWORD=' .env | cut -d= -f2)

echo "!! 将覆盖当前数据库，5 秒后开始（Ctrl+C 取消）"
sleep 5
gunzip -c "$FILE" | docker exec -i blued-mysql mysql -uroot -p"$PW" blued
echo "恢复完成，建议重启后端： docker restart blued-api"
