# 口腔门诊 SaaS 管理系统 — AI 板块需求文档

> 版本：v1.0
> 日期：2026-05-07
> 状态：待评审

---

## 1. 文档概述

### 1.1 目的
本文档定义口腔门诊 SaaS 管理系统中 AI 板块的完整功能需求，涵盖 AI 与系统数据的深度整合、AI 辅助临床工作、可扩展的 AI Agent 架构，以及与外部工作流平台的对接能力。

### 1.2 系统现状
当前系统已具备基础 AI 对话能力（经营分析聊天），支持多供应商 LLM 配置（OpenAI、DeepSeek、Gemini via OpenRouter 等），已实现 Function Calling 框架和 7 个基础数据查询工具。本需求在此基础上进行系统性扩展。

### 1.3 用户角色
| 角色 | AI 使用场景 |
|------|------------|
| 医生 | AI 辅助写病历、查询患者历史、治疗建议、影像分析 |
| 护士/前台 | AI 预约管理、患者随访提醒、咨询预检、分诊辅助 |
| 管理员/老板 | 经营数据分析、财务预警、耗材库存分析、决策建议 |

---

## 2. 核心需求模块

### 模块一：AI 系统数据查询与分析

#### 2.1.1 需求描述
AI 助手能够实时读取系统数据库，回答用户关于患者、预约、病历、收费、耗材、加工单等数据的自然语言查询。

#### 2.1.2 功能细则

**A. 自然语言数据查询**
- 支持提问示例：
  - "查一下张三最近半年的就诊记录"
  - "本月收入排名前5的治疗项目是什么"
  - "下周有哪些预约还没确认"
  - "库存低于预警值的耗材有哪些"
  - "王医生这个月做了多少台种植手术"

**B. 智能数据分析**
- AI 不仅返回原始数据，还应提供：
  - 趋势分析（环比、同比）
  - 异常检测（如某患者收费明显偏离同类治疗均价）
  - 关联分析（如"做种植的患者后续复诊率如何"）
  - 预测建议（如"按当前预约趋势，下周三可能需要增加一位医生"）

**C. 数据可视化联动**
- AI 分析结果可一键生成图表（ECharts）插入对话
- 支持将分析结果导出为日报/周报格式

#### 2.1.3 技术实现方案

| 方案 | 描述 | 适用场景 |
|------|------|---------|
| **方案 A：Function Calling（主方案）** | AI 通过标准函数调用查询数据库 | 实时性要求高、查询逻辑明确的场景 |
| **方案 B：工作流 API 调用** | 调用外部工作流平台（n8n/Dify）执行复杂查询 | 跨系统查询、需要多步骤处理的场景 |
| **方案 C：预计算摘要注入** | 定时生成经营摘要，作为上下文注入 | 高频常规分析、降低 API 成本 |

**推荐架构：三层混合模式**
```
用户提问
  ├─ 简单查询 → Function Calling 直连数据库（实时）
  ├─ 复杂分析 → 调用外部工作流 API（n8n/Dify）
  └─ 常规报表 → 预计算摘要 + AI 润色（低成本）
```

---

### 模块二：AI 辅助病历书写

#### 2.2.1 需求描述
医生在书写病历时，只需输入关键信息（如牙位、主诉关键词、检查结果），AI 自动按口腔医学规范补全完整病历内容。

#### 2.2.2 功能细则

**A. 智能病历补全**
- 医生输入："右上6缺失，种植一期，骨量不足"
- AI 自动补全：
  - 主诉：右上后牙缺失数月，要求修复
  - 现病史：患者数月前因右上6龋坏拔除，现来我院要求种植修复...（自动生成完整病史）
  - 检查：16缺失，缺牙区牙槽嵴宽度约6mm，高度约10mm，CBCT显示...（自动补充检查细节）
  - 诊断：16缺失，牙列缺损；16位点骨量不足（水平向）
  - 治疗计划：16种植修复，先行GBR骨增量术，3-4个月后行种植体植入...
  - 医嘱：术后抗生素预防感染，一周拆线，注意口腔卫生...

