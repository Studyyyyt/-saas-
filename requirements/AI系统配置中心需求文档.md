# AI 系统配置中心需求文档

**文档版本**: v1.0  
**编写日期**: 2026-05-11  
**适用系统**: 舒澳口腔 SaaS 管理系统  

---

## 1. 项目背景与目标

### 1.1 背景
系统现有 AI 功能分散在各个页面（首页 AI 助手、病历 AI 扩写、患者列表 AI 洞察等），且当前系统设置页面采用卡片网格布局，随着配置项增多，查找和管理效率降低。

### 1.2 目标
1. **重构系统设置页面**：从卡片网格模式改为左侧菜单栏 + 右侧内容区的经典后台布局，提升导航效率。
2. **建立 AI 配置中心**：在系统设置下统一规划 AI 相关配置的入口和分级，实现"一处管理，全局生效"。
3. **支持页面级 AI 功能独立配置**：不同业务页面（病历、患者列表等）的 AI 功能可以独立开关、独立配置提示词和参数。
4. **支持外部 AI Agent 接入**：通过 API 链接 n8n、Dify、Coze 等外部平台搭建的智能体工作流。

---

## 2. 总体架构

```
系统设置 (SystemSettings)
├── 基础设置
│   ├── 项目与治疗
│   ├── 财务与收费
│   ├── 知情同意书
│   ├── 义齿加工
│   ├── 耗材管理
│   └── 账号与权限
│
├── AI 智能中心  ← 新增一级菜单
│   ├── AI 总览        (AIOverviewView)         — 全局开关、使用统计
│   ├── 首页助手       (AIAgentConfigView)      — 现有页面整合
│   ├── 模型供应商     (ModelProviderConfigView) — 现有页面整合
│   ├── Agent 链接     (AIAgentLinkView)        — 新增：外部智能体接入
│   └── 页面 AI 功能   (PageAIFunctionView)     — 新增：各页面 AI 子配置入口
│       ├── 病历扩写   (MedicalRecordAIConfigView) — 新增
│       ├── 患者洞察   (PatientAIConfigView)       — 新增
│       └── 更多页面... (预留扩展)
```

---

## 3. 系统设置页面重构需求

### 3.1 布局变更

**当前**: 卡片网格布局（2列），点击卡片进入子页面。  
**目标**: 左侧固定菜单栏（宽度 220px）+ 右侧自适应内容区。

```
┌──────────────────────────────────────────────────────────┐
│  系统设置                                    舒澳口腔 v2.0 │
├──────────────┬───────────────────────────────────────────┤
│              │                                           │
│  基础设置    │                                           │
│  ├ 项目与治疗│         右侧内容区域（路由子页面）          │
│  ├ 财务与收费│                                           │
│  ├ 知情同意书│                                           │
│  ├ 义齿加工  │                                           │
│  ├ 耗材管理  │                                           │
│  └ 账号与权限│                                           │
│              │                                           │
│  AI 智能中心 │                                           │
│  ├ AI 总览   │                                           │
│  ├ 首页助手  │                                           │
│  ├ 模型供应商│                                           │
│  ├ Agent链接 │                                           │
│  └ 页面AI功能│                                           │
│    ├ 病历扩写│                                           │
│    └ 患者洞察│                                           │
│              │                                           │
└──────────────┴───────────────────────────────────────────┘
```

### 3.2 菜单交互规则

1. **一级菜单**：可点击展开/收起，展开后显示二级菜单。
2. **二级菜单**：点击后在右侧内容区加载对应页面（子路由）。
3. **当前选中态**：高亮当前路由对应的菜单项。
4. **默认路由**：进入 `/SystemSettings` 时，默认重定向到第一个可访问的子页面（如 `/SystemSettings/basic/treatment`）。

### 3.3 菜单数据结构

