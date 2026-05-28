# n8n MCP 集成开发计划

## 一、背景

当前系统的 AI 功能（病历扩写、咨询辅助、经营分析等）已完成前后端对接。各页面通过 `ai_function_mapping` 动态绑定 AgentKey，调用 `/api/ai/proxy/{agentKey}` 转发到 n8n Webhook。

但现有工作流存在以下问题：
- 工作流结构需要手动在 n8n 界面配置
- 缺乏标准化的工作流模板
- 无法通过代码快速批量创建或修改工作流

## 二、目标

部署 **n8n MCP 服务器**，让 Claude 通过 MCP 协议直接操作 n8n，实现：
1. 自动读取现有工作流结构
2. 通过自然语言描述自动生成工作流
3. 批量创建、修改、激活工作流
4. 查看执行日志排错

## 三、方案对比

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| **A. n8n MCP 服务器** | 实时查看工作流状态、执行日志；自动化程度高 | 需额外部署 Node 服务；项目维护状态不明 | ⭐⭐⭐ |
| **B. 直接调用 n8n REST API** | 稳定可靠；无额外依赖 | 需要自己写调用逻辑；无法实时查看执行状态 | ⭐⭐⭐⭐ |
| **C. 导入工作流 JSON** | 最快；零部署成本 | 需要手动导入；无法自动化迭代 | ⭐⭐⭐⭐⭐（短期） |

**建议路线：** 先用方案 C 快速落地核心工作流，再用方案 A/B 做长期自动化管理。

## 四、部署步骤（方案 A：n8n MCP）

### 4.1 获取 n8n API Key

1. 登录 n8n 后台：`https://wn8n.smallcherry.cn/`
2. 点击右上角头像 → **Settings**
3. 左侧选择 **API** → 点击 **Create an API key**
4. 复制生成的 Key（格式如：`n8n_api_xxx...`）

### 4.2 部署 n8n-mcp-server（推荐 Docker Compose）

#### 方式一：Docker Compose（推荐）

在项目合适位置（如 `~/n8n-mcp-server`）创建以下文件：

**`docker-compose.yml`**

```yaml
version: '3.8'

services:
  n8n-mcp-server:
    image: leonardsellem/n8n-mcp-server:latest
    container_name: n8n-mcp-server
    restart: unless-stopped
    environment:
      N8N_API_URL: ${N8N_API_URL}
      N8N_API_KEY: ${N8N_API_KEY}
      # 可选：如果 Webhook 启用了 Basic Auth，请取消注释
      # N8N_WEBHOOK_USERNAME: ${N8N_WEBHOOK_USERNAME}
      # N8N_WEBHOOK_PASSWORD: ${N8N_WEBHOOK_PASSWORD}
      DEBUG: ${DEBUG:-false}
```

**`.env`**

```bash
# n8n 实例 API 地址，必须以 /api/v1 结尾
N8N_API_URL=https://wn8n.smallcherry.cn/api/v1

# n8n API Key（从 Settings > API 获取）
N8N_API_KEY=your_actual_n8n_api_key_here

# 可选：Webhook Basic Auth（如启用）
# N8N_WEBHOOK_USERNAME=your_webhook_username
# N8N_WEBHOOK_PASSWORD=your_webhook_password

# 可选：调试日志
DEBUG=false
```

**启动服务**

```bash
docker-compose up -d
docker-compose logs -f n8n-mcp-server
```

#### 方式二：本地 Node（备选）

```bash
git clone https://github.com/leonardsellem/n8n-mcp-server.git
cd n8n-mcp-server
npm install
npm run build
```

### 4.3 配置 Claude Code MCP

#### 配置位置说明（重要）

Claude Code MCP 配置分为两个层级：

| 层级 | 配置文件 | 作用范围 |
|------|---------|---------|
| **全局配置** | `~/.claude.json` | 所有项目生效（推荐用于 n8n 等基础设施服务） |
| **项目级配置** | `.vscode/mcp.json` | 仅当前项目生效 |

**n8n MCP 属于基础设施服务（类似数据库、Redis），建议配置在全局位置 `~/.claude.json`，这样在任何项目会话中都能操作 n8n 工作流。**

#### Docker Compose 方式配置

由于 MCP 服务器运行在容器内，需要通过 `docker exec` 调用：

编辑全局配置文件 `~/.claude.json`：

```json
{
  "mcpServers": {
    "n8n": {
      "command": "docker",
      "args": [
        "exec",
        "-i",
        "n8n-mcp-server",
        "node",
        "/app/build/index.js"
      ],
      "env": {},
      "type": "stdio"
    }
  }
}
```