**B. 病历质量检查**
- AI 实时检查病历完整性：
  - 必填项缺失提醒（如知情同意书未上传）
  - 逻辑一致性检查（如诊断与治疗方案不匹配）
  - 医学术语规范化（自动修正不规范表述）
  - 法律依据提醒（如种植手术必须记录的风险告知）

**C. 历史病历学习**
- AI 学习该医生过往病历书写风格
- 支持按医生个人模板生成（不同医生有不同的表述习惯）
- 支持科室/诊所统一模板

**D. 语音转病历**
- 医生口述，AI 实时转写并格式化为标准病历
- 支持医学术语纠错（如"种牙"自动纠正为"种植修复"）

#### 2.2.3 集成位置
- 在 `MedicalRecordView.vue` 病历编辑页面嵌入 AI 辅助面板
- 支持快捷键触发（如 `/ai` 或 `Ctrl+Space`）
- 支持选中文字后右键"AI 优化"

---

### 模块三：AI Agent 自定义管理

#### 2.3.1 需求描述
系统支持在管理后台自定义配置 AI Agent，每个 Agent 有独立的名称、角色、System Prompt、可用工具集、外观主题等。前端页面可通过 API 调用指定的 Agent。

#### 2.3.2 功能细则

**A. Agent 基础配置**
| 配置项 | 说明 | 示例 |
|--------|------|------|
| Agent 名称 | 显示名称 | "经营分析师"、"病历助手" |
| Agent 标识 | 唯一 key | `finance_analyst`、`medical_writer` |
| 头像/图标 | 头像URL或emoji | 🤖、📊 |
| 主题色 | 渐变色配置 | `linear-gradient(135deg, #2563eb, #3b82f6)` |
| 描述 | 一句话描述 | "帮你分析门店经营数据" |
| 快捷指令 | 预设问题芯片 | `["本月收入", "患者流失分析"]` |

**B. Agent 行为配置**
- **System Prompt**：定义 Agent 的角色、能力边界、回答风格
- **可用工具集**：勾选该 Agent 可调用的工具（患者查询、财务查询、病历查询等）
- **模型偏好**：可指定使用特定模型（如病历助手用 Claude，经营分析用 GPT-4）
- **温度参数**：控制回答创造性（病历书写低温度，营销文案高温度）
- **最大上下文长度**：限制记忆深度

**C. Agent 分类与权限**
- 按角色分类：临床助手、经营分析、患者服务、行政办公
- 按权限隔离：医生 Agent 不可访问财务数据，前台 Agent 不可访问病历详情
- 系统预设 Agent 不可删除，用户可复制后自定义

**D. 外部 Agent 接入**
- 支持配置外部 Agent API 地址（如 Dify、Coze、FastGPT）
- 外部 Agent 调用走统一代理层，传入系统上下文（当前用户信息、会话ID）
- 支持外部 Agent 回调系统工具（通过 Webhook 或标准 Function Calling）

#### 2.3.3 预设 Agent 清单

| Agent 名称 | 角色 | 功能 | 默认工具 |
|-----------|------|------|---------|
| 智能助手 | 通用 | 门诊综合查询与问答 | 全部 |
| 经营分析师 | 管理员 | 收入/支出/趋势分析 | 财务、预约、患者统计 |
| 病历助手 | 医生 | 病历书写辅助与质控 | 病历、患者、治疗 |
| 预约管家 | 前台 | 预约管理与提醒 | 预约、患者 |
| 随访专员 | 护士 | 随访计划生成与跟踪 | 患者、病历、预约 |
| 耗材管家 | 管理员 | 库存预警与采购建议 | 耗材、财务 |
| 咨询师 | 前台 | 患者咨询预检与分诊 | 患者、治疗项目、收费 |

---

### 模块四：外部工作流平台集成

#### 2.4.1 需求描述
系统支持对接外部工作流平台（如 n8n、Dify、FastGPT、Make），将复杂的数据查询或业务流程委托给工作流执行，工作流通过 API 返回结果给 AI 助手。

#### 2.4.2 功能细则

**A. 工作流配置管理**
- 在系统后台配置工作流接口：
  - 工作流名称："查询患者完整档案"
  - 触发 URL：`https://n8n.example.com/webhook/patient-query`
  - 请求方法：POST
  - 认证方式：Bearer Token / API Key
  - 输入参数映射：将 AI 提取的参数映射为工作流输入格式
  - 输出结果映射：将工作流返回映射为 AI 可理解的文本

