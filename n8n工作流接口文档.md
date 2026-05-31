# n8n AI Agent 工作流接口文档（含 curl 测试命令）

> 本文档按 **6 个子工作流场景** 整理 SaaS 系统开放接口，每个接口附带可直接复制运行的 curl 命令。  
> 基础路径：`http://localhost:8080/api/open/clinic/{clinicId}`  
> 认证方式：Header `X-API-Key: sk-saas-xxxxx`

---

## 公共参数说明

| 参数 | 说明 |
|------|------|
| `clinicId` | 诊所ID，测试环境固定传 `1` |
| `X-API-Key` | API Key，测试环境可用 `sk-saas-33d792bdc1274027` |
| `page` / `size` | 分页参数，`page` 从 1 开始，默认每页 10 条 |

**统一返回格式**：
```json
{
  "code": "200",
  "msg": "请求成功",
  "data": { ... }
}
```

列表接口的 `data` 结构为分页格式：
```json
{
  "total": 100,
  "list": [...],
  "pageNum": 1,
  "pageSize": 10,
  "pages": 10,
  "hasNextPage": true
}
```

---

## 场景一：患者查询（子工作流-患者查询）

### 场景说明
用于按姓名搜索患者、查看患者详情、就诊时间轴、风险标签。

### 用到的接口

#### 接口 1：搜索患者列表

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/patients` |
| 参数 | `name`：姓名模糊匹配；`page`、`size`：分页 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/patients?name=吴十&page=1&size=10" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `id` | 患者ID |
| `name` | 姓名 |
| `gender` | 性别 |
| `age` | 年龄 |
| `phone` | 手机号 |
| `customer_source` | 客户来源 |
| `has_arrears` | 是否有欠款 |
| `arrears_amount` | 欠款金额 |
| `latest_visit_doctor` | 最近就诊医生 |
| `latest_treatment` | 最近治疗项目 |
| `created_at` | 建档时间 |

---

#### 接口 2：患者详情（聚合数据）

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/patients/{patientId}/details` |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/patients/10008/details" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `total_visits` | 总就诊次数 |
| `total_spent` | 总消费金额 |
| `total_arrears` | 总欠款金额 |
| `first_visit_date` | 首次就诊日期 |
| `last_visit_date` | 最近就诊日期 |

---

#### 接口 3：患者洞察摘要

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/patients/{patientId}/insight` |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/patients/10008/insight" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `summary` | 智能摘要文本 |
| `risk_level` | 风险等级 |
| `recommendation` | 系统建议 |

---

#### 接口 4：患者时间轴

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/patients/{patientId}/timeline` |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/patients/10008/timeline" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `event_time` | 事件时间 |
| `event_type` | 事件类型（初诊/检查/治疗/复诊/预约/就诊） |
| `event_title` | 事件标题 |
| `event_content` | 事件内容 |

---

#### 接口 5：患者风险标签

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/patients/{patientId}/risk-tags` |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/patients/10008/risk-tags" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `tag_name` | 标签名称 |
| `risk_level` | 风险等级（1=低 2=中 3=高） |
| `source` | 来源 |
| `note` | 备注说明 |

---

### n8n 工作流建议

```
When Executed
  → 提取参数（从 $json 获取 userQuestion / entities / clinicId / apiKey）
  → HTTP Request（搜索患者 /patients?name=xxx）
  → Code（判断是否有结果）
    → 有结果：并行调用 details + insight + timeline + risk-tags
    → 无结果：直接返回"未找到患者"
  → Code（合并所有数据，生成自然语言回答）
```

---

## 场景二：预约查询（子工作流-预约查询）

### 场景说明
用于查询指定日期的预约列表，分析当日排班情况。

### 用到的接口

#### 接口 1：预约列表

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/appointments` |
| 参数 | `appointmentDate`：指定日期；`startDate`/`endDate`：日期范围；`status`：状态；`doctorAccountId`：医生ID；`page`、`size`：分页 |

**curl 命令**（查询今天）：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/appointments?appointmentDate=2026-05-31&page=1&size=50" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**curl 命令**（查询明天）：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/appointments?appointmentDate=2026-06-01&page=1&size=50" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `patient_name` | 患者姓名 |
| `appointment_date` | 预约日期 |
| `appointment_time` | 预约时间 |
| `duration_minutes` | 时长（分钟） |
| `doctor_name` | 医生姓名 |
| `appointment_purpose` | 预约目的 |
| `status` | 预约状态 |
| `clinic_status` | 接诊状态 |
| `check_in_time` | 签到时间 |
| `cancel_reason` | 取消原因 |
| `has_arrears` | 是否有欠款 |
| `arrears_amount` | 欠款金额 |

---

#### 接口 2：医生排班

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/doctors` |
| 参数 | `scheduleDate`：排班日期；`status`：状态；`page`、`size`：分页 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/doctors?page=1&size=50" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `name` | 医生姓名 |
| `title` | 职称 |
| `phone` | 电话 |
| `specialty` | 专长 |
| `status` | 状态 |

---

### n8n 工作流建议

```
When Executed
  → 提取参数（获取 question 中的日期关键词）
  → Code（根据"今天/明天/后天"计算实际日期）
  → HTTP Request（查询预约 /appointments?appointmentDate=xxx）
  → Code（统计总数、按医生分组、计算到诊率）
  → 生成格式化回答