```javascript
const settingMenuGroups = [
  {
    key: 'basic',
    title: '基础设置',
    icon: 'el-icon-s-tools',
    children: [
      { key: 'treatment', title: '项目与治疗', path: '/SystemSettings/basic/treatment' },
      { key: 'payment', title: '财务与收费', path: '/SystemSettings/basic/payment' },
      { key: 'consent', title: '知情同意书', path: '/SystemSettings/basic/consent' },
      { key: 'lab', title: '义齿加工', path: '/SystemSettings/basic/lab' },
      { key: 'material', title: '耗材管理', path: '/SystemSettings/basic/material' },
      { key: 'account', title: '账号与权限', path: '/SystemSettings/basic/account' }
    ]
  },
  {
    key: 'ai',
    title: 'AI 智能中心',
    icon: 'el-icon-cpu',
    children: [
      { key: 'ai-overview', title: 'AI 总览', path: '/SystemSettings/ai/overview' },
      { key: 'ai-agent', title: '首页助手', path: '/SystemSettings/ai/agent' },
      { key: 'ai-model', title: '模型供应商', path: '/SystemSettings/ai/model' },
      { key: 'ai-link', title: 'Agent 链接', path: '/SystemSettings/ai/link' },
      {
        key: 'ai-pages',
        title: '页面 AI 功能',
        children: [
          { key: 'ai-medical', title: '病历扩写', path: '/SystemSettings/ai/pages/medical' },
          { key: 'ai-patient', title: '患者洞察', path: '/SystemSettings/ai/pages/patient' }
        ]
      }
    ]
  }
]
```

---

## 4. AI 总览页面 (AIOverviewView)

### 4.1 功能描述
AI 功能的全局控制面板，展示系统内所有 AI 功能的启用状态、今日调用次数、费用预估等。

### 4.2 页面内容

**全局开关区**：
- AI 功能总开关：一键开启/关闭系统所有 AI 功能（关闭后所有 AI 入口隐藏）。
- 调试模式开关：开启后，前端 AI 请求会打印详细日志。

**统计卡片区**（4列）：
- 今日 AI 调用次数
- 今日 Token 消耗量
- 活跃 AI 功能数 / 总功能数
- 错误率

**功能列表区**：
表格展示各 AI 功能的启用状态和配置快捷入口：

| 功能名称 | 所属页面 | 状态 | 今日调用 | 操作 |
|---------|---------|------|---------|------|
| 首页 AI 助手 | 首页 | 已启用 | 128 | 配置 |
| 病历 AI 扩写 | 病历编辑 | 已启用 | 56 | 配置 |
| 患者 AI 洞察 | 患者列表 | 已禁用 | 0 | 配置 |
| 智能随访生成 | 随访管理 | 已启用 | 23 | 配置 |

---

## 5. Agent 链接页面 (AIAgentLinkView)

### 5.1 功能描述
管理外部平台智能体的接入配置。系统通过调用外部 Agent 的 API（如 n8n Webhook、Dify API、Coze API）来执行复杂 AI 工作流。

### 5.2 页面内容

**Agent 列表**：卡片/表格形式展示已配置的外部 Agent。

每个 Agent 卡片包含：
- 名称、图标
- 平台类型（n8n / Dify / Coze / 自定义 Webhook）
- API 地址（脱敏显示）
- 启用状态 Switch
- 操作：编辑 / 删除 / 测试连接

**新增/编辑表单**：
- 名称
- 平台类型（下拉选择）
- API 地址 / Webhook URL
- 认证方式（API Key / Bearer Token / 无）
- 认证密钥（密码框）
- 请求超时时间（秒）
- 启用状态

### 5.3 调用逻辑
当页面需要调用外部 Agent 时（如病历扩写走 n8n）：

```javascript
// 前端请求后端
POST /api/ai-agent/invoke
{
  "agentId": "n8n-medical-expand",
  "payload": {
    "chiefComplaint": "牙痛",
    "diagnosis": "龋齿"
  }
}

// 后端根据 agentId 查找配置，转发请求到外部 Webhook/API
// 返回外部 Agent 的执行结果
```

---

## 6. 病历 AI 扩写配置页面 (MedicalRecordAIConfigView)