**B. 工作流作为工具（Tool）**
- 每个工作流在 AI 看来就是一个 Function Calling 工具
- AI 决定是否调用工作流（与调用本地工具逻辑一致）
- 工作流执行结果回传给 AI，AI 再润色为自然语言回答

**C. 典型工作流场景**

| 工作流 | 平台 | 功能 |
|--------|------|------|
| 患者 360 查询 | n8n | 聚合患者基本信息、所有病历、所有预约、所有收费、所有影像 |
| 经营日报生成 | n8n | 定时触发，查询昨日数据，生成带图表的 HTML 日报 |
| 患者画像分析 | Dify | 分析患者消费行为、治疗偏好、流失风险 |
| 智能随访生成 | n8n | 根据治疗类型自动生成随访话术，发送到企业微信 |
| 耗材采购建议 | n8n | 分析库存+消耗速度+供应商价格，生成采购清单 |

**D. 工作流触发方式**
- **AI 触发**：AI 对话中智能判断需要调用工作流
- **定时触发**：系统定时任务调用工作流（如每日凌晨生成日报）
- **事件触发**：系统事件触发工作流（如新患者注册后自动发送欢迎语）

---

## 3. 扩展 AI 功能

### 3.1 AI 随访管理

**场景**：种植手术后 1 天、7 天、30 天、90 天需要随访
**功能**：
- AI 根据治疗类型和日期自动生成随访计划
- 生成个性化随访话术（根据患者年龄、治疗项目调整语气）
- 支持一键发送到企业微信/短信（需对接微信 API）
- 患者回复后，AI 预读并标记需医生关注的内容
- 在 `FollowupManagementView.vue` 集成 AI 随访面板

### 3.2 AI 咨询预检与智能分诊

**场景**：患者通过微信或电话初诊咨询
**功能**：
- AI 与患者对话，收集症状、疼痛程度、持续时间
- 根据症状推荐可能的诊疗方向（仅供参考，非诊断）
- 推荐合适的医生和时间段
- 自动生成初诊预检摘要，医生接诊时直接查看
- 在 `ConsultationView.vue` 集成 AI 咨询辅助

### 3.3 AI 影像辅助分析（扩展）

**场景**：患者拍摄 CBCT/口内照/X光片
**功能**：
- AI 辅助标注影像（如种植位点骨高度测量参考线）
- 自动生成影像描述文本（供病历引用）
- 与病历系统联动：影像分析结果自动填入检查所见
- 支持对接外部影像 AI 服务（如 DeepCare、羽医甘蓝）

### 3.4 AI 知情同意书生成

**场景**：种植、正畸、拔牙等需要签署知情同意书
**功能**：
- 选择治疗项目后，AI 自动生成个性化知情同意书
- 自动填入患者信息、医生信息、治疗方案
- 根据患者特殊情况（如糖尿病、高血压）自动追加风险提示
- 生成后支持医生编辑确认，再推送患者微信签署
- 在 `SystemConsentTemplateView.vue` 集成 AI 生成

### 3.5 AI 耗材库存智能分析

**场景**：耗材库存管理
**功能**：
- AI 分析历史消耗速度，预测未来 30 天库存需求
- 识别异常消耗（如某耗材用量突然增加 200%）
- 结合治疗预约计划，预测耗材需求高峰
- 自动生成采购建议（考虑供应商交期、最小起订量）
- 在 `MaterialView.vue` 和 `MaterialStatisticsView.vue` 集成

### 3.6 AI 患者流失预警

**场景**：患者管理
**功能**：
- AI 分析患者就诊规律，识别潜在流失患者
- 预警规则：超过 X 个月未复诊、治疗方案未完成且长期未预约、复查提醒未响应
- 生成挽回话术和建议优惠策略
- 推送给前台或客服跟进
- 在 `PatientView.vue` 和 `Patient360View.vue` 显示流失风险标签

### 3.7 AI 医嘱合规检查

