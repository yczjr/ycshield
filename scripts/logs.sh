#!/usr/bin/env bash
# 日志： bash scripts/logs.sh [api|web|mysql|redis]
cd "$(dirname "$0")/.."
if docker compose version >/dev/null 2>&1; then DC="docker compose"; else DC="docker-compose"; fi
$DC logs -f --tail=200 "${1:-api}"
