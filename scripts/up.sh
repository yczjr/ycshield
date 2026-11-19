#!/usr/bin/env bash
# 一键启动：.env → 下载 IP 库 → 国内源拉镜像 → 构建/启动 → 健康检查
set -e
cd "$(dirname "$0")/.."

# 载入 .env（已存在的环境变量优先）
if [ -f .env ]; then
  while IFS= read -r line; do
    case "$line" in ''|'#'*) continue ;; esac
    k="${line%%=*}"; v="${line#*=}"
    [ -n "${!k:-}" ] && continue
    export "$k=$v"
  done < .env
else
  cp .env.example .env
  echo "== 已生成 .env（可改端口/密码后重新执行）=="
fi

# 兼容 docker compose 插件 与 老版 docker-compose
if docker compose version >/dev/null 2>&1; then
  DC="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
  DC="docker-compose"
else
  echo "!! 未找到 docker compose，请先安装："
  echo "   sudo apt update && sudo apt install -y docker.io docker-compose-plugin"
  exit 1
fi
echo "使用命令：$DC"

bash scripts/fetch-ipdb.sh || echo "!! IP 库未下载成功，大屏地图将无热力点（其余功能正常）"

# ---- 关键：先走国内源把镜像拉下来并打标准名，compose 就不会去回源 Docker Hub ----
if [ "${PREBUILT:-0}" = "1" ]; then
  if [ ! -f backend/target/blued-api.jar ] || [ ! -d frontend/dist ]; then
    echo "!! 预构建模式缺少产物：backend/target/blued-api.jar 或 frontend/dist"
    exit 1
  fi
  echo "== 1/2 国内源准备镜像（运行时）=="
  bash scripts/pull-images.sh
  echo "== 2/2 预构建启动 =="
  $DC -f docker-compose.prebuilt.yml up -d --build
else
  echo "== 1/2 国内源准备镜像（含 maven / node，体积较大）=="
  bash scripts/pull-images.sh --build
  echo "== 2/2 源码构建启动 =="
  $DC up -d --build
fi

echo "== 等待服务就绪（最多 5 分钟）=="
for i in $(seq 1 30); do
  code=$(curl -s -o /dev/null -w '%{http_code}' "http://127.0.0.1:${WEB_PORT:-8090}/api/v1/health" || true)
  if [ "$code" = "200" ]; then
    echo "服务已就绪（第 $((i*10)) 秒）"
    break
  fi
  sleep 10
done

$DC ps 2>/dev/null || true
echo
echo "==================================================="
echo " 访问地址： http://<服务器IP>:${WEB_PORT:-8090}"
echo " 管理员  ： admin / ${ADMIN_PASSWORD:-Admin@Blued2025}"
echo " 其余账号： monitor / Monitor@123   analyst / Analyst@123   handler / Handler@123"
echo "==================================================="
