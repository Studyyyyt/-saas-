# 阶段四：外部 AI Agent 中心

> 目标：预留标准化的外部 AI Agent 对接能力，支持通过 n8n / 阿里云百炼等平台编排的 Agent 接入
> 预计工期：1 周
> 前置条件：阶段三完成，AI 对话 UI 组件（ChatContainer.vue）已可用

---

## 一、任务清单

### 1.1 数据层：Agent 配置管理

#### 1.1.1 数据库表设计
- **文件路径**：`saas-springboot-src/src/main/resources/db/migration/V34__ExternalAgentConfig.sql`
- **需求**：
  1. 新建表 `external_agent_config`：
     ```sql
     CREATE TABLE external_agent_config (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       name VARCHAR(100) NOT NULL COMMENT 'Agent 名称',
       description VARCHAR(500) COMMENT 'Agent 描述',
       icon_url VARCHAR(500) COMMENT '图标 URL',
       endpoint_url VARCHAR(500) NOT NULL COMMENT 'Agent 接入地址',
       auth_type VARCHAR(20) NOT NULL DEFAULT 'none' COMMENT '认证类型：none / bearer / apikey / basic',
       auth_token VARCHAR(500) COMMENT '认证令牌/密码',
       auth_username VARCHAR(100) COMMENT 'Basic 认证用户名',
       protocol VARCHAR(20) NOT NULL DEFAULT 'http_api' COMMENT '协议：http_api / webhook / sse / sdk',
       input_schema JSON COMMENT '输入参数 JSON Schema',
       output_schema JSON COMMENT '输出参数 JSON Schema',
       timeout_seconds INT DEFAULT 30 COMMENT '请求超时时间（秒）',
       retry_times INT DEFAULT 1 COMMENT '失败重试次数',
       enabled TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
       sort_order INT DEFAULT 0 COMMENT '排序权重',
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
     );
     ```
  2. 预设示例数据（Mock Agent，用于前端联调）：
     - "智能客服助手" — 模拟 n8n Webhook 调用
     - "经营数据分析助手" — 模拟百炼 API 调用

#### 1.1.2 Entity / Mapper / Service / Controller
- **涉及文件**：
  - `saas-springboot-src/src/main/java/com/example/springboot/entity/ExternalAgentConfig.java`
  - `saas-springboot-src/src/main/java/com/example/springboot/mapper/ExternalAgentConfigMapper.java`
  - `saas-springboot-src/src/main/java/com/example/springboot/service/ExternalAgentConfigService.java`
  - `saas-springboot-src/src/main/java/com/example/springboot/controller/ExternalAgentConfigController.java`
- **需求**：
  1. Entity：与表结构对应的 POJO，使用 Lombok `@Data`
  2. Mapper：基础 CRUD + `selectByEnabled()` 查询所有启用的 Agent
  3. Service：基础 CRUD + 参数校验（`endpoint_url` 格式校验、`auth_type` 合法性校验）
  4. Controller：REST API，路径 `/external-agent-configs`
     - `GET /external-agent-configs` — 列表查询（分页）
     - `GET /external-agent-configs/{id}` — 详情
     - `POST /external-agent-configs` — 新增（仅 admin）
     - `PUT /external-agent-configs/{id}` — 修改（仅 admin）
     - `DELETE /external-agent-configs/{id}` — 删除（仅 admin）
     - `GET /external-agent-configs/enabled` — 查询所有启用的 Agent（前端 Agent 中心使用，无需鉴权或仅登录即可）

### 1.2 对接层：统一 Agent 调用服务

