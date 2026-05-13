# 口腔门诊 SaaS 系统 MCP Server

本项目为口腔门诊 SaaS 管理系统提供 MCP（Model Context Protocol）服务封装，将系统业务 API 暴露为 MCP Tools，供外部 LLM Agent 或工作流调用。

## 项目说明

- **服务名称**: `clinic-mcp-server`
- **传输协议**: Streamable HTTP
- **默认端口**: `3001`
- **MCP 端点**: `POST /mcp`
- **健康检查**: `GET /health`

## 启动方式

### 本地开发

```bash
cd mcp-server
npm install
npm run dev        # 使用 tsx watch 热重载
```

### 生产构建

```bash
cd mcp-server
npm install
npm run build      # 编译 TypeScript 到 dist/
npm start          # 运行 dist/index.js
```

### Docker 启动

```bash
docker build -t clinic-mcp-server .
docker run -p 3001:3001 \
  -e API_BASE_URL=http://backend:8080 \
  -e API_AUTH_TOKEN=your_token \
  clinic-mcp-server
```

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `API_BASE_URL` | 系统后端 API 地址 | `http://backend:8080` |
| `API_AUTH_TOKEN` | 访问后端 API 的 Bearer Token | （空） |
| `PORT` | MCP 服务监听端口 | `3001` |

## Tools 列表

| Tool 名称 | 对应系统 API | 说明 |
|-----------|-------------|------|
| `query_patients` | `GET /patients/search` | 查询患者列表，支持姓名/手机号模糊搜索 |
| `query_appointments` | `GET /appointments/selectAll` | 查询预约列表，支持按状态过滤 |
| `query_medical_records` | `GET /medical-records/selectByPatientId` | 根据患者ID查询病历记录 |
| `query_finances` | `GET /finances/all` | 查询财务/收费记录，支持日期范围过滤 |
| `query_treatments` | `GET /treatments/selectAll` | 查询治疗处置记录，支持按患者姓名搜索 |
| `query_lab_orders` | `GET /lab-orders/search` | 查询技工加工单，支持关键词搜索 |
| `query_materials` | `GET /materials/search` | 查询耗材库存，支持名称/编号搜索 |
| `query_consultations` | `GET /consultations/search` | 查询咨询/回访记录，支持关键词搜索 |
| `get_patient_360` | `GET /patient360/overview/{patientId}` | 获取患者360度全景视图 |

## 技术栈

- Node.js 18+
- TypeScript 5.7+
- MCP SDK (`@modelcontextprotocol/sdk`)
- Express + Streamable HTTP Transport
- Zod（运行时参数校验）
- Axios（HTTP 客户端）
