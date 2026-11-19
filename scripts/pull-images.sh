#!/usr/bin/env bash
# 从国内加速源拉取所需镜像，并打回标准名（compose 直接用本地镜像，不回源 Docker Hub）
#   bash scripts/pull-images.sh            # 运行时镜像
#   bash scripts/pull-images.sh --build    # 追加 maven / node（源码构建需要）
#   可用环境变量：
#     MIRROR=docker.1ms.run        指定优先源
#     PULL_TIMEOUT=300             单个镜像超时秒数（卡住自动换源）
#     TRY_DIRECT=1                 最后再试一次 Docker Hub 直连
set -u

MIRRORS=(
  "${MIRROR:-}"
  "docker.1ms.run"
  "docker.xuanyuan.me"
  "docker.1panel.live"
  "docker.m.daocloud.io"
  "hub.rat.dev"
  "dockerpull.org"
  "docker.1panel.top"
  "dockerhub.icu"
  "dhub.kubesre.xyz"
  "docker.amingg.com"
  "proxy.1panel.live"
  "docker.actima.top"
)
if [ "${TRY_DIRECT:-0}" = "1" ]; then MIRRORS+=("docker.io"); fi

IMAGES=(mysql:8.0 redis:7-alpine eclipse-temurin:17-jre-alpine nginx:1.27-alpine)
if [ "${1:-}" = "--build" ] || [ "${BUILD_IMAGES:-0}" = "1" ]; then
  IMAGES+=(maven:3.9-eclipse-temurin-17 node:20-alpine)
fi

TIMEOUT="${PULL_TIMEOUT:-900}"
fail=0

for img in "${IMAGES[@]}"; do
  if docker image inspect "$img" >/dev/null 2>&1; then
    echo "[skip] $img 已在本地"
    continue
  fi
  ok=0
  for m in "${MIRRORS[@]}"; do
    [ -z "$m" ] && continue
    # 加速站有两种路径约定，两种都试
    for ref in "$m/$img" "$m/library/$img"; do
      echo "[try ] $ref"
      if timeout "$TIMEOUT" docker pull "$ref"; then
        docker tag "$ref" "$img" 2>/dev/null && echo "[ok  ] $img  ←  $ref"
        ok=1
        break
      fi
    done
    [ "$ok" = "1" ] && break
  done
  if [ "$ok" = "0" ]; then
    echo "[FAIL] $img 所有国内源均失败"
    fail=1
  fi
done

if [ "$fail" = "1" ]; then
  cat <<'EOF'

--------------------------------------------------------------
镜像全部失败时的最后一招：把加速站写进 Docker 守护进程。

  sudo bash scripts/set-daemon-mirror.sh

它会写入 /etc/docker/daemon.json 并重启 docker，然后再执行：

  bash scripts/pull-images.sh

--------------------------------------------------------------
EOF
  exit 1
fi
echo "全部镜像就绪"
