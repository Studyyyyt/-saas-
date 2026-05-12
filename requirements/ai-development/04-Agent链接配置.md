# 04. Agent 链接配置开发文档

**对应前端页面**: `AIAgentLinkView.vue`
**对应后端模块**: `AiAgentLinkController` + `AiAgentLinkService`
**开发优先级**: P2（外部智能体接入）

---

## 1. 功能描述

管理系统与外部 AI 平台（n8n、Dify、Coze、FastGPT 等）的对接配置。每个配置对应一个外部智能体工作流，系统可以通过后端转发请求来调用这些外部 Agent。

适用场景：
- 病历扩写走 n8n 工作流（医生习惯在 n8n 里编排）
- 随访提醒走 Dify 应用
- 经营日报走 Coze 智能体

---

## 2. 页面布局

```
┌─────────────────────────────────────────────┐
│ Agent 链接                        [新增链接] │
├─────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ │
│ │ [n8n] 病历扩写工作流           [启用标签] │ │
│ │      平台: n8n | https://n8n.../medical │ │
│ │      超时: 30秒                         │ │
│ │      [测试] [编辑] [删除]               │ │
│ └─────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────┐ │
│ │ [Dify] 患者随访助手            [禁用标签] │ │
│ │      平台: Dify | https://dify.../chat  │ │
│ │      超时: 30秒                         │ │
│ │      [测试] [编辑] [删除]               │ │
│ └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

---

## 3. 数据模型

### 3.1 前端数据结构

```javascript
agentLink: {
  id: 1,
  name: '病历扩写工作流',           // 名称
  platform: 'n8n',                 // 平台类型
  apiUrl: 'https://n8n.example.com/webhook/xxx',  // API地址
  authType: 'apikey',              // 认证方式：apikey/bearer/none
  authSecret: '',                  // 认证密钥（密码框）
  timeout: 30,                     // 超时秒数
  samplePayload: '',               // 请求参数示例JSON
  isActive: true                   // 启用状态
}
```

### 3.2 后端数据表

**`ai_agent_link`** — 外部 Agent 链接表

```sql
CREATE TABLE ai_agent_link (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    platform VARCHAR(30) COMMENT '平台类型：n8n/dify/coze/fastgpt/custom',
    api_url VARCHAR(500) NOT NULL COMMENT 'API地址或Webhook',
    auth_type VARCHAR(20) COMMENT '认证类型：apikey/bearer/none',
    auth_secret VARCHAR(500) COMMENT '认证密钥（AES加密）',
    timeout_seconds INT DEFAULT 30 COMMENT '请求超时秒数',
    sample_payload TEXT COMMENT '请求参数示例JSON',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    create_time DATETIME DEFAULT NOW(),
    update_time DATETIME DEFAULT NOW()
);
```

---

## 4. 接口定义

### 4.1 获取 Agent 链接列表

**请求**:
```
GET /api/ai-agent/links
```

**响应**:
```json
{
  "code": "200",
  "msg": "success",
  "data": [
    {
      "id": 1,
      "name": "病历扩写工作流",
      "platform": "n8n",
      "apiUrl": "https://n8n.example.com/webhook/medical-expand",
      "authType": "apikey",
      "timeout": 30,
      "samplePayload": "{\"chiefComplaint\":\"牙痛\"}",
      "isActive": true
    }
  ]
}
```

### 4.2 新增 Agent 链接

**请求**:
```
POST /api/ai-agent/links
Content-Type: application/json

{
  "name": "病历扩写工作流",
  "platform": "n8n",
  "apiUrl": "https://n8n.example.com/webhook/medical-expand",
  "authType": "apikey",
  "authSecret": "n8n-api-key-xxx",
  "timeout": 30,
  "samplePayload": "{\"chiefComplaint\":\"牙痛\"}",
  "isActive": true
}
```

### 4.3 更新 Agent 链接

**请求**:
```
PUT /api/ai-agent/links/{id}
Content-Type: application/json

{
  "name": "病历扩写工作流V2",
  "apiUrl": "https://n8n.example.com/webhook/medical-expand-v2",
  "isActive": true
}
```

### 4.4 删除 Agent 链接

**请求**:
```
DELETE /api/ai-agent/links/{id}
```

### 4.5 测试连接

**请求**:
```
POST /api/ai-agent/links/{id}/test
Content-Type: application/json

{
  "payload": {"chiefComplaint": "牙痛"}
}
```

**响应**:
```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "success": true,
    "latency": 1200,
    "response": "扩写后的病历内容..."
  }
}
```

### 4.6 调用 Agent（运行时接口）

**请求**:
```
POST /api/ai-agent/invoke
Content-Type: application/json

{
  "agentId": 1,
  "payload": {
    "chiefComplaint": "牙痛",
    "diagnosis": "龋齿"
  }
}
```

**响应**:
```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "chiefComplaint": "右下后牙自发痛3天",
    "historyOfPresentIllness": "患者3天前..."
  }
}
```

---

## 5. 前端实现要点

### 5.1 页面结构

```
AIAgentLinkView.vue
├── 页面标题区
│   └── [新增链接] 按钮
├── Agent 列表卡片 (section-card)
│   ├── 空状态（暂无链接）
│   └── Agent 卡片列表
│       ├── 平台图标 + 名称 + 状态标签
│       ├── 元信息（平台 | URL | 超时）
│       └── 操作按钮（测试、编辑、删除）
└── 编辑/新增弹窗 (el-dialog)
    ├── 名称
    ├── 平台类型（下拉）+ 超时时间
    ├── API 地址
    ├── 认证方式 + 密钥
    ├── 请求参数示例（JSON 文本域）
    └── 启用状态开关
