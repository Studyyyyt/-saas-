# 口腔诊所 SaaS 系统 - 交付说明

## 交付方式：Docker 镜像交付

本文档面向系统交付人员（开发商），说明如何将系统以 Docker 镜像形式交付给客户。

---

## 交付原理

采用 **镜像交付** 模式，核心特点：

- **不给源码**：客户收到的只有预构建的 Docker 镜像和部署配置，无法查看或修改源码
- **一键部署**：客户在服务器上导入镜像、配置环境变量、启动即可运行
- **数据隔离**：客户的数据（数据库、患者影像）保存在本地磁盘，与容器生命周期无关
- **易于升级**：后续版本只需替换镜像包，数据自动保留

---

## 完整流程概览

```
┌─────────────────────────────────────────────────────────────┐
│  第一阶段：开发（你本地）                                      │
│  修改前端/后端代码 → 构建验证 → 浏览器测试                      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  第二阶段：构建生产镜像（你本地）                               │
│  执行 ./scripts/build-and-export.sh                          │
│  ① 编译后端源码 → 生成 clinic-saas/backend 镜像              │
│  ② 编译前端源码 → 生成 clinic-saas/frontend 镜像             │
│  ③ 导出镜像为 clinic-saas-images.tar                         │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  第三阶段：生成交付包（自动）                                   │
│  脚本自动生成 deliver/ 目录，包含镜像+配置+数据+说明           │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  第四阶段：交付给客户                                          │
│  将 deliver/ 目录打包为压缩包，发送给客户                       │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  第五阶段：客户部署（客户服务器）                               │
│  导入镜像 → 配置环境变量 → 启动容器 → 导入数据 → 访问系统      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  第六阶段：客户使用                                            │
│  数据持久化在服务器本地（mysql-data/ + patient-uploads/）      │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  第七阶段：版本升级                                            │
│  你重新构建镜像 → 发给客户 → 客户替换镜像重启（数据保留）      │
└─────────────────────────────────────────────────────────────┘
```

---

## 交付流程（开发商操作）

### 1. 构建并导出交付包

```bash
cd clinic-saas-source/docker
./build-and-export.sh
```

脚本会自动执行以下操作：
1. 构建后端镜像 `clinic-saas/backend:v1.0.0`
2. 构建前端镜像 `clinic-saas/frontend:v1.0.0`
3. 导出镜像为 `clinic-saas-images.tar`
4. 复制部署所需文件到 `deliver/` 目录

### 2. 检查交付包内容

构建完成后，`docker/deliver/` 目录应包含：

```
deliver/
├── clinic-saas-images.tar       # Docker 镜像包（约 500MB-1GB）
├── docker-compose.yml           # 生产环境编排文件
├── .env.example                 # 环境变量模板
├── database_init.sql            # 数据库初始化脚本
└── README.md                    # 客户部署指南
```

### 3. 发送给客户

将 `deliver/` 目录中的所有文件打包为压缩包，发送给客户：

```bash
cd docker/deliver
tar czf clinic-saas-deliver-$(date +%Y%m%d).tar.gz .
```

---

## 客户部署流程（客户操作）

客户收到交付包后，按照 `README.md` 中的步骤操作：

1. **上传文件**到服务器（如 `/opt/clinic-saas/`）
2. **导入镜像**：`docker load -i clinic-saas-images.tar`
3. **配置环境**：复制 `.env.example` 为 `.env`，修改数据库密码和激活码服务地址
4. **启动系统**：`docker compose up -d`
5. **初始化系统**：浏览器访问系统，按向导设置管理员账号和诊所信息
6. **访问系统**：浏览器打开 `http://服务器IP:7070`

详细步骤见交付包中的 `README.md`。

---

## 关键配置说明

### 激活码服务地址（LICENSE_SERVER_URL）

客户部署前，需要确保激活码服务可被客户服务器访问：

- 如果激活码服务部署在公网：直接填写公网地址，如 `https://license.yourdomain.com`
- 如果激活码服务部署在内网：需要配置内网穿透（frp、ngrok 等），确保客户服务器能访问

### 数据库密码

`.env` 文件中的数据库密码必须满足以下一致性：

```
DB_ROOT_PASSWORD      # MySQL root 密码
DB_USER / DB_PASSWORD # 应用连接数据库的账号密码
```

这三个值需要与 `docker-compose.yml` 中 MySQL 容器的 `MYSQL_ROOT_PASSWORD`、`MYSQL_USER`、`MYSQL_PASSWORD` 保持一致。

---

## 版本升级流程

当系统有新版本时：

1. 开发商重新执行 `./build-and-export.sh` 构建新镜像
2. 将新的 `clinic-saas-images.tar` 发送给客户
3. 客户在服务器上执行：
   ```bash
   docker compose down
   docker load -i clinic-saas-images.tar
   docker compose up -d
   ```
4. 数据库数据自动保留（存储在 `./mysql-data/` 目录中）

