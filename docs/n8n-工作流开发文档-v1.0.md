# n8n 工作流开发文档 v1.0

> 本文档基于口腔 SaaS 管理系统现有接口能力设计，用于指导 n8n 工作流搭建。  
> 适用日期: 2026-05-28  
> 后端地址: `http://localhost:8080`（生产环境替换为实际地址）
> 文档版本: v1.2（已根据 WF-01~WF-04 实际搭建经验更新）

---

## 一、概述

本文档定义 4 个核心自动化工作流场景，所有数据均来自现有后端接口，无需新增接口即可实现。

| 编号 | 场景名称 | 面向用户 | 触发频率 | 状态 |
|---|---|---|---|---|
| WF-01 | 每日营业简报 | 老板 | 每天 1 次 | ✅ 已完成 |
| WF-02 | 明日工作预告 | 前台、医生 | 每天 1 次 | ✅ 已完成 |
| WF-03 | 患者回访与流失预警 | 前台、医生 | 每周 1 次 | ✅ 已创建（JSON 见 `n8n-json/`） |
| WF-04 | 医生业绩周报 | 老板 | 每周 1 次 | ✅ 已创建（JSON 见 `n8n-json/`） |

---

## 二、自动化开发指南（Skills + MCP）

本文档不仅是一份接口参考，更是一份**可执行的自动化规范**。配合已配置的 Claude Code 环境，你可以用自然语言描述需求，由 AI 自动完成工作流的创建、配置和验证。

### 2.1 已配置的能力

| 能力 | 说明 | 状态 |
|---|---|---|
| **n8n MCP** | Claude Code 可直接调用 n8n API，执行搜索节点、创建工作流、验证配置、管理凭证等操作 | ✅ 已配置 (`~/.claude.json`) |
| **n8n Skills** | 7 个专项技能自动激活，辅助表达式编写、节点配置、工作流设计、代码编写、验证排错 | ✅ 已安装 (`~/.claude/skills/`) |

### 2.2 Skills 清单

安装的技能会在讨论 n8n 相关话题时自动触发：

| 技能名称 | 触发场景 |
|---|---|
| `n8n-expression-syntax` | 编写或调试 `{{ }}` 表达式时 |
| `n8n-mcp-tools-expert` | 使用 MCP 工具管理 n8n 资源时 |
| `n8n-workflow-patterns` | 设计工作流结构、选择节点组合时 |
| `n8n-node-configuration` | 配置节点属性、理解字段依赖时 |
| `n8n-code-javascript` | 在 Code 节点编写 JavaScript 时 |
| `n8n-code-python` | 在 Code 节点编写 Python 时 |
| `n8n-validation-expert` | 遇到验证错误、需要排错时 |

### 2.3 推荐的开发流程

**Step 1 — 描述需求**
直接向 Claude 描述你想要的自动化场景，例如：
> "帮我创建一个每日营业简报工作流，早上 8:30 自动执行，调用 `/api/open/clinic/1/business-analysis/latest` 接口，把结果格式化后发送到企业微信"

**Step 2 — 自动创建**
Claude 会：
1. 调用 n8n MCP 工具搜索需要的节点（HTTP Request、Schedule Trigger、Code 等）
2. 按本文档的接口规范配置 HTTP 节点（URL、Headers、参数）
3. 使用 Skills 辅助编写表达式和 Code 节点逻辑
4. 创建工作流并保存到 n8n

**Step 3 — 验证调试**
创建完成后，Claude 可以：
1. 调用 MCP 工具验证工作流配置（检查必填字段、连线完整性）
2. 按需触发测试执行
3. 根据返回结果调整节点配置

**Step 4 — 转为子工作流**
验证通过后，在 n8n 界面中将工作流设置为子工作流（Sub-workflow），供其他主工作流调用。

### 2.4 使用示例

以下对话示例展示了完整的自动化开发过程：

**用户**: "按文档里的 WF-01 场景，帮我创建每日营业简报工作流"

**Claude**: 
- 调用 MCP `search_nodes` 查找 Schedule Trigger 和 HTTP Request 节点
- 调用 MCP `create_workflow` 创建空白工作流
- 配置 Schedule Trigger：每天 8:30（cron `0 30 8 * * *`）
- 配置 HTTP Request：
  - URL: `{{ $env.baseUrl }}/api/open/clinic/{{ $env.clinicId }}/business-analysis/latest`
  - Headers: `X-API-Key: {{ $env.apiKey }}`
- 配置 Code 节点：按本文档 3.1.4 节格式化输出
- 调用 MCP `validate_workflow` 验证配置
- 返回工作流 ID 和测试链接