### 6.1 功能描述
配置病历编辑页面的"AI 一键扩写"功能的行为参数、提示词模板和知识库。

### 6.2 页面分区

#### 6.2.1 基础设置
- **功能开关**：是否启用病历 AI 扩写。
- **默认温度**：0.1 ~ 1.0 滑块，默认 0.2（低创造性，高准确性）。
- **最大输出长度**：默认 2000 Token。
- **空字段处理策略**：
  - 留白（不填充）
  - AI 生成
  - 提示医生手动填写

#### 6.2.2 字段配置（动态表格）
配置哪些字段支持 AI 扩写，以及每个字段的约束规则：

| 字段名 | 启用扩写 | 最大字数 | 必填 | 校验规则 |
|--------|---------|---------|------|---------|
| 主诉 | 是 | 30 | 是 | 必须包含部位+症状+时间 |
| 现病史 | 是 | 500 | 是 | 必须包含时间描述 |
| 检查所见 | 是 | 500 | 否 | - |
| 诊断 | 是 | 100 | 是 | 必须用建议性语气 |
| 治疗计划 | 是 | 300 | 否 | - |

#### 6.2.3 提示词模板编辑
- **系统提示词编辑器**：带语法高亮的文本域，支持插入变量（如 `{kb_content}`、`{input_fields}`）。
- **Few-shot 示例管理**：可添加/删除示例，每个示例包含"简要输入"和"扩写输出"。
- **版本对比**：提示词修改后，可与上一版本进行对比。

#### 6.2.4 知识库关联
- **病种模板库快捷入口**：点击跳转到知识库管理子标签。
- **关联模板**：选择该病历扩写功能关联哪些病种模板作为参考。

#### 6.2.5 安全策略
- **敏感词列表**：输入框，逗号分隔，AI 输出中包含这些词将被拦截。
- **禁用断言**：勾选后，AI 不得使用"确诊"、"绝对"等确定性词汇。
- **输出校验规则**：
  - 诊断字段必须包含"考虑/疑似/待排"
  - 主诉不得超过设定字数

#### 6.2.6 效果测试（页面内嵌）
底部提供实时测试区：
- 左侧：输入简要病历信息（模拟医生填写的内容）。
- 右侧：点击"测试扩写"，展示 AI 输出结果。
- 测试结果可一键"设为 Few-shot 示例"。

---

## 7. 患者列表 AI 洞察配置页面 (PatientAIConfigView)

### 7.1 功能描述
配置患者列表页面的 AI 洞察面板（患者 360 右侧的 AI 分析区域）。

### 7.2 页面分区

#### 7.2.1 基础设置
- **功能开关**
- **默认展开面板**：进入患者详情时 AI 面板是否自动展开。
- **洞察维度**：多选框，可选：
  - 治疗风险分析
  - 消费潜力评估
  - 复诊概率预测
  - 流失风险预警
  - 转介绍可能性

#### 7.2.2 提示词模板
- **系统提示词**：定义 AI 分析患者数据时的角色和行为。
- **输出格式模板**：定义 AI 返回的 JSON 结构，前端按此结构渲染卡片。

```json
{
  "riskLevel": "low/medium/high",
  "riskReasons": ["原因1", "原因2"],
  "suggestedActions": ["建议1", "建议2"],
  "potentialValue": "高消费潜力",
  "summary": "一句话总结"
}
```

#### 7.2.3 数据工具授权
配置该 AI 功能可以查询哪些数据表（权限控制）：
- 患者基本信息
- 就诊历史
- 治疗记录
- 收费记录
- 预约记录

---

## 8. 数据模型设计

### 8.1 提示词模板表 (ai_prompt_template)

