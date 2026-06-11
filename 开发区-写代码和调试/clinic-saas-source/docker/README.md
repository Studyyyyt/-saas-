# 口腔诊所 SaaS 系统 - Docker 部署说明

## 目录结构

```
docker/
├── .env.example                  # 环境变量模板
├── docker-compose.yml            # 生产环境编排
├── docker-compose.dev.yml        # 开发环境编排
├── backend/                      # 后端 Dockerfile
├── frontend/                     # 前端 Dockerfile + Nginx 配置
├── scripts/                      # 脚本目录
│   ├── build-and-export.sh       # 镜像构建与导出
│   ├── build-frontend.sh         # 前端构建
│   ├── 环境切换.sh               # 开发/生产环境切换
│   ├── 启动开发环境.sh           # 一键启动开发环境
│   └── 启动生产环境.sh           # 一键启动生产环境
├── sql/                          # SQL 数据目录
│   ├── init/database_init.sql    # 数据库初始化脚本
│   ├── test/test_data.sql        # 测试数据（可选）
│   └── tools/clear_business_data.sql  # 清空业务数据工具
├── deliver/                      # 交付包输出目录
├── mysql-data/                   # 生产环境数据（运行时生成，勿删）
├── mysql-data-dev/               # 开发环境数据（运行时生成，勿删）
└── patient-uploads/              # 患者影像上传目录（运行时生成，勿删）
```

---

## 第一部分：客户部署指南

### 交付包内容

执行 `./scripts/build-and-export.sh` 后，`deliver/` 目录包含：

- `clinic-saas-images.tar` — Docker 镜像包（后端 + 前端）
- `docker-compose.yml` — 生产环境编排文件
- `.env.example` — 环境变量模板
- `database_init.sql` — 数据库初始化脚本
- `README.md` — 本文件

### 前置条件

- Linux 服务器（推荐 Ubuntu 22.04 / CentOS 8）
- CPU 2 核及以上，内存 4GB 及以上，磁盘 50GB 及以上
- Docker 24.0+、Docker Compose v2+
- 端口 3306、8080、7070 需对外开放

### 部署步骤

**1. 上传交付包到服务器**

```bash
mkdir -p /opt/clinic-saas
cd /opt/clinic-saas
# 将 deliver/ 目录中的所有文件上传到此目录
```

**2. 导入 Docker 镜像**

```bash
docker load -i clinic-saas-images.tar
```

**3. 配置环境变量**

```bash
cp .env.example .env
```

编辑 `.env`，修改以下关键配置：

```
# 激活码服务地址（由开发商提供）
LICENSE_SERVER_URL=https://license.yourdomain.com

# MySQL root 密码（建议修改为强密码）
DB_ROOT_PASSWORD=你的强密码

# 应用连接数据库的账号密码
DB_USER=clinic_user
DB_PASSWORD=你的强密码

# 患者管理二级密码
SECURITY_PATIENT_ADMIN_SECONDARY_PASSWORD=你的强密码
```

**4. 启动系统**

```bash
docker compose up -d
```

首次启动时，MySQL 会自动执行 `database_init.sql` 初始化数据库表结构。等待约 30 秒后检查状态：

```bash
docker compose ps
```

应看到三个容器均为 `running` 或 `healthy` 状态。

**5. 初始化系统**

浏览器访问 `http://服务器IP:7070`，按向导设置：
- 管理员账号和密码
- 诊所名称
- 激活码

**6. 验证部署**

- 前端页面：`http://服务器IP:7070`
- 后端接口：`http://服务器IP:8080`
- Swagger 文档：`http://服务器IP:8080/swagger-ui.html`

### 常用维护命令

```bash
cd /opt/clinic-saas

# 查看容器状态
docker compose ps

# 查看日志
docker compose logs -f
docker compose logs -f backend

# 重启/停止/启动
docker compose restart
docker compose down
docker compose up -d
```