### 2.5 手动与自动的边界

| 任务 | 推荐方式 | 原因 |
|---|---|---|
| 工作流骨架搭建 | 自动（MCP + Skills） | 节点连接、基础配置标准化 |
| 表达式微调 | 自动（Skills 辅助） | 语法检查、变量引用验证 |
| 外部服务连接（微信/邮件） | 手动 | 需要真实账号授权，涉及敏感凭证 |
| 生产环境部署 | 手动 | 需要确认定时触发器、环境变量 |
| 复杂业务逻辑（多分支判断） | 混合 | AI 生成初稿，人工审核边界条件 |

---

## 二、WF-01 实际配置参考（已验证）

以下为 "WF-01 每日营业简报 (AI分析版)" 的实际搭建配置，可作为其他工作流的模板。

### 2.1 节点拓扑

```
Webhook Trigger → HTTP Request (获取数据) → Code (格式化) → AI Agent (DeepSeek) → Structured Output Parser → 输出
```

### 2.2 各节点关键配置

#### Webhook Trigger
- **路径**: 自定义路径，如 `daily-briefing`
- **响应模式**: `lastNode`（最后一个节点输出作为响应）
- **调用方式**: 前端通过 `/api/ai/proxy/{agentKey}` 代理触发

#### HTTP Request（获取经营数据）
- **URL**: `{{ $env.baseUrl || 'http://host.docker.internal:8080' }}/api/open/clinic/{{ $env.clinicId || 1 }}/daily-metrics`
- **Headers**: `X-API-Key: {{ $env.apiKey }}`
- **查询参数**: `date={{ $now.format('yyyy-MM-dd') }}`（如需查询当天；不传则默认昨天）
- **返回结构**: `data` 根级别包含 17 个精简字段

#### Code（数据格式化）

```javascript
const item = $input.all()[0].json;
const m = item.data || item;

return [{
  json: {
    analysis_date: m.analysis_date,
    today_income: m.today_income,
    today_expense: m.today_expense,
    today_net_income: m.today_net_income,
    today_appointments: m.today_appointments,
    today_medical_records: m.today_medical_records,
    today_treatments: m.today_treatments,
    today_unique_patients: m.today_unique_patients,
    visit_patient_count: m.visit_patient_count,
    returning_visit_count: m.returning_visit_count,
    new_visit_count: m.new_visit_count,
    cancellation_rate: m.cancellation_rate,
    top_doctors: m.top_doctors || [],
    top_projects: m.top_projects || [],
    current_month_income: m.current_month_income,
    month_net_change_rate: m.month_net_change_rate
  }
}];
```

> **重要**：字段名**必须**使用英文，Code 节点中不要使用中文字段名，否则后续节点引用会报 `undefined`。

#### AI Agent（DeepSeek 模型）
- **模型**: DeepSeek Chat
- **节点名**: **只能包含 `A-Za-z0-9_`**，如 `AI_Analysis`，**不能用中文**
- **提示词要点**:
  - 要求模型根据经营数据生成结构化分析
  - **必须**在提示词末尾强调：`"最终输出必须是纯 JSON，JSON 内部不要包含换行符，使用紧凑格式"`
  - 提示 DeepSeek 模型容易在 JSON 字符串值中插入多余换行，需显式禁止

#### Structured Output Parser（强制 JSON 格式）
- **模式**: JSON Schema
- **字段**: 使用**中文字段名**，方便前端直接展示和用户阅读
- **示例结构**:

```json
{
  "简报日期": "string",
  "经营评分": "number",
  "评分说明": "string",
  "核心指标": {
    "今日总收入": "number",
    "今日总支出": "number",
    "今日净收入": "number"
  },
  "AI分析摘要": "string",
  "风险提示": "string",
  "建议动作": "string"
}
```

> **重要**：Schema 定义用中文，但 AI Agent 的提示词中引用的变量名用英文（与 Code 节点输出一致）。

### 2.3 踩坑记录

| 问题 | 原因 | 解决方案 |
|---|---|---|
| `Custom date key can only contain characters "A-Za-z0-9_"` | AI Agent 节点名用了中文"AI 经营分析" | 改名成 `AI_Analysis` |
| Code 节点输出全为 0 / undefined | Code 里用了中文字段名如 `m.分析日期` | 统一用英文字段名，如 `m.analysis_date` |
| Structured Output Parser 报"非法控制字符" | 从聊天窗口复制 JSON 时引入了不可见字符 | 从干净文件复制，不要直接从聊天粘贴 |
| DeepSeek 输出 JSON 夹杂换行和空格 | 模型在字符串值中插入了 `\\n` | 提示词显式要求"JSON 内部不要包含换行符，紧凑格式" |
| 前端显示 `[object Object]` | AI 返回嵌套对象，`String(obj)` 转换失败 | 前端使用递归方法 `formatObjectToText` 和 `renderJsonCardValue` 处理 |
| Webhook 只执行第一个节点 | 节点之间未连线 | 检查 n8n 画布上的连线是否完整 |
| `/daily-metrics` 返回昨天数据 | 接口默认 `minusDays(1)` | 传 `date={{ $now.format('yyyy-MM-dd') }}` 获取当天 |