```

---

## 场景三：经营分析（子工作流-经营分析）

### 场景说明
用于获取经营日报、原始指标、收支明细、医生业绩等数据，生成经营分析报告。

### 用到的接口

#### 接口 1：经营统计

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/business-stats` |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/business-stats" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `analysis_date` | 分析日期 |
| `operating_score` | 经营评分（0-100） |
| `trend` | 趋势：上升/下降/持平 |
| `highlights` | 经营亮点 |
| `risks` | 风险点 |
| `opportunities` | 机会点 |
| `actions` | 建议措施 |
| `daily_metrics_summary` | 原始指标对象（见下） |

**`daily_metrics_summary` 子字段**：

| 字段名 | 说明 |
|--------|------|
| `total_patients` | 就诊患者数 |
| `today_appointments` | 今日预约数 |
| `today_income` | 今日收入 |
| `today_expense` | 今日支出 |
| `today_net_income` | 今日净收入 |
| `completed_treatment_count` | 完成治疗数 |
| `current_month_income` | 本月收入 |
| `current_month_expense` | 本月支出 |
| `current_month_net_income` | 本月净收入 |
| `cancellation_rate` | 取消率 |
| `top_doctors` | 医生排行数组 |
| `top_projects` | 项目排行数组 |

---

#### 接口 2：原始经营指标

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/daily-metrics` |
| 参数 | `date`：日期 yyyy-MM-dd，默认昨日 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/daily-metrics?date=2026-05-30" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

---

#### 接口 3：财务收支明细

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/finances` |
| 参数 | `startDate`、`endDate`：日期范围；`type`：income/expense；`page`、`size`：分页 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/finances?startDate=2026-05-01&endDate=2026-05-31&type=income&page=1&size=20" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `name` | 收支名称 |
| `amount` | 金额 |
| `type` | 类型：income/expense |
| `biz_type` | 业务类型 |
| `payment_channel_name` | 支付渠道 |
| `date` | 日期 |
| `remark` | 备注 |

---

#### 接口 4：医生业绩

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/finances/doctor-performance` |
| 参数 | `startDate`、`endDate`：日期范围；`doctorAccountId`：医生ID；`doctorName`：医生姓名 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/finances/doctor-performance?startDate=2026-05-01&endDate=2026-05-31" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `doctor_name` | 医生姓名 |
| `project_count` | 治疗项目数 |
| `turnover_amount` | 营业额 |
| `received_amount` | 实收金额 |
| `refunded_amount` | 退费金额 |
| `arrears_amount` | 欠款金额 |

---

#### 接口 5：治疗记录

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/treatments` |
| 参数 | `startDate`、`endDate`：日期范围；`status`：状态；`page`、`size`：分页 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/treatments?startDate=2026-05-01&endDate=2026-05-31&page=1&size=20" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `patient_name` | 患者姓名 |
| `doctor_name` | 医生姓名 |
| `treatment_date` | 治疗日期 |
| `treatment_content` | 治疗内容 |
| `treatment_fee` | 治疗费用 |
| `charged_amount` | 已收金额 |
| `arrears_amount` | 欠款金额 |
| `billing_status` | 收费状态 |

---

### n8n 工作流建议

```
When Executed
  → 提取参数
  → 并行调用：
    → HTTP Request（business-stats）
    → HTTP Request（daily-metrics?date=昨日）
    → HTTP Request（finances?type=income）
    → HTTP Request（finances?type=expense）
    → HTTP Request（doctor-performance）
  → Merge（合并所有数据）
  → Code（计算净利润、收费率、医生排行等）
  → 生成格式化经营报告
```

---

## 场景四：库存查询（子工作流-库存查询）

### 场景说明
用于查询库存物料状态、低库存预警。

### 用到的接口

#### 接口 1：库存物品

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/inventory` |
| 参数 | `category`：分类；`brand`：品牌；`supplier`：供应商；`keyword`：关键词；`page`、`size`：分页 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/inventory?page=1&size=50" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `product_name` | 物品名称 |
| `category` | 分类 |
| `brand` | 品牌 |
| `supplier` | 供应商 |
| `specification` | 规格 |
| `unit` | 单位 |
| `quantity` | 当前数量 |
| `price` | 单价 |
| `product_batch` | 批次号 |

---

#### 接口 2：耗材列表（支持低库存预警）

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/materials` |
| 参数 | `lowStockOnly=true`：仅低库存；`keyword`：关键词；`page`、`size`：分页 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/materials?lowStockOnly=true&page=1&size=50" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `name` | 名称 |
| `spec` | 规格 |
| `category_name` | 分类 |
| `current_stock` | 当前库存 |
| `min_stock_alert` | 最低预警线 |
| `status` | 状态 |

---

### n8n 工作流建议

```
When Executed
  → 提取参数（获取 keyword 等）
  → HTTP Request（inventory / materials）
  → Code（按分类分组、标记低库存、生成回答）
```

