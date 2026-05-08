# 口腔 SaaS 管理系统 —— 后端接口清单

> 本文件由代码自动提取生成，记录系统当前所有 Controller 暴露的 REST API 接口。
> **用途**：
> 1. 前后端联调时快速查阅现有接口
> 2. 开发新功能前，先在此文件中补充"待开发接口"，再同步开发

---

## 使用规范

1. **新增功能时**：先在本文件底部【待开发接口预留区】补充接口契约，再开始写代码
2. **接口变更时**：同步更新本文件，保持与实际代码一致
3. **前端调用时**：优先查阅本文件确认路径和参数，避免硬编码猜测

---

## 一、登录认证

### loginController

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | /loginController/login | 用户登录认证 |

---

## 二、员工账号与权限

### AccountController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /accounts/search | 分页查询账号列表 |
| GET | /accounts/doctors/active | 查询在职医生账号 |
| GET | /accounts/selectByid | 按ID查询账号 |
| GET | /accounts/selectByname | 按姓名查询账号 |
| POST | /accounts/add | 新增账号 |
| PUT | /accounts/edit | 编辑账号 |
| DELETE | /accounts/delete/{id} | 删除账号 |
| DELETE | /accounts/deleteBatch | 批量删除账号 |

### RoleMenuPermissionController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /role-menu-permissions/overview | 查询权限概览 |
| GET | /role-menu-permissions/byRole | 按角色查询权限 |
| POST | /role-menu-permissions/save | 保存角色权限 |

### DoctorController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /doctors/selectAll | 分页查询所有医生 |
| GET | /doctors/scheduleEntries | 查询医生排班数据 |
| GET | /doctors/selectByid | 按ID查询医生 |
| GET | /doctors/selectByname | 按姓名查询医生 |
| PUT | /doctors/updateStatus/{id} | 更新医生状态 |
| POST | /doctors/add | 新增医生 |
| PUT | /doctors/edit | 编辑医生 |
| DELETE | /doctors/delete/{id} | 删除医生 |
| DELETE | /doctors/deleteBatch | 批量删除医生 |

### DoctorHomeReminderDismissalController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /doctor-home-reminders/dismissed | 查询已忽略的提醒 |
| POST | /doctor-home-reminders/dismiss | 忽略提醒 |
| DELETE | /doctor-home-reminders/dismissed | 清除忽略状态 |

---

## 三、患者管理

### PatientController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /patients/selectAll | 分页查询所有患者 |
| GET | /patients/selectAllForH5 | 查询所有患者（H5用） |
| GET | /patients/selectByid | 按ID查询患者 |
| GET | /patients/selectByname | 按姓名查询患者 |
| GET | /patients/search | 关键词搜索患者 |
| GET | /patients/workbench | 患者工作台查询 |
| GET | /patients/workbench/export | 导出患者工作台 |
| POST | /patients/add | 新增患者 |
| PUT | /patients/edit | 编辑患者 |
| PUT | /patients/bindWechat | 绑定微信 |
| DELETE | /patients/delete/{id} | 删除患者 |
| DELETE | /patients/deleteBatch | 批量删除患者 |

### Patient360Controller

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /patient360/overview/{patientId} | 完整患者360视图 |
| GET | /patient360/basic/{patientId} | 患者基础信息+费用+微信状态 |
| GET | /patient360/medical-records/{patientId} | 病历列表+待处理化验单计数 |
| GET | /patient360/timeline/{patientId} | 患者时间轴 |
| GET | /patient360/appointments/{patientId} | 预约列表 |
| GET | /patient360/treatments/{patientId} | 治疗处置列表（含费用） |
| GET | /patient360/images/{patientId} | 患者影像列表 |
| GET | /patient360/consents/{patientId} | 知情同意书列表 |
| GET | /patient360/followups/{patientId} | 随访记录+下次随访时间 |
| GET | /patient360/insight/{patientId} | 患者洞察摘要+转介绍记录 |
| GET | /patient360/risk-tags/{patientId} | 风险标签 |

### PatientCustomGroupController

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | /patient-groups/add | 新增患者分组 |
| POST | /patient-groups/assign | 分配患者到分组 |

