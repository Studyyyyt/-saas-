# AI架构重构计划：系统展示层 + 外部工作流双轨方案

## 上下文与目标

当前系统内部有完整的AI调用链（`BusinessAnalysisChatService`的流式对话与function calling、`MedicalRecordAIService`的Prompt渲染与模型调用、`AiToolService`的直接数据库查询）。用户要求将**所有AI逻辑外包到外部自动化工作流平台**（n8n/阿里百炼/Dify等），系统退化为纯展示层和数据层。

同时提供两种并行数据获取方案：

1. **HTTP API方案**：提供完整API文档，用户在工作流中逐个配置HTTP节点
2. **MCP服务方案**：将系统API打包为MCP Server，AI Agent通过MCP协议自动发现工具

## 架构变化

### 重构前

```
用户 → 前端 → 后端AI服务（模型调用+function calling+数据库查询） → 前端展示
```

### 重构后

```
用户 → 前端 → 后端统一代理（仅转发） → 外部工作流（AI分析+数据获取+格式化）
                                        ↓
                                   系统业务API（HTTP/MCP）
```

## Phase 1: 后端清理与统一代理层

### 1.1 清理内部AI调用逻辑

**`BusinessAnalysisChatService.java`**（`saas-springboot-src/.../service/BusinessAnalysisChatService.java`）

- 删除：模型调用、SSE流式生成、function calling、工具执行、记忆管理
- 保留：会话管理（`getOrCreateSession`）、聊天记录存储
- 修改为：接收用户消息后，根据agent配置转发到外部端点，将外部响应透传给前端

**`MedicalRecordAIService.java`**（`saas-springboot-src/.../service/MedicalRecordAIService.java`）

- 删除：`callModel()`、`renderPrompt()`、安全校验（敏感词/诊断语气）、JSON解析修复、Mock回退
- 保留：字段配置管理（`getFullConfig`/`saveConfig`）、诊疗场景约束（`buildSceneConstraint`）
- 修改为：`expand()`方法变为纯代理转发
  - 组装请求体：将当前页面填写的字段数据、选中的诊疗场景（sceneId + sceneName + operations）、医生信息（accountId/accountName）按配置模板组装
  - POST到外部端点（`agentKey = medical-expand`）
  - 外部工作流返回完整JSON（包含全部12个病历字段）
  - 系统不做任何过滤/校验，直接透传JSON给前端
  - 前端根据字段启用配置过滤显示，只回填启用的字段

**`AiToolService.java`**（`saas-springboot-src/.../service/AiToolService.java`）

- 删除：所有直接查询Mapper的方法（`queryPatients`、`queryAppointments`等7个方法）
- 保留：`buildToolDefinitions()`方法 → 改造为生成MCP Tools描述和API文档用的Schema

**`AiModelProviderService.java`**（`saas-springboot-src/.../service/AiModelProviderService.java`）

- 标记为废弃或删除（系统不再直接调用模型）

### 1.2 新建统一AI代理层

**`AiProxyService.java`**（新建）

- `forward(String agentKey, Map<String, Object> payload)`：根据agent配置获取外部端点，转发HTTP请求
- 支持SSE模式（流式）和JSON模式（一次性）
- 支持配置化的认证注入（Bearer Token / API Key）
- 请求体模板变量替换：`{{user_message}}`、`{{account_id}}`、`{{account_name}}`、`{{session_id}}`、`{{history}}`

**`AiProxyController.java`**（新建）

- `POST /api/ai/proxy/{agentKey}`：统一AI代理接口
- 支持SSE（`Accept: text/event-stream`）和JSON两种响应模式
- 病历扩写也走此接口（`agentKey = medical-expand`）

### 1.3 AI助手配置表改造

**`ai_agent_config`表新增字段**（Flyway迁移脚本 Vxx）：

- `endpoint_url` VARCHAR(500) — 外部端点地址
- `auth_type` VARCHAR(20) — 认证类型：bearer / apikey / none
- `auth_token` VARCHAR(500) — 认证密钥
- `request_template` TEXT — 请求体JSON模板
- `response_type` VARCHAR(20) — 响应类型：sse / json
- `timeout_seconds` INT DEFAULT 30 — 超时时间

**删除/废弃字段**：

- `system_prompt` — 外部工作流自行管理Prompt
- `enabled_tools` — 工具管理由外部工作流负责

**`AiAgentConfigService.java`改造**：

- CRUD适配新字段
- 删除systemPrompt和enabledTools相关逻辑