**场景**：医生开具治疗计划和医嘱
**功能**：
- 检查药物配伍禁忌（如利多卡因过敏史）
- 检查治疗计划与诊断是否匹配
- 提醒必做检查（如种植前未拍 CBCT）
- 提醒知情同意书签署状态
- 在 `TreatmentView.vue` 治疗计划提交前 AI 检查

### 3.8 AI 多语言患者沟通

**场景**：涉外门诊或外籍患者
**功能**：
- AI 实时翻译医患对话
- 自动生成多语言版病历摘要（供患者带走）
- 支持英语、日语、韩语、阿拉伯语等
- 在 `Patient360View.vue` 患者档案页集成翻译面板

---

## 4. 技术架构

### 4.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Vue 2.6)                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ 首页AI助手 │ │ 病历助手  │ │ 经营分析  │ │ Agent管理 │       │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘       │
│       └─────────────┴─────────────┴─────────────┘            │
│                         │ streamChat()                       │
└─────────────────────────┬───────────────────────────────────┘
                          │ SSE / HTTP
┌─────────────────────────┼───────────────────────────────────┐
│                     后端 (Spring Boot)                       │
│  ┌──────────────────────┼────────────────────────┐          │
│  │              AI 网关层 (AI Gateway)             │          │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐  │          │
│  │  │ 路由分发器  │ │ 上下文组装  │ │ 工具调度器  │  │          │
│  │  └────────────┘ └────────────┘ └────────────┘  │          │
│  └──────────────────────┼────────────────────────┘          │
│                         │                                    │
│  ┌──────────────────────┼────────────────────────┐          │
│  │              AI 服务层                          │          │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────┐   │          │
│  │  │本地Function│ │ 外部Agent │ │  工作流调用   │   │          │
│  │  │ Calling  │ │   代理   │ │  (n8n/Dify)  │   │          │
│  │  └──────────┘ └──────────┘ └──────────────┘   │          │
│  └──────────────────────┼────────────────────────┘          │
│                         │                                    │
│  ┌──────────────────────┼────────────────────────┐          │
│  │              数据层                              │          │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐  │          │
│  │  │患者数据 │ │ 财务数据 │ │ 病历数据 │ │ 耗材数据 │  │          │
│  │  └────────┘ └────────┘ └────────┘ └────────┘  │          │
│  └────────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 核心组件

| 组件 | 职责 | 现有/新增 |
|------|------|----------|
| `AiGatewayService` | 统一入口，负责请求路由、上下文组装、权限校验 | 新增 |
| `BusinessAnalysisChatService` | 流式对话、Function Calling、会话管理 | 已有，需重构为通用网关 |
| `AiToolService` | 本地工具定义与执行 | 已有，需扩展 |
| `ExternalAgentService` | 外部 Agent API 代理调用 | 新增 |
| `WorkflowToolService` | 外部工作流作为工具调用 | 新增 |
| `AiAgentConfigService` | Agent 配置 CRUD | 已有，需扩展 |
| `AiModelProviderService` | LLM 供应商配置管理 | 已有 |
| `MedicalRecordAiService` | 病历 AI 补全与质控 | 新增 |
| `FollowupAiService` | 随访计划与话术生成 | 新增 |
| `PatientRiskService` | 患者流失风险分析 | 新增 |

### 4.3 数据模型扩展

**ai_agent_config（已有，需扩展）**
```sql
ALTER TABLE ai_agent_config ADD COLUMN temperature DECIMAL(3,2) DEFAULT 0.7 COMMENT '温度参数';
ALTER TABLE ai_agent_config ADD COLUMN max_context_tokens INT DEFAULT 8000 COMMENT '最大上下文token数';
ALTER TABLE ai_agent_config ADD COLUMN preferred_model VARCHAR(64) DEFAULT NULL COMMENT '偏好模型名称';
ALTER TABLE ai_agent_config ADD COLUMN allowed_roles JSON DEFAULT NULL COMMENT '允许使用的角色列表';
ALTER TABLE ai_agent_config ADD COLUMN is_external TINYINT(1) DEFAULT 0 COMMENT '是否为外部Agent';
ALTER TABLE ai_agent_config ADD COLUMN external_api_url VARCHAR(512) DEFAULT NULL COMMENT '外部Agent API地址';
ALTER TABLE ai_agent_config ADD COLUMN external_api_key VARCHAR(256) DEFAULT NULL COMMENT '外部Agent API密钥';
ALTER TABLE ai_agent_config ADD COLUMN external_config JSON DEFAULT NULL COMMENT '外部Agent扩展配置';
ALTER TABLE ai_agent_config ADD COLUMN workflow_tools JSON DEFAULT NULL COMMENT '关联的工作流工具列表';
```