---

## 三、接口能力总览

### 3.1 通用请求规范

所有业务接口均需携带以下 Header：

```
X-API-Key: sk-saas-xxxxxxxxxxxxxxxx
```

接口路径统一前缀：

```
/api/open/clinic/{clinicId}
```

- `clinicId`：诊所ID，由 API Key 关联的诊所决定，路径中必须与 Key 一致，否则返回 403
- `baseUrl`：后端服务地址，如 `http://host.docker.internal:8080`

返回结构统一为：

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": { ... }
}
```

列表接口使用 PageHelper 分页，返回结构：

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    "total": 100,
    "list": [],
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10
  }
}
```

**注意**：列表数据均在 `data.list` 数组中，不是 `data` 直接返回数组。

### 3.2 核心接口清单

#### 经营报表接口（已内置全部聚合计算）

| 接口 | 方法 | 开放路径 | 说明 |
|---|---|---|---|
| 获取最新日报 | GET | `/api/open/clinic/{clinicId}/business-analysis/latest` | 返回今日全部经营指标 |
| 获取最新周报 | GET | `/api/open/clinic/{clinicId}/business-analysis/weekly/latest` | 返回本周全部经营指标 |
| 获取最新月报 | GET | `/api/open/clinic/{clinicId}/business-analysis/monthly/latest` | 返回本月全部经营指标 |

**日报返回核心字段（注意层级）：**

```json
{
  "code": "200",
  "data": {
    "operating_score": 82,
    "trend": "up",
    "headline": "今日经营状态一句话",
    "summary": "80-160字摘要",
    "metrics": {
      "today_income": 8560.0,
      "today_expense": 2100.0,
      "today_net_income": 6460.0,
      "today_appointments": 12,
      "today_medical_records": 8,
      "today_treatments": 5,
      "today_unique_patients": 7,
      "appointment_status_breakdown": {
        "待治疗": 2,
        "已预约": 3,
        "已取消": 2,
        "其他": 1
      },
      "cancellation_rate": 8.3,
      "top_doctors": [
        {"doctor_name": "王医生", "appointment_count": 6, "treatment_count": 3, "treatment_revenue": 4200.0}
      ],
      "top_projects": [
        {"project_name": "洁牙", "case_count": 5, "revenue": 2500.0}
      ],
      "current_month_income": 186500.0,
      "previous_month_net_income": 55000.0,
      "month_net_change_rate": 9.09
    },
    "analysis": {
      "highlights": [...],
      "risks": [...],
      "opportunities": [...],
      "actions": [...],
      "management_brief": "..."
    }
  }
}
```

**关键提醒**：
- `operating_score`、`trend`、`headline`、`summary` 在 `data` 根级别
- 经营指标在 `data.metrics` 下
- 深度分析（亮点/风险/机会/动作）在 `data.analysis` 下

#### 预约接口

| 接口 | 方法 | 开放路径 | 参数 |
|---|---|---|---|
| 查询预约列表 | GET | `/api/open/clinic/{clinicId}/appointments` | `page`, `size`, `status`, `appointmentDate`, `startDate`, `endDate`, `doctorAccountId` |

**Appointment 返回字段：**

| 字段 | 类型 | 含义 |
|---|---|---|
| id | int | 预约ID |
| patient_id | Long | 患者ID |
| patient_name | String | 患者姓名 |
| appointment_date | Date | 预约日期 |
| appointment_time | String | 预约时间 |
| duration_minutes | Integer | 时长 |
| doctor_account_id | Long | 医生ID |
| doctor_name | String | 医生姓名 |
| appointment_purpose | String | 预约目的/项目 |
| status | String | 状态 |
| cancel_reason | String | 取消原因 |
| has_arrears | Boolean | 是否有欠款 |
| arrears_amount | Double | 欠款金额 |

#### 患者详情接口

| 接口 | 方法 | 开放路径 | 说明 |
|---|---|---|---|
| 患者基础信息 | GET | `/api/open/clinic/{clinicId}/patients/{patientId}/details` | 返回就诊次数、总费用、欠款等 |
| 患者病历 | GET | `/api/open/clinic/{clinicId}/patients/{patientId}/medical-records` | 返回病历列表 |

