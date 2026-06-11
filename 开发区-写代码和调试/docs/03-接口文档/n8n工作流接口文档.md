# n8n AI Agent 工作流接口文档（含 curl 测试命令）

> 本文档按 **子工作流场景** 整理 SaaS 系统开放接口，每个接口附带可直接复制运行的 curl 命令。  
> 基础路径：`http://localhost:8080/api/open/clinic/{clinicId}`  
> 认证方式：Header `X-API-Key: sk-saas-xxxxx`

---

## 公共参数说明

| 参数 | 说明 |
|------|------|
| `clinicId` | 诊所ID，支持自定义字符串（如门诊拼音），当前环境为 `yiyin1`。可在系统设置 → 开放接口密钥页面查看和修改 |
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
curl -s "http://localhost:8080/api/open/clinic/yiyin1/patients?name=吴十&page=1&size=10" \
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
| `has_arrears` | 是否有欠款（可能为 null） |
| `arrears_amount` | 欠款金额（可能为 null） |
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
curl -s "http://localhost:8080/api/open/clinic/yiyin1/patients/10008/details" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**返回结构**：
```json
{
  "patient": { /* 患者完整信息对象 */ },
  "visitCount": 2,
  "lastVisit": "2026-05-26T16:00:00.000+00:00",
  "totalFee": 12400.0,
  "hasArrears": false,
  "arrearsAmount": 0.0
}
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `patient` | 患者完整信息对象（含 name, phone, customer_source 等） |
| `visitCount` | 总就诊次数 |
| `totalFee` | 总消费金额 |
| `hasArrears` | 是否有欠款 |
| `arrearsAmount` | 欠款金额 |
| `lastVisit` | 最近就诊日期 |

---

#### 接口 3：患者洞察摘要

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/patients/{patientId}/insight` |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/yiyin1/patients/10008/insight" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `total_visit_count` | 总就诊次数 |
| `total_spent` | 总消费金额 |
| `last_visit_date` | 最近就诊日期 |
| `last_treatment_date` | 最近治疗日期 |
| `visit_count_last_6m` | 近6个月就诊次数 |
| `high_value_flag` | 高价值患者标记 |
| `lost_risk_flag` | 流失风险标记 |
| `referred_count` | 转介绍人数 |
| `referred_revenue` | 转介绍带来的收入 |
| `word_of_mouth_flag` | 口碑传播标记 |

---

#### 接口 4：患者时间轴

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/patients/{patientId}/timeline` |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/yiyin1/patients/10008/timeline" \
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
curl -s "http://localhost:8080/api/open/clinic/yiyin1/patients/10008/risk-tags" \
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

### 字段使用指南

| 字段 | 用法说明 |
|------|----------|
| `id` | 患者唯一标识。用于拼接后续接口路径：`/patients/{id}/details` |
| `name` / `gender` / `age` / `phone` | 患者基础信息卡片。在 LLM Prompt 中作为患者身份上下文 |
| `has_arrears` / `arrears_amount` | 条件判断：若 `has_arrears` 为 true，在回答中**高亮显示欠款金额** |
| `latest_visit_doctor` / `latest_treatment` | 一句话概括最近就诊情况，如"最近一次由李医生进行补牙" |
| `customer_source` | 患者来源分析，如"该患者来自微信渠道" |
| `visitCount` / `totalFee` / `lastVisit` | 患者详情聚合数据。用于生成"就诊 X 次，累计消费 X 元" |
| `high_value_flag` / `lost_risk_flag` | 布尔标记。若 `lost_risk_flag` 为 true，在回答末尾**追加流失风险提醒** |
| `total_visit_count` / `visit_count_last_6m` | 频次统计："近半年就诊 X 次" |
| `event_time` / `event_type` / `event_title` | 时间轴按时间倒序排列，格式："2024-07-05 治疗：根管治疗第一次" |
| `tag_name` / `risk_level` / `note` | 风险标签列表。`risk_level=3` 用红色标记，在回答中单独列出注意事项 |

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
curl -s "http://localhost:8080/api/open/clinic/yiyin1/appointments?appointmentDate=2026-05-31&page=1&size=50" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**curl 命令**（查询明天）：
```bash
curl -s "http://localhost:8080/api/open/clinic/yiyin1/appointments?appointmentDate=2026-06-01&page=1&size=50" \
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
| `clinic_status` | 接诊状态（可能为 null） |
| `check_in_time` | 签到时间（可能为 null） |
| `cancel_reason` | 取消原因（可能为 null） |
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
curl -s "http://localhost:8080/api/open/clinic/yiyin1/doctors?scheduleDate=2026-05-28&page=1&size=50" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `doctor_name` | 医生姓名 |
| `schedule_date` | 排班日期 |
| `start_time` | 上班时间 |
| `end_time` | 下班时间 |
| `status` | 状态（上班/休息） |
| `shift_type` | 班次类型（全天班/半天班等） |

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

