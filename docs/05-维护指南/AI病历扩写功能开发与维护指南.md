# AI 病历扩写功能开发与维护指南

> 本文档记录 AI 病历扩写（medical-expand）功能的完整数据流、字段映射、代码位置及常见问题。
> 最后更新：2026-05-16

---

## 一、功能概述

AI 病历扩写功能允许医生在新增/编辑病历页面，点击"AI 扩写"按钮后，系统自动将当前病历表单数据发送到外部 AI 平台（如 n8n、Dify、Coze），由 AI 生成完整的病历内容，并自动回填到表单各字段中。

**核心链路：**
```
前端表单 → 后端代理 → 外部 Webhook（AI 平台）→ 后端解包 → 前端回填
```

---

## 二、完整数据流

### 2.1 请求方向（前端 → Webhook）

```
[前端] MedicalRecordView.vue
    ↓ POST /api/ai/proxy/medical-expand
    { fields: { patient_id, patient_name, chief_complaint, ... }, account_id: 1 }

[后端] AiProxyController.proxy()
    ↓ 校验登录态、白名单
    ↓ 调用 aiProxyService.forward()

[后端] AiProxyService.forward()
    ↓ 查找 ai_agent_config 配置
    ↓ 调用 wrapProtocolIfNeeded() 包装为标准协议
    ↓ 调用 buildRequestBody() 构建请求体
    ↓ HTTP POST 发送到外部 Webhook

[外部 Webhook] 接收标准协议 JSON，由 AI 处理后返回结果
```

### 2.2 响应方向（Webhook → 前端）

```
[外部 Webhook] 返回 JSON：{ code: "200", msg: "success", data: { chief_complaint: "...", ... } }

[后端] AiProxyService.forward() 返回原始响应字符串

[后端] AiProxyController.proxy()
    ↓ 解析 JSON 为对象
    ↓ 【关键】检测到标准格式 {code, data} 时，提取内层 data
    ↓ 包装为 Result.success(data) 返回前端

[前端] MedicalRecordView.vue expandWithAI()
    ↓ 读取 res.data.data（此时直接是业务字段对象）
    ↓ 逐字段回填到 this.form.xxx
```

---

## 三、字段映射总表

### 3.1 前端表单字段 ↔ 发送字段 ↔ 返回字段

| 前端表单字段名 | 发送给 Webhook 的字段名 | Webhook 返回字段名 | 回填目标字段 |
|---|---|---|---|
| `patient_id` | `patient_id` | — | —（只读） |
| `patient_name` | `patient_name` | — | —（只读） |
| `doctor_account_id` | `doctor_account_id` | — | — |
| `doctor_name` | `doctor_name` | — | — |
| `nurse_name` | `nurse_name` | — | — |
| `assistant_name` | `assistant_name` | — | — |
| `visit_date` | `visit_date` | — | — |
| `record_type` | `record_type` | — | — |
| `chief_complaint` | `chief_complaint` | `chief_complaint` | `this.form.chief_complaint` |
| `present_illness_history` | `present_illness_history` | `present_illness_history` | `this.form.present_illness_history` |
| `past_history` | `past_medical_history` | `past_medical_history` | `this.form.past_history` |
| `infectious_history` | `infectious_history` | `infectious_history` | `this.form.infectious_history` |
| `allergy_history` | `allergy_history` | `allergy_history` | `this.form.allergy_history` |
| `general_condition` | `general_condition` | `general_condition` | `this.form.general_condition` |
| `examination` | `examination_findings` | `examination_findings` | `this.form.examination` |
| `auxiliary_examination` | `auxiliary_examination` | `auxiliary_examination` | `this.form.auxiliary_examination` |
| `diagnosis` | `diagnosis` | `diagnosis` | `this.form.diagnosis` |
| `treatment_plan` | `treatment_plan` | `treatment_plan` | `this.form.treatment_plan` |
| `treatment` | `treatment` | `treatment` | `this.form.treatment` |
| `tooth_positions` | `tooth_positions` | — | — |
| `medical_advice` | `medical_advice` | `medical_advice` | `this.form.medical_advice` |
| `prescription` | `prescription` | `prescription` | `this.form.prescription` |
| `record_tags` | `record_tags` | `record_tags` | `this.form.record_tags` |
| `image_summary` | `image_summary` | `image_summary` | `this.form.image_summary` |
| `notes` | `notes` | `notes` | `this.form.notes` |
| `record_status` | `record_status` | — | — |
| `operation_items` | `operation_items` | — | — |
| — | `draft_record`（由 chief_complaint + present_illness_history 拼接） | — | — |