```sql
CREATE TABLE ai_prompt_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene VARCHAR(50) NOT NULL COMMENT '场景：病历扩写、患者洞察、随访生成等',
    name VARCHAR(100) COMMENT '模板名称',
    system_prompt TEXT NOT NULL COMMENT '系统提示词',
    temperature DECIMAL(3,2) DEFAULT 0.2 COMMENT '温度',
    max_tokens INT DEFAULT 2000 COMMENT '最大Token',
    response_format VARCHAR(20) DEFAULT 'json' COMMENT '响应格式：json/text',
    json_schema TEXT COMMENT 'JSON Schema约束',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    version INT DEFAULT 1 COMMENT '版本号',
    create_time DATETIME DEFAULT NOW(),
    update_time DATETIME DEFAULT NOW()
);
```

### 8.2 Few-shot 示例表 (ai_few_shot_example)

```sql
CREATE TABLE ai_few_shot_example (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_id BIGINT NOT NULL COMMENT '关联模板ID',
    scene VARCHAR(50) NOT NULL,
    input_content TEXT COMMENT '输入示例',
    output_content TEXT COMMENT '输出示例',
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT NOW()
);
```

### 8.3 外部 Agent 链接表 (ai_agent_link)

```sql
CREATE TABLE ai_agent_link (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    platform VARCHAR(30) COMMENT '平台：n8n、dify、coze、custom',
    api_url VARCHAR(500) NOT NULL COMMENT 'API地址或Webhook',
    auth_type VARCHAR(20) COMMENT '认证类型：apikey、bearer、none',
    auth_secret VARCHAR(255) COMMENT '认证密钥（加密存储）',
    timeout_seconds INT DEFAULT 30,
    is_active BOOLEAN DEFAULT TRUE,
    create_time DATETIME DEFAULT NOW(),
    update_time DATETIME DEFAULT NOW()
);
```

### 8.4 AI 功能配置表 (ai_function_config)

```sql
CREATE TABLE ai_function_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    function_key VARCHAR(50) NOT NULL UNIQUE COMMENT '功能标识：medical_expand、patient_insight等',
    function_name VARCHAR(100) COMMENT '功能名称',
    page_path VARCHAR(100) COMMENT '所属页面路径',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    prompt_template_id BIGINT COMMENT '关联提示词模板ID',
    extra_config JSON COMMENT '额外配置（各功能私有参数）',
    create_time DATETIME DEFAULT NOW(),
    update_time DATETIME DEFAULT NOW()
);
```

### 8.5 病历字段规则表 (medical_record_ai_field)

```sql
CREATE TABLE medical_record_ai_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    field_key VARCHAR(50) NOT NULL COMMENT '字段标识：chiefComplaint等',
    field_name VARCHAR(50) COMMENT '字段中文名',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用AI扩写',
    max_length INT COMMENT '最大长度',
    is_required BOOLEAN DEFAULT FALSE COMMENT '是否必填',
    validation_rule VARCHAR(255) COMMENT '校验规则正则',
    validation_hint VARCHAR(255) COMMENT '校验失败提示语',
    sort_order INT DEFAULT 0
);
```

---

## 9. 接口设计

### 9.1 AI 功能配置接口

```
GET    /api/ai-config/functions           — 获取所有AI功能配置列表
GET    /api/ai-config/functions/{key}     — 获取指定功能配置
PUT    /api/ai-config/functions/{key}     — 更新功能配置（开关、模板关联等）
GET    /api/ai-config/overview            — 获取AI总览统计
```

### 9.2 提示词模板接口

```
GET    /api/ai-config/prompts             — 获取提示词模板列表
GET    /api/ai-config/prompts/{id}        — 获取模板详情
POST   /api/ai-config/prompts             — 新增模板
PUT    /api/ai-config/prompts/{id}        — 更新模板
DELETE /api/ai-config/prompts/{id}        — 删除模板
POST   /api/ai-config/prompts/{id}/test   — 测试提示词效果
```

### 9.3 外部 Agent 链接接口

```
GET    /api/ai-agent/links                — 获取Agent链接列表
POST   /api/ai-agent/links                — 新增Agent链接
PUT    /api/ai-agent/links/{id}           — 更新Agent链接
DELETE /api/ai-agent/links/{id}           — 删除Agent链接
POST   /api/ai-agent/links/{id}/test      — 测试连接
POST   /api/ai-agent/invoke               — 调用指定Agent
```