### 1.4 AI总览与开关改造

**`AiConfigController.java` / `AiConfigService.java`**：

- 保留全局开关和功能开关（控制前端AI入口显隐）
- 删除：今日调用统计、Token消耗统计、错误率（这些由外部工作流管理）
- 保留：功能列表、启用状态管理

## Phase 2: MCP服务（方案二）

### 2.1 新建MCP Server模块

**独立项目**：`shuao-clinic-saas-source/mcp-server/`

- 使用Node.js + `@modelcontextprotocol/sdk`
- SSE模式运行（HTTP Server-Sent Events）
- 独立Docker容器部署

**`src/server.ts`**：

- 连接系统后端API获取数据（而非直接连数据库）
- 定义Tools：将系统主要业务API封装为MCP Tools
  - `query_patients` → GET /patients/search
  - `query_appointments` → GET /appointments/selectAll
  - `query_medical_records` → GET /medical-records/selectByPatientId
  - `query_finances` → GET /finances/all
  - `query_treatments` → GET /treatments/selectAll
  - `query_lab_orders` → GET /lab-orders/search
  - `query_materials` → GET /materials/search
  - `query_consultations` → GET /consultations/search
  - `get_patient_360` → GET /patient360/overview/{patientId}
- 认证：通过环境变量配置系统API的访问Token

### 2.2 Docker集成

**`docker-compose.dev.yml`新增mcp-server服务**：

```yaml
mcp-server:
  image: node:18
  container_name: clinic-mcp-server
  working_dir: /app
  command: npm start
  environment:
    API_BASE_URL: http://backend:8080
    API_AUTH_TOKEN: ${MCP_API_TOKEN:-}
    PORT: 3001
  ports:
    - "3001:3001"
  volumes:
    - ../mcp-server:/app
  networks:
    - clinic-network
```

### 2.3 API文档生成

**`ApiDocumentationController.java`**（新建）

- `GET /api/docs` — 返回所有业务API的文档（JSON格式）
- 包含：接口路径、方法、参数说明、响应示例
- 同时生成Markdown格式供人工阅读

**文档内容来源**：

- 手动维护核心API列表（推荐，保证准确性）
- 或基于SpringDoc/OpenAPI注解自动生成

## Phase 3: 前端改造

### 3.1 AI助手配置页面重写

**`AIAgentConfigView.vue`**（`saas-vue-src/src/views/Manager/AIAgentConfigView.vue`）

**去掉**：

- 系统提示词输入框
- 数据工具勾选（查询患者/预约/病历等7个工具）

**新增/保留**：

- 助手名称、图标、描述、主题色（保留）
- 快捷指令（保留）
- **外部端点配置卡片**：
  - 端点地址（URL）
  - 请求方法（POST/GET，默认POST）
  - 认证类型（Bearer Token / API Key / 无）
  - 认证密钥
  - 请求体模板（JSON编辑器，支持变量提示）
  - 响应类型（SSE流式 / JSON一次性）
  - 超时时间
- **变量提示说明**：`{{user_message}}`、`{{account_id}}`、`{{account_name}}`、`{{session_id}}`、`{{history}}`

### 3.2 病历扩写配置页面调整

**`MedicalRecordAIConfigView.vue`**（`saas-vue-src/src/views/Manager/MedicalRecordAIConfigView.vue`）

**去掉**：

- 提示词模板编辑
- Few-shot示例管理
- 温度/最大Token等模型参数
- 系统提示词

**保留**：

- 功能开关
- 字段配置（启用/禁用、最大长度、默认值、校验规则）——这些是业务规则不是AI逻辑
- 诊疗场景库（场景、步骤、禁止关键词、必须包含关键词）——业务规则
- 空字段策略（留白/提示医生）——业务规则

**新增**：

- 外部扩写端点配置（URL、认证、请求模板）
- 请求模板默认变量：
  - `{{fields}}` — 当前页面填写的所有字段键值对
  - `{{scene_id}}` — 选中的诊疗场景ID
  - `{{scene_name}}` — 选中的诊疗场景名称
  - `{{operations}}` — 已选中的操作步骤数组
  - `{{account_id}}` — 当前医生ID
  - `{{account_name}}` — 当前医生姓名
  - `{{enabled_fields}}` — 启用的字段列表（供工作流参考）
- 默认请求体模板示例：
  ```json
  {
    "scene_id": {{scene_id}},
    "scene_name": "{{scene_name}}",
    "operations": {{operations}},
    "fields": {{fields}},
    "account_id": {{account_id}},
    "account_name": "{{account_name}}",
    "enabled_fields": {{enabled_fields}}
  }
  ```

