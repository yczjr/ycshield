#!/usr/bin/env bash
# 可选：在本机预构建 jar 和 dist，生成后可用 docker-compose.prebuilt.yml 快速部署
set -e
cd "$(dirname "$0")/.."

echo "== 构建后端 jar（需要 maven 镜像）=="
docker run --rm -v "$PWD/backend:/b" -v "$HOME/.m2:/root/.m2" -w /b \
  maven:3.9-eclipse-temurin-17 mvn -B -q clean package -DskipTests

echo "== 构建前端 dist（需要 node 镜像）=="
docker run --rm -v "$PWD/frontend:/f" -w /f node:20-alpine \
  sh -c "npm config set registry https://registry.npmmirror.com && npm ci --no-audit --no-fund && npm run build"

ls -lh backend/target/blued-api.jar
du -sh frontend/dist
echo "完成，可用： PREBUILT=1 bash scripts/up.sh"