### 字段使用指南

| 字段 | 用法说明 |
|------|----------|
| `patient_name` / `appointment_date` / `appointment_time` | 预约卡片核心字段。按时间排序后生成列表："09:00 张三 - 洗牙（李医生）" |
| `status` | 状态过滤：用户问"今天有哪些取消的预约"时，只保留 `status="已取消"` |
| `clinic_status` | 接诊状态："已就诊""待就诊"，用于计算到诊率 |
| `duration_minutes` | 累加计算总工作时长："今日排班总时长约 X 小时" |
| `has_arrears` / `arrears_amount` | 在预约卡片中标记欠款，如"⚠️ 欠款 ¥5000" |
| `doctor_name` / `schedule_date` / `start_time` / `end_time` | 排班表展示："李医生 09:00-18:00 全天班" |
| `shift_type` | 班次说明："全天班""半天班"，结合 `status="休息"` 标识休息日 |

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
curl -s "http://localhost:8080/api/open/clinic/yiyin1/business-stats" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**返回结构**：
```json
{
  "analysis_date": "2026-05-19",
  "operating_score": 55,
  "trend": "flat",
  "highlights": null,
  "risks": null,
  "opportunities": null,
  "actions": null,
  "daily_metrics_summary": { ... }
}
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `analysis_date` | 分析日期 |
| `operating_score` | 经营评分（0-100，可能为 null） |
| `trend` | 趋势：上升/下降/持平 |
| `highlights` | 经营亮点（系统已停用内置AI，通常为 null，需 n8n 自行分析） |
| `risks` | 风险点（同上，通常为 null） |
| `opportunities` | 机会点（同上，通常为 null） |
| `actions` | 建议措施（同上，通常为 null） |
| `daily_metrics_summary` | 原始指标对象（见下） |

> **注意**：`highlights`/`risks`/`opportunities`/`actions` 及 `operating_score` 当前版本中通常为 null。系统已停用内置 AI 分析，n8n 子工作流需读取 `daily_metrics_summary` 中的原始数据，通过 LLM 节点自行生成分析结论。

**`daily_metrics_summary` 子字段**：

| 字段名 | 说明 |
|--------|------|
| `analysis_date` | 分析日期 |
| `total_patients` | 总患者数 |
| `today_appointments` | 今日预约数 |
| `today_medical_records` | 今日病历数 |
| `today_treatments` | 今日治疗数 |
| `today_unique_patients` | 今日独立患者数 |
| `cancellation_rate` | 取消率 |
| `today_income` | 今日收入 |
| `today_expense` | 今日支出 |
| `today_net_income` | 今日净收入 |
| `completed_treatment_count` | 完成治疗数 |
| `current_month_income` | 本月收入 |
| `current_month_expense` | 本月支出 |
| `current_month_net_income` | 本月净收入 |
| `top_doctors` | 医生排行数组（医生名、预约数、治疗数、营收） |
| `top_projects` | 项目排行数组（项目名、病例数、营收） |

---

#### 接口 2：原始经营指标

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/daily-metrics` |
| 参数 | `date`：日期 yyyy-MM-dd，默认昨日 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/yiyin1/daily-metrics?date=2026-05-30" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `date` | 日期 |
| `day_of_week` | 星期 |
| `appointment_count` | 预约数 |
| `appointment_unique_patient_count` | 预约独立患者数 |
| `appointment_completed_count` | 已完成预约数 |
| `appointment_cancelled_count` | 已取消预约数 |
| `total_income` | 总收入 |
| `total_expense` | 总支出 |
| `net_profit` | 净利润 |
| `visit_patient_count` | 就诊患者数 |
| `registration_count` | 挂号数 |
| `top_doctors` | 医生排行 |
| `top_projects` | 项目排行 |

---