**注意：**
- 发送字段名和返回字段名**必须保持一致**，否则前端无法正确回填。
- `past_history`（前端）和 `past_medical_history`（发送/返回）是特例，需要手动映射。
- `examination`（前端）和 `examination_findings`（发送/返回）也是特例。

---

## 四、涉及文件清单

### 4.1 前端文件

| 文件路径 | 作用 |
|---|---|
| `saas-vue-src/src/views/Manager/MedicalRecordView.vue` | 病历新增/编辑页面，包含 `expandWithAI()` 方法（发送字段 + 回填逻辑） |
| `saas-vue-src/src/views/Manager/AIOverviewView.vue` | AI 总览页面，配置 Agent 的 Webhook 地址、认证令牌、用途位置 |

### 4.2 后端文件

| 文件路径 | 作用 |
|---|---|
| `saas-springboot-src/src/main/java/com/example/springboot/controller/AiProxyController.java` | AI 统一代理控制器，负责校验、转发、解包标准格式响应 |
| `saas-springboot-src/src/main/java/com/example/springboot/service/AiProxyService.java` | AI 代理服务，负责协议包装、请求构建、HTTP 转发 |
| `saas-springboot-src/src/main/java/com/example/springboot/entity/AiAgentConfig.java` | Agent 配置实体，映射 ai_agent_config 表 |
| `saas-springboot-src/src/main/java/com/example/springboot/mapper/AiAgentConfigMapper.java` | MyBatis Mapper，CRUD ai_agent_config 表 |

### 4.3 测试文件

| 文件路径 | 作用 |
|---|---|
| `docker/mock-webhook/mock_server.py` | Python Mock Webhook 服务，用于本地测试 |
| `docker/mock-webhook/Dockerfile` | Mock 服务容器镜像构建文件 |
| `docker/docker-compose.dev.yml` | 开发环境编排，包含 mock-webhook 服务定义 |

---

## 五、如何修改发送的字段

如果你希望在前端发送更多字段给 Webhook，或删除某些字段：

### 步骤 1：修改前端发送字段

编辑文件：`saas-vue-src/src/views/Manager/MedicalRecordView.vue`

找到 `expandWithAI()` 方法中的 `fields` 对象（约第 1858 行）：

```javascript
const fields = {
  patient_id: patientId,
  patient_name: patientName,
  // ... 现有字段
  // 新增字段示例：
  new_field: this.form.new_field || '',
}
```

### 步骤 2：修改前端回填逻辑（如果需要 AI 返回该字段）

在同文件的 `expandWithAI()` 方法中，找到回填部分（约第 1892 行）：

```javascript
if (result.new_field) this.form.new_field = result.new_field
```

### 步骤 3：修改 Mock Server（本地测试时使用）

编辑文件：`docker/mock-webhook/mock_server.py`

在 `response["data"]` 中增加对应字段：

```python
"data": {
    # ... 现有字段
    "new_field": "测试内容",
}
```

### 步骤 4：重新构建并测试

```bash
cd shuao-clinic-saas-source/docker
docker compose -f docker-compose.dev.yml up -d --build mock-webhook
```

---

## 六、如何修改 Webhook 接收的字段（后端协议层）

如果你希望调整后端包装的标准协议格式：

### 6.1 修改协议包装逻辑

编辑文件：`saas-springboot-src/src/main/java/com/example/springboot/service/AiProxyService.java`

找到 `wrapProtocolIfNeeded()` 方法（约第 294 行）：

- 修改 `context` 的内容：增加/删除上下文字段
- 修改 `input_fields` 的包装方式：编辑 `extractInputFields()` 和 `buildFieldMeta()`
- 修改 `output_schema` 的默认值：编辑 `buildOutputSchema()`

### 6.2 修改请求体模板支持

在 `buildRequestBody()` 方法（约第 496 行）中：

- 模式一（模板替换）：在 AI 总览页面配置 `request_template`，使用 `{{变量名}}` 占位符
- 模式二（直接序列化）：清空 `request_template`，后端直接发送标准协议 JSON

---

## 七、Mock 测试服务使用说明

### 7.1 启动 Mock 服务

```bash
cd shuao-clinic-saas-source/docker
docker compose -f docker-compose.dev.yml up -d --build mock-webhook
```

### 7.2 配置系统使用 Mock

1. 打开系统设置 → AI 智能中心 → AI 总览
2. 编辑 `medical-expand` 配置
3. Webhook 地址填写：`http://mock-webhook:9000/ai-test`
4. 保存

### 7.3 查看请求日志

