#!/usr/bin/env bash
# 把国内加速站写入 Docker 守护进程配置并重启 docker（需要 root）
#   sudo bash scripts/set-daemon-mirror.sh
set -e
[ "$(id -u)" = "0" ] || { echo "请用 sudo 运行"; exit 1; }

MIRRORS='[
    "https://docker.1ms.run",
    "https://docker.xuanyuan.me",
    "https://docker.1panel.live",
    "https://docker.m.daocloud.io",
    "https://hub.rat.dev",
    "https://dockerpull.org",
    "https://docker.1panel.top"
  ]'

mkdir -p /etc/docker
if [ -f /etc/docker/daemon.json ]; then
  cp /etc/docker/daemon.json "/etc/docker/daemon.json.bak.$(date +%Y%m%d%H%M%S)"
  echo "已备份原 daemon.json"
  if command -v python3 >/dev/null 2>&1; then
    python3 - "$MIRRORS" <<'PY'
import json, sys
cfg_path = "/etc/docker/daemon.json"
try:
    cfg = json.load(open(cfg_path))
except Exception:
    cfg = {}
mirrors = [m.rstrip("/") for m in json.loads(sys.argv[1])]
cfg["registry-mirrors"] = mirrors
json.dump(cfg, open(cfg_path, "w"), indent=2, ensure_ascii=False)
print("registry-mirrors 已写入", len(mirrors), "个")
PY
  else
    # 没 python3 就整体覆盖（原文件已备份）
    printf '{ "registry-mirrors": %s }\n' "$MIRRORS" > /etc/docker/daemon.json
  fi
else
  printf '{ "registry-mirrors": %s }\n' "$MIRRORS" > /etc/docker/daemon.json
  echo "已创建 /etc/docker/daemon.json"
fi

cat /etc/docker/daemon.json
systemctl daemon-reload || true
systemctl restart docker
sleep 3
docker info 2>/dev/null | grep -A10 "Registry Mirrors" || true
echo "完成，可重新执行： bash scripts/pull-images.sh"