#### 接口 3：财务收支明细

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/finances` |
| 参数 | `startDate`、`endDate`：日期范围；`type`：income/expense；`page`、`size`：分页 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/yiyin1/finances?startDate=2026-05-01&endDate=2026-05-31&type=income&page=1&size=20" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `name` | 收支名称 |
| `amount` | 金额 |
| `type` | 类型：income/expense |
| `biz_type` | 业务类型（TREATMENT_CHARGE=治疗收费, TREATMENT_REFUND=治疗退款） |
| `payment_channel_name` | 支付渠道 |
| `date` | 日期 |
| `remark` | 备注 |
| `patient_id` | 关联患者ID |
| `treatment_id` | 关联治疗ID |

---

#### 接口 4：医生业绩

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/finances/doctor-performance` |
| 参数 | `startDate`、`endDate`：日期范围；`doctorAccountId`：医生ID；`doctorName`：医生姓名 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/yiyin1/finances/doctor-performance?startDate=2026-05-01&endDate=2026-05-31" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**返回结构**：
```json
{
  "start_date": "2026-05-01",
  "end_date": "2026-05-31",
  "doctor_count": 5,
  "list": [...],
  "summary": { /* 合计数据 */ }
}
```

**list 数组中需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `doctor_account_id` | 医生账号ID |
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
curl -s "http://localhost:8080/api/open/clinic/yiyin1/treatments?startDate=2026-05-01&endDate=2026-05-31&page=1&size=20" \
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
| `charged_amount` | 已收金额（可能为 null） |
| `arrears_amount` | 欠款金额（可能为 null） |
| `billing_status` | 收费状态（可能为 null） |
| `status` | 治疗状态 |

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
  → LLM 节点（生成经营分析结论，因为 highlights/risks 等字段已停用）
  → 生成格式化经营报告
```

### 字段使用指南

| 字段 | 用法说明 |
|------|----------|
| `operating_score` / `trend` | 直接展示："经营评分 55 分，趋势持平"。若为空则跳过 |
| `daily_metrics_summary.today_income` / `today_expense` / `today_net_income` | 核心财务指标："今日收入 X 元，支出 X 元，净利润 X 元" |
| `daily_metrics_summary.today_appointments` / `cancellation_rate` | 预约分析："今日预约 X 人，取消率 X%" |
| `daily_metrics_summary.top_doctors` | 医生排行数组。遍历生成列表："1. 李医生 - 营业额 ¥5000" |
| `daily_metrics_summary.top_projects` | 项目排行数组。用于分析热门治疗项目 |
| `date` / `day_of_week` / `appointment_count` / `net_profit` | 日报原始数据。LLM Prompt 中作为分析素材 |
| `name` / `amount` / `type` / `payment_channel_name` | 收支明细列表。按日期倒序，收入用绿色、支出用红色标记 |
| `biz_type` | 业务类型判断：`TREATMENT_CHARGE`=治疗收费，`TREATMENT_REFUND`=治疗退款 |
| `doctor_name` / `turnover_amount` / `received_amount` / `arrears_amount` | 医生业绩排名表。计算实收率：`received_amount / turnover_amount * 100%` |
| `list` / `summary` | 医生业绩返回结构。`list` 是明细数组，`summary` 是全院合计 |
| `patient_name` / `treatment_fee` / `status` | 治疗记录列表。统计已完成数量：`status="已完成"` 的条数 |

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
curl -s "http://localhost:8080/api/open/clinic/yiyin1/inventory?page=1&size=50" \
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
curl -s "http://localhost:8080/api/open/clinic/yiyin1/materials?lowStockOnly=true&page=1&size=50" \
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
| `alert_gap` | 缺口数量 |

---

### n8n 工作流建议

```
When Executed
  → 提取参数（获取 keyword 等）
  → HTTP Request（inventory / materials）
  → Code（按分类分组、标记低库存、生成回答）
```

### 字段使用指南

| 字段 | 用法说明 |
|------|----------|
| `product_name` / `quantity` / `price` | 库存列表核心字段。格式："氧化锆瓷块 C2色 - 库存6块 - ¥680" |
| `category` / `brand` / `supplier` | 分类/品牌/供应商筛选。用户问"赛特力品牌有哪些"时按 `brand` 过滤 |
| `status` / `alert_gap` | 低库存预警。`status="低库存"` 时用红色标记，提示"缺口 X 个，建议采购" |
| `current_stock` / `min_stock_alert` | 库存对比："当前库存 X，预警线 Y，低于预警" |
| `name` / `spec` / `category_name` | 耗材列表字段。格式："扩大针 15#-40# - 根管治疗 - 库存2支" |

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
curl -s "http://localhost:8080/api/open/clinic/yiyin1/treatment-catalog" \
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
| `status` | 状态（1=启用） |

---

### n8n 工作流建议

```
When Executed
  → 提取参数
  → HTTP Request（treatment-catalog）
  → Code（根据关键词过滤项目、格式化价格列表）
