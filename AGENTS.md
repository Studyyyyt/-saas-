# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## 项目概述

口腔门诊 SaaS 管理系统，包含管理后台（PC 桌面端）、AI 智能中心（可选）两大组成部分。

### 技术栈

- **前端**: Vue 2.6.14 + Vue Router 3 + Element UI 2.15 + Axios + ECharts + XLSX + Dexie（IndexedDB）+ marked
  - 正在进行 Apple Design 风格主题改造，样式分布在 `src/styles/`（旧主题）和 `src/styles/apple-design/`（新主题）
  - 自定义组件库位于 `src/components/apple-design/`
  - 前端 `package.json` 未配置 ESLint、Prettier 或单元测试脚本（仅 `serve` 和 `build`）
- **后端**: Spring Boot 3.2.5 + MyBatis + MySQL + Flyway + PageHelper + Lombok + ZXing（二维码）+ pinyin4j
  - MyBatis 已开启 `map-underscore-to-camel-case: true`，数据库下划线字段自动映射为 Java 驼峰命名
- **Java 版本**: 17

## 项目结构

```
口腔saas管理系统开发/
├── docs/                           # 项目文档
│   ├── 01-架构设计/                 # 系统架构与重构计划
│   ├── 02-开发文档/                 # 开发规范与集成指南
│   ├── 03-接口文档/                 # API 与 MCP 服务文档
│   ├── 04-验证测试/                 # 测试清单与验证步骤
│   ├── 05-维护指南/                 # 运维手册与问题排查
│   └── 06-开发计划/                 # 开发计划与排期
└── 开发区-写代码和调试/              # 所有源码和文档
    ├── clinic-saas-source/
    ├── docker/                     # Docker Compose 配置
    │   ├── docker-compose.dev.yml  # 开发环境编排（MySQL + 后端 + 前端）
    │   ├── database_init.sql       # MySQL 首次启动初始化脚本
    │   └── .env.example            # 环境变量模板
    ├── saas-vue-src/               # 前端源码（Vue CLI 5 项目）
    │   ├── src/views/Manager/      # 后台管理页面
    │   ├── src/router/index.js     # 路由总表，含角色权限控制
    │   ├── src/utils/offline/      # 离线缓存与同步逻辑
    │   └── vue.config.js           # 开发服务器端口 7070，代理到 8080
    └── saas-springboot-src/        # 后端源码（Maven 项目）
        ├── src/main/java/com/example/springboot/
        │   ├── controller/         # REST 控制器
        │   ├── service/            # 业务逻辑层
        │   ├── mapper/             # MyBatis Mapper 接口
        │   ├── entity/             # 实体/请求/响应类
        │   ├── config/             # 配置类（CORS、Spa 转发、Schema 初始化）
        │   └── common/             # 统一返回结构 Result
        ├── src/main/resources/
        │   ├── application.yml     # 主配置（数据库、CORS、Flyway、Swagger）
        │   └── db/migration/       # Flyway 迁移脚本（V1~V66+）
        ├── sql/                    # 手动执行的 SQL 脚本
        └── scripts/                # 部署与运维脚本
```

## Docker 开发规范（强制）

**本项目所有开发、测试、联调工作必须通过 Docker 启动服务完成。严禁直接在宿主机（本机）上启动前后端服务进行测试。**

原因：
- 保证开发环境与生产环境一致性，避免"在我机器上能跑"的问题。
- 统一依赖管理（JDK、Maven、Node 版本），避免本机环境差异导致的问题。
- 方便团队成员快速接入，无需在本机安装配置 Java、Maven、Node 等工具链。
- 数据库（MySQL）、文件存储等外部服务已容器化，通过 Docker Compose 一键编排。

### 前置条件

- Docker + Docker Compose 已安装
- 端口 **3306、8080、7070** 未被占用
- 首次启动前复制环境变量模板：

  ```bash
  cd clinic-saas-source/docker
  cp .env.example .env
  # 按需修改 .env 中的数据库密码、激活码服务地址等配置
  ```

