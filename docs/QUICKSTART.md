# 快速部署卡片

```bash
git clone <你的仓库地址> blued && cd blued
cp .env.example .env         # 按需改端口 / 密码
bash scripts/up.sh           # 自动：下载 IP 库 → 构建 → 启动
```

访问 `http://服务器IP:8090`，账号 `admin / Admin@Blued2025`。

## 只用三条命令（手动版）

```bash
cp .env.example .env
bash scripts/fetch-ipdb.sh
docker compose up -d --build
```

## 国内网络镜像拉不动

```bash
bash scripts/pull-images.sh --build    # 自动轮流试国内加速源
# 或者：在 .env 中设置 IMAGE_PREFIX=docker.1ms.run/library/
```

## 端口冲突

`.env` 里改 `WEB_PORT`（默认 8090）。

## 内存要求

最低 2 核 4G；`docker stats` 中 mysql 约 400MB，api 约 500MB。