```

### 字段使用指南

| 字段 | 用法说明 |
|------|----------|
| `item_name` / `default_fee` | 价格回答核心："洗牙 ¥200/次" |
| `default_content` | 项目描述。用于回答"洗牙包含什么"："超声波洁牙，去除牙结石和牙菌斑" |
| `default_product` | 使用产品说明。如"使用3M树脂材料" |
| `medical_insurance_name` / `self_pay_ratio` | 医保报销计算。如"医保报销，自付比例 20%，实际自付 ¥70" |
| `status` | 过滤条件：`status=1` 为启用项目，仅展示可预约项目 |

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

### 字段使用指南

| 字段 | 用法说明 |
|------|----------|
| `output.text` | 百炼应用返回的完整回答文本。直接作为子工作流最终输出，无需额外处理 |
| `output.finish_reason` | 结束原因判断。若不为 `stop`，提示"回答可能不完整，请重试" |
| `usage.input_tokens` / `usage.output_tokens` | Token 用量记录。可用于成本统计或日志记录 |

---

## 场景七：回访查询（子工作流-回访查询）

### 场景说明
用于查询患者回访记录，了解术后恢复和随访情况。

### 用到的接口

#### 接口：回访列表

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/followups` |
| 参数 | `patientName`：患者姓名；`startDate`、`endDate`：日期范围；`page`、`size`：分页 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/yiyin1/followups?startDate=2026-05-01&endDate=2026-05-31&page=1&size=20" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `patient_name` | 患者姓名 |
| `patient_phone` | 患者电话 |
| `doctor_name` | 随访医生 |
| `followup_date` | 随访日期 |
| `followup_type` | 随访方式（微信随访/电话随访/到院复查） |
| `summary` | 随访摘要 |
| `next_followup_date` | 下次随访日期 |

### 字段使用指南

| 字段 | 用法说明 |
|------|----------|
| `patient_name` / `patient_phone` | 回访对象信息。格式："张三 13800138000" |
| `followup_date` / `followup_type` | 回访记录时间线："2026-05-02 微信随访" |
| `summary` | 回访内容摘要。直接引用作为回访结果描述 |
| `next_followup_date` | 下次随访提醒。若日期临近（3天内），高亮提示"需安排随访" |
| `doctor_name` | 负责医生。用于筛选"李医生的随访记录" |

---

## 场景八：咨询查询（子工作流-咨询查询）

### 场景说明
用于查询患者咨询记录，分析获客渠道和转化率。

### 用到的接口

#### 接口：咨询记录列表

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/consultations` |
| 参数 | `startDate`、`endDate`：日期范围；`channel`：渠道；`intentLevel`：意向等级；`handlingResult`：跟进结果；`keyword`：关键词；`page`、`size`：分页 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/yiyin1/consultations?startDate=2026-05-01&endDate=2026-05-31&page=1&size=20" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `contact_name` | 咨询人姓名 |
| `contact_phone` | 咨询人电话 |
| `consultation_time` | 咨询时间 |
| `consultation_channel` | 咨询渠道（微信/电话/美团等） |
| `chief_project` | 意向项目 |
| `intent_level` | 意向等级（高/中/低） |
| `handling_result` | 跟进结果 |
| `estimated_amount` | 预估金额 |
| `created_by_name` | 录入人 |

### 字段使用指南

| 字段 | 用法说明 |
|------|----------|
| `contact_name` / `contact_phone` | 咨询人信息。用于回访联系 |
| `consultation_channel` | 渠道统计。按渠道分组计数："微信 15 条，电话 8 条" |
| `intent_level` | 意向筛选。用户问"高意向咨询有哪些"时过滤 `intent_level="高"` |
| `handling_result` | 转化分析。统计"已成交""待跟进""未成交"占比 |
| `chief_project` / `estimated_amount` | 项目意向和预估金额。格式："补牙 - 预估 ¥5000" |
| `consultation_time` | 按时间排序，展示最近咨询 |

---

## 场景九：义齿加工查询（子工作流-义齿加工）

### 场景说明
用于查询义齿加工订单状态，了解加工进度。

### 用到的接口

#### 接口：义齿加工订单列表

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/lab-orders` |
| 参数 | `startDate`、`endDate`：日期范围；`status`：状态；`factoryId`：加工厂ID；`keyword`：关键词；`page`、`size`：分页 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/yiyin1/lab-orders?status=in_progress&page=1&size=20" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `patient_name` | 患者姓名 |
| `factory_name` | 加工厂名称 |
| `project_name` | 项目名称 |
| `product_name` | 产品名称 |
| `order_date` | 下单日期 |
| `expected_delivery_date` | 预计交货日期 |
| `actual_delivery_date` | 实际交货日期（可能为 null） |
| `status` | 状态 |
| `total_amount` | 总金额 |