```

### 5.2 关键逻辑

```javascript
methods: {
  async loadLinks() {
    const res = await this.$request.get('/api/ai-agent/links')
    if (res.code === '200') {
      this.agentLinks = res.data
    }
  },

  async saveAgent() {
    const payload = { ...this.form }
    if (this.editingId) {
      await this.$request.put(`/api/ai-agent/links/${this.editingId}`, payload)
    } else {
      await this.$request.post('/api/ai-agent/links', payload)
    }
    this.editorVisible = false
    this.loadLinks()
  },

  async testAgent(agent) {
    const res = await this.$request.post(`/api/ai-agent/links/${agent.id}/test`, {
      payload: agent.samplePayload ? JSON.parse(agent.samplePayload) : {}
    })
    if (res.code === '200') {
      this.$message.success(`连接成功，延迟 ${res.data.latency}ms`)
    } else {
      this.$message.error(res.msg || '连接失败')
    }
  }
}
```

---

## 6. 后端实现要点

### 6.1 Controller

```java
@RestController
@RequestMapping("/api/ai-agent")
public class AiAgentLinkController {

    @Autowired
    private AiAgentLinkService agentLinkService;

    @GetMapping("/links")
    public Result list() {
        return Result.success(agentLinkService.listAll());
    }

    @PostMapping("/links")
    public Result save(@RequestBody AiAgentLinkDTO dto) {
        return Result.success(agentLinkService.save(dto));
    }

    @PutMapping("/links/{id}")
    public Result update(@PathVariable Long id, @RequestBody AiAgentLinkDTO dto) {
        agentLinkService.update(id, dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/links/{id}")
    public Result delete(@PathVariable Long id) {
        agentLinkService.delete(id);
        return Result.success("删除成功");
    }

    @PostMapping("/links/{id}/test")
    public Result test(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object payload = body.get("payload");
        TestResultVO result = agentLinkService.testConnection(id, payload);
        return Result.success(result);
    }

    @PostMapping("/invoke")
    public Result invoke(@RequestBody AgentInvokeDTO dto) {
        Object result = agentLinkService.invoke(dto.getAgentId(), dto.getPayload());
        return Result.success(result);
    }
}
```

### 6.2 Service 核心逻辑（调用转发）

```java
@Service
public class AiAgentLinkServiceImpl implements AiAgentLinkService {

    @Autowired
    private AiAgentLinkMapper mapper;
    @Autowired
    private AesEncryptor encryptor;

    @Override
    public Object invoke(Long agentId, Object payload) {
        AiAgentLink link = mapper.selectById(agentId);
        if (link == null || !Boolean.TRUE.equals(link.getIsActive())) {
            throw new RuntimeException("Agent 不存在或未启用");
        }

        RestTemplate restTemplate = new RestTemplate();
        // 设置超时
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(link.getTimeoutSeconds() * 1000);
        factory.setReadTimeout(link.getTimeoutSeconds() * 1000);
        restTemplate.setRequestFactory(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 认证
        if ("bearer".equals(link.getAuthType())) {
            headers.setBearerAuth(encryptor.decrypt(link.getAuthSecret()));
        } else if ("apikey".equals(link.getAuthType())) {
            headers.set("X-API-Key", encryptor.decrypt(link.getAuthSecret()));
        }

        HttpEntity<Object> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Object> response = restTemplate.postForEntity(
            link.getApiUrl(), request, Object.class
        );
        return response.getBody();
    }
}
```

---

## 7. 实现步骤 checklist

### 前端
- [ ] 页面框架 `AIAgentLinkView.vue` 已完成，对接真实接口
- [ ] 列表调用 `GET /api/ai-agent/links`
- [ ] 新增/编辑调用 `POST/PUT /api/ai-agent/links`
- [ ] 删除调用 `DELETE /api/ai-agent/links/{id}`
- [ ] 测试连接调用 `POST /api/ai-agent/links/{id}/test`
- [ ] API URL 脱敏显示（域名 + ... + 路径末尾）
- [ ] 弹窗表单校验（名称、URL 必填）

### 后端
- [ ] Flyway 创建 `ai_agent_link` 表
- [ ] 创建 `AiAgentLink` Entity
- [ ] 创建 `AiAgentLinkMapper` + XML
- [ ] 创建 `AiAgentLinkController`
- [ ] 创建 `AiAgentLinkService` + `AiAgentLinkServiceImpl`
- [ ] 创建 DTO: `AiAgentLinkDTO`, `AgentInvokeDTO`
- [ ] 创建 VO: `TestResultVO`
- [ ] 实现 `invoke()` 方法（后端转发请求到外部 Webhook）
- [ ] 实现 `testConnection()` 方法（带超时控制）

### 联调验证
- [ ] 新增 n8n Webhook 链接，保存后列表显示
- [ ] 点击测试按钮，n8n 收到请求并返回结果
- [ ] 修改 API 地址后测试，验证新地址生效
- [ ] 禁用链接后，前端调用 `POST /api/ai-agent/invoke` 应返回错误
- [ ] 超时场景测试（n8n 延迟响应），前端收到超时错误提示