#### 1.2.1 ExternalAgentService
- **文件路径**：`saas-springboot-src/src/main/java/com/example/springboot/service/ExternalAgentService.java`
- **需求**：
  1. **统一调用入口**：`invokeAgent(Long agentId, String userMessage, Map<String, Object> context)`
  2. **协议适配**：
     - `http_api`：发送 POST 请求，JSON body 包含 `message` 和 `context`，返回 JSON
     - `webhook`：发送 POST 请求到 webhook URL，异步等待回调（或同步等待响应）
     - `sse`：建立 SSE 连接，将流式数据通过 `SseEmitter` 返回给前端
     - `sdk`：预留接口，实际调用由前端 JS SDK 直接执行，后端仅做配置转发
  3. **认证处理**：
     - `none`：无认证
     - `bearer`：Header 增加 `Authorization: Bearer {token}`
     - `apikey`：Header 增加 `X-API-Key: {token}` 或 Query 参数
     - `basic`：Header 增加 `Authorization: Basic {base64(username:password)}`
  4. **熔断与降级**：
     - 请求超时（默认 30s，可配置）后抛出友好异常
     - 失败时按配置重试（默认 1 次）
     - 最终失败时返回预设兜底消息："Agent 服务暂时不可用，请稍后重试"
  5. **日志记录**：记录每次调用的请求参数、响应摘要、耗时、状态（成功/失败）

#### 1.2.2 SSE 流式代理
- **文件路径**：`saas-springboot-src/src/main/java/com/example/springboot/controller/ExternalAgentController.java`
- **需求**：
  1. `GET /external-agents/{agentId}/chat?message=xxx` — 统一聊天入口
  2. 根据 Agent 配置决定同步返回还是 SSE 流式返回
  3. 若是 SSE 协议，后端作为代理：接收外部 Agent 的 SSE 流，逐条转发给前端
  4. 支持会话上下文传递（`sessionId` 参数）

### 1.3 前端：AI Agent 中心

#### 1.3.1 Agent 中心列表页
- **文件路径**：`saas-vue-src/src/views/Manager/AiAgentCenterView.vue`
- **需求**：
  1. **页面标题**："AI 智能体中心" + 副标题"连接外部 AI 能力，扩展诊所智慧大脑"
  2. **Agent 卡片网格**：
     - 每个 Agent 一个卡片：图标、名称、描述、状态（在线/离线）
     - 在线判定：后端定时探测 `endpoint_url`（或首次加载时探测），返回 200 即在线
     - 卡片 hover 效果：阴影增强、轻微上移
     - 点击卡片进入该 Agent 的对话页面
  3. **空状态**：未配置任何 Agent 时，展示引导插画 + "管理员可在系统设置中添加 AI Agent"
  4. **管理员快捷入口**：admin 角色显示"管理 Agent 配置"按钮，跳转至配置管理页

#### 1.3.2 Agent 配置管理页
- **文件路径**：`saas-vue-src/src/views/Manager/AiAgentConfigView.vue`
- **需求**：
  1. **列表展示**：表格展示所有 Agent 配置（名称、协议、地址、状态、操作）
  2. **新增/编辑弹窗**：
     - 表单字段：名称、描述、图标 URL、接入地址、协议（下拉选择）、认证类型（联动显示对应输入框）、超时时间、重试次数
     - 协议选择 `sdk` 时，显示"前端 SDK 配置"文本域（JSON）
     - 表单校验：接入地址必须为合法 URL，名称必填
  3. **启用/禁用开关**：表格行内可直接切换 enabled 状态
  4. **测试连接**：编辑页增加"测试连接"按钮，后端发送探测请求并返回结果

#### 1.3.3 Agent 独立对话页
- **文件路径**：`saas-vue-src/src/views/Manager/AiAgentChatView.vue`
- **需求**：
  1. **复用 Chat UI**：复用阶段三开发的 `ChatContainer.vue`、`ChatMessage.vue`、`ChatInput.vue`
  2. **页面路由**：`/ai-agent-chat/:agentId`，进入时根据 `agentId` 加载 Agent 信息（名称、图标）
  3. **会话隔离**：每个 Agent 的会话独立存储（localStorage 或后端会话表），切换 Agent 时会话不混淆
  4. **消息发送**：调用后端 `GET /external-agents/{agentId}/chat?message=xxx`
  5. **SSE 支持**：若 Agent 协议为 `sse`，前端使用 `EventSource` 接收流式响应
  6. **错误处理**：Agent 离线或服务异常时，对话区域显示友好提示 + "重试"按钮

