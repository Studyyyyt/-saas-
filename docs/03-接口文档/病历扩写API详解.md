# 病历扩写 API 详解

> 本文档详细说明病历 AI 扩写功能涉及的所有后端接口、请求参数与返回结构。

---

## 接口总览

| 序号 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 1 | GET | `/api/ai-config/medical-record` | 获取病历扩写配置 |
| 2 | PUT | `/api/ai-config/medical-record` | 保存病历扩写配置 |
| 3 | POST | `/api/ai-config/medical-record/preview` | 预览 Prompt（调试未保存配置） |
| 4 | POST | `/api/ai/proxy/medical-expand` | **执行病历 AI 扩写（核心接口）** |

> 旧接口 `POST /api/ai/medical-record/expand` 已废弃（返回 410），前端已统一迁移到 `/api/ai/proxy/medical-expand`。

---

## 1. 获取病历扩写配置

### 请求

```
GET /api/ai-config/medical-record
```

### 响应示例

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    "config": {
      "systemPrompt": "你是一位资深口腔科医生助手...",
      "emptyFieldStrategy": "skip",
      "model": "gpt-4o"
    },
    "fields": [
      {
        "id": 1,
        "fieldKey": "chief_complaint",
        "fieldName": "主诉",
        "enabled": true,
        "maxLength": 500,
        "required": true,
        "validationRule": "",
        "validationHint": "",
        "defaultValue": "",
        "sortOrder": 1
      }
    ],
    "fewShots": [
      {
        "id": 1,
        "input": "患者因牙痛就诊",
        "output": "主诉：右下后牙持续性跳痛3天，夜间加重..."
      }
    ]
  }
}
```

---

## 2. 保存病历扩写配置

### 请求

```
PUT /api/ai-config/medical-record
Content-Type: application/json
```

### 请求体（MedicalRecordAIConfigDTO）

```json
{
  "config": {
    "systemPrompt": "你是一位资深口腔科医生助手，请根据医生提供的草稿扩写为规范病历...",
    "emptyFieldStrategy": "skip",
    "model": "gpt-4o"
  },
  "fields": [
    {
      "id": 1,
      "fieldKey": "chief_complaint",
      "fieldName": "主诉",
      "enabled": true,
      "maxLength": 500,
      "required": true,
      "validationRule": "",
      "validationHint": "",
      "defaultValue": "",
      "sortOrder": 1
    }
  ],
  "fewShots": [
    {
      "id": 1,
      "input": "患者因牙痛就诊",
      "output": "主诉：右下后牙持续性跳痛3天..."
    }
  ]
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| config | Object | 是 | 全局配置对象，包含 systemPrompt、emptyFieldStrategy、model 等 |
| fields | Array | 是 | 扩写字段规则列表 |
| fields[].fieldKey | String | 是 | 字段标识（如 chief_complaint） |
| fields[].fieldName | String | 是 | 字段显示名称（如"主诉"） |
| fields[].enabled | Boolean | 否 | 是否启用该字段的扩写 |
| fields[].maxLength | Integer | 否 | 最大长度限制 |
| fields[].required | Boolean | 否 | 是否为必填项 |
| fields[].validationRule | String | 否 | 校验规则（正则） |
| fields[].validationHint | String | 否 | 校验失败提示语 |
| fields[].sortOrder | Integer | 否 | 排序号 |
| fewShots | Array | 否 | Few-shot 示例列表（**注意**：当前前端 `MedicalRecordAIConfigView.vue:646` 实际发送的 payload 中未包含 `fewShots`，若需保存 Few-shot 示例需同步修复前端） |
| fewShots[].input | String | 否 | 输入示例 |
| fewShots[].output | String | 否 | 输出示例 |

### 响应示例

```json
{
  "code": "200",
  "msg": "保存成功",
  "data": null
}
```

---

## 3. 预览 Prompt

用于在保存配置前，预览实际发送给 AI 的 Prompt 内容。

### 请求

```
POST /api/ai-config/medical-record/preview
Content-Type: application/json
```

### 请求体（TreatmentSceneExpandRequest）

```json
{
  "fields": {
    "chief_complaint": "牙痛",
    "present_illness_history": "患者3天前开始牙痛"
  },
  "sceneId": 1,
  "operations": ["拔牙", "根管治疗"],
  "testMode": false,
  "systemPrompt": "你是一位资深口腔科医生助手...",
  "fewShots": [
    { "input": "牙痛", "output": "主诉：右下后牙跳痛3天" }
  ],
  "emptyFieldStrategy": "skip"
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fields | Map<String,String> | 是 | 病历字段键值对 |
| sceneId | Long | 否 | 治疗场景ID |
| operations | Array<String> | 否 | 治疗操作列表 |
| testMode | Boolean | 否 | 是否为测试模式 |
| systemPrompt | String | 否 | 临时系统提示词（未保存时预览用） |
| fewShots | Array<Map> | 否 | 临时 Few-shot 示例 |
| emptyFieldStrategy | String | 否 | 空字段策略（skip/fill/keep） |

### 响应示例

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": "你是一位资深口腔科医生助手...\n\n主诉：牙痛..."
}
```

---

## 4. 执行病历 AI 扩写（核心接口）

前端点击"AI 扩写"按钮时，实际调用的是**统一 AI 代理接口**。

### 请求

```
POST /api/ai/proxy/medical-expand
Content-Type: application/json
```

### 请求体

```json
{
  "account_id": "1001",
  "fields": {
    "patient_id": "123",
    "patient_name": "张三",
    "doctor_account_id": "1001",
    "doctor_name": "李医生",
    "nurse_name": "王护士",
    "assistant_name": "",
    "visit_date": "2026-05-16 09:30:00",
    "record_type": "初诊",
    "chief_complaint": "牙痛",
    "present_illness_history": "3天前开始右下后牙疼痛",
    "past_medical_history": "无特殊",
    "infectious_history": "无",
    "allergy_history": "无",
    "general_condition": "良好",
    "examination_findings": "右下6远中龋坏，探诊敏感",
    "auxiliary_examination": "X线示龋坏近髓",
    "diagnosis": "右下6深龋",
    "treatment_plan": "根管治疗后冠修复",
    "treatment": "",
    "tooth_positions": "36",
    "medical_advice": "",
    "prescription": "",
    "record_tags": "",
    "image_summary": "",
    "notes": "",
    "record_status": "draft",
    "operation_items": [],
    "draft_record": "牙痛 3天前开始右下后牙疼痛"
  }
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account_id | String/Number | **是** | 当前操作用户ID，用于后端登录校验，缺失返回401 |
| fields | Object | 否 | 病历字段集合，透传给外部Webhook |
| fields.patient_id | String/Number | 否 | 患者ID（隐藏字段） |
| fields.patient_name | String | 否 | 患者姓名 |
| fields.doctor_account_id | String/Number | 否 | 接诊医生账号ID |
| fields.doctor_name | String | 否 | 接诊医生姓名 |
| fields.nurse_name | String | 否 | 护士姓名 |
| fields.assistant_name | String | 否 | 助理姓名 |
| fields.visit_date | String | 否 | 就诊时间（yyyy-MM-dd HH:mm:ss） |
| fields.record_type | String | 否 | 病历类型（初诊/复诊/急诊等） |
| fields.chief_complaint | String | 否 | 主诉 |
| fields.present_illness_history | String | 否 | 现病史 |
| fields.past_medical_history | String | 否 | 既往史。前端表单字段名为 `past_history`，发送时映射为 `past_medical_history` |
| fields.infectious_history | String | 否 | 传染病史 |
| fields.allergy_history | String | 否 | 过敏史 |
| fields.general_condition | String | 否 | 一般情况 |
| fields.examination_findings | String | 否 | 口腔检查。前端表单字段名为 `examination`，发送时映射为 `examination_findings` |
| fields.auxiliary_examination | String | 否 | 辅助检查 |
| fields.diagnosis | String | 否 | 诊断 |
| fields.treatment_plan | String | 否 | 治疗方案 |
| fields.treatment | String | 否 | 处置记录 |
| fields.tooth_positions | String | 否 | 牙位 |
| fields.medical_advice | String | 否 | 医嘱 |
| fields.prescription | String | 否 | 处方 |
| fields.record_tags | String | 否 | 病历标签 |
| fields.image_summary | String | 否 | 影像摘要 |
| fields.notes | String | 否 | 备注 |
| fields.record_status | String | 否 | 病历状态（draft/finalized） |
| fields.operation_items | Array | 否 | 操作项目列表（数组类型，如 `["拔牙", "根管治疗"]`） |
| fields.draft_record | String | 否 | 计算字段，由 `chief_complaint + ' ' + present_illness_history` 拼接而成，作为给AI的草稿提示 |

> **字段映射注意事项**：
> 1. 前端表单字段 `past_history` ↔ 发送/接收字段 `past_medical_history`
> 2. 前端表单字段 `examination` ↔ 发送/接收字段 `examination_findings`
> 3. 前端发送的字段名为下划线风格（如 `chief_complaint`），后端默认配置中 `MedicalRecordAIField.fieldKey` 使用驼峰风格（如 `chiefComplaint`），但 `AiProxyController` 直接透传 `Map`，不做转换，Webhook 实际收到的是下划线风格字段名。
> 4. `operation_items` 在前端为数组类型，后端 `AiProxyController` 使用 `Map<String, Object>` 接收，可正常透传。

### 响应示例

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    "chief_complaint": "主诉：右下后牙持续性跳痛3天，夜间加重...",
    "present_illness_history": "现病史：患者于3天前无明显诱因出现右下后牙疼痛...",
    "past_medical_history": "既往史：否认高血压、糖尿病等系统性疾病...",
    "examination_findings": "口腔检查：36牙远中邻面深龋，探诊敏感，冷测敏感...",
    "diagnosis": "诊断：36深龋伴可复性牙髓炎",
    "treatment_plan": "治疗计划：1. 36根管治疗；2. 术后全冠修复",
    "treatment": "处置：开髓引流，安抚治疗...",
    "medical_advice": "医嘱：避免患侧咀嚼，如有明显疼痛及时复诊...",
    "notes": "..."
  }
}
```

### 返回字段说明

AI 返回的字段名与请求中的 `fields` 字段名保持一致。前端根据返回字段自动回填表单：

| AI 返回字段 | 回填到前端表单字段 |
|------------|------------------|
| `chief_complaint` | `chief_complaint` |
| `present_illness_history` | `present_illness_history` |
| `past_medical_history` | `past_history` |
| `infectious_history` | `infectious_history` |
| `allergy_history` | `allergy_history` |
| `general_condition` | `general_condition` |
| `examination_findings` | `examination` |
| `auxiliary_examination` | `auxiliary_examination` |
| `diagnosis` | `diagnosis` |
| `treatment_plan` | `treatment_plan` |
| `treatment` | `treatment` |
| `medical_advice` | `medical_advice` |
| `prescription` | `prescription` |
| `record_tags` | `record_tags` |
| `image_summary` | `image_summary` |
| `notes` | `notes` |

> **回填规则**：只有 AI 返回的字段值非空时，才会覆盖医生已手动填写的内容。

---

## 数据流说明

```
前端 (MedicalRecordView.vue)
    │
    ▼
POST /api/ai/proxy/medical-expand
    │  { account_id, fields: {...} }
    ▼
后端 (AiProxyController)
    │  1. 校验 account_id（登录态）
    │  2. 校验 agentKey = "medical-expand" 白名单
    │  3. 检查 AI 功能是否启用
    │  4. 将请求包装为标准协议
    ▼
外部 Webhook（Dify / 其他工作流平台）
    │  接收标准协议，调用大模型生成扩写内容
    ▼
后端 (AiProxyController)
    │  对 Webhook 响应做解包处理：
    │  如果响应包含 {code, data}，提取内层 data
    ▼
前端
    │  res.data.data = { chief_complaint: "...", ... }
    │  自动回填到表单对应字段
```

---

## 错误处理

| 场景 | 返回码 | 说明 |
|------|--------|------|
| 未登录 | 401 | 请求体缺少 `account_id` |
| 非法 agentKey | 403 | `medical-expand` 不在白名单中 |
| AI 功能未启用 | 500 | 该诊所/账户未开启此 AI 功能 |
| Webhook 调用失败 | 500 | AI 代理调用失败 |
| 旧接口调用 | 410 | `POST /api/ai/medical-record/expand` 已废弃 |