**`/patients/{patientId}/details` 返回字段：**

| 字段 | 类型 | 含义 |
|---|---|---|
| patient | Object | 患者基础信息 |
| visitCount | Integer | 就诊次数 |
| lastVisit | Date | 最后就诊日期 |
| totalFee | Double | 总消费金额 |
| hasArrears | Boolean | 是否有欠款 |
| arrearsAmount | Double | 欠款金额 |

#### 患者工作台接口（内置风险标记）

| 接口 | 方法 | 开放路径 | 参数 |
|---|---|---|---|
| 患者工作台 | GET | `/api/open/clinic/{clinicId}/patients/workbench` | `page`, `size`, `searchType`, `keyword`, `quickScope`, `groupKey` 等 |

**PatientWorkbenchRow 关键字段：**

| 字段 | 类型 | 含义 |
|---|---|---|
| id | Long | 患者ID |
| name | String | 姓名 |
| phone | String | 电话 |
| gender | String | 性别 |
| age | Integer | 年龄 |
| customer_source | String | 客户来源 |
| visit_count | Integer | 就诊次数 |
| total_spent | Double | 总消费金额 |
| last_visit_date | Date | 最后就诊日期 |
| last_treatment_date | Date | 最后治疗日期 |
| next_followup_date | Date | 下次随访日期 |
| next_followup_overdue | Boolean | 随访是否逾期 |
| high_value_flag | Boolean | 高价值客户标记 |
| lost_risk_flag | Boolean | 流失风险标记 |
| has_arrears | Boolean | 是否有欠款 |
| arrears_amount | Double | 欠款金额 |

#### 医生业绩接口

| 接口 | 方法 | 开放路径 | 参数 |
|---|---|---|---|
| 医生业绩 | GET | `/api/open/clinic/{clinicId}/finances/doctor-performance` | `startDate`, `endDate`, `doctorAccountId`, `doctorName` |

**DoctorPerformanceStat 返回字段：**

| 字段 | 类型 | 含义 |
|---|---|---|
| doctor_account_id | Long | 医生账号ID |
| doctor_name | String | 医生姓名 |
| project_count | Integer | 项目数量 |
| turnover_amount | Double | 营业额 |
| received_amount | Double | 实收金额 |
| refunded_amount | Double | 退款金额 |
| arrears_amount | Double | 欠款金额 |

#### 治疗记录接口

| 接口 | 方法 | 开放路径 | 参数 |
|---|---|---|---|
| 查询治疗记录 | GET | `/api/open/clinic/{clinicId}/treatments` | `page`, `size`, `patientId`, `doctorAccountId`, `startDate`, `endDate`, `status` |

**Treatment 关键字段：**

| 字段 | 类型 | 含义 |
|---|---|---|
| id | Long | 治疗ID |
| patient_id | Long | 患者ID |
| patient_name | String | 患者姓名 |
| appointment_purpose | String | 项目名称 |
| doctor_account_id | Long | 医生ID |
| doctor_name | String | 医生姓名 |
| treatment_date | Date | 治疗日期 |
| treatment_fee | String | 治疗费用 |
| charged_amount | Double | 已收费金额 |
| arrears_amount | Double | 欠款金额 |
| billing_status | String | 收费状态 |

#### 医生排班接口

| 接口 | 方法 | 开放路径 | 参数 |
|---|---|---|---|
| 查询排班 | GET | `/api/open/clinic/{clinicId}/doctors` | `page`, `size`, `status`, `scheduleDate` |

**Doctor 返回字段：**

| 字段 | 类型 | 含义 |
|---|---|---|
| id | Long | 排班ID |
| doctor_name | String | 医生姓名 |
| schedule_date | Date | 排班日期 |
| start_time | Time | 开始时间 |
| end_time | Time | 结束时间 |
| status | String | 排班状态 |
| shift_type | String | 班次类型 |

#### 病历接口

| 接口 | 方法 | 开放路径 | 参数 |
|---|---|---|---|
| 按患者查病历 | GET | `/api/open/clinic/{clinicId}/patients/{patientId}/medical-records` | `patientId`(路径), `page`, `size` |

**MedicalRecord 关键字段：**

| 字段 | 类型 | 含义 |
|---|---|---|
| id | Long | 病历ID |
| patient_id | Long | 患者ID |
| patient_name | String | 患者姓名 |
| doctor_name | String | 医生姓名 |
| visit_date | Date | 就诊日期 |
| record_type | String | 病历类型（初诊/复诊） |
| diagnosis | String | 诊断 |
| treatment_plan | String | 治疗计划 |