#### 1.3.4 悬浮窗模式（可选增强）
- **文件路径**：`saas-vue-src/src/components/ai/AgentFloatButton.vue`
- **需求**：
  1. 在所有管理后台页面右下角显示悬浮球（机器人图标）
  2. 点击后展开小面板：展示已启用的 Agent 列表（图标 + 名称）
  3. 选择 Agent 后，以小弹窗形式展开对话（类似 Intercom 客服窗口）
  4. 支持拖拽移动弹窗位置
  5. 最小化后恢复为悬浮球，未读消息显示红点徽标
  6. **配置项**：管理员可在系统设置中开启/关闭悬浮窗模式

### 1.4 接入示例与文档

#### 1.4.1 n8n Webhook 接入示例
- **文件路径**：`requirements/接入示例-n8n-webhook.md`
- **内容**：
  1. n8n 工作流搭建步骤（触发器 → HTTP Request → 处理逻辑 → 响应）
  2. 配置示例：Webhook URL、认证方式、请求体格式
  3. 测试方法：使用 Postman 或本系统的"测试连接"功能验证

#### 1.4.2 阿里云百炼接入示例
- **文件路径**：`requirements/接入示例-阿里云百炼.md`
- **内容**：
  1. 百炼控制台创建应用步骤
  2. 获取 API Key 和 Endpoint
  3. 在本系统 Agent 配置中填入参数（协议选 `http_api` 或 `sse`，认证选 `apikey`）
  4. 请求/响应格式对照表

---

## 二、关键修改文件汇总

| 模块 | 文件路径 | 修改类型 |
|------|---------|---------|
| 数据库 | `db/migration/V34__ExternalAgentConfig.sql` | 新增 |
| 后端 Entity | `entity/ExternalAgentConfig.java` | 新增 |
| 后端 Mapper | `mapper/ExternalAgentConfigMapper.java` | 新增 |
| 后端 Service | `service/ExternalAgentConfigService.java` | 新增 |
| 后端 Service | `service/ExternalAgentService.java` | 新增 |
| 后端 Controller | `controller/ExternalAgentConfigController.java` | 新增 |
| 后端 Controller | `controller/ExternalAgentController.java` | 新增 |
| 前端页面 | `src/views/Manager/AiAgentCenterView.vue` | 新增 |
| 前端页面 | `src/views/Manager/AiAgentConfigView.vue` | 新增 |
| 前端页面 | `src/views/Manager/AiAgentChatView.vue` | 新增 |
| 前端组件 | `src/components/ai/AgentFloatButton.vue` | 新增（可选） |
| 前端路由 | `src/router/index.js` | 修改（注册新页面） |
| 前端菜单 | `src/views/Manager.vue` | 修改（增加 AI Agent 中心菜单） |
| 文档 | `requirements/接入示例-n8n-webhook.md` | 新增 |
| 文档 | `requirements/接入示例-阿里云百炼.md` | 新增 |

---

## 三、验收标准

- [ ] 管理员可在"AI Agent 配置"页面新增、编辑、删除、启用/禁用外部 Agent
- [ ] Agent 中心页面展示所有已启用的 Agent 卡片，显示在线/离线状态
- [ ] 点击 Agent 卡片进入独立对话页，可正常发送消息并接收回复
- [ ] SSE 协议的 Agent 支持流式输出，前端实时展示打字机效果
- [ ] Agent 服务不可用时，前端显示友好提示，不崩溃
- [ ] 提供 n8n Webhook 和阿里云百炼的接入示例文档，按文档步骤可在 10 分钟内完成一个新 Agent 的接入
- [ ] （可选）悬浮窗模式在所有后台页面右下角可用，支持多 Agent 切换

---

## 四、风险与注意事项

1. **外部 Agent 接口不可控**：n8n/百炼的接口格式可能变化，需设计足够的灵活性（`input_schema` / `output_schema` 预留）
2. **跨域问题**：若外部 Agent 部署在不同域名，需确认 CORS 支持或通过后端的代理转发
3. **认证信息安全**：`auth_token` 存储在数据库中，需确保数据库访问权限控制，生产环境建议加密存储
4. **SSE 代理性能**：后端作为 SSE 代理时，需确保连接池和线程数足够，避免高并发时阻塞
5. **前端组件复用**：阶段四高度依赖阶段三的 Chat UI 组件，若阶段三延期，阶段四需同步调整