---

## 场景五：治疗项目查询（子工作流-治疗项目）

### 场景说明
用于查询治疗项目目录及价格，回答"洗牙多少钱""种植牙价格"等问题。

### 用到的接口

#### 接口 1：治疗项目目录

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/treatment-catalog` |
| 参数 | `enabledOnly`：是否只返回启用状态，默认 false |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/1/treatment-catalog" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `item_name` | 项目名称 |
| `default_fee` | 默认费用 |
| `default_content` | 默认内容描述 |
| `default_product` | 使用产品 |
| `medical_insurance_name` | 医保名称 |
| `medical_insurance_category` | 医保分类 |
| `self_pay_ratio` | 自付比例（0-1） |

---

### n8n 工作流建议

```
When Executed
  → 提取参数
  → HTTP Request（treatment-catalog）
  → Code（根据关键词过滤项目、格式化价格列表）
```

---

## 场景六：百炼知识库问答（子工作流-百炼知识库）

### 场景说明
用于回答通用口腔知识问题（如"拔牙后要注意什么"），不查询 SaaS 数据，直接调用阿里云百炼应用。

### 用到的接口

#### 接口：百炼应用调用

| 项目 | 内容 |
|------|------|
| 方法 | POST |
| 路径 | `https://dashscope.aliyuncs.com/api/v1/apps/{app_id}/completion` |
| 认证 | Header `Authorization: Bearer {api_key}` |

**curl 命令**：
```bash
curl -s -X POST "https://dashscope.aliyuncs.com/api/v1/apps/YOUR_APP_ID/completion" \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "input": {
      "prompt": "拔牙后要注意什么？"
    },
    "parameters": {
      "incremental_output": false
    }
  }'
```

**响应字段**：

| 字段名 | 说明 |
|--------|------|
| `output.text` | 知识库回答内容 |
| `output.finish_reason` | 结束原因（stop=正常） |
| `usage.input_tokens` | 输入 token 数 |
| `usage.output_tokens` | 输出 token 数 |

---

### n8n 工作流建议

```
When Executed
  → 提取参数（获取 question）
  → HTTP Request（POST 百炼 API）
  → Code（提取 output.text 作为回答）
```

**HTTP Request 节点配置**：
- Method: POST
- URL: `https://dashscope.aliyuncs.com/api/v1/apps/{{ $env.BAILIAN_APP_ID }}/completion`
- Headers:
  - `Authorization`: `Bearer {{ $env.BAILIAN_API_KEY }}`
  - `Content-Type`: `application/json`
- Body (JSON):
  ```json
  {
    "input": {
      "prompt": "={{ $json.question }}"
    },
    "parameters": {
      "incremental_output": false
    }
  }
  ```

---

## 附录：主路由工作流（意图识别 + 分发）

### 主路由结构

```
Webhook（接收用户提问）
  → Set（解析输入：userQuestion, clinicId, apiKey）
  → OpenAI（意图识别，输出 JSON：intent + entities）
  → Code（解析 OpenAI 返回的 JSON）
  → Switch（根据 intent 分发到 7 个分支）
    ├─ patient_query / medical_record → Execute Workflow → 子工作流-患者查询
    ├─ appointment_query → Execute Workflow → 子工作流-预约查询
    ├─ business_stats → Execute Workflow → 子工作流-经营分析
    ├─ inventory_query → Execute Workflow → 子工作流-库存查询
    ├─ treatment_query → Execute Workflow → 子工作流-治疗项目
    ├─ general_knowledge → Execute Workflow → 子工作流-百炼知识库
    └─ fallback → 未知意图回复
  → Respond to Webhook（返回最终回答）
```

### 意图分类 Prompt（给 OpenAI 的 System Prompt）

```
你是一个口腔门诊SaaS系统的AI助手，负责判断用户问题的意图类型。

请严格按以下JSON格式返回，不要输出任何其他内容：
{
  "intent": "场景编码",
  "confidence": 0.95,
  "entities": {
    "patientName": "患者姓名或null",
    "date": "日期或null(格式yyyy-MM-dd)",
    "doctorName": "医生姓名或null",
    "keyword": "其他关键词或null"
  },
  "reasoning": "简要判断理由"
}

可选的意图编码：
- patient_query: 患者信息查询
- appointment_query: 预约相关
- business_stats: 经营分析
- inventory_query: 库存物料
- treatment_query: 治疗项目价格
- general_knowledge: 通用口腔知识
- unknown: 无法判断
```

### 环境变量配置

在 n8n Settings → Environments 中添加：

| 变量名 | 值 | 说明 |
|--------|------|------|
| `SAAS_BASE_URL` | `http://host.docker.internal:8080` | SaaS 后端地址 |
| `SAAS_API_KEY` | `sk-saas-33d792bdc1274027` | API Key |
| `BAILIAN_APP_ID` | `你的百炼应用ID` | 百炼应用 ID |
| `BAILIAN_API_KEY` | `你的百炼API Key` | 百炼 API Key |

---

*文档版本：v2.0*  
*最后更新：2026-05-31*