#### 耗材接口

| 接口 | 方法 | 开放路径 | 参数 |
|---|---|---|---|
| 低库存预警 | GET | `/api/open/clinic/{clinicId}/materials` | `lowStockOnly=true` |

**Material 关键字段：**

| 字段 | 类型 | 含义 |
|---|---|---|
| id | Long | 耗材ID |
| name | String | 耗材名称 |
| category_name | String | 分类名称 |
| current_stock | Integer | 当前库存 |
| min_stock_alert | Integer | 最低预警值 |
| alert_gap | Integer | 预警缺口 |
| status | String | 状态 |

---

## 三、场景设计

### 场景 1：每日营业简报（WF-01）

#### 3.1.1 场景说明

**目标用户**：老板  
**触发频率**：每天 1 次（建议早上 8:30）  
**核心目标**：用 30 秒了解昨天/今天诊所整体经营情况

#### 3.1.2 数据来源

| 数据项 | 接口 | 参数 |
|---|---|---|
| 经营指标汇总 | `GET /api/open/clinic/{clinicId}/business-analysis/latest` | 无 |

**说明**：该接口已内置全部聚合计算，无需调多个接口自行汇总。

#### 3.1.3 数据提取字段

从 `data.metrics` 中提取以下字段：

| 字段路径 | 中文含义 |
|---|---|
| `data.metrics.today_income` | 今日总收入 |
| `data.metrics.today_expense` | 今日总支出 |
| `data.metrics.today_net_income` | 今日净收入 |
| `data.metrics.today_appointments` | 今日预约数 |
| `data.metrics.today_unique_patients` | 今日接诊患者数 |
| `data.metrics.appointment_status_breakdown` | 预约状态分布 |
| `data.metrics.cancellation_rate` | 取消率 |
| `data.metrics.top_doctors` | 医生业绩排行 |
| `data.metrics.top_projects` | 项目收入排行 |
| `data.metrics.current_month_income` | 本月累计收入 |
| `data.metrics.month_net_change_rate` | 较上月增长率 |

从 `data` 根级别提取：

| 字段路径 | 中文含义 |
|---|---|
| `data.headline` | 一句话 headline |
| `data.summary` | 经营摘要 |
| `data.operating_score` | 经营评分 |
| `data.trend` | 趋势（up/flat/down）|

#### 3.1.4 输出格式

```
📊 某某口腔 | {analysis_date} 营业简报

经营评分: {operating_score}分 ({trend_icon})
{headline}

💰 今日收支:
   收入: ¥{today_income} | 支出: ¥{today_expense} | 净收入: ¥{today_net_income}

👥 今日预约:
   预约: {today_appointments}人 | 接诊: {today_unique_patients}人
   取消率: {cancellation_rate}%
   状态分布: 待治疗{xx} | 已完成{xx} | 已取消{xx}

👨‍⚕️ 医生排行:
   1. {doctor_name} | 接诊{appointment_count}人 | 收入¥{treatment_revenue}
   2. ...

🏆 热门项目:
   1. {project_name} | {case_count}例 | ¥{revenue}
   2. ...

📈 本月累计: ¥{current_month_income} | 较上月 {change_direction}{month_net_change_rate}%

💡 AI 摘要:
   {summary}
```

#### 3.1.5 异常处理

| 异常场景 | 处理方案 |
|---|---|
| 日报尚未生成（返回 PENDING） | 等待 5 分钟后重试，或返回"日报生成中，请稍后再试" |
| 接口返回 401 | 检查 X-API-Key 是否有效 |
| data.metrics 为空 | 返回基础版简报（仅显示 headline + summary） |

---

### 场景 2：明日工作预告（WF-02）

#### 3.2.1 场景说明

**目标用户**：前台、医生  
**触发频率**：每天 1 次（建议下午 18:00）  
**核心目标**：为第二天工作提前做准备，包括患者背景了解、医生排班确认、耗材库存预警

#### 3.2.2 数据来源

| 数据项 | 接口 | 参数 |
|---|---|---|
| 明日预约清单 | `GET /api/open/clinic/{clinicId}/appointments` | `appointmentDate=明天`, `size=1000` |
| 患者背景信息 | `GET /api/open/clinic/{clinicId}/patients/{patientId}/details` | 逐个患者调用 |
| 初诊/复诊判断 | `GET /api/open/clinic/{clinicId}/patients/{patientId}/medical-records` | `patientId`(路径), `size=1` |
| 医生排班 | `GET /api/open/clinic/{clinicId}/doctors` | `scheduleDate=明天`, `size=100` |
| 低库存耗材 | `GET /api/open/clinic/{clinicId}/materials` | `lowStockOnly=true`, `size=100` |