### PatientRiskTagController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /risk-tags/selectByPatientId | 按患者ID查询风险标签 |
| POST | /risk-tags/add | 新增风险标签 |
| PUT | /risk-tags/edit | 编辑风险标签 |
| DELETE | /risk-tags/delete/{id} | 删除风险标签 |

### PatientInsightController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /patient-insights/overview | 患者洞察概览 |

### PatientImageController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /patient-images/selectByPatientId | 按患者ID查询影像 |
| POST | /patient-images/upload | 上传患者影像 |
| GET | /patient-images/file/{id} | 查看影像文件 |
| POST | /patient-images/send/{id} | 发送影像给患者 |
| DELETE | /patient-images/delete/{id} | 删除影像 |

### PatientFollowupController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /followup/selectAll | 分页查询所有随访 |
| GET | /followup/selectAllDetail | 查询所有随访详情 |
| GET | /followup/selectByPatientId | 按患者ID查询随访 |
| POST | /followup/add | 新增随访 |
| PUT | /followup/edit | 编辑随访 |
| DELETE | /followup/delete/{id} | 删除随访 |

### PatientConsentController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /patient-consent/selectByPatientId | 按患者ID查询知情同意书 |
| GET | /patient-consent/detail/{id} | 查询知情同意书详情 |
| POST | /patient-consent/issue | 发放知情同意书 |

### ConsentTemplateController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /consent-template/selectAll | 查询所有模板 |
| GET | /consent-template/selectEnabled | 查询启用的模板 |
| POST | /consent-template/add | 新增模板 |
| PUT | /consent-template/edit | 编辑模板 |
| DELETE | /consent-template/delete/{id} | 删除模板 |

---

## 四、预约管理

### AppointmentController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /appointments/selectAll | 分页查询所有预约（支持状态过滤） |
| GET | /appointments/scheduleEntries | 查询预约日程条目 |
| GET | /appointments/selectByid | 按ID查询预约 |
| GET | /appointments/public/detail | 公开预约详情 |
| GET | /appointments/selectByname | 按姓名查询预约 |
| PUT | /appointments/updateStatus/{id} | 更新预约状态 |
| POST | /appointments/add | 新增预约 |
| POST | /appointments/manual-next-day-reminder | 手动触发次日预约提醒 |
| PUT | /appointments/edit | 编辑预约 |
| POST | /appointments/cancel/{id} | 取消预约 |
| DELETE | /appointments/delete/{id} | 删除预约 |
| DELETE | /appointments/deleteBatch | 批量删除预约 |

---

## 五、病历管理

### MedicalRecordController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /medical-records/selectAll | 分页查询所有病历 |
| GET | /medical-records/selectByPatientId | 按患者ID查询病历 |
| GET | /medical-records/selectByPatientName | 按患者姓名查询病历 |
| GET | /medical-records/selectById | 按ID查询病历 |
| POST | /medical-records/add | 新增病历 |
| PUT | /medical-records/edit | 编辑病历 |
| DELETE | /medical-records/delete/{id} | 删除病历 |

### MedicalRecordTemplateController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /medical-record-templates/selectAll | 查询所有病历模板 |
| GET | /medical-record-templates/selectEnabled | 查询启用的病历模板 |
| POST | /medical-record-templates/add | 新增病历模板 |
| PUT | /medical-record-templates/edit | 编辑病历模板 |
| DELETE | /medical-record-templates/delete/{id} | 删除病历模板 |

### MedicalRecordOperationController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /medical-record-operations/selectByMedicalRecordId | 按病历ID查询操作 |
| GET | /medical-record-operations/pendingLabList | 查询待加工化验单列表 |
| PUT | /medical-record-operations/markSkip | 标记跳过操作 |

---

## 六、治疗与收费

### TreatmentController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /treatments/selectAll | 分页查询所有治疗 |
| GET | /treatments/recentByPatientId | 查询患者近期治疗 |
| GET | /treatments/selectByid | 按ID查询治疗 |
| GET | /treatments/selectByname | 按姓名查询治疗 |
| POST | /treatments/add | 新增治疗 |
| POST | /treatments/batchAdd | 批量新增治疗 |
| PUT | /treatments/edit | 编辑治疗 |
| POST | /treatments/charge/{id} | 治疗收费 |
| POST | /treatments/chargeBatch/{batchNo} | 批量收费 |
| POST | /treatments/refund/{id} | 治疗退费 |
| DELETE | /treatments/delete/{id} | 删除治疗 |

