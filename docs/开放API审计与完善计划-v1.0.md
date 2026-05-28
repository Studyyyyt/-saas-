# 开放API审计与完善计划 v1.0

> 本文档用于评估当前后端开放接口（OpenDataController）的能力覆盖度，并制定分阶段完善计划。
> 目标：让开放API能够支撑系统中任何数据的获取与分析需求。

---

## 一、现有接口审计

### 1.1 现有接口清单（OpenDataController）

| # | 接口路径 | 方法 | 已有能力 | 分页 | 性能 | 主要问题 |
|---|---|---|---|---|---|---|
| 1 | `/business-stats` | GET | 返回日报/周报/月报缓存 | 否 | 中 | 返回 `buildView()` 完整结构，含 `id`, `analysis_status`, `source_type`, `trigger_type`, `model_name`, `created_at`, `updated_at` 等大量元数据字段 |
| 2 | `/business-analysis/latest` | GET | 返回最新AI经营日报 | 否 | 中 | 依赖数据库缓存，如果调度器未执行会返回旧数据；字段冗余 |
| 3 | `/business-analysis/weekly/latest` | GET | 返回最新AI经营周报 | 否 | 中 | 同上 |
| 4 | `/business-analysis/monthly/latest` | GET | 返回最新AI经营月报 | 否 | 中 | 同上 |
| 5 | `/daily-metrics` | GET | 返回指定日期原始经营指标 | 否 | 中 | **已精简为17个核心字段**，`data_limitations` 已清空为 `[]`，支持 `date` 参数查询指定日期 |
| 6 | `/followups` | GET | 回访列表 | **是** | 良 | 已改为数据库分页，支持 `page`, `size` |
| 7 | `/patients` | GET | 患者列表（模糊搜索） | **是** | 良 | 已改为数据库分页，支持 `page`, `size`, `keyword` |
| 8 | `/appointments` | GET | 预约列表 | **是** | 良 | 支持日期/状态/医生筛选，PageHelper分页 |
| 9 | `/patients/{id}/details` | GET | 患者360详情 | 否 | 良 | 较好 |
| 10 | `/patients/{id}/medical-records` | GET | 患者病历列表 | **是** | 良 | 较好 |
| 11 | `/doctors` | GET | 医生排班列表 | **是** | 良 | 较好 |
| 12 | `/materials` | GET | 耗材列表 | **是** | 良 | 已改为数据库分页，支持 `page`, `size`, `lowStockOnly` |
| 13 | `/patients/workbench` | GET | 患者工作台 | 否 | 良 | 较好 |
| 14 | `/finances/doctor-performance` | GET | 医生业绩聚合统计 | 否 | 良 | 只能看聚合结果，查不到单笔财务流水 |
| 15 | `/treatments` | GET | 治疗记录列表 | **是** | 良 | 较好 |

### 1.2 核心问题总结

| 问题类别 | 具体表现 | 影响 |
|---|---|---|
| **性能隐患** | `/followups` 全量查询内存过滤；`/patients` 无分页；`/materials` 内存分页 | 数据量增长后接口超时或OOM |
| **字段冗余** | `/business-analysis/*` 返回15+个字段，其中一半是元数据 | n8n Set节点提取困难，传输体积大 |
| **数据口径干扰** | ~~`/daily-metrics` 的 `data_limitations` 是后端硬编码的中文说明~~ **（已修复：已清空为 `[]`）** | ~~干扰AI分析，增加无效token~~ |
| **缺少核心模块** | 系统中60+张表，只开放了约10张表的查询能力 | 大量业务数据无法获取 |
| **无通用查询** | 每个接口返回固定字段，不能按需求指定字段 | 灵活性差，每新增分析维度就要改后端 |
| **安全粒度粗** | 所有接口共用同一个API Key，无权限细分 | 如果Key泄露，全部数据暴露 |

---

## 二、数据覆盖度审计

系统中共有 **60+ 个Mapper（对应60+张表）**，OpenDataController 仅覆盖了 **约10个实体**。

### 2.1 已开放（约10个）

