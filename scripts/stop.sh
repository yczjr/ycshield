#!/usr/bin/env bash
# 停止服务（保留数据）： bash scripts/stop.sh
cd "$(dirname "$0")/.."
if docker compose version >/dev/null 2>&1; then DC="docker compose"; else DC="docker-compose"; fi
$DC -f docker-compose.prebuilt.yml down 2>/dev/null || $DC down
echo "已停止（数据保留在 ./data）"