#### 3.2.3 数据处理逻辑

**步骤 1：获取明日预约列表**

调用 `/api/open/clinic/{clinicId}/appointments?appointmentDate={明天}&size=1000`，获取全部明日预约。

**步骤 2：判断初诊/复诊**

对预约列表中的每个患者：
- 调用 `/api/open/clinic/{clinicId}/patients/{id}/medical-records?size=1`
- 如果返回记录数为 0 → 初诊
- 如果返回记录数 > 0 → 复诊

**步骤 3：获取患者背景**

对复诊患者，调用 `/api/open/clinic/{clinicId}/patients/{patientId}/details` 获取：
- 就诊次数 (`visitCount`)
- 最后就诊日期 (`lastVisit`)
- 总消费 (`totalFee`)
- 欠款情况 (`hasArrears`, `arrearsAmount`)

**步骤 4：获取医生排班**

调用 `/api/open/clinic/{clinicId}/doctors?scheduleDate={明天}&size=100`，确认明日各医生是否上班。

**步骤 5：获取低库存耗材**

调用 `/api/open/clinic/{clinicId}/materials?lowStockOnly=true&size=100`，获取库存预警清单。

#### 3.2.4 输出格式

```
📅 明日工作预告 | {明天日期}

👨‍⚕️ 明日排班:
   王医生 | 09:00-18:00 ✅
   李医生 | 09:00-18:00 ✅
   张医生 | 休息 ❌

📋 明日预约清单:

09:00 张三（初诊）| 洁牙 | 王医生 ✅
   ⚠️ 新患者，前台准备建档

10:00 李四（复诊）| 根管复查 | 李医生 ✅
   📋 就诊3次，总消费¥15,800，无欠款
   上次就诊: 2026-05-17

14:00 王五（初诊）| 补牙 | 王医生 ✅
   ⚠️ 新患者，前台准备建档

16:00 赵六（复诊）| 拆线 | 李医生 ✅
   📋 就诊8次，总消费¥42,000
   ⚠️ 当前欠款¥1,200

📦 耗材预警（明日需关注）:
   扩大针 | 库存2支（预警线5支）| 根管治疗需用 ⚠️
   树脂材料 | 库存3盒（预警线5盒）| 补牙需用 ⚠️
```

#### 3.2.5 异常处理

| 异常场景 | 处理方案 |
|---|---|
| 某患者详情接口超时 | 跳过该患者背景，仅显示预约基础信息 |
| 病历接口返回空 | 标记为"初诊" |
| 明日无预约 | 输出"明日暂无预约安排" |
| 医生排班为空 | 输出警告："明日排班未设置，请确认医生是否上班" |

---

### 场景 3：患者回访与流失预警（WF-03）

#### 3.3.1 场景说明

**目标用户**：前台、医生  
**触发频率**：每周 1 次（建议周一早上）  
**核心目标**：主动发现随访逾期和流失风险患者，生成回访任务清单

#### 3.3.2 数据来源

| 数据项 | 接口 | 参数 |
|---|---|---|
| 患者工作台（全部） | `GET /api/open/clinic/{clinicId}/patients/workbench` | `page=1`, `size=1000` |
| 患者详情补充 | `GET /api/open/clinic/{clinicId}/patients/{patientId}/details` | 逐个调用 |

**说明**：`/patients/workbench` 接口已内置患者风险标记，无需自行计算。

#### 3.3.3 数据处理逻辑

**步骤 1：获取全部患者列表**

调用 `/api/open/clinic/{clinicId}/patients/workbench?page=1&size=1000`，获取患者工作台数据。

**步骤 2：筛选风险患者**

对返回的患者列表进行筛选：

| 风险等级 | 筛选条件 |
|---|---|
| 🔴 高风险 | `next_followup_overdue=true` **且** `lost_risk_flag=true` |
| 🟡 中风险（随访逾期） | `next_followup_overdue=true` **且** `lost_risk_flag=false` |
| 🟢 维护（流失风险） | `next_followup_overdue=false` **且** `lost_risk_flag=true` |
| 💎 高价值维护 | `high_value_flag=true` |

**步骤 3：排序**

- 高风险 > 中风险 > 流失风险 > 高价值维护
- 同一等级内按 `last_visit_date` 升序（越久未就诊越靠前）

**步骤 4：补充联系方式**

患者工作台已返回 `phone` 字段，无需额外查询。

