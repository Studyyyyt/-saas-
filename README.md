# 口腔门诊 SaaS 管理系统

## 项目简介

口腔门诊 SaaS 管理系统，为口腔诊所提供一站式数字化管理解决方案。

### 系统入口

- **管理后台（PC 桌面端）**：诊所管理员、医生、护士使用的 Web 管理后台
- **AI 智能中心**：集成在管理后台首页，通过 n8n 工作流调用各类 AI 模型
- **MCP Data Server**：外部数据接口层，供 Claude Code CLI 查询诊所数据

> 微信门户和员工 H5 工作台已移除，系统当前以 PC 管理后台为核心。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 2 + Vue Router + Element UI | 2.6.14 / 3.5.1 / 2.15.14 |
| 前端构建 | Vue CLI | 5.x |
| 后端 | Spring Boot + MyBatis | 3.2.5 / 3.0.3 |
| 数据库 | MySQL + Flyway | 9.6 |
| Java 版本 | Eclipse Temurin JDK/JRE | 17 |
| Node 版本 | Node.js | 18 |
| 容器化 | Docker + Docker Compose | — |
| AI 集成 | n8n 工作流（外部） | — |
| 数据接口 | MCP Data Server（Python + fastmcp） | — |

## 目录结构

```
口腔saas管理系统开发/
├── docs/                           # 项目文档
│   ├── 01-架构设计/                 # 系统架构与重构计划
│   ├── 02-开发文档/                 # 开发规范与集成指南
│   ├── 03-接口文档/                 # API 与 MCP 服务文档
│   ├── 04-验证测试/                 # 测试清单与验证步骤
│   ├── 05-维护指南/                 # 运维手册与问题排查
│   └── 06-开发计划/                 # 开发计划与排期
├── mcp-data-server/                # MCP 数据服务（独立启动）
├── n8n/                            # n8n 工作流配置（独立启动）
├── shuao-clinic-saas-source/       # 本系统源码
│   ├── docker/                     # Docker Compose 配置
│   ├── saas-vue-src/               # 前端源码（Vue CLI 5）
│   └── saas-springboot-src/        # 后端源码（Maven）
└── README.md                       # 本文件
```

## 单台电脑完整启动指南

### 前置条件

- Docker + Docker Compose 已安装
- 至少 4GB 可用内存
- 端口 3306、8080、7070、5678、8000 未被占用

### 1. 准备环境变量

```bash
# 诊所系统
cd shuao-clinic-saas-source/docker && cp .env.example .env

# n8n
cd n8n && cp .env.example .env

# MCP Data Server
cd mcp-data-server && cp .env.example .env
```

按需修改各 `.env` 文件中的密码和连接地址。

### 2. 按顺序启动服务

**第 1 步：启动诊所系统（MySQL + 后端 + 前端）**

```bash
cd shuao-clinic-saas-source/docker
./启动生产环境.sh
```

等待约 60 秒，确认 `clinic-backend` 状态为 `healthy`：
```bash
docker compose ps
```

**第 2 步：启动 n8n 工作流平台**

```bash
cd n8n
docker compose up -d
```

**第 3 步：启动 MCP Data Server**

```bash
cd mcp-data-server
docker compose up -d
```

### 3. 访问地址

| 服务 | 地址 |
|------|------|
| 诊所前端 | `http://localhost:7070` |
| 诊所后端 API | `http://localhost:8080` |
| n8n | `http://localhost:5678` |
| MCP SSE | `http://localhost:8000/sse` |

### 4. 停止所有服务

```bash
cd mcp-data-server && docker compose down
cd n8n && docker compose down
cd shuao-clinic-saas-source/docker && docker compose down
```

### 5. 切换开发/生产环境

```bash
cd shuao-clinic-saas-source/docker
./启动开发环境.sh    # 开发环境（代码热更新）
./启动生产环境.sh    # 生产环境（构建镜像）
./环境切换.sh status # 查看当前状态
```

---

## 常用命令

```bash
# 查看日志
docker compose -f docker-compose.dev.yml logs -f backend
docker compose -f docker-compose.dev.yml logs -f frontend

# 后端热重载（修改 Java 代码后）
docker compose -f docker-compose.dev.yml exec backend mvn compile -q

# 停止环境
docker compose -f docker-compose.dev.yml down
```

## AI 架构说明

系统不直接调用 OpenAI API，所有 AI 能力通过 n8n 工作流间接调用：

```
前端页面 → AiProxyController → AiProxyService → n8n Webhook → AI 模型
```

- **配置驱动**：AI Agent 的端点 URL 和启用字段通过数据库 `ai_agent_config` 表动态管理
- **无需 OpenAI Key**：诊所系统内不配置任何 AI API Key

详见 `docs/系统架构说明.md`。

## 文档索引

| 分类 | 路径 | 说明 |
|------|------|------|
| 架构说明 | `docs/系统架构说明.md` | 完整的系统架构文档 |
| 开发文档 | `docs/02-开发文档/` | 前端 AI 集成指南 |
| 接口文档 | `docs/03-接口文档/` | HTTP API 与 MCP 服务参考 |
| 维护指南 | `docs/05-维护指南/` | 运维手册与问题排查 |
| 开发计划 | `docs/06-开发计划/` | 开发计划与排期 |

## 开发规范

- 所有开发、测试、联调工作必须通过 Docker 启动服务完成
- 代码注释使用中文
- Git 提交信息使用中文，遵循 Conventional Commits 格式
- 详见 `CLAUDE.md`