### 字段使用指南

| 字段 | 用法说明 |
|------|----------|
| `patient_name` / `factory_name` | 订单标识："张三 - 精工义齿加工厂" |
| `project_name` / `product_name` | 加工内容："二氧化锆全瓷冠 - 氧化锆全瓷冠" |
| `order_date` / `expected_delivery_date` | 工期计算："2026-05-07 下单，预计 2026-05-09 交货，还剩 X 天" |
| `actual_delivery_date` | 交付状态判断。若为 null 表示未交付，若已超 `expected_delivery_date` 则标红提示"已逾期" |
| `status` | 状态过滤。`in_progress`=加工中，`completed`=已完成 |
| `total_amount` | 订单金额。用于统计本月加工费支出 |

---

## 场景十：病历查询（子工作流-病历查询）

### 场景说明
用于查询病历记录，独立于患者查询场景，支持按日期范围和医生筛选。

### 用到的接口

#### 接口：病历列表

| 项目 | 内容 |
|------|------|
| 方法 | GET |
| 路径 | `/api/open/clinic/{clinicId}/medical-records` |
| 参数 | `startDate`、`endDate`：日期范围；`doctorAccountId`：医生ID；`keyword`：关键词；`page`、`size`：分页 |

**curl 命令**：
```bash
curl -s "http://localhost:8080/api/open/clinic/yiyin1/medical-records?startDate=2026-05-01&endDate=2026-05-31&page=1&size=20" \
  -H "X-API-Key: sk-saas-33d792bdc1274027"
```

**需要保留的字段**：

| 字段名 | 说明 |
|--------|------|
| `patient_name` | 患者姓名 |
| `doctor_name` | 医生姓名 |
| `visit_date` | 就诊日期 |
| `record_type` | 病历类型（初诊/复诊） |
| `chief_complaint` | 主诉 |
| `diagnosis` | 诊断 |
| `treatment_plan` | 治疗方案 |
| `record_status` | 病历状态 |

### 字段使用指南

| 字段 | 用法说明 |
|------|----------|
| `patient_name` / `doctor_name` / `visit_date` | 病历卡片头部："2026-05-25 张三（李医生）" |
| `record_type` | 类型标记："初诊""复诊"，用不同颜色标签区分 |
| `chief_complaint` | 主诉摘要。一句话概括就诊原因 |
| `diagnosis` | 诊断结果。回答"张三上次诊断是什么"时直接引用 |
| `treatment_plan` | 治疗方案。如"树脂充填"，用于跟踪治疗执行情况 |
| `record_status` | 状态判断：`final`=已完成，`draft`=草稿。草稿病历提醒"待完善" |

---

## 附录：主路由工作流（意图识别 + 分发）

### 主路由结构

```
Webhook（接收用户提问）
  → Set（解析输入：userQuestion, clinicId, apiKey）
  → OpenAI（意图识别，输出 JSON：intent + entities）
  → Code（解析 OpenAI 返回的 JSON）
  → Switch（根据 intent 分发到子工作流）
    ├─ patient_query → Execute Workflow → 子工作流-患者查询
    ├─ medical_record → Execute Workflow → 子工作流-病历查询
    ├─ appointment_query → Execute Workflow → 子工作流-预约查询
    ├─ business_stats → Execute Workflow → 子工作流-经营分析
    ├─ inventory_query → Execute Workflow → 子工作流-库存查询
    ├─ treatment_query → Execute Workflow → 子工作流-治疗项目
    ├─ followup_query → Execute Workflow → 子工作流-回访查询
    ├─ consultation_query → Execute Workflow → 子工作流-咨询查询
    ├─ lab_order_query → Execute Workflow → 子工作流-义齿加工
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
- patient_query: 患者信息查询（如"张三的欠款多少"）
- medical_record: 病历查询（如"最近有哪些初诊病历"）
- appointment_query: 预约相关（如"明天有哪些预约"）
- business_stats: 经营分析（如"昨天营收多少"）
- inventory_query: 库存物料（如"扩大针还有多少"）
- treatment_query: 治疗项目价格（如"洗牙多少钱"）
- followup_query: 回访查询（如"这周有哪些随访"）
- consultation_query: 咨询查询（如"这周有多少微信咨询"）
- lab_order_query: 义齿加工（如"李老师的冠做好了没"）
- general_knowledge: 通用口腔知识（如"拔牙后要注意什么"）
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

*文档版本：v3.0*  
*最后更新：2026-05-31*
