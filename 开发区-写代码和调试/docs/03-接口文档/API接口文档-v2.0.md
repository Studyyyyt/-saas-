# 口腔SaaS管理系统 - API接口文档 v1.0

> 版本: v2.0
> 日期: 2026-05-25
> 适用范围: 后端服务 `http://localhost:8080`

---

## 一、通用说明

### 1.1 返回结构

所有接口统一返回以下JSON结构：

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {}
}
```

| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | string | 状态码，`200`表示成功 |
| msg | string | 提示信息 |
| data | object/array | 响应数据 |

### 1.2 分页说明

列表查询接口均使用PageHelper分页，返回结构：

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

### 1.3 通用错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 401 | 未授权（缺少或无效API Key） |
| 403 | 权限不足（非管理员） |
| 404 | 请求路径不存在 |
| 500 | 系统错误 |

### 1.4 API Key 认证

### 1.5 开放 API 基础路径

供外部系统（n8n、MCP、脚本等）调用的开放数据接口，基础路径为：

```
/api/open/clinic/{clinicId}
```

所有开放接口均需在请求头中携带 `X-API-Key`，且 Key 所属的 `clinicId` 必须与路径中的 `clinicId` 一致，否则返回 `403`。

**所有业务接口**均支持通过 `X-API-Key` 请求头访问。认证规则如下：

```
X-API-Key: sk-saas-xxx
```

| 调用方 | 是否需要 X-API-Key | 说明 |
|--------|-------------------|------|
| 外部系统（n8n、MCP、Claude Code、脚本等） | **必须携带** | 未携带或 Key 无效时返回 `401` |
| 系统前端页面（医生/护士正常操作） | **自动携带** | 前端 axios 拦截器自动从 localStorage 读取并附加 |
| 登录/注册接口 | 不需要 | `/auth/**` 为白名单 |
| API Key 自身管理接口 | 不需要 | `/api/api-key` 为白名单 |

**缺少 Key 的响应：**

```json
{
  "code": "401",
  "msg": "缺少X-API-Key"
}
```

**错误响应示例（Key 无效）：**

```json
{
  "code": "401",
  "msg": "无效的API Key"
}
```

---

## 二、API Key 管理接口（单 Key 模式）

每个诊所仅拥有一个 API Key。系统设置页面可查看当前 Key（脱敏展示）或重新生成。

### 2.1 获取当前诊所 API Key

- **方法**: GET
- **路径**: `/api/api-key`
- **功能**: 获取当前诊所的 API Key；如不存在则自动创建

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| clinicId | long | 否 | 1 | 诊所ID |

#### curl示例

```bash
curl -s "http://localhost:8080/api/api-key?clinicId=1"
```

#### 返回示例

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    "id": 1,
    "clinicId": 1,
    "name": "默认Key",
    "key": "sk-saa******273",
    "maskedKey": "sk-saa******273",
    "isEnabled": true,
    "description": null,
    "expiresAt": null,
    "lastUsedAt": "2026-05-23T08:22:34",
    "usageCount": 3,
    "createdAt": "2026-05-23T08:19:29",
    "updatedAt": "2026-05-23T08:22:34"
  }
}
```

---

### 2.2 重新生成 API Key

- **方法**: POST
- **路径**: `/api/api-key/regenerate`
- **功能**: 删除该诊所旧 Key 并生成新的 API Key（明文仅展示一次）

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| clinicId | long | 否 | 1 | 诊所ID |

#### curl示例

```bash
curl -s -X POST "http://localhost:8080/api/api-key/regenerate?clinicId=1"
```

#### 返回示例

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    "id": 3,
    "clinicId": 1,
    "name": "默认Key",
    "key": "sk-saas-a1b2c3d4e5f67890",
    "maskedKey": "sk-saa******890",
    "isEnabled": true,
    "description": null,
    "expiresAt": null,
    "lastUsedAt": null,
    "usageCount": null,
    "createdAt": null,
    "updatedAt": null
  }
}
```

---

## 三、预约接口

### 3.1 查询预约列表

- **方法**: GET
- **路径**: `/appointments/selectAll`
- **功能**: 多条件组合查询预约记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | 预约状态：待治疗/已治疗/已完成/已取消/治疗中/已就诊 |
| appointmentDate | string | 否 | - | 预约日期，格式`yyyy-MM-dd` |
| doctorAccountId | long | 否 | - | 医生账号ID |

#### curl示例

```bash
# 示例1: 查询全部预约
curl -s "http://localhost:8080/appointments/selectAll?page=1&size=10"

# 示例2: 按日期筛选
curl -s "http://localhost:8080/appointments/selectAll?page=1&size=10&appointmentDate=2026-05-23"

# 示例3: 按状态筛选
curl -s "http://localhost:8080/appointments/selectAll?page=1&size=10&status=%E5%BE%85%E6%B2%BB%E7%96%97"

# 示例4: 组合筛选（日期+医生）
curl -s "http://localhost:8080/appointments/selectAll?page=1&size=10&appointmentDate=2026-05-23&doctorAccountId=3"
```

#### 返回示例

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    "total": 28,
    "list": [
      {
        "id": 30,
        "patient_id": 3,
        "patient_name": "??",
        "appointment_date": "2026-05-23",
        "appointment_time": "09:00",
        "duration_minutes": 60,
        "doctor_account_id": 2,
        "doctor_name": "???",
        "appointment_purpose": "???",
        "status": "???",
        "clinic_status": null,
        "check_in_time": null
      }
    ],
    "pageNum": 1,
    "pageSize": 10,
    "pages": 3
  }
}
```

---

## 四、治疗记录接口

### 4.1 查询治疗记录列表

- **方法**: GET
- **路径**: `/treatments/selectAll`
- **功能**: 多条件组合查询治疗记录

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| patientId | long | 否 | - | 患者ID |
| doctorAccountId | long | 否 | - | 医生账号ID |
| startDate | string | 否 | - | 开始日期，格式`yyyy-MM-dd` |
| endDate | string | 否 | - | 结束日期，格式`yyyy-MM-dd` |
| status | string | 否 | - | 治疗状态 |

#### curl示例

```bash
# 示例1: 查询全部治疗记录
curl -s "http://localhost:8080/treatments/selectAll?page=1&size=10"

# 示例2: 按患者筛选
curl -s "http://localhost:8080/treatments/selectAll?page=1&size=10&patientId=3"

# 示例3: 按日期范围筛选
curl -s "http://localhost:8080/treatments/selectAll?page=1&size=10&startDate=2026-05-01&endDate=2026-05-31"

# 示例4: 组合筛选（医生+日期范围）
curl -s "http://localhost:8080/treatments/selectAll?page=1&size=10&doctorAccountId=3&startDate=2026-05-01&endDate=2026-05-31"
```

---

## 五、财务记录接口

### 5.1 查询财务记录列表

- **方法**: GET
- **路径**: `/finances/selectAll`
- **功能**: 多条件组合查询财务记录（合并了原selectBytype和selectBydate能力）

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| recordType | string | 否 | - | 收支类型，如`income` |
| startDate | string | 否 | - | 开始日期，格式`yyyy-MM-dd` |
| endDate | string | 否 | - | 结束日期，格式`yyyy-MM-dd` |
| patientId | long | 否 | - | 患者ID |
| keyword | string | 否 | - | 关键词（匹配名称或备注） |

#### curl示例

```bash
# 示例1: 查询全部财务记录
curl -s "http://localhost:8080/finances/selectAll?page=1&size=10"

# 示例2: 按类型筛选（收入）
curl -s "http://localhost:8080/finances/selectAll?page=1&size=10&recordType=income"

# 示例3: 按日期范围筛选
curl -s "http://localhost:8080/finances/selectAll?page=1&size=10&startDate=2026-05-01&endDate=2026-05-31"

# 示例4: 组合筛选（类型+日期范围+患者）
curl -s "http://localhost:8080/finances/selectAll?page=1&size=10&recordType=income&startDate=2026-05-01&endDate=2026-05-31&patientId=3"
```

---

## 六、库存接口

### 6.1 查询库存列表

- **方法**: GET
- **路径**: `/inventory/selectAll`
- **功能**: 多条件组合查询库存物品

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| category | string | 否 | - | 物品分类 |
| brand | string | 否 | - | 品牌 |
| supplier | string | 否 | - | 供应商 |
| keyword | string | 否 | - | 关键词（匹配产品名、分类、品牌） |

#### curl示例

```bash
# 示例1: 查询全部库存
curl -s "http://localhost:8080/inventory/selectAll?page=1&size=10"

# 示例2: 按分类筛选
curl -s "http://localhost:8080/inventory/selectAll?page=1&size=10&category=%E8%A1%A5%E7%89%99%E6%9D%90%E6%96%99"

# 示例3: 按品牌筛选
curl -s "http://localhost:8080/inventory/selectAll?page=1&size=10&brand=3M"

# 示例4: 关键词搜索
curl -s "http://localhost:8080/inventory/selectAll?page=1&size=10&keyword=3M"
```

---

## 七、患者接口

### 7.1 搜索患者

- **方法**: GET
- **路径**: `/patients/search`
- **功能**: 多条件组合搜索患者

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| keyword | string | 否 | - | 搜索关键词（姓名、电话、拼音） |
| gender | string | 否 | - | 性别：男/女 |
| ageMin | int | 否 | - | 最小年龄 |
| ageMax | int | 否 | - | 最大年龄 |
| customerSource | string | 否 | - | 客户来源 |
| hasArrears | boolean | 否 | - | 是否有欠款（在Service层过滤） |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数 |

#### curl示例

```bash
# 示例1: 查询全部患者
curl -s "http://localhost:8080/patients/search?page=1&size=20"

# 示例2: 按性别筛选
curl -s "http://localhost:8080/patients/search?page=1&size=20&gender=%E7%94%B7"

# 示例3: 按年龄范围筛选
curl -s "http://localhost:8080/patients/search?page=1&size=20&ageMin=30&ageMax=50"

# 示例4: 组合筛选（性别+年龄+关键词）
curl -s "http://localhost:8080/patients/search?page=1&size=20&gender=%E7%94%B7&ageMin=30&ageMax=50&keyword=%E5%BC%A0"
```

---

## 八、医生排班接口

### 8.1 查询医生排班列表

- **方法**: GET
- **路径**: `/doctors/selectAll`
- **功能**: 多条件组合查询医生排班

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | 排班状态，如`working` |
| scheduleDate | string | 否 | - | 排班日期，格式`yyyy-MM-dd` |

#### curl示例

```bash
# 示例1: 查询全部排班
curl -s "http://localhost:8080/doctors/selectAll?page=1&size=10"

# 示例2: 按状态筛选
curl -s "http://localhost:8080/doctors/selectAll?page=1&size=10&status=working"

# 示例3: 按日期筛选
curl -s "http://localhost:8080/doctors/selectAll?page=1&size=10&scheduleDate=2026-05-08"

# 示例4: 组合筛选
curl -s "http://localhost:8080/doctors/selectAll?page=1&size=10&status=working&scheduleDate=2026-05-08"
```

---

## 九、索引优化建议

为支持新增查询条件，建议在以下字段添加数据库索引：

```sql
-- 预约表
ALTER TABLE appointment ADD INDEX idx_appointment_date (appointment_date);
ALTER TABLE appointment ADD INDEX idx_doctor_account_id (doctor_account_id);
ALTER TABLE appointment ADD INDEX idx_status (status);

-- 治疗表
ALTER TABLE treatment ADD INDEX idx_patient_id (patient_id);
ALTER TABLE treatment ADD INDEX idx_doctor_account_id (doctor_account_id);
ALTER TABLE treatment ADD INDEX idx_treatment_date (treatment_date);

-- 财务表
ALTER TABLE finances ADD INDEX idx_date (date);
ALTER TABLE finances ADD INDEX idx_type (type);
ALTER TABLE finances ADD INDEX idx_patient_id (patient_id);

-- 库存表
ALTER TABLE inventory ADD INDEX idx_category (category);
ALTER TABLE inventory ADD INDEX idx_brand (brand);
ALTER TABLE inventory ADD INDEX idx_supplier (supplier);

-- 患者表
ALTER TABLE patients ADD INDEX idx_gender (gender);
ALTER TABLE patients ADD INDEX idx_age (age);
ALTER TABLE patients ADD INDEX idx_customer_source (customer_source);

-- 医生排班表
ALTER TABLE doctors ADD INDEX idx_status (status);
ALTER TABLE doctors ADD INDEX idx_schedule_date (schedule_date);
```

---

---

## 十、开放数据接口（Open API）

基础路径：`/api/open/clinic/{clinicId}`

认证方式：请求头 `X-API-Key: sk-saas-xxx`

以下接口按业务模块分类列出。

---

### 10.1 经营分析

#### 10.1.1 获取经营统计（日报/周报/月报）

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/business-stats`
- **功能**: 获取最新经营日报/周报/月报（字段已精简，移除元数据）

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| period | string | 否 | day | 统计周期：`day` / `week` / `month` |

**curl 示例**

```bash
curl -s -H "X-API-Key: sk-saas-xxx" "http://localhost:8080/api/open/clinic/1/business-stats?period=day"
```

---

#### 10.1.2 获取最新经营日报（原始完整视图）

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/business-analysis/latest`

---

#### 10.1.3 获取最新经营周报

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/business-analysis/weekly/latest`

---

#### 10.1.4 获取最新经营月报

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/business-analysis/monthly/latest`

---

#### 10.1.5 获取指定日期原始经营指标

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/daily-metrics`
- **功能**: 返回精简后的每日经营指标（17个核心字段）

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| date | string | 否 | 昨日 | 日期，格式 `yyyy-MM-dd` |

**curl 示例**

```bash
curl -s -H "X-API-Key: sk-saas-xxx" "http://localhost:8080/api/open/clinic/1/daily-metrics?date=2026-05-24"
```

**返回字段（精简后）**

| 字段名 | 说明 |
|--------|------|
| date | 日期 |
| day_of_week | 星期 |
| appointment_count | 预约总数 |
| appointment_unique_patient_count | 预约独立患者数（去重后实际接诊人数，含新老患者） |
| appointment_completed_count | 已完成预约数 |
| appointment_cancelled_count | 已取消预约数 |
| total_income | 总收入 |
| total_expense | 总支出 |
| net_profit | 净利润 |
| new_patient_income | 新患收入（当前未拆分，固定为0） |
| old_patient_income | 老患收入（当前未拆分，固定为0） |
| arrears_amount | 欠款金额 |
| visit_patient_count | 今日接诊患者数（去重，含新老患者） |
| returning_visit_count | 复诊人次（预约总数 - 独立患者数） |
| total_visit_count | 总就诊次数（病历数） |
| registration_count | 登记次数（预约数） |
| top_doctors | 热门医生排行 |
| top_projects | 热门项目排行 |
| consultation_count | 咨询数 |
| deal_count | 成交数 |

---

### 10.2 患者相关

#### 10.2.1 获取患者列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/patients`
- **功能**: 支持分页和姓名模糊匹配

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| name | string | 否 | - | 患者姓名（模糊匹配） |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

---

#### 10.2.2 获取患者基础详情

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/patients/{patientId}/details`

---

#### 10.2.3 获取患者病历列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/patients/{patientId}/medical-records`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

---

#### 10.2.4 获取患者风险标签

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/patients/{patientId}/risk-tags`

---

#### 10.2.5 获取患者影像列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/patients/{patientId}/images`

---

#### 10.2.6 获取患者时间轴

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/patients/{patientId}/timeline`

---

#### 10.2.7 获取患者洞察摘要

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/patients/{patientId}/insight`

---

#### 10.2.8 患者工作台查询

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/patients/workbench`

---

### 10.3 预约

#### 10.3.1 查询预约列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/appointments`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | 预约状态 |
| appointmentDate | string | 否 | - | 预约日期 `yyyy-MM-dd` |
| startDate | string | 否 | - | 日期范围起始 |
| endDate | string | 否 | - | 日期范围截止 |
| doctorAccountId | long | 否 | - | 医生账号ID |

---

### 10.4 回访

#### 10.4.1 获取回访列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/followups`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| patientName | string | 否 | - | 患者姓名（模糊匹配） |
| startDate | string | 否 | - | 回访日期起始 `yyyy-MM-dd` |
| endDate | string | 否 | - | 回访日期截止 `yyyy-MM-dd` |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

---

### 10.5 医生

#### 10.5.1 查询医生排班列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/doctors`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| status | string | 否 | - | 排班状态 |
| scheduleDate | string | 否 | - | 排班日期 `yyyy-MM-dd` |

---

#### 10.5.2 医生业绩统计

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/finances/doctor-performance`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| startDate | string | 否 | - | 起始日期 |
| endDate | string | 否 | - | 截止日期 |
| doctorAccountId | long | 否 | - | 医生账号ID |
| doctorName | string | 否 | - | 医生姓名 |

---

### 10.6 财务

#### 10.6.1 查询财务流水明细

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/finances`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| startDate | string | 否 | - | 起始日期 |
| endDate | string | 否 | - | 截止日期 |
| type | string | 否 | - | 收支类型：`收入` / `支出` |
| patientId | long | 否 | - | 患者ID |
| keyword | string | 否 | - | 关键词（匹配名称或备注） |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

---

### 10.7 病历

#### 10.7.1 查询全量病历列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/medical-records`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| startDate | string | 否 | - | 就诊日期起始 |
| endDate | string | 否 | - | 就诊日期截止 |
| doctorAccountId | long | 否 | - | 医生账号ID |
| status | string | 否 | - | 病历状态 |
| keyword | string | 否 | - | 关键词（匹配患者姓名） |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

---

### 10.8 治疗

#### 10.8.1 查询治疗记录列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/treatments`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |
| patientId | long | 否 | - | 患者ID |
| doctorAccountId | long | 否 | - | 医生账号ID |
| startDate | string | 否 | - | 起始日期 |
| endDate | string | 否 | - | 截止日期 |
| status | string | 否 | - | 治疗状态 |

---

### 10.9 耗材

#### 10.9.1 查询耗材列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/materials`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| keyword | string | 否 | - | 关键词 |
| categoryId | long | 否 | - | 分类ID |
| lowStockOnly | boolean | 否 | false | 仅低库存 |
| status | string | 否 | - | 状态 |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页条数 |

---

### 10.10 义齿加工

#### 10.10.1 查询义齿加工订单列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/lab-orders`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| startDate | string | 否 | - | 下单日期起始 |
| endDate | string | 否 | - | 下单日期截止 |
| status | string | 否 | - | 订单状态 |
| factoryId | long | 否 | - | 加工厂ID |
| keyword | string | 否 | - | 关键词（患者姓名、订单ID、项目、产品） |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

---

#### 10.10.2 查询加工厂列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/lab-factories`

---

### 10.11 咨询

#### 10.11.1 查询咨询记录列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/consultations`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| startDate | string | 否 | - | 咨询日期起始 `yyyy-MM-dd` |
| endDate | string | 否 | - | 咨询日期截止 `yyyy-MM-dd` |
| channel | string | 否 | - | 咨询渠道 |
| intentLevel | string | 否 | - | 意向等级 |
| handlingResult | string | 否 | - | 跟进结果 |
| keyword | string | 否 | - | 关键词 |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

---

#### 10.11.2 查询指定咨询的跟进记录

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/consultations/{id}/followups`

---

### 10.12 耗材采购

#### 10.12.1 查询耗材采购单列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/material-purchases`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| startDate | string | 否 | - | 采购日期起始 |
| endDate | string | 否 | - | 采购日期截止 |
| supplierName | string | 否 | - | 供应商名称（模糊） |
| status | string | 否 | - | 采购状态 |
| keyword | string | 否 | - | 关键词（供应商或备注） |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

---

### 10.13 治疗项目

#### 10.13.1 查询治疗项目目录

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/treatment-catalog`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| enabledOnly | boolean | 否 | false | 是否只返回启用状态 |

---

### 10.14 广告花费

#### 10.14.1 查询广告花费列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/advertising-spending`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| startDate | string | 否 | - | 投放开始日期起始 |
| endDate | string | 否 | - | 投放结束日期截止 |
| platform | string | 否 | - | 投放平台 |
| keyword | string | 否 | - | 关键词（平台、活动名、备注） |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

---

### 10.15 患者分组

#### 10.15.1 查询患者分组列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/patient-groups`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| keyword | string | 否 | - | 关键词（分组名称或标识） |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

---

### 10.16 库存

#### 10.16.1 查询库存物品列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/inventory`

**请求参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| category | string | 否 | - | 物品分类 |
| brand | string | 否 | - | 品牌 |
| supplier | string | 否 | - | 供应商 |
| keyword | string | 否 | - | 关键词（产品名、分类、品牌） |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

---

### 10.17 收费渠道

#### 10.17.1 查询收费渠道列表

- **方法**: GET
- **路径**: `/api/open/clinic/{clinicId}/payment-channels`

---

## 十一、版本变更记录

| 版本 | 日期 | 变更内容 |
|---|---|---|
| v2.0 | 2026-05-25 | 新增开放数据接口（Open API）章节，涵盖 Phase 1~4 全部 19 个新接口；更新 `/business-stats` 和 `/daily-metrics` 字段精简说明；补充 `X-API-Key` 认证和开放 API 基础路径说明 |
| v1.0 | 2026-05-23 | 初始版本，包含内部业务接口 |

*文档结束*