### Treatment_plansController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /treatment_plans/selectAll | 查询所有治疗方案 |

### TreatmentCatalogController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /treatment-catalog/selectAll | 查询所有治疗目录 |
| GET | /treatment-catalog/selectEnabled | 查询启用的治疗目录 |
| POST | /treatment-catalog/add | 新增治疗目录 |
| PUT | /treatment-catalog/edit | 编辑治疗目录 |
| DELETE | /treatment-catalog/delete/{id} | 删除治疗目录 |

### TreatmentProjectController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /treatment-projects/search | 搜索治疗项目 |
| GET | /treatment-projects/selectEnabled | 查询启用的治疗项目 |
| GET | /treatment-projects/selectById | 按ID查询治疗项目 |
| POST | /treatment-projects/add | 新增治疗项目 |
| PUT | /treatment-projects/edit | 编辑治疗项目 |
| DELETE | /treatment-projects/delete/{id} | 删除治疗项目 |
| POST | /treatment-projects/importBatch | 批量导入治疗项目 |

### TreatmentProjectCategoryController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /treatment-project-categories/tree | 查询分类树 |
| GET | /treatment-project-categories/selectEnabled | 查询启用的分类 |
| POST | /treatment-project-categories/add | 新增分类 |
| PUT | /treatment-project-categories/edit | 编辑分类 |
| DELETE | /treatment-project-categories/delete/{id} | 删除分类 |

### TreatmentOperationController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /treatment-operations/search | 搜索治疗操作 |
| GET | /treatment-operations/selectEnabled | 查询启用的操作 |
| GET | /treatment-operations/selectById | 按ID查询操作 |
| POST | /treatment-operations/add | 新增操作 |
| PUT | /treatment-operations/edit | 编辑操作 |
| DELETE | /treatment-operations/delete/{id} | 删除操作 |
| POST | /treatment-operations/importBatch | 批量导入操作 |

---

## 七、财务与收款

### FinanceController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /finances/all | 分页查询所有财务记录 |
| GET | /finances/recentByPatientId | 查询患者近期财务记录 |
| GET | /finances/selectByid | 按ID查询财务 |
| GET | /finances/selectByname | 按姓名查询财务 |
| GET | /finances/selectByamount | 按金额查询财务 |
| GET | /finances/selectBytype | 按类型查询财务 |
| GET | /finances/selectBydate | 按日期查询财务 |
| GET | /finances/selectByMonth | 按月查询财务 |
| GET | /finances/doctorPerformance | 医生业绩统计 |
| GET | /finances/expenseOverview | 支出概览 |
| GET | /finances/manualExpenseSearch | 搜索手工支出 |
| POST | /finances/manualExpense/add | 新增手工支出 |
| PUT | /finances/manualExpense/edit | 编辑手工支出 |
| DELETE | /finances/manualExpense/delete/{id} | 删除手工支出 |
| GET | /finances/select1Byid | 按月+ID查询财务 |
| GET | /finances/select1Byamount | 按月+金额查询财务 |
| GET | /finances/select1Byname | 按月+姓名查询财务 |
| GET | /finances/select1Bydate | 按月+日期查询财务 |
| GET | /finances/select1Bytype | 按月+类型查询财务 |
| POST | /finances/add | 新增财务记录 |
| PUT | /finances/edit | 编辑财务记录 |
| PUT | /finances/update | 更新财务记录 |
| DELETE | /finances/delete/{id} | 删除财务记录 |

### PaymentChannelController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /payment-channels/selectAll | 查询所有收款渠道 |
| GET | /payment-channels/selectEnabled | 查询启用的收款渠道 |
| POST | /payment-channels/add | 新增收款渠道 |
| PUT | /payment-channels/edit | 编辑收款渠道 |
| DELETE | /payment-channels/delete/{id} | 删除收款渠道 |

