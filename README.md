# 蓝盾YC · 蓝队防守作战协同平台

面向**单单位蓝队防守值班**的事件协同处置平台：监测员上报 → 分析员研判 → 研判办结 / 封禁结案 / 转应急处置，全过程留痕，附管理员态势感知大屏。

> 定位：先把"能打仗、能追溯"的核心流程跑通，界面以够用为准，态势大屏单独给管理员"撑场面"。

---

## 1. 功能一览

**监测员**
- 事件上报：攻击类型（两级字典）、攻击方向（外对内/内对内/内对外）、攻击成功与否、危害等级
- 一条事件可挂**多条告警记录**（不同设备/时间），受害 IP 自动汇总去重
- 来源设备、设备名称走**管理员预设字典**，下拉选择
- 原始报文粘贴、截图直接 `Ctrl+V` 粘贴为附件
- 提交前自动检测疑似重复事件
  <img width="3074" height="1624" alt="24c4152998db5a2a19ffb89f74f5fad7" src="https://github.com/user-attachments/assets/d07e9d3b-6a8b-4f2b-9754-9c3ba8439372" />
  <img width="3074" height="1624" alt="24c4152998db5a2a19ffb89f74f5fad7" src="https://github.com/user-attachments/assets/9f975639-9b81-4176-9a18-c0f56ed9531e" />

**分析员（研判台）**
- 待认领 / 研判中两个队列，一键认领
- 研判结论不限字数、**可多次补充**，每次变更独立留痕
- 改判攻击结果 / 等级 / 类型 / 方向时记录**字段级变更**（旧值 → 新值 + 原因）
- 三个出口：
  - **研判办结归档**：一般扫描、攻击未成功等，无需处置，直接结束
  - **直接封禁 IP 结案**：恶意扫描源就地封禁（不接入设备，仅记录决定）
  - **转应急处置**：确实需要应急时，才指派处置员
  <img width="3032" height="940" alt="9752df94edee1a6798fbc359b1d3d9db" src="https://github.com/user-attachments/assets/24e831c5-50a9-48d8-856d-3d5ccfdb9090" />

**处置员（处置台）**
- 我的待处置 / 待复核队列
- 结构化处置记录：处置动作、处置对象、执行设备、执行时间、生效时间、验证结果
- 提交复核
  <img width="3068" height="1112" alt="1e50e6064dc19215a6094ca470268950" src="https://github.com/user-attachments/assets/6b60487e-52fc-4834-a8eb-60564f8d3b41" />

**管理员**
- **态势感知大屏**（全屏深色）：中国地图攻击源热力 + 涟漪点、14 天威胁趋势、类型/等级/方向分布、攻击源 IP TOP10、境外来源 TOP、受害资产 TOP、实时事件流，20 秒自动刷新
- 字典管理（攻击类型、来源设备、设备名称、处置动作等全部可改，改完即时生效）
- 用户管理、角色分配
<img width="3106" height="1850" alt="44e777bbf5372b6ded2aefbd6a872e80" src="https://github.com/user-attachments/assets/8211e9d8-9fbb-479c-aed4-d6b18d8247de" />
<img width="3060" height="1142" alt="0575466075f51cbd35d9df112a1f56f6" src="https://github.com/user-attachments/assets/532b8820-7f1c-41da-ac38-04bd725942bf" />
<img width="2988" height="1614" alt="55bfeb72dc536fd138c328a7a43b2b62" src="https://github.com/user-attachments/assets/8119751d-3db7-4665-aa78-676ef1399999" />
<img width="3082" height="1698" alt="25f377bbc11517f3bbdc88a20bd353ea" src="https://github.com/user-attachments/assets/b43a481b-7dfb-4569-9d67-39035f17eb55" />
<img width="3042" height="1516" alt="87c6f954efb8cb04e9f576e81fd11f9f" src="https://github.com/user-attachments/assets/587e0280-5485-49eb-8804-f46b1fab4e62" />


