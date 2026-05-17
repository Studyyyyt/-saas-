# 系统API接口清单

> 本文档汇总口腔门诊SaaS管理系统后端所有RESTful API接口，按业务模块分组整理。
> 基准路径：`http://localhost:8080`（Docker开发环境）

---

## 目录

1. [通用规范](#通用规范)
2. [认证与账号管理](#认证与账号管理)
3. [患者管理](#患者管理)
4. [预约管理](#预约管理)
5. [病历管理](#病历管理)
6. [治疗管理](#治疗管理)
7. [财务管理](#财务管理)
8. [库存与采购](#库存与采购)
9. [加工单管理](#加工单管理)
10. [回访与咨询](#回访与咨询)
11. [医生与排班](#医生与排班)
12. [保险管理](#保险管理)
13. [AI功能](#ai功能)
14. [系统配置](#系统配置)
15. [开放与Webhook](#开放与webhook)

---

## 通用规范

### 统一返回结构

所有接口统一返回 `Result` 包装结构：

```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | String | 状态码，`200` 为成功，`500` 为系统错误，其他为业务错误 |
| msg | String | 提示信息 |
| data | Object | 响应数据（可为null、对象或数组） |

### 特殊说明

- **鉴权**：登录后由前端维护登录状态，后端目前无JWT/Session过滤器，依赖前端路由控制。
- **管理员二级密码**：删除患者、删除预约、删除财务等敏感操作需在请求头中携带 `X-Secondary-Password`。
- **操作人标识**：部分写操作需在请求头中携带 `X-Operator-Account-Id`。
- **遗留接口**：极少数早期接口（如 `/finances/update`）直接返回字符串，非 `Result` 包装。

---

## 认证与账号管理

### 认证（Auth）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| POST | `/auth/login` | 用户登录（用户名+密码），返回账号信息及菜单权限 |

### 账号管理（Account）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/accounts/search` | 分页查询账号列表 |
| GET | `/accounts/doctors/active` | 获取在职医生账号列表 |
| GET | `/accounts/selectByid` | 根据ID查询账号 |
| GET | `/accounts/selectByname` | 根据姓名查询账号 |
| POST | `/accounts/add` | 新增账号 |
| PUT | `/accounts/edit` | 编辑账号 |
| DELETE | `/accounts/delete/{id}` | 删除账号 |
| DELETE | `/accounts/deleteBatch` | 批量删除账号 |

---

## 患者管理

### 患者（Patient）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/patients/selectAll` | 分页查询全部患者 |
| GET | `/patients/selectAllForH5` | 查询全部患者（供H5使用） |
| GET | `/patients/selectByid` | 根据ID查询患者 |
| GET | `/patients/selectByname` | 根据姓名查询患者 |
| GET | `/patients/search` | 关键词搜索患者（姓名/手机号） |
| GET | `/patients/workbench` | 患者工作台查询 |
| GET | `/patients/workbench/export` | 导出患者工作台数据（需管理员二级密码） |
| POST | `/patients/add` | 新增患者 |
| PUT | `/patients/edit` | 编辑患者 |
| DELETE | `/patients/delete/{id}` | 删除患者（需管理员二级密码） |
| DELETE | `/patients/deleteBatch` | 批量删除患者（需管理员二级密码） |

### 患者详情（PatientDetail）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/patient-details/overview/{patientId}` | 患者全景视图（基础信息+病历+随访+影像+预约+治疗等） |
| GET | `/patient-details/basic/{patientId}` | 患者基础信息+费用统计 |
| GET | `/patient-details/medical-records/{patientId}` | 患者病历列表+待处理化验单计数 |
| GET | `/patient-details/timeline/{patientId}` | 患者时间轴 |
| GET | `/patient-details/appointments/{patientId}` | 患者预约列表 |
| GET | `/patient-details/treatments/{patientId}` | 患者治疗处置列表（含费用） |
| GET | `/patient-details/images/{patientId}` | 患者影像列表 |
| GET | `/patient-details/consents/{patientId}` | 患者知情同意书列表 |
| GET | `/patient-details/followups/{patientId}` | 患者随访记录+下次随访时间 |
| GET | `/patient-details/insight/{patientId}` | 患者洞察摘要+转介绍记录 |
| GET | `/patient-details/risk-tags/{patientId}` | 患者风险标签 |

### 患者分组（PatientCustomGroup）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| POST | `/patient-groups/add` | 新增患者自定义分组 |
| POST | `/patient-groups/assign` | 分配患者到分组 |

### 患者影像（PatientImage）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/patient-images/selectByPatientId` | 根据患者ID查询影像 |
| POST | `/patient-images/upload` | 上传患者影像 |
| GET | `/patient-images/file/{id}` | 读取影像文件流 |
| POST | `/patient-images/send/{id}` | 标记影像已发送给患者 |
| DELETE | `/patient-images/delete/{id}` | 删除影像 |

### 患者洞察（PatientInsight）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/patient-insights/overview` | 患者洞察总览 |

### 风险标签（RiskTag）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/risk-tags/selectByPatientId` | 查询患者风险标签 |
| POST | `/risk-tags/add` | 新增风险标签 |
| PUT | `/risk-tags/edit` | 编辑风险标签 |
| DELETE | `/risk-tags/delete/{id}` | 删除风险标签 |

### 知情同意书（PatientConsent）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/patient-consent/selectByPatientId` | 查询患者知情同意书 |
| GET | `/patient-consent/detail/{id}` | 知情同意书详情 |
| POST | `/patient-consent/issue` | 签发知情同意书 |

### 知情同意书模板（ConsentTemplate）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/consent-template/selectAll` | 查询全部模板 |
| GET | `/consent-template/selectEnabled` | 查询启用的模板 |
| POST | `/consent-template/add` | 新增模板 |
| PUT | `/consent-template/edit` | 编辑模板 |
| DELETE | `/consent-template/delete/{id}` | 删除模板 |

---

## 预约管理

### 预约（Appointment）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/appointments/selectAll` | 分页查询预约列表（支持按状态过滤） |
| GET | `/appointments/scheduleEntries` | 查询日程条目 |
| GET | `/appointments/selectByid` | 根据ID查询预约 |
| GET | `/appointments/public/detail` | 公开预约详情（无需鉴权） |
| GET | `/appointments/selectByname` | 根据患者姓名查询预约 |
| PUT | `/appointments/updateStatus/{id}` | 更新预约状态 |
| PUT | `/appointments/updateClinicStatus/{id}` | 更新接诊状态 |
| POST | `/appointments/add` | 新增预约 |
| POST | `/appointments/manual-next-day-reminder` | 手动触发次日预约提醒 |
| PUT | `/appointments/edit` | 编辑预约 |
| POST | `/appointments/cancel/{id}` | 取消预约 |
| DELETE | `/appointments/delete/{id}` | 删除预约（需管理员二级密码） |
| DELETE | `/appointments/deleteBatch` | 批量删除预约（需管理员二级密码） |

---

## 病历管理

### 病历（MedicalRecord）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/medical-records/selectAll` | 分页查询病历列表（支持医生/状态/日期过滤） |
| GET | `/medical-records/selectByPatientId` | 根据患者ID查询病历 |
| GET | `/medical-records/selectByPatientName` | 根据患者姓名查询病历 |
| GET | `/medical-records/selectById` | 根据ID查询病历 |
| POST | `/medical-records/add` | 新增病历 |
| PUT | `/medical-records/edit` | 编辑病历 |
| DELETE | `/medical-records/delete/{id}` | 删除病历（需管理员二级密码） |

### 病历操作（MedicalRecordOperation）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/medical-record-operations/selectByMedicalRecordId` | 查询病历关联操作 |
| GET | `/medical-record-operations/pendingLabList` | 待处理化验单列表 |
| PUT | `/medical-record-operations/markSkip` | 标记跳过操作 |

### 病历模板（MedicalRecordTemplate）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/medical-record-templates/selectAll` | 查询全部病历模板 |
| GET | `/medical-record-templates/selectEnabled` | 查询启用的模板 |
| POST | `/medical-record-templates/add` | 新增模板 |
| PUT | `/medical-record-templates/edit` | 编辑模板 |
| DELETE | `/medical-record-templates/delete/{id}` | 删除模板 |

### 病历短语（MedicalRecordPhrase）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/medical-record-phrases/selectByFieldType` | 按字段类型查询短语 |
| GET | `/medical-record-phrases/selectAll` | 查询全部短语 |
| POST | `/medical-record-phrases/add` | 新增短语 |
| PUT | `/medical-record-phrases/edit` | 编辑短语 |
| DELETE | `/medical-record-phrases/delete/{id}` | 删除短语 |

### 病历AI扩写（MedicalRecordAI）

> 详细请求/响应参数说明见：[病历扩写API详解.md](./病历扩写API详解.md)

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/ai-config/medical-record` | 获取病历AI配置（由 `MedicalRecordAIConfigView.vue` 内嵌调用） |
| PUT | `/api/ai-config/medical-record` | 保存病历AI配置（由 `MedicalRecordAIConfigView.vue` 内嵌调用） |
| POST | `/api/ai/medical-record/expand` | （已废弃，返回410）病历AI扩写 |
| POST | `/api/ai-config/medical-record/preview` | 预览AI扩写Prompt |
| **POST** | **`/api/ai/proxy/medical-expand`** | **执行病历AI扩写（核心接口，前端实际调用）** |

---

## 治疗管理

### 治疗记录（Treatment）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/treatments/selectAll` | 分页查询治疗记录 |
| GET | `/treatments/recentByPatientId` | 查询患者最近治疗记录 |
| GET | `/treatments/selectByid` | 根据ID查询治疗记录 |
| GET | `/treatments/selectByname` | 根据患者姓名查询治疗记录 |
| POST | `/treatments/add` | 新增治疗记录 |
| POST | `/treatments/batchAdd` | 批量新增治疗记录 |
| PUT | `/treatments/edit` | 编辑治疗记录 |
| POST | `/treatments/charge/{id}` | 治疗收费 |
| POST | `/treatments/chargeBatch/{batchNo}` | 批量收费 |
| POST | `/treatments/refund/{id}` | 治疗退费 |
| DELETE | `/treatments/delete/{id}` | 删除治疗记录 |

### 治疗项目（TreatmentProject）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/treatment-projects/search` | 搜索治疗项目 |
| GET | `/treatment-projects/selectEnabled` | 查询启用的项目 |
| GET | `/treatment-projects/selectById` | 根据ID查询项目 |
| POST | `/treatment-projects/add` | 新增项目 |
| PUT | `/treatment-projects/edit` | 编辑项目 |
| DELETE | `/treatment-projects/delete/{id}` | 删除项目 |
| POST | `/treatment-projects/importBatch` | 批量导入项目 |

### 治疗项目分类（TreatmentProjectCategory）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/treatment-project-categories/tree` | 查询分类树 |
| GET | `/treatment-project-categories/selectEnabled` | 查询启用的分类 |
| POST | `/treatment-project-categories/add` | 新增分类 |
| PUT | `/treatment-project-categories/edit` | 编辑分类 |
| DELETE | `/treatment-project-categories/delete/{id}` | 删除分类 |

### 治疗操作（TreatmentOperation）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/treatment-operations/search` | 搜索治疗操作 |
| GET | `/treatment-operations/selectEnabled` | 查询启用的操作 |
| GET | `/treatment-operations/selectById` | 根据ID查询操作 |
| POST | `/treatment-operations/add` | 新增操作 |
| PUT | `/treatment-operations/edit` | 编辑操作 |
| DELETE | `/treatment-operations/delete/{id}` | 删除操作 |
| POST | `/treatment-operations/importBatch` | 批量导入操作 |

### 治疗目录（TreatmentCatalog）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/treatment-catalog/selectAll` | 查询全部治疗目录 |
| GET | `/treatment-catalog/selectEnabled` | 查询启用的目录 |
| POST | `/treatment-catalog/add` | 新增目录项 |
| PUT | `/treatment-catalog/edit` | 编辑目录项 |
| DELETE | `/treatment-catalog/delete/{id}` | 删除目录项 |

### 治疗场景（TreatmentScene）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/treatment-scenes` | 查询全部治疗场景 |
| GET | `/api/treatment-scenes/enabled` | 查询启用的场景 |
| GET | `/api/treatment-scenes/{id}` | 场景详情 |
| POST | `/api/treatment-scenes` | 保存场景（含步骤） |
| DELETE | `/api/treatment-scenes/{id}` | 删除场景 |
| GET | `/api/treatment-scenes/{id}/steps` | 查询场景步骤 |

### 治疗计划（TreatmentPlan）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/treatment-plans/selectAll` | 查询全部治疗计划 |

---

## 财务管理

### 财务记录（Finance）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/finances/selectAll` | 分页查询全部财务记录 |
| GET | `/finances/recentByPatientId` | 查询患者最近财务记录 |
| GET | `/finances/selectById` | 根据ID查询财务 |
| GET | `/finances/selectByName` | 根据姓名查询财务 |
| GET | `/finances/selectByamount` | 根据金额查询财务 |
| GET | `/finances/selectBytype` | 根据类型查询财务 |
| GET | `/finances/selectBydate` | 根据日期查询财务 |
| GET | `/finances/selectByMonth` | 按月查询财务 |
| GET | `/finances/doctorPerformance` | 医生业绩统计 |
| GET | `/finances/expenseOverview` | 费用概览 |
| GET | `/finances/manualExpenseSearch` | 手动支出搜索 |
| POST | `/finances/manualExpense/add` | 新增手动支出 |
| PUT | `/finances/manualExpense/edit` | 编辑手动支出 |
| DELETE | `/finances/manualExpense/delete/{id}` | 删除手动支出（需管理员二级密码） |
| GET | `/finances/select1Byid` | 按月+ID查询财务 |
| GET | `/finances/select1Byamount` | 按月+金额查询财务 |
| GET | `/finances/select1Byname` | 按月+姓名查询财务 |
| GET | `/finances/select1Bydate` | 按月+日期查询财务 |
| GET | `/finances/select1Bytype` | 按月+类型查询财务 |
| POST | `/finances/add` | 新增财务记录 |
| PUT | `/finances/edit` | 编辑财务记录 |
| PUT | `/finances/update` | 更新财务记录（返回字符串） |
| DELETE | `/finances/delete/{id}` | 删除财务记录（需管理员二级密码） |

### 收款渠道（PaymentChannel）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/payment-channels/selectAll` | 查询全部收款渠道 |
| GET | `/payment-channels/selectEnabled` | 查询启用的渠道 |
| POST | `/payment-channels/add` | 新增收款渠道 |
| PUT | `/payment-channels/edit` | 编辑收款渠道 |
| DELETE | `/payment-channels/delete/{id}` | 删除收款渠道 |

---

## 库存与采购

### 库存管理（Inventory）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/inventory/selectAll1` | 查询全部库存（不分页） |
| GET | `/inventory/selectAll` | 分页查询全部库存 |
| GET | `/inventory/selectByid` | 根据ID查询库存 |
| GET | `/inventory/selectByname` | 根据名称查询库存 |
| GET | `/inventory/selectBycategory` | 根据分类查询库存 |
| GET | `/inventory/selectBybrand` | 根据品牌查询库存 |
| GET | `/inventory/selectBysupplier` | 根据供应商查询库存 |
| POST | `/inventory/add` | 新增库存 |
| PUT | `/inventory/edit` | 编辑库存 |
| DELETE | `/inventory/delete/{id}` | 删除库存 |
| DELETE | `/inventory/deleteBatch` | 批量删除库存 |
| PUT | `/inventory/update/{id}` | 更新库存数量 |
| POST | `/inventory/addBatch` | 批量新增库存 |
| GET | `/inventory/selectLowStock` | 查询低库存 |
| GET | `/inventory/selectById` | 查询库存明细（另一套逻辑） |
| GET | `/inventory/selectByName` | 按名称查询库存（另一套逻辑） |
| GET | `/inventory/select1Bycategory` | 按分类查询（另一套逻辑） |
| GET | `/inventory/select1Bybrand` | 按品牌查询（另一套逻辑） |
| GET | `/inventory/select1Bysupplier` | 按供应商查询（另一套逻辑） |

### 采购管理（Purchase）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/purchases/selectAll` | 分页查询采购记录 |
| GET | `/purchases/selectByid` | 根据ID查询采购 |
| GET | `/purchases/selectByname` | 根据名称查询采购 |
| GET | `/purchases/selectBycategory` | 根据分类查询采购 |
| GET | `/purchases/selectBybrand` | 根据品牌查询采购 |
| GET | `/purchases/selectBysupplier` | 根据供应商查询采购 |
| POST | `/purchases/add` | 新增采购 |
| DELETE | `/purchases/delete/{id}` | 删除采购 |
| DELETE | `/purchases/deleteBatch` | 批量删除采购 |
| PUT | `/purchases/updateStatus` | 更新采购状态 |

### 耗材管理（Material）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/materials/selectAll` | 搜索耗材（关键词/分类/低库存/状态） |
| GET | `/materials/searchLite` | 轻量搜索耗材 |
| GET | `/materials/{id}` | 耗材详情 |
| POST | `/materials/add` | 新增耗材 |
| PUT | `/materials/edit` | 编辑耗材 |

### 耗材分类（MaterialCategory）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/material-categories/tree` | 查询分类树 |
| GET | `/material-categories/selectEnabled` | 查询启用的分类 |
| POST | `/material-categories/add` | 新增分类 |
| PUT | `/material-categories/edit` | 编辑分类 |
| DELETE | `/material-categories/delete/{id}` | 删除分类 |

### 耗材采购（MaterialPurchase）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/material-purchases/search` | 搜索采购单 |
| GET | `/material-purchases/{id}` | 采购单详情 |
| POST | `/material-purchases/add` | 新增采购单 |
| PUT | `/material-purchases/edit` | 编辑采购单 |
| POST | `/material-purchases/void/{id}` | 作废采购单 |
| POST | `/material-purchases/uploadInvoice` | 上传发票 |
| GET | `/material-purchases/invoice/{purchaseId}` | 下载发票文件 |

### 耗材统计（MaterialStatistics）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/material-statistics/overview` | 耗材统计概览 |

---

## 加工单管理

### 加工单（LabOrder）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/lab-orders/dashboard/overview` | 加工单概览 |
| GET | `/lab-orders/search` | 搜索加工单 |
| GET | `/lab-orders/{id}` | 加工单详情 |
| POST | `/lab-orders/add` | 新增加工单 |
| PUT | `/lab-orders/edit` | 编辑加工单 |
| POST | `/lab-orders/batchStatus` | 批量更新加工单状态 |
| DELETE | `/lab-orders/delete/{id}` | 删除加工单 |

### 加工厂（LabFactory）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/lab-factories/dashboard/overview` | 加工厂概览 |
| GET | `/lab-factories/search` | 搜索加工厂 |
| GET | `/lab-factories/selectEnabled` | 查询启用的加工厂 |
| GET | `/lab-factories/{id}` | 加工厂详情 |
| POST | `/lab-factories/add` | 新增加工厂 |
| PUT | `/lab-factories/edit` | 编辑加工厂 |
| DELETE | `/lab-factories/delete/{id}` | 删除加工厂 |
| GET | `/lab-factories/{factoryId}/products` | 查询工厂产品 |
| POST | `/lab-factories/{factoryId}/products/add` | 新增工厂产品 |
| PUT | `/lab-factories/{factoryId}/products/edit` | 编辑工厂产品 |
| POST | `/lab-factories/{factoryId}/products/batchSave` | 批量保存产品 |
| DELETE | `/lab-factories/{factoryId}/products/delete/{productId}` | 删除工厂产品 |
| GET | `/lab-factories/{factoryId}/templates` | 查询工厂账单模板 |
| POST | `/lab-factories/{factoryId}/templates/add` | 新增账单模板 |
| PUT | `/lab-factories/{factoryId}/templates/edit` | 编辑账单模板 |
| DELETE | `/lab-factories/{factoryId}/templates/delete/{templateId}` | 删除账单模板 |

### 加工账单（LabBill）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/lab-bills/search` | 搜索账单 |
| GET | `/lab-bills/{id}` | 账单详情 |
| POST | `/lab-bills/import` | 导入账单 |
| PUT | `/lab-bills/items/{id}/resolution` | 更新账单项目对账 |
| PUT | `/lab-bills/unmatched-orders/{id}/resolution` | 更新未匹配订单对账 |
| POST | `/lab-bills/confirm/{id}` | 确认账单 |
| GET | `/lab-bills/file/{id}` | 下载账单文件 |

### 加工统计（LabStatistics）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/lab-statistics/overview` | 加工统计概览 |

---

## 回访与咨询

### 回访管理（Followup）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/followups/selectAll` | 分页查询回访记录 |
| GET | `/followups/selectAllDetail` | 查询全部回访详情 |
| GET | `/followups/selectByPatientId` | 根据患者ID查询回访 |
| POST | `/followups/add` | 新增回访 |
| PUT | `/followups/edit` | 编辑回访 |
| DELETE | `/followups/delete/{id}` | 删除回访 |

### 咨询记录（ConsultationRecord）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/consultations/search` | 搜索咨询记录 |
| GET | `/consultations/selectById` | 根据ID查询咨询 |
| GET | `/consultations/selectByPatientId` | 根据患者ID查询咨询 |
| POST | `/consultations/add` | 新增咨询 |
| PUT | `/consultations/edit` | 编辑咨询 |
| GET | `/consultations/matchPatientByPhone` | 根据手机号匹配患者 |
| GET | `/consultations/matchForPatientCreate` | 查询可关联患者的咨询 |
| POST | `/consultations/linkPatient` | 关联咨询到患者 |
| GET | `/consultations/{id}/followups` | 查询咨询跟进历史 |
| POST | `/consultations/aiAnalyze` | AI分析咨询（模拟数据） |
| GET | `/consultations/dashboard/overview` | 咨询概览（管理员） |
| GET | `/consultations/dashboard/funnel` | 咨询漏斗（管理员） |
| GET | `/consultations/dashboard/channelAnalysis` | 渠道分析（管理员） |
| GET | `/consultations/dashboard/projectAnalysis` | 项目分析（管理员） |
| GET | `/consultations/dashboard/hourHeatmap` | 时段热力图（管理员） |
| GET | `/consultations/dashboard/nursePerformance` | 客服业绩（管理员） |
| GET | `/consultations/dashboard/referralAnalysis` | 转介绍分析（管理员） |

### 咨询跟进（ConsultationFollowup）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/consultations/followups/list` | 查询跟进列表 |
| POST | `/consultations/followups/add` | 新增跟进 |
| DELETE | `/consultations/followups/delete/{id}` | 删除跟进 |

### 广告支出（AdvertisingSpending）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/advertising-spending/search` | 搜索广告支出 |
| POST | `/advertising-spending/add` | 新增广告支出 |
| PUT | `/advertising-spending/edit` | 编辑广告支出 |
| DELETE | `/advertising-spending/delete/{id}` | 删除广告支出 |
| GET | `/advertising-spending/dashboard/overview` | 广告支出概览 |

---

## 医生与排班

### 医生管理（Doctor）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/doctors/selectAll` | 分页查询医生列表 |
| GET | `/doctors/scheduleEntries` | 查询医生日程条目 |
| GET | `/doctors/selectByid` | 根据ID查询医生 |
| GET | `/doctors/selectByname` | 根据姓名查询医生 |
| PUT | `/doctors/updateStatus/{id}` | 更新医生状态 |
| POST | `/doctors/add` | 新增医生 |
| PUT | `/doctors/edit` | 编辑医生 |
| DELETE | `/doctors/delete/{id}` | 删除医生 |
| DELETE | `/doctors/deleteBatch` | 批量删除医生 |
| GET | `/doctors/schedules` | 按日期范围查询排班 |
| GET | `/doctors/shiftTemplates` | 查询排班模板 |
| POST | `/doctors/batchSave` | 批量保存排班 |

### 医生首页提醒（DoctorHomeReminder）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/doctor-home-reminders/dismissed` | 查询已关闭的提醒 |
| POST | `/doctor-home-reminders/dismiss` | 关闭提醒 |
| DELETE | `/doctor-home-reminders/dismissed` | 清除提醒关闭状态 |

---

## 保险管理

### 保险（Insurance）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/insurances/overview` | 保险概览 |
| GET | `/insurances/config` | 保险配置 |
| POST | `/insurances/config` | 保存保险配置 |
| GET | `/insurances/patient-profile/{patientId}` | 患者保险档案 |
| POST | `/insurances/patient-profile` | 保存患者保险档案 |
| GET | `/insurances/settlements` | 保险结算列表 |
| POST | `/insurances/settlements/draft` | 创建结算草稿 |
| GET | `/insurances/logs` | 保险日志 |
| POST | `/insurances/mock/settlement-payload` | 构建模拟结算数据 |

---

## AI功能

### AI配置（AiConfig）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/ai-config/overview` | AI配置概览 |
| GET | `/api/ai-config/functions` | AI功能列表 |
| PUT | `/api/ai-config/global` | 更新全局配置 |
| PUT | `/api/ai-config/functions/{key}` | 更新功能状态 |

### AI代理配置（AiAgentConfig）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/ai-agent-configs` | 查询代理配置列表 |
| GET | `/api/ai-agent-configs/{id}` | 代理配置详情 |
| GET | `/api/ai-agent-configs/by-key` | 按key查询配置 |
| POST | `/api/ai-agent-configs` | 创建代理配置 |
| PUT | `/api/ai-agent-configs/{id}` | 更新代理配置 |
| DELETE | `/api/ai-agent-configs/{id}` | 删除代理配置 |

### AI模型提供商（AiModelProvider）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/model-providers` | 获取当前模型提供商 |
| POST | `/api/model-providers` | 保存模型提供商 |
| POST | `/api/model-providers/test` | 测试连接 |
| DELETE | `/api/model-providers/{id}` | 删除模型提供商 |

### AI代理（AiProxy）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| POST | `/api/ai/proxy/{agentKey}` | 统一AI代理接口（支持SSE流式） |

### AI Hub（AiHub）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/ai/stream/{agentKey}` | SSE流式AI接口 |

### 经营分析（BusinessDailyAnalysis）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/business-analysis/latest` | 最新经营日报 |
| GET | `/business-analysis/history` | 经营分析历史 |
| GET | `/business-analysis/probe` | 测试模型连接 |
| GET | `/business-analysis/chat/session` | 获取聊天会话 |
| GET | `/business-analysis/chat/memory` | 聊天记忆（已废弃，返回空） |
| POST | `/business-analysis/chat/message` | 发送聊天消息 |
| POST | `/business-analysis/chat/stream` | 流式聊天（已废弃） |
| GET | `/business-analysis/weekly/latest` | 最新周报 |
| POST | `/business-analysis/weekly/run` | 手动运行周报 |
| GET | `/business-analysis/weekly/run/status` | 周报运行状态 |
| GET | `/business-analysis/monthly/latest` | 最新月报 |
| POST | `/business-analysis/monthly/run` | 手动运行月报 |
| GET | `/business-analysis/monthly/run/status` | 月报运行状态 |
| GET | `/business-analysis/alerts/recent` | 最近告警 |
| POST | `/business-analysis/alerts/scan` | 手动扫描告警 |
| GET | `/business-analysis/{id}` | 分析详情 |
| POST | `/business-analysis/run` | 手动运行日报 |
| GET | `/business-analysis/run/status` | 日报运行状态 |

---

## 系统配置

### 角色菜单权限（RoleMenuPermission）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/role-menu-permissions/overview` | 权限概览 |
| GET | `/role-menu-permissions/byRole` | 按角色查询权限 |
| POST | `/role-menu-permissions/save` | 保存权限配置 |

### API Key管理（ApiKey）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/api-key` | 获取当前诊所API Key |
| POST | `/api/api-key/regenerate` | 重新生成API Key |

### API文档（ApiDocumentation）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/docs` | 获取API文档列表 |

---

## 开放与Webhook

### 开放数据（OpenData）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/open/clinic/{clinicId}/business-stats` | 经营统计（供外部系统调用） |
| GET | `/api/open/clinic/{clinicId}/followups` | 回访列表（供外部系统调用） |
| GET | `/api/open/clinic/{clinicId}/patients` | 患者列表（供外部系统调用） |

### Webhook通知（WebhookNotification）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| POST | `/api/webhook/notify` | 通用通知（企业微信） |
| POST | `/api/webhook/ai-task` | AI任务状态通知 |

### 文件传输（FileTransfer）

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/file-transfer` | 文件传输页面（HTML） |
| POST | `/file-transfer/upload` | 上传文件 |
| GET | `/file-transfer/files/{filename}` | 下载文件 |

---

## 附录

### 状态码说明

| 状态码 | 含义 |
|--------|------|
| 200 | 请求成功 |
| 500 | 系统错误 |

### 需要管理员二级密码的操作

以下操作需在请求头中携带 `X-Secondary-Password`：

- 删除患者（单条/批量）
- 删除预约（单条/批量）
- 删除财务记录（单条/批量）
- 删除手动支出
- 导出患者工作台数据
