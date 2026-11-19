# 在 Kali / Debian 上部署

## 1. 装 Docker（Kali 自带源即可）

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin
sudo systemctl enable --now docker
docker version && docker compose version
```

如果提示找不到 `docker-compose-plugin`（部分 Kali 源较旧），用官方源一条命令装上：

```bash
curl -fsSL https://get.docker.com | sudo sh
```

> 脚本会自动探测 `docker compose` 或老版 `docker-compose`，两种都能用。

## 2. 解压并启动

```bash
tar -xzf blued-docker-prebuilt.tar.gz     # 预构建版（推荐，快）
# 或 tar -xzf blued-docker-src.tar.gz     # 源码版（需要联网拉 maven/node 镜像）
cd blued
cp .env.example .env                      # 可改 WEB_PORT / DB_PASSWORD / JWT_SECRET
bash scripts/up.sh
```

预构建版也可以直接：`bash deploy.sh`

## 3. 访问

`http://<kali的IP>:8090` → `admin / Admin@Blued2025`

## 4. Kali 上常见坑

| 现象 | 原因 / 解决 |
|---|---|
| `docker pull` 卡住 / `context deadline exceeded` | Docker Hub 被墙。`scripts/up.sh` **默认就走国内加速源**，不需要额外操作；单独重试用 `bash scripts/pull-images.sh`，全部失败再 `sudo bash scripts/set-daemon-mirror.sh` |
| `permission denied /var/run/docker.sock` | 用 `sudo` 跑，或 `sudo usermod -aG docker $USER` 后重新登录 |
| 8090 被占用 | 改 `.env` 的 `WEB_PORT`（Kali 常见占用：8080、443） |
| 容器起来但页面 502 | 后端还在启动，等 1-2 分钟；`bash scripts/logs.sh api` 看日志 |
| 内存不足被 OOM 杀 | 至少 4G 内存；`docker stats` 观察 |
| WSL 里跑 | 需要在 WSL 内装 docker 并启用 systemd，或用 Docker Desktop 的 WSL 集成 |

## 5. 常用命令

```bash
bash scripts/status.sh        # 状态 + 健康检查
bash scripts/logs.sh api      # 后端日志
bash scripts/backup.sh        # 备份数据库与附件
bash scripts/stop.sh          # 停止（数据保留）
```
