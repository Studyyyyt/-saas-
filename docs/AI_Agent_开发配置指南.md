# 口腔门诊 SaaS AI Agent 开发配置指南

> 本文档用于指导在 n8n 中逐个配置 AI Agent 工作流，以及前端接入。
> 文档版本：v1.0 | 更新日期：2026-05-16

---

## 目录

1. [总体架构](#一总体架构)
2. [五大 Agent 分类与配置清单](#二五大-agent-分类与配置清单)
3. [通用技术规范](#三通用技术规范)
4. [经营分析 Agent 详细配置](#四经营分析-agent-详细配置)
5. [医疗辅助 Agent 详细配置](#五医疗辅助-agent-详细配置)
6. [运营服务 Agent 详细配置](#六运营服务-agent-详细配置)
7. [供应链 Agent 详细配置](#七供应链-agent-详细配置)
8. [员工管理 Agent 详细配置](#八员工管理-agent-详细配置)
9. [前端配置说明](#九前端配置说明)
10. [Mock 测试指南](#十mock-测试指南)
11. [实施路线图与检查清单](#十一实施路线图与检查清单)

---

## 一、总体架构

### 1.1 数据流向

```
用户提问 (HomeView.vue)
    ↓
前端发送 POST /api/ai/proxy/{agentKey}
    ↓
后端 AiProxyController 校验 agentKey 白名单
    ↓
后端转发到 n8n Webhook 地址
    ↓
n8n 工作流执行：
    Step 1: Webhook 触发
    Step 2: Function 节点解析参数（日期/意图）
    Step 3: HTTP Request 节点调用系统 API 获取数据
    Step 4: AI Agent 节点分析数据并生成回复
    Step 5: 格式化 JSON 输出
    ↓
返回标准 JSON → 前端渲染（文本回复 或 JSON 卡片）
```

### 1.2 统一请求体（前端发送）

```json
{
  "message": "用户输入的文本",
  "account_id": "1",
  "account_name": "管理员",
  "session_id": "xxx",
  "clinic_id": "1"
}
```

### 1.3 统一响应体（n8n 返回）

```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "reply": "文本回复内容",
    "summary": "摘要",
    "suggestion": "建议",
    "warning": "预警信息",
    "metrics": { "核心指标": "值" },
    "trend": "趋势描述",
    "comparison": "同比环比"
  }
}
```

> 如果 `data` 中只有 `reply` 字段，前端显示纯文本。如果有其他字段，前端会自动渲染成 JSON 卡片。

---

## 二、五大 Agent 分类与配置清单

| 序号 | 分类 | AgentKey | 核心功能 | 优先级 |
|------|------|----------|---------|--------|
| 1 | **经营分析** | `business-analysis` | 收入/支出/预约/患者/医生绩效分析 | P0 |
| 2 | **医疗辅助** | `medical-assist` | 病历扩写、诊断辅助、治疗方案生成 | P0 |
| 3 | **运营服务** | `operation-service` | 回访任务、咨询话术、转化漏斗 | P1 |
| 4 | **供应链** | `supply-chain` | 库存预警、采购建议、加工统计 | P1 |
| 5 | **员工管理** | `staff-management` | 排班建议、医生绩效 | P2 |

### 2.1 系统已有功能对应表

| 已有功能 | 对应 AgentKey | 状态 |
|---------|-------------|------|
| 病历扩写（一键扩写） | `medical-record-expand` | 已上线 |
| AI 经营分析（旧版） | `business-analysis` | 已上线（SSE 版） |

> 注意：`medical-record-expand` 是一键扩写按钮专用的 agentKey，不走首页聊天框。

---

## 三、通用技术规范

### 3.1 日期解析规范（所有 Agent 通用）

**不要在 n8n 外做日期解析，统一在工作流中处理。**

n8n Function 节点代码（通用日期解析器）：

```javascript
/**
 * 自然语言日期解析器
 * 输入：用户原始 message
 * 输出：startDate, endDate（格式 yyyy-MM-dd）
 */
const now = new Date();
const fmt = (d) => d.toISOString().split('T')[0];

const today = fmt(now);
const yesterday = fmt(new Date(now.getTime() - 86400000));

// 本周一和周日
const day = now.getDay() || 7;
const monday = fmt(new Date(now.getTime() - (day - 1) * 86400000));
const sunday = fmt(new Date(now.getTime() + (7 - day) * 86400000));

// 上周一和上周日
const lastMonday = fmt(new Date(now.getTime() - (day + 6) * 86400000));
const lastSunday = fmt(new Date(now.getTime() - day * 86400000));

// 本月
const monthStart = fmt(new Date(now.getFullYear(), now.getMonth(), 1));
const monthEnd = fmt(new Date(now.getFullYear(), now.getMonth() + 1, 0));

// 上月
const lastMonthStart = fmt(new Date(now.getFullYear(), now.getMonth() - 1, 1));
const lastMonthEnd = fmt(new Date(now.getFullYear(), now.getMonth(), 0));

const msg = ($input.first().json.message || "").toLowerCase();

let startDate = monthStart;
let endDate = today;

if (msg.includes("今天") || msg.includes("今日")) {
  startDate = today; endDate = today;
} else if (msg.includes("昨天") || msg.includes("昨日")) {
  startDate = yesterday; endDate = yesterday;
} else if (msg.includes("本周") || msg.includes("这个星期") || msg.includes("这周")) {
  startDate = monday; endDate = sunday;
} else if (msg.includes("上周") || msg.includes("上个星期")) {
  startDate = lastMonday; endDate = lastSunday;
} else if (msg.includes("本月") || msg.includes("这个月")) {
  startDate = monthStart; endDate = today;
} else if (msg.includes("上月") || msg.includes("上个月")) {
  startDate = lastMonthStart; endDate = lastMonthEnd;
} else if (/\d{4}-\d{2}-\d{2}/.test(msg)) {
  // 用户直接输入了日期格式 2026-05-01
  const match = msg.match(/(\d{4}-\d{2}-\d{2})/);
  startDate = match[1]; endDate = match[1];
}

return [{
  json: {
    startDate,
    endDate,
    today,
    originalMessage: $input.first().json.message,
    account_id: $input.first().json.account_id,
    account_name: $input.first().json.account_name,
    clinic_id: $input.first().json.clinic_id
  }
}];
```

### 3.2 调用系统 API 规范

n8n 中调用系统 API 时：
- **Base URL**: `http://backend:8080`（Docker 内部网络）
- **Header**: `X-Operator-Account-Id: {{ $json.account_id }}`
- **返回格式**: 统一为 `Result` 包装，实际数据在 `data` 字段中

n8n HTTP Request 节点配置示例：
```
Method: GET
URL: http://backend:8080/finances/expenseOverview
Query Parameters:
  startDate = {{ $json.startDate }}
  endDate = {{ $json.endDate }}
Headers:
  X-Operator-Account-Id = {{ $json.account_id }}
```

### 3.3 AI Agent 节点 Prompt 模板规范

所有 AI Agent 节点的 System Prompt 必须包含：
1. **角色定义**：你是一位口腔门诊的 XX 专家
2. **输出格式要求**：必须返回 JSON，字段名使用中文映射表中的英文名
3. **数据范围**：基于提供的真实数据，不要编造
4. **禁止事项**：不要编造患者姓名、不要透露隐私信息

---

## 四、经营分析 Agent 详细配置

### 4.1 基础信息

| 配置项 | 值 |
|--------|-----|
| AgentKey | `business-analysis` |
| 名称 | 经营分析 |
| Webhook 地址 | `http://clinic-n8n:5678/webhook/business-analysis`（示例） |
| 适用角色 | admin（管理员/老板） |

### 4.2 快捷指令（前端配置）

```json
[
  "分析本月经营情况",
  "查看今日收入",
  "上周医生绩效排名",
  "本月预约到诊率",
  "有什么经营异常需要关注"
]
```

### 4.3 需要调用的系统 API

#### API 1：支出总览
```
GET /finances/expenseOverview?startDate={startDate}&endDate={endDate}
```
| 字段 | 类型 | 中文含义 | 用途 |
|------|------|---------|------|
| totalIncome | double | 总收入 | 经营概况 |
| totalExpense | double | 总支出 | 利润计算 |
| netProfit | double | 净利润 | 核心指标 |
| categoryBreakdown | array | 分类明细 | 饼图数据 |

#### API 2：医生绩效
```
GET /finances/doctorPerformance?startDate={startDate}&endDate={endDate}
```
| 字段 | 类型 | 中文含义 | 用途 |
|------|------|---------|------|
| doctorName | string | 医生姓名 | 排名展示 |
| totalAmount | double | 业绩金额 | 绩效计算 |
| patientCount | int | 接诊人数 | 工作量 |

#### API 3：预约数据
```
GET /appointments/selectAll?page=1&size=9999
```
| 字段 | 类型 | 中文含义 | 用途 |
|------|------|---------|------|
| patient_name | string | 患者姓名 | 统计 |
| appointment_date | date | 预约日期 | 时间筛选 |
| doctor_name | string | 医生 | 医生负荷 |
| status | string | 预约状态 | 到诊率计算 |
| clinic_status | string | 院内状态 | 流程分析 |

#### API 4：财务流水（补充明细）
```
GET /finances/selectByMonth?year={year}&month={month}
```
| 字段 | 类型 | 中文含义 | 用途 |
|------|------|---------|------|
| name | string | 项目/患者名 | 明细展示 |
| amount | double | 金额 | 统计 |
| date | string | 日期 | 时间线 |
| type | string | 收支类型 | 分类统计 |
| biz_type | string | 业务类型 | 细分维度 |
| payment_channel_name | string | 支付渠道 | 渠道分析 |

### 4.4 n8n 工作流设计

```
[Webhook] 
  → [Function: 日期解析] 
  → [Parallel Branch 1: HTTP GET /finances/expenseOverview]
  → [Parallel Branch 2: HTTP GET /finances/doctorPerformance]
  → [Parallel Branch 3: HTTP GET /appointments/selectAll]
  → [Code: 合并数据]
  → [AI Agent: 分析并生成报告]
  → [Function: 格式化输出]
  → [Respond to Webhook]
```

### 4.5 AI Agent System Prompt

```
你是一位口腔门诊经营分析专家。请基于提供的真实数据，生成经营分析报告。

【输出要求】
必须返回以下 JSON 格式：
{
  "reply": "总体评价（2-3句话）",
  "summary": "经营摘要",
  "metrics": {
    "总收入": "¥xxx",
    "总支出": "¥xxx",
    "净利润": "¥xxx",
    "预约到诊率": "xx%",
    "新患者数": "xx人"
  },
  "trend": "与上月/上周对比趋势",
  "suggestion": "具体改进建议（2-3条）",
  "warning": "异常预警（如有）"
}

【规则】
1. 所有金额保留整数，加 ¥ 符号
2. 百分比保留1位小数
3. 如果没有异常，warning 字段留空
4. 不要编造数据，只能基于提供的数据分析
```

---

## 五、医疗辅助 Agent 详细配置

### 5.1 基础信息

| 配置项 | 值 |
|--------|-----|
| AgentKey | `medical-assist` |
| 名称 | 医疗辅助 |
| Webhook 地址 | `http://clinic-n8n:5678/webhook/medical-assist` |
| 适用角色 | doctor（医生） |

### 5.2 子功能路由

医疗辅助 Agent 内部需要按意图路由到不同处理逻辑：

| 用户意图 | 识别关键词 | 处理逻辑 |
|---------|----------|---------|
| 病历扩写 | "扩写"、"补全病历" | 直接走 AI 生成病历字段 |
| 诊断辅助 | "诊断"、"鉴别诊断" | 根据症状给出建议 |
| 治疗方案 | "方案"、"治疗计划"、"怎么做" | 拉取项目价格表 → 匹配生成方案 |

### 5.3 病历扩写（已上线，仅供参考）

已有接口：`medical-record-expand`，前端一键调用。如需要在聊天框中也支持，可复制相同 prompt。

### 5.4 治疗方案生成（重点）

#### 需要调用的系统 API

**API 1：获取全部治疗项目**
```
GET /treatment-projects/selectEnabled
```
| 字段 | 类型 | 中文含义 | 用途 |
|------|------|---------|------|
| id | long | 项目ID | 唯一标识 |
| project_name | string | 项目名称 | 展示 |
| default_price | BigDecimal | 默认价格 | 报价 |
| category_name | string | 所属分类 | 按科分类 |
| estimated_visit_count | int | 预计就诊次数 | 疗程规划 |
| estimated_cycle_days | int | 预计周期（天）| 时间规划 |
| remark | string | 项目说明 | 备注 |

**API 2：获取分类树**
```
GET /treatment-project-categories/tree?includeDisabled=false
```
用于了解分类结构，如：牙体牙髓 → 根管治疗 → 具体项目。

#### 工作流设计

```
[Webhook]
  → [Function: 意图识别 + 提取诊断文本]
  → [IF: 诊断文本不为空]
    → [HTTP GET /treatment-projects/selectEnabled]
    → [AI Agent: 根据诊断匹配项目并生成方案]
    → [Function: 计算总价和周期]
  → [Respond to Webhook]
```

#### AI Agent System Prompt（治疗方案）

```
你是一位口腔治疗规划专家。请根据患者的诊断信息，从系统提供的项目列表中匹配最适合的治疗方案。

【输入数据】
1. 患者诊断：{{ $json.diagnosis }}
2. 系统项目列表：{{ $json.projects }}

【输出要求】
必须返回以下 JSON 格式：
{
  "reply": "治疗方案总体说明",
  "diagnosis": "患者诊断",
  "treatment_plan": "治疗步骤概述（1. 2. 3.）",
  "items": [
    {
      "project_name": "项目名称",
      "price": 1200,
      "visit_count": 3,
      "cycle_days": 14,
      "category": "所属分类",
      "remark": "说明"
    }
  ],
  "total_price": 4700,
  "total_visits": 5,
  "total_days": 30,
  "medical_advice": "医嘱建议",
  "notes": "注意事项"
}

【规则】
1. 项目必须从提供的系统列表中选择，不能编造
2. 总价自动计算，等于各项目价格之和
3. 就诊次数取各项目最大值（可并行时不累加）
4. 优先推荐标准治疗方案，不要过度治疗
```

---

## 六、运营服务 Agent 详细配置

### 6.1 基础信息

| 配置项 | 值 |
|--------|-----|
| AgentKey | `operation-service` |
| 名称 | 运营服务 |
| Webhook 地址 | `http://clinic-n8n:5678/webhook/operation-service` |
| 适用角色 | admin / nurse（前台/客服） |

### 6.2 快捷指令

```json
[
  "生成今日待回访患者列表",
  "分析本月咨询转化情况",
  "哪些患者有流失风险",
  "生成咨询回复话术"
]
```

### 6.3 需要调用的系统 API

#### API 1：患者列表
```
GET /patients/selectAll?page=1&size=9999
```
| 字段 | 类型 | 中文含义 |
|------|------|---------|
| id | long | 患者ID |
| name | string | 姓名 |
| phone | string | 电话 |
| last_visit_date | date | 最近就诊日期 |

#### API 2：咨询记录
> 需要确认系统是否有咨询管理模块的 API。如无，可先通过患者就诊记录推算。

#### API 3：回访记录
> 需要确认系统是否有回访管理模块的 API。

### 6.4 工作流设计建议

```
[Webhook]
  → [Function: 意图识别（回访/转化/流失/话术）]
  → [Switch: 按意图分支]
    → 回访分支：HTTP GET /patients/selectAll → 筛选最近就诊患者 → 生成回访话术
    → 转化分支：统计预约/到诊/付费转化率
    → 流失分支：筛选长期未就诊患者 → 生成召回方案
  → [AI Agent: 生成对应内容]
  → [Respond to Webhook]
```

---

## 七、供应链 Agent 详细配置

### 7.1 基础信息

| 配置项 | 值 |
|--------|-----|
| AgentKey | `supply-chain` |
| 名称 | 供应链 |
| Webhook 地址 | `http://clinic-n8n:5678/webhook/supply-chain` |
| 适用角色 | nurse / admin（护士/库管） |

### 7.2 快捷指令

```json
[
  "库存预警",
  "本月采购建议",
  "加工单统计",
  "哪些材料即将过期"
]
```

### 7.3 需要调用的系统 API

需要调研以下模块的 API：
- 库存管理模块（材料/药品）
- 采购模块
- 加工单模块（LabOrder）

> 目前已知：`/lab-statistics` 相关接口存在，需进一步确认字段。

### 7.4 占位配置

在 API 未完全确认前，可先配置为：
```
[Webhook] 
  → [AI Agent: 基于通用知识回答]
  → [Respond to Webhook]
```
待系统 API 补充后再接入真实数据。

---

## 八、员工管理 Agent 详细配置

### 8.1 基础信息

| 配置项 | 值 |
|--------|-----|
| AgentKey | `staff-management` |
| 名称 | 员工管理 |
| Webhook 地址 | `http://clinic-n8n:5678/webhook/staff-management` |
| 适用角色 | admin（管理员） |

### 8.2 快捷指令

```json
[
  "下周排班建议",
  "本月医生绩效排名",
  "哪位医生工作量最饱和"
]
```

### 8.3 需要调用的系统 API

#### API 1：医生绩效（已确认）
```
GET /finances/doctorPerformance?startDate={startDate}&endDate={endDate}
```

#### API 2：预约排班
```
GET /appointments/scheduleEntries
```
> 需确认返回字段。

#### API 3：医生列表
```
GET /doctors/selectAll
```
> 需确认具体路径。

---

## 九、前端配置说明

### 9.1 在 AI 智能中心添加 Agent

进入前端页面：系统设置 → AI 智能中心 → Agent 配置

每个 Agent 需要填写：

| 字段 | 填写内容 |
|------|---------|
| AgentKey | 使用本文档中的 key，如 `business-analysis` |
| 名称 | 如"经营分析" |
| Webhook 地址 | n8n 工作流的 Webhook URL |
| 启用状态 | 开/关 |
| 快捷指令 | JSON 数组格式，见各 Agent 配置 |

### 9.2 快捷指令格式

```json
[
  "分析本月经营情况",
  "查看今日收入",
  "上周医生绩效排名"
]
```

### 9.3 前端渲染说明

前端 `HomeView.vue` 已支持两种渲染模式：

1. **纯文本模式**：`data.reply` 存在时直接显示文本
2. **JSON 卡片模式**：`data` 中存在其他字段时，自动渲染为卡片

已知字段会自动显示中文标签：
- `reply` / `content` → 回复内容
- `summary` → 摘要
- `suggestion` → 建议
- `warning` → 预警
- `metrics` → 核心指标
- `trend` → 趋势
- `comparison` → 对比

**如需添加新字段映射**，修改 `HomeView.vue` 中 `renderJsonCard` 方法的 `fieldOrder` 数组。

---

## 十、Mock 测试指南

### 10.1 Mock 服务地址

开发环境已启动 mock-webhook 容器：
- 容器内地址：`http://mock-webhook:9000`
- 宿主机地址：`http://localhost:9000`
- 测试端点：`POST /ai-test`

### 10.2 新增 Mock 接口方法

编辑文件：`shuao-clinic-saas-source/docker/mock-webhook/mock_server.py`

新增路由示例：
```python
@app.route("/ai-business", methods=["POST"])
def ai_business():
    payload = request.get_json(silent=True) or {}
    
    # 打印请求日志
    print_banner("收到经营分析请求", cyan)
    print_json("请求体", payload, cyan)
    
    response = {
        "code": "200",
        "msg": "success",
        "data": {
            "reply": "本月经营概况良好",
            "summary": "收入稳步增长",
            "metrics": {
                "总收入": "¥128,500",
                "总支出": "¥45,200",
                "净利润": "¥83,300",
                "预约到诊率": "87.5%"
            },
            "trend": "环比增长 12%",
            "suggestion": "建议增加周末预约排班",
            "warning": "周五下午预约量偏低"
        }
    }
    
    print_json("响应体", response, green)
    return jsonify(response)
```

修改后**无需重启容器**，直接生效（因为 docker-compose 已挂载 volume）。

### 10.3 测试步骤

1. 确保 mock-webhook 容器运行中：`docker compose -f docker-compose.dev.yml ps`
2. 在前端 AI 智能中心配置 Webhook 地址为 `http://mock-webhook:9000/ai-business`
3. 在首页聊天框输入快捷指令测试
4. 查看前端渲染效果（文本 or 卡片）

---

## 十一、实施路线图与检查清单

### 第一阶段：框架验证（已完成）

- [x] 首页 AI 聊天框接入
- [x] JSON 卡片渲染
- [x] AgentKey 白名单校验
- [x] Mock 服务联调
- [x] 病历扩写功能上线

### 第二阶段：经营分析 Agent（P0）

- [ ] 在 n8n 创建 `business-analysis` 工作流
- [ ] 配置 Webhook 触发节点
- [ ] 添加 Function 日期解析节点（复制本文档 3.1 代码）
- [ ] 添加 HTTP Request 节点调用 `/finances/expenseOverview`
- [ ] 添加 HTTP Request 节点调用 `/finances/doctorPerformance`
- [ ] 添加 HTTP Request 节点调用 `/appointments/selectAll`
- [ ] 添加 AI Agent 节点（复制本文档 4.5 prompt）
- [ ] 添加格式化输出节点
- [ ] 前端添加 `business-analysis` Agent 配置
- [ ] Mock 测试
- [ ] 真实数据测试

### 第三阶段：治疗方案生成（P0）

- [ ] 在 n8n 创建 `medical-assist` 工作流
- [ ] 添加意图识别节点（区分扩写/诊断/方案）
- [ ] 添加 HTTP Request 节点调用 `/treatment-projects/selectEnabled`
- [ ] 添加 AI Agent 节点匹配项目（复制本文档 5.4 prompt）
- [ ] 前端添加 `medical-assist` Agent 配置
- [ ] 病历编辑页面测试治疗方案生成

### 第四阶段：运营服务 Agent（P1）

- [ ] 确认咨询/回访模块 API
- [ ] 配置 `operation-service` 工作流
- [ ] 前端配置

### 第五阶段：供应链 + 员工管理（P1/P2）

- [ ] 确认库存/采购 API
- [ ] 配置 `supply-chain` 工作流
- [ ] 确认排班 API
- [ ] 配置 `staff-management` 工作流

---

## 附录

### A. 系统 API 速查表

| API | 路径 | 用途 |
|-----|------|------|
| 财务支出总览 | `GET /finances/expenseOverview?startDate=&endDate=` | 收入支出统计 |
| 医生绩效 | `GET /finances/doctorPerformance?startDate=&endDate=` | 医生业绩排名 |
| 财务按月 | `GET /finances/selectByMonth?year=&month=` | 月度明细 |
| 全部预约 | `GET /appointments/selectAll?page=&size=` | 预约数据 |
| 全部患者 | `GET /patients/selectAll?page=&size=` | 患者列表 |
| 治疗项目 | `GET /treatment-projects/selectEnabled` | 项目价格表 |
| 项目分类 | `GET /treatment-project-categories/tree` | 分类树 |
| 经营日报 | `GET /business-analysis/latest` | AI 已生成的日报 |

### B. 后端转发接口

前端实际请求的地址格式：
```
POST /api/ai/proxy/{agentKey}
```

后端会根据 `ai_agent_config` 表中配置的 Webhook 地址进行转发。

### C. 环境变量

后端 `application.yml` 相关配置：
```yaml
openai:
  enabled: ${OPENAI_ENABLED:false}
  api-key: ${OPENAI_API_KEY:}
  base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
```

开发环境默认 `OPENAI_ENABLED=false`，如需测试真实 AI 能力，需在 `.env` 或启动参数中设置：
```
OPENAI_ENABLED=true
OPENAI_API_KEY=sk-xxxx
```

---

> 文档结束。配置过程中如有问题，请查看后端日志和 n8n 执行日志定位。