**注意：** 容器内路径 `/app/build/index.js` 需根据实际镜像调整。环境变量已在 `docker-compose.yml` 中设置，无需在 Claude 配置中重复。

#### 本地 Node 方式配置

```json
{
  "mcpServers": {
    "n8n": {
      "command": "node",
      "args": ["/path/to/n8n-mcp-server/dist/index.js"],
      "env": {
        "N8N_API_URL": "https://wn8n.smallcherry.cn/api/v1",
        "N8N_API_KEY": "你的API密钥"
      },
      "type": "stdio"
    }
  }
}
```

### 4.4 重启 Claude Code

完全退出 Claude Code（包括 VS Code 插件），重新启动后 MCP 工具即可使用。

### 4.5 验证 MCP 连接

重启后，向 Claude 发送：

> "列出我 n8n 里的所有工作流"

如果 Claude 能正确返回工作流列表，说明 MCP 配置成功。

## 五、MCP 可用工具（预期）

部署成功后，Claude 可通过以下工具操作 n8n：

| 工具名 | 功能 |
|--------|------|
| `list_workflows` | 列出所有工作流 |
| `get_workflow` | 读取指定工作流详情 |
| `create_workflow` | 创建新工作流 |
| `update_workflow` | 更新工作流 |
| `activate_workflow` | 激活/停用工作流 |
| `execute_workflow` | 触发工作流执行 |
| `list_executions` | 查看执行历史 |

## 六、标准工作流数据流模式（重要）

所有 AI 功能工作流必须遵循以下统一数据流，确保前端、n8n、后端 API 之间的数据传递一致、可维护。

### 6.1 整体架构

```
┌──────────────┐      Webhook POST       ┌──────────────┐
│   前端页面    │ ──────────────────────> │   n8n 工作流  │
│ (Vue + Axios)│   {agentKey, userInput} │              │
└──────────────┘                         └──────┬───────┘
                                                │
                                                ▼
                                      ┌─────────────────┐
                                      │ 1. Webhook 节点  │
                                      │   接收用户输入    │
                                      └────────┬────────┘
                                               │
                                               ▼
                                      ┌─────────────────┐
                                      │ 2. AI Agent 节点 │
                                      │  分析用户意图    │
                                      │ 输出固定字段    │
                                      └────────┬────────┘
                                               │
                                               ▼
                                      ┌─────────────────┐
                                      │ 3. HTTP Request │
                                      │   调用后端 API  │
                                      │ 获取业务数据    │
                                      └────────┬────────┘
                                               │
                                               ▼
                                      ┌─────────────────┐
                                      │ 4. Code 节点    │
                                      │ 数据清洗/聚合   │
                                      └────────┬────────┘
                                               │
                                               ▼
                                      ┌─────────────────┐
                                      │ 5. 返回节点      │
                                      │ 构造标准响应    │
                                      └────────┬────────┘
                                               │
                                               ▼
                                        返回前端
```

### 6.2 各节点详细说明

#### 节点 1：Webhook 节点（接收请求）

- **触发方式**：POST
- **路径**：由 AgentKey 决定，如 `/webhook/medical-record-expand`
- **接收参数**：

```json
{
  "agentKey": "medical-record-expand",
  "userInput": "患者张三，男，35岁，主诉牙痛",
  "context": {
    "patientId": 123,
    "clinicId": 1
  }
}
```

#### 节点 2：AI Agent 节点（意图分析）

- **作用**：解析用户输入，提取关键参数，决定需要调用哪些后端 API
- **输出固定字段**：

```json
{
  "intent": "medical_record_expand",
  "extractedParams": {
    "patientName": "张三",
    "gender": "男",
    "age": 35,
    "chiefComplaint": "牙痛"
  },
  "requiredApis": [
    "GET /patient-details/basic/{patientId}",
    "GET /medical-records/selectByPatientId"
  ]
}
```

#### 节点 3：HTTP Request 节点（调用后端 API）

- **作用**：根据 AI Agent 输出的 `requiredApis`，逐个调用后端接口获取数据
- **请求规范**：
  - Base URL：`http://host.docker.internal:8080`（Docker 内访问宿主机）或 `http://192.168.1.x:8080`
  - Headers：必须携带 `X-API-Key: {{ $env.API_KEY }}`
  - 参数：从 AI Agent 输出中提取 `extractedParams` 作为路径参数或查询参数

**示例配置：**

```
Method: GET
URL: {{ $env.BASE_URL }}/patient-details/basic/{{ $json.extractedParams.patientId }}
Headers:
  X-API-Key: {{ $env.API_KEY }}
```

#### 节点 4：Code 节点（数据处理）

- **作用**：聚合多个 API 的返回结果，进行数据清洗、格式转换、业务计算
- **输入**：各 HTTP Request 节点的返回数据
- **输出**：整理为前端需要的字段结构