### 9.4 病历扩写配置接口

```
GET    /api/ai-config/medical-record      — 获取病历扩写完整配置
PUT    /api/ai-config/medical-record      — 保存病历扩写配置
GET    /api/ai-config/medical-record/fields — 获取字段规则列表
PUT    /api/ai-config/medical-record/fields — 批量更新字段规则
```

---

## 10. 前端实现要点

### 10.1 系统设置布局组件

新建 `SystemSettingsLayout.vue` 作为系统设置页面的外壳组件：
- 左侧菜单栏（固定 220px，可滚动）。
- 右侧内容区（`router-view`，自适应宽度）。
- 菜单数据从配置文件读取，支持多级嵌套。

### 10.2 路由调整

`SystemSettingsView` 不再作为叶子页面，而是作为布局容器。所有子页面作为它的嵌套路由：

```javascript
{
  path: '/SystemSettings',
  component: SystemSettingsLayout,
  redirect: '/SystemSettings/basic/treatment',
  children: [
    // 基础设置
    { path: 'basic/treatment', component: SystemTreatmentCatalogView },
    { path: 'basic/payment', component: SystemPaymentChannelView },
    // ...
    // AI 智能中心
    { path: 'ai/overview', component: AIOverviewView },
    { path: 'ai/agent', component: AIAgentConfigView },
    { path: 'ai/model', component: ModelProviderConfigView },
    { path: 'ai/link', component: AIAgentLinkView },
    { path: 'ai/pages/medical', component: MedicalRecordAIConfigView },
    { path: 'ai/pages/patient', component: PatientAIConfigView }
  ]
}
```

### 10.3 页面内交互规范

- **保存机制**：各配置页面独立保存，改动后若未保存离开，提示"有未保存的更改"。
- **测试机制**：提示词编辑区旁边必须提供"效果测试"按钮，输入测试数据后实时查看 AI 输出。
- **权限控制**：AI 配置中心仅管理员可访问。

---

## 11. 开发优先级

| 优先级 | 模块 | 说明 |
|--------|------|------|
| P0 | 系统设置菜单栏重构 | 布局框架，所有后续页面的基础 |
| P0 | AI 总览页面 | 全局开关 + 统计展示 |
| P1 | 病历扩写配置页面 | 用户当前最急需的一键写病历功能 |
| P1 | Agent 链接配置 | 对接 n8n 等外部工作流 |
| P2 | 患者洞察配置 | 患者列表 AI 面板配置 |
| P2 | 提示词版本管理 | A/B 测试、版本回滚 |
| P3 | 知识库管理页面 | 病种模板库的可视化维护 |

---

## 12. 附录

### 12.1 现有页面整合说明

| 现有页面 | 原路由 | 新路由 | 修改内容 |
|---------|--------|--------|---------|
| AIAgentConfigView | `/SystemAIAgentConfig` | `/SystemSettings/ai/agent` | 无功能变更，仅嵌入新布局 |
| ModelProviderConfigView | `/SystemModelProviderConfig` | `/SystemSettings/ai/model` | 无功能变更，仅嵌入新布局 |
| SystemSettingsView | `/SystemSettings` | `/SystemSettings` | 改为布局容器，移除卡片内容 |

### 12.2 页面路径汇总

| 页面名称 | 文件路径 | 路由路径 |
|---------|---------|---------|
| 系统设置布局 | `SystemSettingsLayout.vue` | `/SystemSettings` |
| AI 总览 | `AIOverviewView.vue` | `/SystemSettings/ai/overview` |
| Agent 链接 | `AIAgentLinkView.vue` | `/SystemSettings/ai/link` |
| 病历扩写配置 | `MedicalRecordAIConfigView.vue` | `/SystemSettings/ai/pages/medical` |
| 患者洞察配置 | `PatientAIConfigView.vue` | `/SystemSettings/ai/pages/patient` |