#### 3.3.4 输出格式

```
⚠️ 患者回访预警 | {本周日期范围}

📊 统计:
   高风险患者: {count}人
   随访逾期: {count}人
   流失风险: {count}人
   高价值待维护: {count}人

🔴 高风险（随访逾期 + 流失风险）:
   1. 张三 | 男 | 35岁
      电话: 138xxxx | 最后就诊: 2026-03-15（逾期70天）
      总消费: ¥3,500 | 就诊次数: 2次
      建议: 电话回访，了解未复诊原因，必要时提供优惠促回访

   2. ...

🟡 随访逾期:
   1. 李四 | 女 | 42岁
      电话: 139xxxx | 最后就诊: 2026-04-20（逾期34天）
      总消费: ¥12,000 | 就诊次数: 6次
      建议: 提醒预约复查，强调定期检查的重要性

   2. ...

🟢 流失风险:
   1. 王五 | 男 | 28岁
      电话: 137xxxx | 最后就诊: 2026-05-10
      总消费: ¥2,800 | 就诊次数: 1次
      建议: 初诊后未回访，主动电话关怀，询问治疗效果

💎 高价值客户维护:
   1. 赵六 | 女 | 50岁
      电话: 136xxxx | 最后就诊: 2026-05-10
      总消费: ¥68,000 | 就诊次数: 12次 | 口碑客户
      建议: 发送关怀短信，邀请参加老客户活动，促进转介绍
```

#### 3.3.5 异常处理

| 异常场景 | 处理方案 |
|---|---|
| 患者工作台返回数据量过大 | 分页处理，先取前 1000 条 |
| 电话字段为空 | 输出"电话缺失，请前台补充" |
| 无风险患者 | 输出"本周无高风险患者，经营状态良好" |

---

### 场景 4：医生业绩周报（WF-04）

#### 3.4.1 场景说明

**目标用户**：老板  
**触发频率**：每周 1 次（建议周一早上）  
**核心目标**：统计每位医生本周的工作量、收入、项目分布，用于绩效评估和资源调配

#### 3.4.2 数据来源

| 数据项 | 接口 | 参数 |
|---|---|---|
| 医生业绩 | `GET /api/open/clinic/{clinicId}/finances/doctor-performance` | `startDate=本周一`, `endDate=本周日` |
| 治疗项目分布 | `GET /api/open/clinic/{clinicId}/treatments` | `startDate=本周一`, `endDate=本周日`, `size=1000` |
| 预约完成情况 | `GET /api/open/clinic/{clinicId}/appointments` | `startDate=本周一`, `endDate=本周日`, `size=1000` |

#### 3.4.3 数据处理逻辑

**步骤 1：获取医生业绩**

调用 `/api/open/clinic/{clinicId}/finances/doctor-performance?startDate={本周一}&endDate={本周日}`，获取每位医生的业绩数据。

**步骤 2：获取治疗项目分布**

调用 `/api/open/clinic/{clinicId}/treatments?startDate={本周一}&endDate={本周日}&size=1000`，获取本周全部治疗记录。

按 `doctor_name` 分组，再按 `appointment_purpose`（项目名称）统计每位医生各项目完成次数。

**步骤 3：计算预约到院率**

调用 `/api/open/clinic/{clinicId}/appointments?startDate={本周一}&endDate={本周日}&size=1000`，获取本周全部预约。

按 `doctor_name` 分组统计：
- 预约总数 = 该医生的预约数
- 实际到访数 = status 为"已完成"或"已就诊"的数量
- 到院率 = 实际到访数 / 预约总数 × 100%

**步骤 4：汇总排名**

按 `turnover_amount`（营业额）降序排列医生。

#### 3.4.4 输出格式

```
📈 医生业绩周报 | {本周一} ~ {本周日}

💰 本周合计:
   总项目数: {sum_project_count}
   总营业额: ¥{sum_turnover_amount}
   总实收: ¥{sum_received_amount}
   总退款: ¥{sum_refunded_amount}
   总欠费: ¥{sum_arrears_amount}
   平均客单价: ¥{avg_price}

🏆 医生排行:

1. 王医生
   项目数: 32 | 营业额: ¥42,500 | 实收: ¥38,200 | 欠费: ¥3,200
   项目分布:
      洁牙: 14次 | 根管: 8次 | 种植: 4次 | 其他: 6次
   预约到院率: 92%（预约13人，到访12人）

2. 李医生
   项目数: 28 | 营业额: ¥35,000 | 实收: ¥31,500 | 欠费: ¥2,800
   项目分布:
      洁牙: 12次 | 根管: 6次 | 补牙: 8次 | 其他: 2次
   预约到院率: 85%（预约13人，到访11人）

3. 张医生
   项目数: 15 | 营业额: ¥18,000 | 实收: ¥16,500 | 欠费: ¥1,200
   ...

📊 对比上周:
   王医生: 营业额 {+5%} | 李医生: 营业额 {-2%} | 张医生: 营业额 {+8%}
```