`BusinessDailyAnalysis`, `BusinessPeriodReport`, `Patient`, `PatientFollowup`, `Appointment`, `MedicalRecord`, `Doctor`, `Material`, `Finance`（仅聚合）, `Treatment`

### 2.2 未开放的核心业务模块（约20个）

| 模块 | 缺失实体/表 | 业务重要性 | 分析用途 |
|---|---|---|---|
| **咨询管理** | `consultation_records`, `consultation_followups` | **P0** | 获客漏斗、渠道ROI、线索转化率、咨询员绩效 |
| **财务流水** | `finances`（单笔明细） | **P0** | 收支明细、对账、现金流趋势、支付方式分析 |
| **义齿加工** | `lab_orders`, `lab_factories` | **P0** | 加工进度追踪、周转天数、加工厂对账、加工成本占比 |
| **耗材采购** | `material_purchases`, `purchase` | **P1** | 采购成本、供应商对账、库存周转、耗材占比 |
| **治疗项目** | `treatment_catalog`, `treatment_projects` | **P1** | 项目利润分析、价目表、医生擅长项目分布 |
| **广告花费** | `advertising_spending` | **P1** | 投放ROI、渠道成本、获客成本计算 |
| **患者标签** | `patient_risk_tags`, `patient_custom_groups` | **P1** | 患者分层、精准营销、风险预警 |
| **患者影像** | `patient_images` | **P2** | 影像归档、复诊对比 |
| **患者时间轴** | `patient_timeline` | **P2** | 患者全生命周期视图 |
| **患者洞察** | `patient_insight_summary` | **P2** | 患者价值评分、流失预警 |
| **病历模板** | `medical_record_templates`, `medical_record_phrases` | **P2** | 模板使用频率、病历质量分析 |
| **收费渠道** | `payment_channels` | **P2** | 支付方式占比、渠道手续费分析 |
| **保险结算** | `insurance_settlements`, `insurance_patient_profiles` | **视业务** | 医保报销比例、保险收入占比 |
| **库存流水** | `inventory` | **P2** | 库存变动、出入库明细 |
| **同意书** | `patient_consents`, `consent_templates` | **P2** | 合规统计、签署率 |
| **排班模板** | `shift_templates` | **P2** | 医生出勤率、排班饱和度 |

### 2.3 不需要开放的管理类模块

`Account`, `User`, `RoleMenuPermission`, `ApiKey`, `AiAgentConfig`, `AiModelProvider`, `AiPromptTemplate`, `AiOperationLog`, `BusinessAlertLog` 等属于系统内部管理，不建议通过开放API暴露。

---

## 三、接口完善计划（分4个阶段）

### 设计原则

1. **每个核心实体一个列表查询接口**，支持：日期范围筛选、关键词搜索、分页
2. **聚合接口保持独立**（如 `/daily-metrics`, `/finances/doctor-performance`），用于高频场景
3. **统一分页格式**：全部使用 PageHelper，返回 `PageInfo` 标准结构
4. **字段精简**：列表接口只返回核心字段，详情接口返回完整字段
5. **安全**：暂时沿用 `X-API-Key + clinicId` 校验，后续可升级为 Key 级别权限

---

### Phase 1：核心经营数据（优先级最高）

**目标**：补齐诊所日常经营分析最急需的数据，让 WF-01 ~ WF-04 及扩展分析都能跑通。