**ai_workflow_tool（新增）**
```sql
CREATE TABLE ai_workflow_tool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL COMMENT '工作流名称',
    tool_key VARCHAR(64) NOT NULL UNIQUE COMMENT '工具标识',
    description TEXT COMMENT '功能描述',
    webhook_url VARCHAR(512) NOT NULL COMMENT 'Webhook地址',
    http_method VARCHAR(16) DEFAULT 'POST' COMMENT 'HTTP方法',
    auth_type VARCHAR(32) DEFAULT 'bearer' COMMENT '认证类型：bearer/apikey/none',
    auth_config JSON DEFAULT NULL COMMENT '认证配置',
    input_schema JSON NOT NULL COMMENT '输入参数Schema',
    output_mapping JSON DEFAULT NULL COMMENT '输出结果映射规则',
    timeout_seconds INT DEFAULT 30 COMMENT '超时时间',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI工作流工具配置表';
```

**ai_medical_record_template（新增）**
```sql
CREATE TABLE ai_medical_record_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT DEFAULT NULL COMMENT '所属医生ID，NULL表示科室默认模板',
    template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
    category VARCHAR(64) NOT NULL COMMENT '病历类别：主诉/现病史/检查/诊断/治疗计划/医嘱',
    trigger_keywords JSON DEFAULT NULL COMMENT '触发关键词列表',
    prompt_template TEXT NOT NULL COMMENT 'AI生成Prompt模板',
    temperature DECIMAL(3,2) DEFAULT 0.3 COMMENT '生成温度',
    sort_order INT DEFAULT 0 COMMENT '排序',
    enabled TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI病历书写模板表';
```

**ai_followup_plan（新增）**
```sql
CREATE TABLE ai_followup_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    treatment_type VARCHAR(64) NOT NULL COMMENT '治疗类型',
    followup_day INT NOT NULL COMMENT '术后第几天',
    followup_date DATE NOT NULL COMMENT '随访日期',
    content_template TEXT COMMENT '随访内容模板',
    status VARCHAR(32) DEFAULT 'pending' COMMENT '状态：pending/sent/replied/completed',
    channel VARCHAR(32) DEFAULT 'wechat' COMMENT '渠道：wechat/sms/phone',
    sent_at TIMESTAMP NULL COMMENT '发送时间',
    reply_content TEXT COMMENT '患者回复内容',
    ai_summary TEXT COMMENT 'AI预读摘要',
    ai_flag VARCHAR(32) DEFAULT NULL COMMENT 'AI标记：正常/需关注/紧急',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI随访计划表';
```

---

## 5. 接口规范

### 5.1 AI 对话统一接口

```
POST /api/v1/ai/chat/stream
Content-Type: application/json

{
  "agent_key": "medical_writer",
  "message": "右上6缺失，种植一期",
  "context": {
    "page": "medical_record",
    "patient_id": 12345,
    "record_id": 67890
  },
  "session_id": "uuid",
  "stream": true
}
```

### 5.2 外部 Agent 调用接口

```
POST /api/v1/ai/external-agent/{agent_key}/chat
Content-Type: application/json

{
  "message": "本月经营情况如何",
  "session_id": "uuid",
  "system_context": {
    "account_id": 1,
    "account_name": "管理员",
    "clinic_id": 1
  }
}
```

### 5.3 工作流工具执行接口

```
POST /api/v1/ai/workflow/{tool_key}/execute
Content-Type: application/json

{
  "parameters": {
    "patient_name": "张三",
    "date_range": "2026-04-01~2026-05-01"
  }
}
```

### 5.4 病历 AI 补全接口

```
POST /api/v1/ai/medical-record/generate
Content-Type: application/json

{
  "record_id": 67890,
  "section": "treatment_plan",
  "keywords": "右上6缺失，种植一期，骨量不足",
  "patient_id": 12345
}

Response:
{
  "code": "200",
  "data": {
    "content": "1. 16种植修复...",
    "suggestions": ["建议先进行GBR骨增量", "3-4个月后复查CBCT评估骨结合情况"]
  }
}
```