### InsuranceController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /insurance/overview | 保险概览 |
| GET | /insurance/config | 查询保险配置 |
| POST | /insurance/config | 保存保险配置 |
| GET | /insurance/patient-profile/{patientId} | 查询患者保险档案 |
| POST | /insurance/patient-profile | 保存患者保险档案 |
| GET | /insurance/settlements | 查询结算单 |
| POST | /insurance/settlements/draft | 创建结算草稿 |
| GET | /insurance/logs | 查询保险日志 |
| POST | /insurance/mock/settlement-payload | 构建模拟结算数据 |

---

## 八、库存与耗材

### InventoryController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /Inventory/selectAll1 | 查询全部库存 |
| GET | /Inventory/selectAll | 分页查询全部库存 |
| GET | /Inventory/selectByid | 按ID查询库存 |
| GET | /Inventory/selectByname | 按名称查询库存 |
| GET | /Inventory/selectBycategory | 按分类查询库存 |
| GET | /Inventory/selectBybrand | 按品牌查询库存 |
| GET | /Inventory/selectBysupplier | 按供应商查询库存 |
| POST | /Inventory/add | 新增库存 |
| PUT | /Inventory/edit | 编辑库存 |
| DELETE | /Inventory/delete/{id} | 删除库存 |
| DELETE | /Inventory/deleteBatch | 批量删除库存 |
| PUT | /Inventory/update/{id} | 更新库存数量 |
| POST | /Inventory/addBatch | 批量新增库存 |
| GET | /Inventory/selectLowStock | 查询低库存 |
| GET | /Inventory/select1Byid | 按ID查询库存（另一视图） |
| GET | /Inventory/select1Byname | 按名称查询库存（另一视图） |
| GET | /Inventory/select1Bycategory | 按分类查询库存（另一视图） |
| GET | /Inventory/select1Bybrand | 按品牌查询库存（另一视图） |
| GET | /Inventory/select1Bysupplier | 按供应商查询库存（另一视图） |

### PurchaseController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /purchase/selectAll | 分页查询所有采购 |
| GET | /purchase/selectByid | 按ID查询采购 |
| GET | /purchase/selectByname | 按名称查询采购 |
| GET | /purchase/selectBycategory | 按分类查询采购 |
| GET | /purchase/selectBybrand | 按品牌查询采购 |
| GET | /purchase/selectBysupplier | 按供应商查询采购 |
| POST | /purchase/add | 新增采购 |
| DELETE | /purchase/delete/{id} | 删除采购 |
| DELETE | /purchase/deleteBatch | 批量删除采购 |
| PUT | /purchase/updateStatus | 更新采购状态 |

### MaterialController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /materials/search | 搜索耗材 |
| GET | /materials/searchLite | 轻量搜索耗材 |
| GET | /materials/{id} | 按ID查询耗材 |
| POST | /materials/add | 新增耗材 |
| PUT | /materials/edit | 编辑耗材 |

### MaterialCategoryController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /material-categories/tree | 查询分类树 |
| GET | /material-categories/selectEnabled | 查询启用的分类 |
| POST | /material-categories/add | 新增分类 |
| PUT | /material-categories/edit | 编辑分类 |
| DELETE | /material-categories/delete/{id} | 删除分类 |

### MaterialPurchaseController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /material-purchases/search | 搜索耗材采购单 |
| GET | /material-purchases/{id} | 按ID查询采购单 |
| POST | /material-purchases/add | 新增采购单 |
| PUT | /material-purchases/edit | 编辑采购单 |
| POST | /material-purchases/void/{id} | 作废采购单 |
| POST | /material-purchases/uploadInvoice | 上传发票 |
| GET | /material-purchases/invoice/{purchaseId} | 查看发票文件 |

### MaterialStatisticsController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /material-statistics/overview | 耗材统计概览 |

---

## 九、技工加工