### 数据备份

**备份数据库**

```bash
docker compose exec mysql \
  mysqldump -uroot -p你的root密码 clinic_system > backup_$(date +%Y%m%d).sql
```

**备份患者影像**

```bash
tar czf patient-images-backup_$(date +%Y%m%d).tar.gz ./patient-uploads
```

### 升级说明

1. 停止当前服务：`docker compose down`
2. 导入新镜像：`docker load -i clinic-saas-images.tar`
3. 启动服务：`docker compose up -d`
4. 数据库数据自动保留（存储在 `./mysql-data/` 中）

### 故障排查

| 问题 | 排查方法 |
|------|----------|
| 无法访问 7070 端口 | 检查服务器防火墙是否放行 7070 端口 |
| 登录提示"授权无效" | 检查 `.env` 中 `LICENSE_SERVER_URL` 配置，确认激活码服务可访问 |
| 数据库连接失败 | 检查 `.env` 中数据库密码是否与启动时一致 |
| 容器反复重启 | 执行 `docker compose logs` 查看错误日志 |
| 页面显示 404 | 检查前端容器是否正常运行 |

---

## 第二部分：开发环境说明

### 首次启动

```bash
cd docker

# 1. 复制环境变量模板
cp .env.example .env

# 2. 启动全部服务（MySQL + 后端 + 前端）
./scripts/环境切换.sh dev
```

访问地址：
- 前端页面：`http://localhost:7070`
- 后端接口：`http://localhost:8080`
- Swagger 文档：`http://localhost:8080/swagger-ui.html`

### 日常开发

**修改后端代码（Java）**

修改代码后，在容器内执行编译即可自动热重启：

```bash
docker compose -f docker-compose.dev.yml exec backend mvn compile -q
```

约 0.3 秒后后端自动重启。

**修改前端代码（Vue）**

修改代码后需重新构建：

```bash
# 方式一：使用构建脚本（推荐）
./scripts/build-frontend.sh

# 方式二：手动执行
cd ../saas-vue-src && npm run build
cd ../docker && docker compose -f docker-compose.dev.yml restart frontend
```

### 环境切换

```bash
# 交互式菜单
./scripts/环境切换.sh

# 直接启动开发环境
./scripts/启动开发环境.sh

# 直接启动生产环境
./scripts/启动生产环境.sh
```

### 数据初始化（可选）

首次启动后，如需导入测试数据：

```bash
# 导入测试数据
docker compose -f docker-compose.dev.yml exec -T mysql \
  mysql -uclinic_user -pclinic_pass clinic_system < sql/test/test_data.sql

# 清空业务数据（保留系统配置）
docker compose -f docker-compose.dev.yml exec -T mysql \
  mysql -uclinic_user -pclinic_pass clinic_system < sql/tools/clear_business_data.sql
```

### 常见问题

| 问题 | 原因 | 解决 |
|------|------|------|
| 前端页面空白/无更新 | 修改代码后未重新构建 | 执行 `./scripts/build-frontend.sh` |
| 端口被占用 | 3306/8080/7070 已被其他程序占用 | 关闭占用端口的程序 |
| 后端修改不生效 | 未执行编译 | 执行 `docker compose exec backend mvn compile -q` |
| MySQL 数据丢失 | 删除了 mysql-data-dev 目录 | 该目录为运行时数据，删除后需重新初始化 |
| 容器启动失败 | .env 配置错误 | 检查 `.env` 文件中的数据库密码和许可证地址 |

---

## 注意事项

1. `mysql-data/`、`mysql-data-dev/`、`patient-uploads/` 为运行时数据目录，已加入 `.gitignore`，**不要手动删除**
2. 修改 `docker-compose.dev.yml` 或 Dockerfile 后，需要加 `--build` 参数重新创建容器
3. 生产环境使用 `docker-compose.yml`，通过 Dockerfile 构建镜像，与开发环境隔离