**通用**
- JWT 鉴权 + 四角色权限控制
- 事件编号 `BS-yyyyMMdd-NNNN`（Redis 自增）
- 超时预警（高危 15/30/120 分钟；其余 60/240/1440 分钟；待复核 24 小时）
- 附件上传（本地卷存储）、流转时间线、审计留痕
- 离线 IP 归属地解析（ip2region，无需联网）

---

## 2. 技术栈

| 层 | 选型 |
|---|---|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia + ECharts |
| 后端 | Spring Boot 3.2.5（JDK 17）+ Spring Security + JWT + MyBatis-Plus |
| 数据库 | MySQL 8.0（12 张表） |
| 缓存 | Redis 7（事件编号自增） |
| 部署 | Docker + Docker Compose + Nginx 静态托管/反向代理 |

资源占用（单机）：2 核 4G 起步；镜像合计约 700MB，容器内存约 1GB。

---

## 3. 快速开始（Docker，推荐）

> Kali / Debian 部署看 [docs/DEPLOY-KALI.md](docs/DEPLOY-KALI.md)。

```bash
git clone <你的仓库地址> blued
cd blued
cp .env.example .env      # 改端口、密码、JWT 密钥
bash scripts/up.sh        # 下载 IP 库 → 构建镜像 → 启动
```

启动完成后访问：`http://服务器IP:8090`

首次启动会自动建表、导入字典、创建四个默认账号。

### 默认账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 管理员 | admin | `Admin@Blued2025` |
| 监测员 | monitor | `Monitor@123` |
| 分析员 | analyst | `Analyst@123` |
| 处置员 | handler | `Handler@123` |

密码可用 `.env` 里的 `ADMIN_PASSWORD` 覆盖（仅首次建库生效），登录后建议立刻改密码。

### 手动三步

```bash
cp .env.example .env
bash scripts/fetch-ipdb.sh
docker compose up -d --build
```

### 预构建模式（构建机没网/想快）

```bash
bash scripts/build-artifacts.sh     # 生成 backend/target/*.jar 和 frontend/dist
PREBUILT=1 bash scripts/up.sh       # 只用小体积运行时镜像
```

---

## 4. 配置项（.env）

| 变量 | 说明 | 默认 |
|---|---|---|
| `WEB_PORT` | 宿主机对外端口 | `8090` |
| `DB_PASSWORD` | MySQL root 密码（首次初始化生效） | `blued123456` |
| `JWT_SECRET` | JWT 签名密钥，生产务必修改 | 示例值 |
| `ADMIN_PASSWORD` | 初始化管理员密码 | `Admin@Blued2025` |
| `IMAGE_PREFIX` | 镜像加速前缀，如 `docker.1ms.run/library/` | 空 |
| `NPM_REGISTRY` | npm 源 | npmmirror |
| `MAVEN_MIRROR` | maven 源 | 阿里云 |

端口：宿主机 `${WEB_PORT}` → 容器 80（Nginx）；后端 8081 只在容器网络内，不需要对外映射。

---

## 5. 目录结构

```
.
├── docker-compose.yml           # 源码构建部署
├── docker-compose.prebuilt.yml  # 预构建产物部署
├── .env.example
├── backend/                     # Spring Boot 后端
│   ├── Dockerfile               # 多阶段：maven 构建 → jre-alpine 运行
│   ├── Dockerfile.prebuilt
│   └── src/main/java/com/blued/
│       ├── common/              # 统一返回、字典缓存、IP 归属地、健康检查
│       ├── config/              # 安全、跨域、MyBatis-Plus、Jackson
│       ├── security/            # JWT 过滤器与用户上下文
│       ├── system/              # 用户 / 角色 / 字典
│       ├── event/               # 事件、告警、处置、流转
│       ├── stat/                # 总览与大屏统计
│       ├── file/                # 附件上传下载
│       └── init/                # 初始化账号、归属地回填
├── frontend/                    # Vue 3 前端
│   ├── Dockerfile               # 多阶段：node 构建 → nginx 托管
│   ├── nginx.conf               # SPA 回退 + /api /files 反向代理
│   └── src/views/               # Dashboard(大屏) / 研判台 / 处置台 / 事件…
├── db/
│   ├── 01_schema.sql            # 建表
│   ├── 02_data_dict.sql         # 角色与全部字典
│   └── upgrade/                 # 老库升级脚本
├── scripts/                     # 一键部署、镜像加速、备份恢复
└── docs/QUICKSTART.md
```