---

## 6. 前端页面规划

### 6.1 新增/修改页面清单

| 页面 | 路径 | 说明 |
|------|------|------|
| AI 助手面板（全局悬浮） | `HomeView.vue` | 首页右侧 AI 助手，支持切换 Agent |
| 病历 AI 助手 | `MedicalRecordView.vue` | 病历编辑页嵌入 AI 补全面板 |
| AI Agent 管理 | `AIAgentConfigView.vue` | 已有，需扩展外部 Agent 和工作流配置 |
| 工作流工具管理 | 新增子页面 | 配置 n8n/Dify 工作流作为工具 |
| 病历模板管理 | 新增页面 | 管理 AI 病历书写模板 |
| 随访管理（AI 增强） | `FollowupManagementView.vue` | 集成 AI 随访计划生成与跟踪 |
| 患者风险看板 | 新增页面/面板 | 显示流失风险患者列表 |
| 经营分析（已有） | `BusinessAnalysisView.vue` | 已有，需增强多 Agent 切换 |

### 6.2 全局 AI 入口

- **悬浮 AI 按钮**：所有页面右下角悬浮 AI 助手入口，点击展开对话面板
- **上下文感知**：AI 根据当前页面自动加载相关上下文（如在病历页自动加载当前患者信息）
- **Agent 切换器**：对话面板顶部支持快速切换不同 Agent

---

## 7. 安全与合规

### 7.1 数据安全
- AI 对话日志记录审计（who/what/when）
- 敏感数据（患者姓名、电话）在传输给外部 Agent/工作流时脱敏处理
- API 密钥加密存储（AES-256）

### 7.2 医疗合规
- AI 生成内容必须标注"AI 辅助生成，请医生确认"
- 病历 AI 补全需医生确认后方可保存
- AI 不做诊断，仅提供参考建议
- 保留 AI 生成内容的修改痕迹（记录医生修改前后的差异）

### 7.3 权限控制
- AI 工具调用受 RBAC 权限控制（医生不能调用财务工具）
- 外部 Agent/工作流调用需管理员审批后方可启用
- 会话隔离：用户只能访问自己权限范围内的数据

---

## 8. 实施优先级

### Phase 1：基础能力夯实（2-3 周）
1. 修复现有 Function Calling 格式问题
2. 完善 AI Agent 配置管理（扩展字段）
3. 实现统一 AI 网关层（AiGatewayService）
4. 首页 AI 助手支持 Agent 切换

### Phase 2：数据查询增强（2-3 周）
1. 扩展本地工具集（影像、耗材、加工单深度查询）
2. 实现工作流工具配置与调用
3. 对接 n8n 示例工作流（患者 360 查询）
4. 预计算经营摘要注入

### Phase 3：临床 AI 辅助（3-4 周）
1. AI 病历书写补全
2. 病历质量检查
3. AI 随访计划生成
4. 医嘱合规检查

### Phase 4：智能分析与扩展（2-3 周）
1. 患者流失预警
2. 耗材智能分析
3. 咨询预检与分诊
4. 知情同意书 AI 生成

---

## 9. 附录

### 9.1 术语表
| 术语 | 说明 |
|------|------|
| Function Calling | OpenAI 定义的函数调用机制，AI 决定调用哪个函数并传入参数 |
| Agent | 具有特定角色和能力配置的 AI 助手 |
| System Prompt | 定义 AI 角色和行为的高级指令 |
| SSE | Server-Sent Events，服务器推送事件，用于流式输出 |
| Workflow | 外部工作流平台（如 n8n）定义的自动化流程 |
| Tool | AI 可调用的功能单元，可以是本地查询或外部工作流 |

### 9.2 参考外部平台
- [n8n](https://n8n.io/) - 开源工作流自动化
- [Dify](https://dify.ai/) - LLM 应用开发平台
- [FastGPT](https://fastgpt.in/) - 基于 LLM 的知识库问答
- [Coze](https://www.coze.com/) - 字节跳动 AI Bot 开发平台