**示例逻辑：**

```javascript
// 合并患者基础信息和病历记录
const patientInfo = $json["HTTP Request"][0].json.data;
const records = $json["HTTP Request 2"][0].json.data.list;

return [{
  json: {
    patientName: patientInfo.name,
    visitCount: patientInfo.visitCount,
    lastVisit: patientInfo.lastVisit,
    recentRecords: records.slice(0, 3).map(r => ({
      diagnosis: r.diagnosis,
      treatmentPlan: r.treatment_plan,
      visitDate: r.visit_date
    }))
  }
}];
```

#### 节点 5：返回节点（构造响应）

- **作用**：将处理后的数据包装为标准格式返回给前端
- **必须遵循的返回结构**（详见第 7 节）：

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    // 具体业务字段
  }
}
```

### 6.3 数据流关键原则

| 原则 | 说明 |
|------|------|
| **AI 不直接查库** | AI Agent 只负责分析意图和提取参数，实际数据获取必须通过后端 API |
| **API 统一入口** | 所有数据请求走后端 REST API，复用现有鉴权和业务逻辑 |
| **参数传递标准化** | AI Agent 输出固定字段名，HTTP 节点通过表达式引用 |
| **错误分层处理** | API 错误在 HTTP 节点捕获，AI 处理错误在 Agent 节点捕获，分别返回不同错误码 |
| **环境变量隔离** | Base URL、API Key 等配置通过 n8n 环境变量传入，不硬编码在工作流中 |

### 6.4 错误处理流程

```
Webhook 接收请求
    │
    ▼
AI Agent 分析意图
    │
    ├── 意图不明确 → 返回 { code: "400", msg: "无法理解您的请求" }
    │
    ▼
HTTP Request 调用 API
    │
    ├── API 401/403 → 返回 { code: "401", msg: "API Key 无效" }
    ├── API 500 → 返回 { code: "500", msg: "后端服务异常" }
    ├── API 无数据 → 返回 { code: "200", msg: "暂无数据", data: {} }
    │
    ▼
Code 数据处理
    │
    ├── 处理异常 → 返回 { code: "500", msg: "数据处理失败" }
    │
    ▼
返回前端
```

---

## 七、第一阶段工作流清单

需要创建/标准化的 n8n 工作流，全部基于上述数据流模式：

| 系统功能 | AgentKey | Webhook 输入 | AI Agent 提取参数 | 调用 API | 输出字段 |
|----------|----------|------|----------|
| 病历扩写 | `medical-record-expand` | 患者信息、病历字段 | `chief_complaint`, `present_illness_history`, `past_medical_history`, `infectious_history`, `allergy_history`, `general_condition`, `examination_findings`, `auxiliary_examination`, `diagnosis`, `treatment_plan`, `treatment`, `medical_advice`, `prescription`, `image_summary`, `notes` |
| 咨询辅助 | `consultation-assist` | 咨询记录信息 | `analysis_result`, `suggestions` |
| 经营分析 | `business-analysis` | 财务数据、预约数据 | `analysis_report` |
| 患者洞察 | `patient-insight` | 患者档案 | `insight_summary` |
| 回访辅助 | `followup-assist` | 回访记录 | `followup_plan`, `suggestions` |

## 七、返回格式规范

所有 n8n 工作流最终响应必须遵循以下标准格式：

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    "chief_complaint": "...",
    "present_illness_history": "...",
    "...": "..."
  }
}
```

**关键要求：**
- 必须包含 `code` + `data` 字段
- `data` 必须是 JSON 对象，不能是字符串化的 map
- 字段名必须与前端期望的完全一致
- 不要包裹在数组 `[]` 中

## 八、风险与应对

| 风险 | 应对策略 |
|------|----------|
| MCP 服务器不稳定 | 保留方案 B（REST API）作为 fallback |
| n8n 版本升级导致 API 变更 | 定期更新 MCP 服务器版本 |
| API Key 泄露 | 限制 API Key 权限；定期轮换 |
| 网络不通（Claude ↔ n8n MCP） | 确保部署在同一内网或公网可达 |

## 九、下一步行动

1. [ ] 获取 n8n API Key
2. [ ] 部署 n8n-mcp-server
3. [ ] 配置 Claude Code MCP 连接
4. [ ] 验证 MCP 工具可用（测试 `list_workflows`）
5. [ ] 创建第一个标准化工作流（病历扩写模板）
6. [ ] 前端联调测试回填逻辑
7. [ ] 批量复制模板到其他功能

---

**创建日期：** 2026-05-19  
**负责人：** yintao272  
**关联项目：** 口腔门诊 SaaS 管理系统 AI 模块
