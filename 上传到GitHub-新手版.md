# 上传到 GitHub（新手版，照着敲）

目录：`D:\ycshield`（这是已整理好的源码版，不含 jar/dist，共 109 个文件）

---

## 第一步：装 Git（只装一次）

在**开始菜单**搜索 `PowerShell`，右键 → **以管理员身份运行**，粘贴：

```powershell
winget install --id Git.Git -e --source winget
```

装完**关掉 PowerShell 重新开一个**（让 PATH 生效），验证：

```powershell
git --version
```

看到 `git version 2.x.x` 就成了。

---

## 第二步：配一次身份（改成你自己的）

```powershell
git config --global user.name "yc"
git config --global user.email "你的GitHub邮箱"
```

用哪个邮箱都行，但**建议和你 GitHub 账号的邮箱一致**，这样提交记录会算在你名下。

---

## 第三步：本地建仓库并提交

```powershell
cd D:\ycshield
git init
git add -A
git commit -m "蓝盾YC · 蓝队防守作战协同平台 v1.0"
```

`git add -A` 之后可以看一眼要传的文件有多少：

```powershell
git status --short | measure-object -line
```

---

## 第四步：在 GitHub 网页创建仓库（关键：什么都不要勾）

1. 打开 https://github.com/new
2. **Repository name** 填 `ycshield`（或 `blued`）
3. **Description** 填：`蓝盾YC · 蓝队防守作战协同平台 — 事件上报/研判/处置/态势大屏`
4. 选 **Private**（私有）或 **Public**（公开）
5. **下面的 Add a README / .gitignore / license 三个都不要勾！**
   （勾了会多一个 README 文件，第一次 push 会冲突，新手最容易卡在这）
6. 点 **Create repository**

创建完页面会显示一串命令，先别管，跳到第五步。

---

## 第五步：把本地代码推上去

在 `D:\ycshield` 目录里执行（把 `你的用户名` 换成 GitHub 用户名）：

```powershell
git remote add origin https://github.com/你的用户名/ycshield.git
git branch -M main
git push -u origin main
```

`git push` 时会**弹出一个浏览器窗口**让你登录 GitHub：

- 点 `Sign in with your browser` → 浏览器里授权 → 自动完成
- 如果没弹窗，看下面「常见问题」

看到 `branch 'main' set up to track 'origin/main'` 就成功了，刷新网页就能看到全部文件。

---

## 第六步：打版本标签 + 传预构建附件（可选，但推荐）

预构建包 55MB 不能放进代码里，放 **Release 附件**：

```powershell
cd D:\ycshield
git tag -a v1.0.0 -m "首版：事件上报-研判-处置全链路 + 态势感知大屏"
git push origin v1.0.0
```

然后网页上：仓库 → 右侧 **Releases** → **Draft a new release** →
Choose a tag 选 `v1.0.0` → 标题填 `v1.0.0` →
把 `blued-docker-prebuilt.tar.gz` 拖到 **Attach binaries** 区域 →
**Publish release**。

这样别人有两种用法：
- 下 Release 的 tar 包 → `sudo bash deploy.sh`（不用编译）
- `git clone` → `bash scripts/up.sh`（自己编译）

---

## 常见问题

**1. `git push` 提示要密码，输密码不对？**
GitHub 早就不让用账号密码 push 了。两个办法：
- 装 Git 时自带的「Git Credential Manager」会弹浏览器登录，用这个最省事；
- 或者生成 Token：GitHub → 右上头像 → Settings → Developer settings →
  Personal access tokens → Tokens (classic) → Generate new token →
  勾 `repo` → 生成后复制那串 `ghp_xxx`，**push 时密码处粘贴它**（只显示一次）。

**2. 报 `remote: Repository not found`**
- 仓库名拼错了，或者 `你的用户名` 没替换；
- 私有仓库要确保登录的是有权限的账号。

**3. 报 `failed to push some refs` / `updates were rejected`**
说明建仓库时勾了 README，远端已经有 1 个提交。两种解法：
```powershell
# 解法A：把远端内容拉下来合并
git pull origin main --allow-unrelated-histories
git push -u origin main

# 解法B（更干脆）：强制覆盖远端（远端只有个 README，没损失）
git push -u origin main --force
```

**4. 别做的事**
- 不要把 `blued-docker-prebuilt.tar.gz`（55MB）提交进仓库 —— GitHub 单文件超 50MB 会报警、超 100MB 直接拒绝，而且会永久留在历史里
- 不要提交 `.env`（里面是密码）—— `.gitignore` 已经挡住了，别用 `git add -f` 强行加
- 不要把 `backend/target/`、`frontend/dist/` 提交上去 —— 同理已被挡住

**5. 以后改了代码怎么更新**
```powershell
cd D:\ycshield
git add -A
git commit -m "改了什么"
git push
```

---

## 上传前最后确认（可选）

`.gitignore` 和 `.gitattributes` 已在目录里，作用：
- `.gitignore`：挡住 jar、dist、node_modules、.env、data、日志
- `.gitattributes`：强制 `.sh`/`.sql`/`Dockerfile` 用 LF 换行
  —— 这条很重要，否则别人在 Windows 上 clone 再拿到 Linux 跑，
  脚本会报 `$'\r': command not found`