---

## 镜像交付 vs 源码交付

| 对比项 | 镜像交付（当前方案） | 源码交付 |
|--------|----------------------|----------|
| 客户能否看到源码 | **不能** | 能 |
| 客户能否二次开发 | **不能** | 能 |
| 部署复杂度 | 简单（导入+启动，约5分钟） | 复杂（需安装 Java/Node/Maven，约30-60分钟） |
| 部署耗时 | 5分钟 | 30-60分钟 |
| 安全性 | 高（代码不泄露） | 低 |
| 适用场景 | 商业授权、SaaS托管 | 开源项目、定制开发 |

---

## 各阶段详细说明

### 第一阶段：开发（你本地）

开发环境使用 Docker 启动，前后端源码通过挂载方式进入容器：

```bash
cd docker
./环境切换.sh dev
```

- 修改 **前端 Vue 代码** → 执行 `./build-frontend.sh` 重新构建并重启前端容器
- 修改 **后端 Java 代码** → 执行 `docker compose exec backend mvn compile -q` 热重启
- 浏览器访问 `http://localhost:7070` 验证功能

### 第二阶段：构建生产镜像（你本地）

开发完成后，执行构建脚本：

```bash
cd docker
./build-and-export.sh
```

脚本内部流程：
1. 读取 `saas-springboot-src/` 后端源码 → Maven 编译打包 → 构建 `clinic-saas/backend:v1.0.0` 镜像
2. 读取 `saas-vue-src/` 前端源码 → npm 构建 → 构建 `clinic-saas/frontend:v1.0.0` 镜像
3. `docker save` 导出为 `clinic-saas-images.tar`
4. 复制部署文件到 `deliver/` 目录
5. 自动生成客户部署说明文档

### 第三阶段：生成交付包（自动）

脚本执行完成后，`docker/deliver/` 目录包含：

```
deliver/
├── clinic-saas-images.tar       # Docker 镜像包（约 500MB-1GB）
├── docker-compose.yml           # 生产环境编排文件（无 build 指令）
├── .env.example                 # 环境变量模板
├── database_init.sql            # 数据库初始化脚本
├── supplement_data.sql          # 基础业务数据
└── 部署说明.md                   # 客户部署指南
```

**注意：交付包中不含任何源码。**

### 第四阶段：交付给客户

将 `deliver/` 目录打包发送：

```bash
cd docker/deliver
tar czf clinic-saas-deliver-$(date +%Y%m%d).tar.gz .
```

通过邮件、网盘或 U 盘发送给客户。

### 第五阶段：客户部署（客户服务器）

客户在 Linux 服务器上的操作：

```bash
# 1. 上传并解压交付包
cd /opt/clinic-saas

# 2. 导入镜像
docker load -i clinic-saas-images.tar

# 3. 配置环境变量
cp .env.example .env
# 编辑 .env，修改数据库密码和激活码服务地址

# 4. 启动系统
docker compose up -d

# 5. 等待 MySQL 启动后，访问系统按向导初始化
# 浏览器访问 http://服务器IP:7070，设置管理员账号和诊所信息
```

访问 `http://服务器IP:7070` 即可使用。

### 第六阶段：客户使用

客户使用过程中产生的数据保存在服务器本地，与容器无关：

| 数据类型 | 存储位置 | 说明 |
|----------|----------|------|
| 数据库 | `./mysql-data/` | MySQL 数据文件，删除容器不丢失 |
| 患者影像 | `./patient-uploads/` | 上传的图片文件 |

### 第七阶段：版本升级

发布新版本时：

1. 你重新执行 `./build-and-export.sh` 构建新镜像
2. 将新的 `clinic-saas-images.tar` 发送给客户
3. 客户执行：
   ```bash
   docker compose down
   docker load -i clinic-saas-images.tar
   docker compose up -d
   ```
4. 数据库数据自动保留（存储在 `./mysql-data/` 中）

---

## 注意事项

1. **切勿将源码提交到交付包中**：交付包只包含镜像 tar 文件和部署配置
2. **mysql-data 目录不会被包含**：客户的数据库存储在本地 `./mysql-data/`，不在交付包中
3. **patient-uploads 目录不会被包含**：患者影像存储在本地 `./patient-uploads/`
4. **激活码服务必须独立部署**：确保客户系统能访问到激活码验证服务
5. **首次启动后必须导入 supplement_data.sql**：否则系统没有管理员账号和基础数据

---

## 文件清单

| 文件 | 说明 |
|------|------|
| `docker/docker-compose.yml` | 本地生产构建与客户部署配置 |
| `docker/scripts/build-and-export.sh` | 镜像构建与导出脚本 |
| `docker/deliver/` | 交付包输出目录（执行脚本后自动生成） |
| `docker/.env.example` | 环境变量模板（交付给客户） |
| `docker/sql/init/database_init.sql` | 数据库初始化脚本（交付给客户） |