### 3.3 聊天功能接入统一代理

**`aiStreamClient.js`**（`saas-vue-src/src/utils/aiStreamClient.js`）

- `streamChat()` 修改请求目标：从 `/business-analysis/chat/stream` 改为 `/api/ai/proxy/{agentKey}`
- 删除模型供应商相关方法（`fetchModelProviderConfig`、`saveModelProviderConfig`等）
- 保留调试日志系统

**`HomeView.vue`** / **`BusinessAnalysisView.vue`**：

- 调用方式不变（仍然是`streamChat()`），只需确保传入正确的`agentKey`

### 3.4 病历扩写接入外部端点

**`MedicalRecordView.vue`**：

`aiAssist()`方法详细链路：

1. **收集字段数据**：遍历当前页面所有病历字段输入框，组装 `fields` 对象（key-value）
2. **收集场景信息**：获取当前选中的 `scene_id`、`scene_name`、`operations` 数组
3. **获取医生信息**：从 sessionStorage 读取 `account_id`、`account_name`
4. **获取启用字段列表**：从配置中读取 `enabled_fields`（当前启用的字段key列表）
5. **发送请求**：POST 到 `/api/ai/proxy/medical-expand`，请求体按模板组装
6. **接收并过滤回填**：
   - 接收工作流返回的完整JSON（包含全部12个字段）
   - 根据 `enabled_fields` 过滤：只回填启用的字段
   - 未启用的字段忽略（不显示、不报错）
   - 未返回的字段保持原值或按空字段策略处理
7. **删除直接调用 `/api/ai/medical-record/expand` 的旧逻辑**

### 3.5 模型供应商配置页面处理

**`ModelProviderConfigView.vue`**：

- 标记为废弃或删除（系统不再直接调用模型）
- 或在菜单中隐藏

## Phase 4: 数据库迁移

### Flyway迁移脚本

**`Vxx__ai_agent_config_add_endpoint_fields.sql`**：

```sql
ALTER TABLE ai_agent_config
  ADD COLUMN endpoint_url VARCHAR(500),
  ADD COLUMN auth_type VARCHAR(20) DEFAULT 'none',
  ADD COLUMN auth_token VARCHAR(500),
  ADD COLUMN request_template TEXT,
  ADD COLUMN response_type VARCHAR(20) DEFAULT 'json',
  ADD COLUMN timeout_seconds INT DEFAULT 30;
```

**数据迁移**：

- 现有4个默认Agent（default/finance/patient/schedule）的endpoint_url设为空（表示未配置，前端隐藏或提示配置）
- 用户后续在配置页面补充外部端点

## Phase 5: 验证测试

### 测试场景

1. **配置测试**：
   - 在AI助手配置页面配置一个外部端点（如n8n webhook测试地址）
   - 保存后确认数据库存储正确

2. **聊天转发测试**：
   - 在首页发送消息
   - 确认请求被转发到配置的外部端点
   - 确认外部返回的SSE流正确展示在前端

3. **病历扩写测试**：
   - 配置病历扩写外部端点
   - 在病历页面点击AI扩写
   - 确认字段数据被正确发送到外部端点
   - 确认返回的JSON正确映射到表单

4. **MCP服务测试**：
   - 启动mcp-server容器
   - 用MCP Inspector或curl测试工具列表和调用
   - 确认能正确调用系统业务API

5. **API文档测试**：
   - 访问 `/api/docs`
   - 确认文档内容完整、准确

## 关键文件清单

### 后端（修改）

- `saas-springboot-src/.../service/BusinessAnalysisChatService.java`
- `saas-springboot-src/.../service/MedicalRecordAIService.java`
- `saas-springboot-src/.../service/AiToolService.java`
- `saas-springboot-src/.../service/AiAgentConfigService.java`
- `saas-springboot-src/.../service/AiConfigService.java`
- `saas-springboot-src/.../controller/BusinessDailyAnalysisController.java`
- `saas-springboot-src/.../controller/MedicalRecordAIController.java`
- `saas-springboot-src/.../controller/AiAgentConfigController.java`

### 后端（新建）

- `saas-springboot-src/.../service/AiProxyService.java`
- `saas-springboot-src/.../controller/AiProxyController.java`
- `saas-springboot-src/.../controller/ApiDocumentationController.java`

### 前端（修改）