### 启动开发环境
```bash
cd clinic-saas-source/docker
docker compose -f docker-compose.dev.yml up -d
```
- 首次启动会下载依赖，耗时约 1-3 分钟。
- MySQL 初始化脚本 `docker/database_init.sql` 会在数据库首次启动时自动执行。
- 后端开发服务器：`http://localhost:8080`（自动热重启）
- 前端开发服务器：`http://localhost:7070`（自动热刷新）

### 查看日志
```bash
cd clinic-saas-source/docker
docker compose -f docker-compose.dev.yml logs -f backend
docker compose -f docker-compose.dev.yml logs -f frontend
```

### 停止开发环境
```bash
docker compose -f docker-compose.dev.yml down
```

### 后端代码热重载（修改 Java 代码后使用）

项目已配置 `spring-boot-devtools`，修改 Java 代码后无需重启容器，执行以下命令即可自动热重启（约 0.3 秒）：

```bash
cd clinic-saas-source/docker
docker compose -f docker-compose.dev.yml exec backend mvn compile -q
```

> 说明：此命令在运行中的后端容器内执行编译，devtools 检测到 `target/classes` 变化后自动重启 JVM，比 `docker compose restart backend` 快 50 倍以上。

### 重新构建（仅在修改 Dockerfile、docker-compose 文件或新增系统级依赖时使用）
```bash
docker compose -f docker-compose.dev.yml up -d --build
```

## 常用命令

### 前端本地构建（仅用于打包验证，不用于启动服务）
```bash
cd clinic-saas-source/saas-vue-src
npm install
npm run serve          # 启动开发服务器（端口 7070）
npm run build          # 打包输出到 dist/
```
> 注意：`package.json` 未配置 `test` 和 `lint` 脚本。

### 后端本地构建（仅用于打包验证，不用于启动服务）
```bash
cd clinic-saas-source/saas-springboot-src
mvn clean package      # 打包 JAR
mvn test               # 运行全部单元测试（约 44 个测试文件）
mvn test -Dtest=ClassName           # 运行单个测试类
mvn test -Dtest=ClassName#method    # 运行单个测试方法
```

### 前后端联调
- 前端 `vue.config.js` 已将常用 API 路径代理到 `http://127.0.0.1:8080`
- 开发时确保 Docker 开发环境中的前后端服务均已启动，然后直接访问 `http://localhost:7070`

### 生产部署
```bash
bash clinic-saas-source/saas-springboot-src/scripts/deploy_backend_frontend.sh
bash clinic-saas-source/saas-springboot-src/scripts/deploy_backend_frontend.sh --skip-tests
bash clinic-saas-source/saas-springboot-src/scripts/deploy_backend_frontend.sh --skip-tests --with-regression
```
部署脚本默认：构建前端 -> 复制到 Spring Boot static -> `mvn package` -> 备份并替换运行 jar -> 重启进程 -> 健康检查。

### 启动 n8n（可选，AI 功能依赖）

如需使用 AI 功能，可单独启动 n8n 工作流平台：

```bash
# 1. 诊所系统（必须先启动）
cd clinic-saas-source/docker && docker compose -f docker-compose.dev.yml up -d

# 2. n8n 工作流平台（AI 功能依赖，可选）
cd "开发区-写代码和调试/n8n" && cp .env.example .env && docker compose up -d
```

| 服务 | 地址 |
|------|------|
| 诊所前端 | `http://localhost:7070` |
| 诊所后端 API | `http://localhost:8080` |
| Swagger 文档 | `http://localhost:8080/swagger-ui.html` |
| n8n（如启用） | `http://localhost:5678` |

## 架构要点

### 路由与权限
- `src/router/index.js` 使用 `meta.allowedRoles` 控制页面访问（`admin`/`doctor`/`nurse`）。
- 菜单权限由 `roleMenuCatalog.js` 维护，后端通过 `RoleMenuPermissionController` 持久化配置。
- 公开路径（登录、注册、绑定成功等）在白名单 `PUBLIC_PATHS` 中放行。

### 前端主题架构
- 系统正从 Element UI 默认主题向 Apple Design 风格迁移。
- 样式文件分布在 `src/styles/`（旧主题）和 `src/styles/apple-design/`（新主题）。
- 自定义组件库位于 `src/components/apple-design/`，逐步替换 Element UI 组件。
- `vue.config.js` 的 `bypassForSpa` 函数处理前端路由与后端 API 路径冲突：当浏览器直接访问页面（Accept 包含 html）时返回 `index.html`，否则代理到后端 API。部分路径（如 `/SystemSettings`）在 proxy 之前通过 `onBeforeSetupMiddleware` 特殊拦截。