### LabFactoryController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /lab-factories/search | 搜索加工厂 |
| GET | /lab-factories/selectEnabled | 查询启用的加工厂 |
| GET | /lab-factories/{id} | 按ID查询加工厂 |
| POST | /lab-factories/add | 新增加工厂 |
| PUT | /lab-factories/edit | 编辑加工厂 |
| DELETE | /lab-factories/delete/{id} | 删除加工厂 |
| GET | /lab-factories/{factoryId}/products | 查询加工产品 |
| POST | /lab-factories/{factoryId}/products/add | 新增加工产品 |
| PUT | /lab-factories/{factoryId}/products/edit | 编辑加工产品 |
| POST | /lab-factories/{factoryId}/products/batchSave | 批量保存加工产品 |
| DELETE | /lab-factories/{factoryId}/products/delete/{productId} | 删除加工产品 |
| GET | /lab-factories/{factoryId}/templates | 查询对账单模板 |
| POST | /lab-factories/{factoryId}/templates/add | 新增对账单模板 |
| PUT | /lab-factories/{factoryId}/templates/edit | 编辑对账单模板 |
| DELETE | /lab-factories/{factoryId}/templates/delete/{templateId} | 删除对账单模板 |

### LabOrderController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /lab-orders/search | 搜索加工订单 |
| GET | /lab-orders/{id} | 按ID查询订单 |
| POST | /lab-orders/add | 新增订单 |
| PUT | /lab-orders/edit | 编辑订单 |
| POST | /lab-orders/batchStatus | 批量更新订单状态 |
| DELETE | /lab-orders/delete/{id} | 删除订单 |

### LabBillController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /lab-bills/search | 搜索对账单 |
| GET | /lab-bills/{id} | 查询对账单详情 |
| POST | /lab-bills/import | 导入对账单 |
| PUT | /lab-bills/items/{id}/resolution | 更新对账明细解析 |
| PUT | /lab-bills/unmatched-orders/{id}/resolution | 更新未匹配订单解析 |
| POST | /lab-bills/confirm/{id} | 确认对账单 |
| GET | /lab-bills/file/{id} | 下载对账单文件 |

### LabStatisticsController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /lab-statistics/overview | 加工统计概览 |

---

## 十、咨询与营销

### ConsultationRecordController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /consultations/search | 搜索咨询记录 |
| GET | /consultations/selectById | 按ID查询咨询记录 |
| GET | /consultations/selectByPatientId | 按患者ID查询咨询记录 |
| POST | /consultations/add | 新增咨询记录 |
| PUT | /consultations/edit | 编辑咨询记录 |
| GET | /consultations/matchPatientByPhone | 按手机号匹配患者 |
| GET | /consultations/matchForPatientCreate | 查询可关联患者的咨询记录 |
| POST | /consultations/linkPatient | 关联患者到咨询记录 |
| GET | /consultations/dashboard/overview | 咨询概览看板 |
| GET | /consultations/dashboard/funnel | 咨询漏斗分析 |
| GET | /consultations/dashboard/channelAnalysis | 渠道分析 |
| GET | /consultations/dashboard/projectAnalysis | 项目分析 |
| GET | /consultations/dashboard/hourHeatmap | 时段热力图 |
| GET | /consultations/dashboard/nursePerformance | 护士业绩分析 |
| GET | /consultations/dashboard/referralAnalysis | 转介绍分析 |

### AdvertisingSpendingController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /advertising-spending/search | 搜索广告支出 |
| POST | /advertising-spending/add | 新增广告支出 |
| PUT | /advertising-spending/edit | 编辑广告支出 |
| DELETE | /advertising-spending/delete/{id} | 删除广告支出 |
| GET | /advertising-spending/dashboard/overview | 广告支出概览 |

---

## 十一、AI 与经营分析

### BusinessDailyAnalysisController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /business-analysis/latest | 最新经营分析 |
| GET | /business-analysis/history | 历史经营分析 |
| GET | /business-analysis/probe | 测试模型连接 |
| GET | /business-analysis/chat/session | 获取聊天会话 |
| GET | /business-analysis/chat/memory | 获取聊天记忆 |
| POST | /business-analysis/chat/message | 发送聊天消息 |
| POST | /business-analysis/chat/stream | 流式聊天 |
| GET | /business-analysis/weekly/latest | 最新周报 |
| POST | /business-analysis/weekly/run | 手动运行周报 |
| GET | /business-analysis/weekly/run/status | 周报运行状态 |
| GET | /business-analysis/monthly/latest | 最新月报 |
| POST | /business-analysis/monthly/run | 手动运行月报 |
| GET | /business-analysis/monthly/run/status | 月报运行状态 |
| GET | /business-analysis/alerts/recent | 近期经营预警 |
| POST | /business-analysis/alerts/scan | 扫描经营预警 |
| GET | /business-analysis/{id} | 查询分析详情 |
| POST | /business-analysis/run | 手动运行分析 |
| GET | /business-analysis/run/status | 分析运行状态 |

