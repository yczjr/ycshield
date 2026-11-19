#!/usr/bin/env bash
# 查看状态： bash scripts/status.sh
cd "$(dirname "$0")/.."
if docker compose version >/dev/null 2>&1; then DC="docker compose"; else DC="docker-compose"; fi
$DC ps 2>/dev/null | head -10
PORT=$(grep -E '^WEB_PORT=' .env 2>/dev/null | cut -d= -f2)
echo
curl -s -o /dev/null -w "健康检查 http://127.0.0.1:${PORT:-8090}/api/v1/health -> %{http_code}\n" \
  "http://127.0.0.1:${PORT:-8090}/api/v1/health" || true
docker stats --no-stream --format '{{.Name}}  内存 {{.MemUsage}}  CPU {{.CPUPerc}}' 2>/dev/null | head -6 || true