| # | 新增/优化接口 | 说明 | 涉及文件 |
|---|---|---|---|
| 1 | `GET /finances` | 财务流水明细列表。支持 `startDate`, `endDate`, `type`, `patientId`, `keyword`, `page`, `size`。✅ **已完成**：Service 层已补 `PageHelper` | `OpenDataController` + `FinanceService` |
| 2 | `GET /consultations` | 咨询记录列表。支持 `startDate`, `endDate`, `channel`, `intentLevel`, `handlingResult`, `keyword`, `page`, `size`。✅ **已完成**：Controller 层已做日期参数映射，Service 层已补 `PageHelper` | `OpenDataController` + `ConsultationRecordMapper` |
| 3 | `GET /consultations/{id}/followups` | 单条咨询的跟进历史。✅ **已完成** | `OpenDataController` + `ConsultationFollowupMapper` |
| 4 | `GET /lab-orders` | 义齿加工订单列表。支持 `startDate`, `endDate`, `status`, `factoryId`, `keyword`, `page`, `size`。✅ **已完成**：`LabOrderMapper` 已新增 `search` 动态 SQL | `OpenDataController` + `LabOrderMapper` |
| 5 | `GET /lab-factories` | 加工厂名录。✅ **已完成** | `OpenDataController` + `LabFactoryMapper` |
| 6 | **优化** `/followups` | 增加 `page`, `size` 分页参数，改为数据库分页。✅ **已完成** | `OpenDataController` + `PatientFollowupMapper` |
| 7 | **优化** `/patients` | 增加 `page`, `size` 分页参数，改为数据库分页。✅ **已完成**：Service 层已用 `PageHelper` 拦截 | `OpenDataController` + `PatientMapper` |
| 8 | **优化** `/materials` | 改为数据库分页。✅ **已完成**：过滤逻辑已下沉到 `MaterialMapper` 动态 SQL | `OpenDataController` + `MaterialService` + `MaterialMapper` |

**Phase 1 完成后可覆盖的分析场景**：

- 每日经营简报（已有）
- 明日就诊预览（已有）
- 患者回访提醒（已有，且支持分页）
- 医生周业绩简报（已有）
- **新增**：咨询漏斗分析、渠道ROI、线索转化
- **新增**：义齿加工进度追踪、加工厂对账
- **新增**：财务流水明细查询、现金流分析

---

### Phase 2：扩展业务数据

**目标**：支持成本分析、项目分析、营销分析。

| # | 新增接口 | 说明 | 涉及文件 |
|---|---|---|---|
| 1 | `GET /material-purchases` | 耗材采购单列表。支持 `startDate`, `endDate`, `supplierName`, `status`, `page`, `size`。✅ **已完成** | `OpenDataController` + `MaterialPurchaseMapper` |
| 2 | `GET /treatment-catalog` | 治疗项目目录（价目表）。✅ **已完成** | `OpenDataController` + `TreatmentCatalogMapper` |
| 3 | `GET /advertising-spending` | 广告花费列表。支持 `startDate`, `endDate`, `platform`, `page`, `size`。✅ **已完成** | `OpenDataController` + `AdvertisingSpendingMapper` |
| 4 | `GET /patients/{id}/risk-tags` | 患者风险标签。✅ **已完成** | `OpenDataController` + `PatientRiskTagMapper` |
| 5 | `GET /patient-groups` | 患者分组列表。✅ **已完成** | `OpenDataController` + `PatientCustomGroupMapper` |
| 6 | `GET /patients/{id}/images` | 患者影像列表。✅ **已完成** | `OpenDataController` + `PatientImageMapper` |

**Phase 2 完成后可覆盖的分析场景**：

- 耗材采购成本月报
- 项目利润分析（哪些项目最赚钱）
- 广告投放ROI（结合咨询记录计算）
- 患者分层运营（高价值/高风险患者）

---

### Phase 3：患者360与辅助数据

**目标**：支持患者全生命周期分析、病历质量分析。

| # | 新增接口 | 说明 | 涉及文件 |
|---|---|---|---|
| 1 | `GET /patients/{id}/timeline` | 患者时间轴 | `OpenDataController` + `PatientTimelineMapper` |
| 2 | `GET /patients/{id}/insight` | 患者洞察摘要 | `OpenDataController` + `PatientInsightSummaryService` |
| 3 | `GET /medical-records` | 全量病历列表（支持医生/日期筛选，已有 `selectAllWithFilter`） | `OpenDataController` + `MedicalRecordMapper` |
| 4 | `GET /inventory` | 库存流水/物品列表 | `OpenDataController` + `InventoryMapper` |
| 5 | `GET /payment-channels` | 收费渠道列表 | `OpenDataController` + `PaymentChannelMapper` |

---

### Phase 4：通用能力与优化