#### 3.4.5 异常处理

| 异常场景 | 处理方案 |
|---|---|
| 某医生无治疗记录 | 显示"本周无治疗记录" |
| 预约记录状态不全 | 仅统计有明确状态的数据，缺失的标记为"未知" |
| 医生业绩接口返回空 | 返回"本周暂无业绩数据" |

---

## 四、通用规范

### 4.1 日期处理

所有接口使用 `yyyy-MM-dd` 格式日期。

**n8n 中获取日期表达式：**

```
今天: {{ $now.format('yyyy-MM-dd') }}
明天: {{ $now.plus(1, 'day').format('yyyy-MM-dd') }}
本周一: {{ $now.startOf('week').format('yyyy-MM-dd') }}
本周日: {{ $now.endOf('week').format('yyyy-MM-dd') }}
本月1日: {{ $now.startOf('month').format('yyyy-MM-dd') }}
本月最后1日: {{ $now.endOf('month').format('yyyy-MM-dd') }}
```

### 4.2 工作流输入参数（建议）

每个子工作流建议定义以下输入参数：

| 参数名 | 类型 | 说明 | 默认值 |
|---|---|---|---|
| `baseUrl` | string | 后端服务地址 | `http://localhost:8080` |
| `apiKey` | string | X-API-Key | 从凭证读取 |
| `clinicId` | string | 诊所ID | 1 |
| `targetDate` | string | 目标日期（yyyy-MM-dd）| 今天/明天等 |
| `pageSize` | number | 分页大小 | 1000 |

### 4.3 数据聚合常用逻辑

**按字段分组计数（n8n Code 节点示例）：**

```javascript
// 输入: items (数组)
// 按 doctor_name 分组统计项目数
const groups = {};
for (const item of items) {
  const key = item.json.doctor_name;
  if (!groups[key]) groups[key] = { doctor_name: key, count: 0 };
  groups[key].count++;
}
return Object.values(groups).sort((a, b) => b.count - a.count);
```

**筛选 + 求和：**

```javascript
// 筛选欠费记录并求和
const arrearsItems = items.filter(i => i.json.arrears_amount > 0);
const totalArrears = arrearsItems.reduce((sum, i) => sum + i.json.arrears_amount, 0);
return [{ json: { totalArrears, count: arrearsItems.length } }];
```

### 4.4 错误处理规范

| 错误码 | 含义 | 处理方案 |
|---|---|---|
| 401 | 缺少或无效 API Key | 终止工作流，输出"API Key 失效，请检查系统设置" |
| 403 | 权限不足 | 终止工作流，输出"当前账号权限不足" |
| 404 | 接口不存在 | 终止工作流，输出"接口路径错误" |
| 500 | 服务器错误 | 重试 1 次，仍失败则输出"服务器繁忙，请稍后重试" |
| 无数据 | 查询结果为空 | 输出"暂无数据"的友好提示，不报错 |

---

## 五、附录

### 5.1 预约状态枚举

常见状态值（以实际数据库为准）：

- `待治疗`
- `已治疗`
- `已完成`
- `已取消`
- `治疗中`
- `已就诊`

### 5.2 病历类型枚举

- `初诊`
- `复诊`

### 5.3 治疗收费状态枚举

- `未收费`
- `部分收费`
- `已收费`
- `已退款`

### 5.4 财务类型枚举

- `收入`
- `支出`
- `退款`

业务类型 (`biz_type`)：
- `TREATMENT_CHARGE` - 治疗收费
- `TREATMENT_REFUND` - 治疗退款
- `LAB_ORDER` - 义齿加工
- `MATERIAL_PURCHASE` - 耗材采购
- `MANUAL_EXPENSE` - 手动支出

### 5.5 患者工作台分组 Key

可用于 `groupKey` 参数的分组：

- `all` - 全部
- `recent` - 最近就诊
- `highValue` - 高价值
- `lostRisk` - 流失风险
- `unconverted` - 未成交
- `implant` - 种植
- `rootCanal` - 根管
- `ortho` - 正畸
- `repair` - 修复
- `cleaning` - 洁牙
- `extraction` - 拔牙
- `filling` - 补牙
- `periodontal` - 牙周
- `removable` - 活动义齿

---

*文档结束*