### AiAgentConfigController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /ai-agent-configs | 查询AI智能体配置列表 |
| GET | /ai-agent-configs/{id} | 查询AI智能体配置详情 |
| GET | /ai-agent-configs/by-key | 按key查询配置 |
| POST | /ai-agent-configs | 创建AI智能体配置 |
| PUT | /ai-agent-configs/{id} | 更新AI智能体配置 |
| DELETE | /ai-agent-configs/{id} | 删除AI智能体配置 |

### AiModelProviderController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /api/model-providers | 查询模型提供商配置 |
| POST | /api/model-providers | 保存模型提供商配置 |
| POST | /api/model-providers/test | 测试模型连接 |
| DELETE | /api/model-providers/{id} | 删除模型提供商配置 |

---

## 十二、微信与门户

### WechatMpCallbackController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /wechat/mp | 公众号服务器验证 |
| POST | /wechat/mp | 接收公众号消息事件 |

### WechatPortalController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /wechat/portal | 患者门户入口跳转 |

### WechatBindController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /wechat/bind/start | 启动微信绑定 |
| GET | /wechat/bind/callback | 微信绑定回调 |
| GET | /wechat/bind/qrcode | 生成绑定二维码 |

### WechatMenuController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /wechat/menu/current | 查询当前公众号菜单 |
| GET | /wechat/menu/preview | 预览默认菜单 |
| POST | /wechat/menu/publish | 发布默认菜单 |

### StaffPortalController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /staff-portal/entry | 员工门户入口 |
| GET | /staff-portal/callback | 员工门户回调 |
| POST | /staff-portal/bind | 员工账号绑定 |
| GET | /staff-portal/overview | 员工门户概览 |

### PatientPortalController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /patient-portal/entry | 患者门户入口 |
| GET | /patient-portal/callback | 患者门户回调 |
| GET | /patient-portal/overview | 患者门户概览 |
| GET | /patient-portal/consents/{id} | 查询知情同意书详情 |
| POST | /patient-portal/consents/{id}/sign | 签署知情同意书 |
| POST | /patient-portal/appointments/{id}/cancel | 取消预约 |
| PUT | /patient-portal/appointments/{id}/edit | 编辑预约 |

---

## 十三、其他

### AdminReportPortalController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /admin-report-portal/overview | 管理员报表概览 |

### FileTransferController

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /file-transfer | 文件传输首页 |
| POST | /file-transfer/upload | 上传文件 |
| GET | /file-transfer/files/{filename} | 下载文件 |

---

## 待开发接口预留区

> 新增功能时，在此区域补充接口设计，开发完成后再归档到上方对应模块。

### 示例：AI 写病历（待开发）

| 方法 | 路径 | 功能 | 请求参数 | 响应结构 |
|------|------|------|----------|----------|
| POST | /ai/medical-record/draft | AI 根据患者信息生成病历草稿 | `{ patient_id, complaint, existing_history }` | `{ code, data: { complaint, present_illness, diagnosis, treatment_plan } }` |
| POST | /ai/treatment/pricing | AI 根据治疗项目生成方案与定价 | `{ treatment_items: [] }` | `{ code, data: { suggested_plan, total_price, breakdown: [] } }` |

### 示例：首页 AI Agent 管理（已预留入口）

前端已在首页 AI 面板添加"管理"按钮，跳转 `/AIAgentConfig` 配置页。
后端已有 `AiAgentConfigController` 提供完整 CRUD，直接复用以下接口即可：

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /ai-agent-configs?accountId={id} | 获取当前用户配置的 Agent 列表 |
| POST | /ai-agent-configs | 新增自定义 Agent |
| PUT | /ai-agent-configs/{id} | 更新 Agent 配置 |
| DELETE | /ai-agent-configs/{id} | 删除 Agent |

---

*最后更新：2026-05-08*
