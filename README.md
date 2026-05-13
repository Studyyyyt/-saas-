# 口腔门诊 SaaS 管理系统

## 项目简介

口腔门诊 SaaS 管理系统，为口腔诊所提供一站式数字化管理解决方案。

### 三大入口

- **管理后台（桌面端）**：诊所管理员、医生、护士使用的 Web 管理后台
- **患者微信门户**：患者通过微信访问的预约、查询、支付入口
- **员工微信 H5 工作台**：员工通过微信访问的移动办公入口

## 技术栈

- **前端**：Vue 2.6.14 + Vue Router 3 + Element UI 2.15 + Axios + ECharts + XLSX
- **后端**：Spring Boot 3.2.5 + MyBatis + MySQL + Flyway + PageHelper + Lombok
- **Java 版本**：17
- **容器化**：Docker + Docker Compose

## 目录结构

```
口腔saas管理系统开发/
├── docs/                           # 项目文档
│   ├── 01-架构设计/                 # 系统架构与重构计划
│   ├── 02-开发文档/                 # 开发规范与集成指南
│   ├── 03-接口文档/                 # API 与 MCP 服务文档
│   ├── 04-验证测试/                 # 测试清单与验证步骤
│   ├── 05-维护指南/                 # 运维手册与问题排查
│   └── 06-历史归档/                 # 已废弃或历史文档
├── requirements/                   # 需求文档
│   ├── ai-development/             # AI 功能需求文档
│   └── ...                         # 其他业务需求文档
├── shuao-clinic-saas-source/       # 项目源码
│   ├── saas-vue-src/               # 前端源码（Vue CLI 5）
│   └── saas-springboot-src/        # 后端源码（Maven）
└── README.md                       # 本文件
```

## 快速启动（Docker）

```bash
cd shuao-clinic-saas-source/docker
docker compose -f docker-compose.dev.yml up -d
```

- 前端开发服务器：http://localhost:7070
- 后端开发服务器：http://localhost:8080

### 常用命令

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

系统已完成 AI 架构重构：

- **旧架构**：内置 AI 逻辑，包含模型供应商管理、Agent 链接配置、患者洞察等复杂模块
- **新架构**：系统退化为展示层和数据层，所有 AI 逻辑外包到外部工作流平台
- **统一代理层**：前端通过 `aiStreamClient` 调用后端代理接口，后端转发到外部端点
- **配置驱动**：AI Agent 的端点 URL 和启用字段通过数据库配置动态管理

详见 `docs/01-架构设计/AI架构重构计划.md`。

## 文档索引

| 分类 | 路径 | 说明 |
|------|------|------|
| 架构设计 | `docs/01-架构设计/` | AI 架构重构计划 |
| 开发文档 | `docs/02-开发文档/` | 前端 AI 集成指南 |
| 接口文档 | `docs/03-接口文档/` | HTTP API 与 MCP 服务参考 |
| 验证测试 | `docs/04-验证测试/` | 功能验证清单 |
| 维护指南 | `docs/05-维护指南/` | 运维手册与问题排查 |
| 历史归档 | `docs/06-历史归档/` | 已废弃的旧版文档 |

## 开发规范

- 所有开发、测试、联调工作必须通过 Docker 启动服务完成
- 代码注释使用中文
- Git 提交信息使用中文，遵循 Conventional Commits 格式
- 详见 `CLAUDE.md`