**目标**：提升灵活性和性能，减少后续改动。

| # | 事项 | 说明 |
|---|---|---|
| 1 | **统一分页格式** | 所有列表接口统一返回 `PageInfo`（`total`, `list`, `pageNum`, `pageSize`），不再混用直接数组 |
| 2 | **统一日期参数名** | 全部使用 `startDate` / `endDate`（yyyy-MM-dd），不再混用 `startTime` / `endTime` |
| 3 | **通用查询接口（可选）** | 如果 Phase 1~3 仍不能满足某些临时分析需求，可增加 `POST /query`，支持传表名、字段、条件、排序。但需严格限流 + 只读 + 白名单表名 |
| 4 | **字段选择（可选）** | 列表接口支持 `fields` 参数，只返回指定字段，减少传输体积 |
| 5 | **API Key 权限细分（可选）** | 支持为不同 Key 配置可访问的接口白名单 |

---

## 四、执行建议

### 选项A：按阶段逐步推进（推荐）

先完成 **Phase 1**（约8个接口），这是最核心的缺口。完成后 n8n 工作流可以覆盖 80% 的经营分析场景。然后再看是否需要继续 Phase 2。

**推荐理由**：
- Phase 1 的 `ConsultationRecordMapper.search()`、`FinanceMapper.searchFinances()` 已有过滤能力，但**均不支持分页**，需在 Service/Controller 层补 `PageHelper`；`LabOrderMapper` **无任何过滤方法**，需新增动态 SQL
- Phase 1 中 3 个接口可直接复用（consultations followups、lab factories），2 个需小幅调整（参数映射 + PageHelper），3 个需新增/修改 Mapper（lab orders、patients、materials）
- Phase 1 完成后，现有4个工作流 + 咨询/义齿/财务分析都可以跑通，ROI最高
- 逐步推进可以及时发现问题（如分页性能、字段需求），避免一次性做太多返工

### 选项B：一次性全做完

如果确定后续所有场景都要做，可以一次性把 Phase 1~3 全部补齐（约20个接口）。工作量大，但以后不再需要改后端。

### 选项C：只做通用查询接口

不做逐个接口，直接做一个 `POST /query` 通用只读查询接口。好处是以后任意分析都不需要再改后端；坏处是安全风险高，n8n 里需要写类似SQL的条件，维护成本高。

---

## 五、附录：全局接口规范

### 5.1 认证方式

所有开放接口统一使用：
- **路径参数**：`clinicId`（如 `/api/open/clinic/1/...`）
- **请求头**：`X-API-Key: {apiKey}`
- **校验逻辑**：比较 `apiKey` 对应的 `clinicId` 与路径中的 `clinicId` 是否一致

### 5.2 分页规范

列表查询接口统一支持：
- `page`：页码，默认 1
- `size`：每页条数，默认 10，最大 100

统一返回 PageInfo 结构：
```json
{
  "code": "200",
  "msg": "请求成功",
  "data": {
    "total": 100,
    "list": [ ... ],
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10
  }
}
```

### 5.3 日期参数规范

- 参数名统一为 `startDate` / `endDate`
- 格式统一为 `yyyy-MM-dd`
- 时区由后端统一处理（Asia/Shanghai）

### 5.4 响应码规范

| code | 含义 |
|---|---|
| 200 | 成功 |
| 403 | 无权访问（clinicId不匹配或API Key无效） |
| 400 | 参数错误（如日期格式错误） |
| 500 | 服务器内部错误 |

---

---

## 六、版本变更记录

| 版本 | 日期 | 变更内容 |
|---|---|---|
| v1.2 | 2026-05-25 | Phase 1（8个接口）和 Phase 2（6个接口）全部开发完成并验证。已标注各接口完成状态。后端 OpenDataController 已新增 19+ 个接口 |
| v1.1 | 2026-05-25 | 根据代码审计修正 Phase 1 接口实现细节 |
| v1.0 | 2026-05-25 | 初始版本 |

*当前文档版本：v1.1*  
*对应后端版本：内置 AI 架构已清理，待 Phase 1 接口完成后更新*