### 离线支持
- 前端 `src/utils/offline/` 实现 IndexedDB 缓存 + 待同步队列。
- 在线时自动缓存列表页与患者 360 数据；断网时新增/编辑患者、预约、病历会入队待同步。
- 网络恢复后自动回放队列；冲突以保留失败状态并允许重试为主。

### 数据库迁移
- 使用 Flyway，迁移脚本位于 `src/main/resources/db/migration/`，版本从 V1 到 V66+。
- 另有 `schema-extra.sql` 由 `SchemaInitializer` 在启动时执行，需在 `application.yml` 中设置 `legacy.schema-initializer.enabled: true`。

### AI 架构
- **系统不直接调用 OpenAI API**，所有 AI 能力通过 n8n 工作流间接调用。
- 前端通过 `AiProxyController` (`/api/ai/proxy/{agentKey}`) 将请求转发到 n8n Webhook。
- AI Agent 配置存储在 `ai_agent_config` 表中，支持动态配置 Webhook 地址、认证、请求模板。
- 经营日报/周报/月报使用规则引擎生成，不调用外部 AI API。

### 影像存储
- 患者影像上传后保存到本地磁盘 `~/.local/uploads/patient-images/`。
- `PatientImageController` 提供上传、读取文件流、删除接口。

### Swagger / OpenAPI 接口文档
- 后端集成 `springdoc-openapi-starter-webmvc-ui`，启动后访问 `http://localhost:8080/swagger-ui.html` 查看接口文档。
- 生产环境建议通过 `springdoc.api-docs.enabled` 关闭文档暴露。

### 安全与鉴权现状
- 登录使用明文密码比对，无 JWT 或 Session 过滤器。
- 前端菜单权限控制不等于后端接口权限控制，修改接口时需注意鉴权盲区。

### 激活码授权管理
- 系统通过独立的 **Easytoac 激活码服务** 管理诊所授权，采用独立服务对接模式。
- `clinic` 表新增 `activation_code`（激活码）和 `license_expires_at`（授权过期时间）字段。
- `LicenseVerificationService` 负责调用外部激活码服务验证授权状态，支持网络异常时的本地过期时间容错。
- 用户登录时，如果只有一个关联诊所，自动验证该诊所的激活码；授权过期则拒绝登录并返回 `403`。
- `application.yml` 中 `license.server.url` 配置激活码服务地址，`license.enabled` 控制是否启用验证（开发环境可关闭）。
- `docker-compose.dev.yml` 已集成 `license-server` 服务，构建上下文指向 Easytoac 项目目录。

## 开发注意事项

- **数据库关联**: 部分模块（预约、治疗）早期通过患者姓名关联而非 `patient_id`，涉及重名或改名场景的数据一致性风险已在多轮迁移中逐步修复，新增逻辑务必使用稳定主键关联。
- **返回结构**: 后端统一返回 `Result`（`code`/`msg`/`data`），但部分老接口返回原始字符串，前后端对接时需确认。
- **CORS**: `CorsConfig` 已全局开放，生产环境部署时建议收紧。
- **路由大小写**: 前端路由存在大小写混用（如 `/Inventory`、`/Appointment`），`SpaForwardController` 手工维护转发列表，新增页面需同步添加。
- **前端代理**: `vue.config.js` 维护大量路径代理规则（`/auth`、`/patients`、`/api` 等），新增 API 路径时需在代理列表和 `SpaForwardController` 中同步添加，否则开发环境请求 404。
- **配置加载优先级**: `application.yml` 配置加载顺序为：环境变量 > 配置文件默认值 > JVM 系统属性。生产环境建议通过环境变量注入敏感配置。
- **文件上传限制**: `application.yml` 中配置 `max-file-size: 200MB`、`max-request-size: 500MB`，患者影像上传受此限制。
- **数据库初始化**: Docker 首次启动时，`docker/database_init.sql` 会自动导入到 MySQL 中，修改该脚本后需删除 `docker/mysql-data` 卷并重新启动容器才能重新执行。