- `saas-vue-src/src/views/Manager/AIAgentConfigView.vue`
- `saas-vue-src/src/views/Manager/MedicalRecordAIConfigView.vue`
- `saas-vue-src/src/views/Manager/HomeView.vue`
- `saas-vue-src/src/views/Manager/BusinessAnalysisView.vue`
- `saas-vue-src/src/views/Manager/MedicalRecordView.vue`
- `saas-vue-src/src/utils/aiStreamClient.js`

### 前端（删除/废弃）

- `saas-vue-src/src/views/Manager/ModelProviderConfigView.vue`（菜单隐藏）
- `saas-vue-src/src/views/Manager/PatientAIConfigView.vue`（菜单隐藏）

### MCP服务（新建独立项目）

- `mcp-server/package.json`
- `mcp-server/src/server.ts`
- `mcp-server/Dockerfile`
- `mcp-server/README.md`（API映射说明）

### 基础设施

- `docker-compose.dev.yml`（新增mcp-server服务）
- `saas-springboot-src/src/main/resources/db/migration/Vxx__ai_agent_config_add_endpoint_fields.sql`

## 附录：病历扩写完整数据流

### A.1 前端 → 系统的请求数据

当用户在病历页面选择"根管治疗"，填写部分字段后点击"一键扩写"，系统组装如下请求体发送给外部工作流：

```json
{
  "scene_id": 3,
  "scene_name": "根管治疗",
  "operations": ["开髓引流"],
  "fields": {
    "chiefComplaint": "右下牙痛3天",
    "historyOfPresentIllness": "3天前开始痛",
    "pastHistory": "",
    "generalCondition": "",
    "examinationFindings": "",
    "auxiliaryExamination": "",
    "diagnosis": "",
    "treatmentPlan": "",
    "treatment": "",
    "medicalAdvice": "",
    "prescription": "",
    "notes": ""
  },
  "account_id": 5,
  "account_name": "张医生",
  "enabled_fields": [
    "chiefComplaint",
    "historyOfPresentIllness",
    "pastHistory",
    "generalCondition",
    "examinationFindings",
    "auxiliaryExamination",
    "diagnosis",
    "treatmentPlan",
    "treatment",
    "medicalAdvice",
    "prescription",
    "notes"
  ]
}
```

### A.2 外部工作流处理逻辑（由用户在工作流平台自行配置）

1. 接收上述JSON
2. 根据 `scene_name` = "根管治疗" 判断内容类型
3. 根据 `operations` = ["开髓引流"] 确定当前治疗阶段
4. 调用AI模型生成各专业字段内容（Prompt和模型选择由工作流管理）
5. 进行安全校验（敏感词、诊断语气等，由工作流管理）
6. 返回完整JSON

### A.3 工作流 → 系统的返回数据

工作流返回包含全部12个字段的JSON：

```json
{
  "chiefComplaint": "右下后牙自发痛3天",
  "historyOfPresentIllness": "患者3天前无明显诱因出现右下后牙自发性疼痛...",
  "pastHistory": "否认全身系统性疾病史，否认药物过敏史。",
  "generalCondition": "精神可，饮食睡眠尚可，大小便正常。",
  "examinationFindings": "面部对称，张口度正常。右下后牙对应牙位牙龈轻度红肿...",
  "auxiliaryExamination": "46牙根尖片示：龋坏达牙本质深层，近髓，根尖周未见明显异常。",
  "diagnosis": "考虑：1. 急性牙髓炎（46）；2. 深龋（46）",
  "treatmentPlan": "择期行根管治疗。建议术前完善根尖片检查。",
  "treatment": "局麻下46牙开髓，揭顶，拔髓，探及3个根管口，封入无砷失活剂，氧化锌暂封。",
  "medicalAdvice": "1. 避免患侧咀嚼硬物；2. 注意口腔卫生；3. 如疼痛加重可口服止痛药；4. 按预约时间复诊。",
  "prescription": "暂无",
  "notes": "患者对治疗方案表示理解，配合度良好。"
}
```

### A.4 系统 → 前端的过滤逻辑

1. 系统不做任何过滤，直接透传JSON给前端
2. 前端根据字段启用配置过滤：
   - 如果 "prescription" 在字段配置中被禁用 → 忽略该字段，不在页面上回填
   - 只回填 `enabled_fields` 列表中包含的字段
3. 前端空字段策略：
   - 如果工作流返回的字段值为空字符串，且空字段策略为"留白" → 保持空值
   - 如果空字段策略为"提示医生" → 显示"请医生手动填写"