---

## 6. 事件流转

```
监测上报(SUBMITTED)
   └─ 认领 → 研判(ANALYZING)
        ├─ 研判办结归档 ─────────────► 已归档(ARCHIVED)
        ├─ 直接封禁IP结案 ───────────► 已归档(ARCHIVED) + 处置记录(人工)
        ├─ 误报/重复关闭 ────────────► 已关闭(CLOSED)
        └─ 转应急处置 → 处置中(HANDLING) → 提交复核(REVIEWING) → 复核归档(ARCHIVED)
```

一条事件 → N 条告警记录；受害 IP 取并集去重；研判阶段的所有改判都写 `event_flow`，带旧值/新值/原因。

---

## 7. 运维

```bash
bash scripts/logs.sh api          # 查看后端日志
docker compose ps                 # 服务状态
docker compose restart api        # 重启后端
bash scripts/backup.sh            # 备份数据库 + 附件 到 backups/
bash scripts/restore.sh backups/blued-db-20260101-120000.sql.gz
```

数据都在 `./data`：`data/mysql`（数据库）、`data/redis`、`data/upload`（附件）、`data/ip2region.xdb`（IP 库）。
**备份 `data/` 目录即可整站迁移。**

升级：
```bash
git pull
docker compose up -d --build
# 若 db/upgrade 下有新脚本，按需手动执行：
# docker exec -i blued-mysql mysql -uroot -p密码 blued < db/upgrade/xxx.sql
```

---

## 8. 常见问题

**Q：`docker pull` 卡住不动 / 超时？**
国内直连 Docker Hub 基本不可用（镜像层被重定向到境外 CDN）。`scripts/up.sh` 已经默认先走国内加速源拉取并打标准 tag，正常情况下无需手工干预。手动处理：

1. `bash scripts/pull-images.sh`：依次尝试 12 个国内加速源（1ms.run / 玄轩 / 1panel / daocloud / rat.dev…）；
2. `sudo bash scripts/set-daemon-mirror.sh`：写 `/etc/docker/daemon.json` 的 `registry-mirrors` 并重启 docker；
3. `.env` 里指定 `MIRROR=docker.1ms.run` 换一个优先源再跑。

**Q：8090 被占用？**
改 `.env` 的 `WEB_PORT` 后 `docker compose up -d`。

**Q：大屏地图没有热力点？**
攻击源归属地来自离线库 `data/ip2region.xdb`。执行 `bash scripts/fetch-ipdb.sh` 下载；私有 IP、内网地址会显示"未知"。

**Q：时间差 8 小时？**
容器已设 `TZ=Asia/Shanghai`，MySQL 也设了 `--default-time-zone=+08:00`；如仍异常检查宿主机时间。

**Q：想清空演示数据重新开始？**
```bash
docker exec -i blued-mysql mysql -uroot -p密码 blued -e "
DELETE FROM event_alarm; DELETE FROM event_flow; DELETE FROM event_disposal;
DELETE FROM event_evidence; DELETE FROM event_main;"
```

**Q：不用 Docker，裸机跑？**
需要 JDK 17 + MySQL 8 + Redis 7 + Nginx：导入 `db/*.sql` → `mvn package` → `java -jar` →
Nginx 指向 `frontend/dist` 并把 `/api/`、`/files/` 反代到 `127.0.0.1:8081`。

---

## 9. 路线图

- [ ] Excel 批量导入/导出、单事件打印
- [ ] 审计日志页面化、用户角色细粒度配置
- [ ] 告警自动去重合并、设备/API 对接
- [ ] 大屏攻击飞线动画、值班排名、攻击链关系图
- [ ] 多单位隔离（当前为单单位设计）

---

## 10. 说明

- 本项目按"单单位内部使用"设计，未做多租户数据隔离。
- 截图/附件存放于宿主机 `data/upload`，如需对象存储可替换 `file` 模块实现。
- 仅供内部防守演练与日常值守使用，请遵守当地法律法规。