```bash
cd shuao-clinic-saas-source/docker
docker compose -f docker-compose.dev.yml logs -f mock-webhook
```

日志会完整打印请求体和响应体的格式化 JSON。

### 7.4 修改 Mock 返回数据

编辑 `docker/mock-webhook/mock_server.py`，修改 `response["data"]` 中的字段值，然后重启：

```bash
docker compose -f docker-compose.dev.yml up -d --build mock-webhook
```

---

## 八、常见问题

### 8.1 前端表单没有回填内容

**现象**：点击"AI 扩写"后，表单字段仍然是空的。

**排查步骤：**

1. 查看浏览器开发者工具 Network 面板，确认 `/api/ai/proxy/medical-expand` 接口返回了数据
2. 确认返回结构是 `res.data.data = { chief_complaint: "...", ... }`（不是双重嵌套）
3. 确认返回字段名和前端回填代码中的字段名一致
4. 查看 mock-webhook 日志，确认响应体中有对应字段

**根因与修复记录（2026-05-16）：**
- **问题**：后端 `AiProxyController.proxy()` 直接把 Webhook 返回的 `{code, msg, data}` 整个塞进了 `Result.data`，导致前端收到双重嵌套。
- **修复**：在 `AiProxyController.proxy()` 中增加解包逻辑，检测到标准格式时提取内层 `data`。

### 8.2 部分字段没有回填

**现象**：主诉、现病史回填了，但流行病史、医嘱、处方等没有。

**根因与修复记录（2026-05-16）：**
- **问题一**：Mock Server 返回的 `data` 中缺少 `infectious_history`、`treatment`、`medical_advice`、`prescription`、`record_tags`、`image_summary` 字段。
- **修复**：在 `mock_server.py` 的 `response["data"]` 中补充这些字段。
- **问题二**：前端 `expandWithAI()` 的回填逻辑中缺少 `record_tags` 和 `image_summary` 的处理。
- **修复**：在 `MedicalRecordView.vue` 的回填代码中增加这两个字段的赋值。

### 8.3 如何接入真实的 AI 平台

**n8n 示例：**
1. 在 n8n 中创建 Webhook 工作流，触发器选择 Webhook
2. 复制 n8n 生成的 Webhook URL
3. 在系统设置 → AI 智能中心 → AI 总览中，编辑 `medical-expand` 配置
4. Webhook 地址填入 n8n URL
5. 认证令牌填入 n8n 的 Bearer Token（如有）
6. 保存后测试

**n8n 工作流接收的数据格式：**
```json
{
  "protocol_version": "1.0",
  "function": "medical-expand",
  "context": { "account_id": 1, "scene_name": "medical-expand", "timestamp": 1778860210475 },
  "input_fields": {
    "fields": { "label": "fields", "value": { "patient_id": "13", "chief_complaint": "", ... }, "enabled": true }
  },
  "output_schema": { "format": "json", "required": [], "optional": [] },
  "_original_payload": { "fields": { ... }, "account_id": 1 }
}
```

**n8n 应返回的格式：**
```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "chief_complaint": "...",
    "present_illness_history": "...",
    "diagnosis": "...",
    "treatment_plan": "...",
    ...
  }
}
```

### 8.4 后端编译不生效

修改 Java 代码后，在运行中的容器内执行编译即可热重载：

```bash
cd shuao-clinic-saas-source/docker
docker compose -f docker-compose.dev.yml exec backend mvn compile -q
```

---

## 九、扩展：为其他页面新增 AI Agent

如果你想用同样的逻辑为其他页面（如咨询管理、回访管理）新增 AI 功能：

1. **数据库**：在 `ai_agent_config` 表中新增一条配置，`agent_key` 取新的标识（如 `consultation-analysis`）
2. **前端**：在对应页面的 Vue 文件中，参考 `MedicalRecordView.vue` 的 `expandWithAI()` 方法，编写发送和回填逻辑
3. **后端**：无需修改代码，`AiProxyController` 的白名单已包含大部分常见标识，动态白名单会从 `ai_function_config` 表自动加载
4. **AI 总览**：在系统设置中配置 Webhook 地址、用途位置、认证令牌

---

## 十、相关数据库表

| 表名 | 说明 |
|---|---|
| `ai_agent_config` | Agent 配置表，存储 Webhook 地址、认证信息、用途位置等 |
| `ai_function_config` | 功能白名单表，控制哪些 agentKey 可以使用 |
| `ai_operation_log` | AI 操作日志表，记录每次调用的输入、输出、异常信息 |
| `medical_record_phrases` | 病历常用词条表，用于前端词条快捷输入 |
