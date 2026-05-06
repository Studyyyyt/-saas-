# Task 0 Shared Preparation

## Flyway version allocation

- `V14`: 义齿加工管理模块
- `V15`: 耗材采购与库存模块

## Finance write entry

- Shared expense helper: `com.example.springboot.service.FinanceService#recordExpense(...)`
- File: `src/main/java/com/example/springboot/service/FinanceService.java`
- Current schema does not have a dedicated `biz_id` column.
- The helper writes:
  - `name` = expense category
  - `type` = `支出`
  - `biz_type` = business source type
  - `remark` = standardized `bizId=... | ...` text when `bizId` exists

## Current finance write paths

- Manual CRUD entry:
  - `POST /finances/add`
  - `FinanceController -> FinanceService.addFinance -> FinanceMapper.addFinance`
- Treatment billing writes:
  - `TreatmentBillingService.chargeTreatment(...)`
  - `TreatmentBillingService.chargeTreatmentBatch(...)`
  - `TreatmentBillingService.refundTreatment(...)`
  - These currently call `FinanceMapper.addFinance(...)` directly

## Current table notes

### finances

Actual persisted columns:

- `id`
- `patient_id`
- `treatment_id`
- `payment_channel_id`
- `payment_channel_name`
- `name`
- `amount`
- `date`
- `type`
- `biz_type`
- `remark`

### treatment

Actual persisted columns:

- `id`
- `patient_id`
- `patient_name`
- `batch_no`
- `appointment_purpose`
- `status`
- `doctor_account_id`
- `doctor_name`
- `treatment_date`
- `treatment_content`
- `tooth_positions`
- `treatment_product`
- `treatment_fee`
- `created_at`
- `updated_at`

De facto status values in current code:

- `进行中`
- `完成`
- `取消`

Compatibility checks also recognize:

- `已取消`
- `已完成`
- `已治疗`

### patients

Actual persisted columns:

- `id`
- `name`
- `gender`
- `age`
- `date_of_birth`
- `phone`
- `email`
- `address`
- `relation_type`
- `related_patient_id`
- `related_patient_name`
- `wechat_openid`
- `customer_source`
- `created_at`
- `updated_at`

Not real table columns, only computed or transient in entity:

- `has_arrears`
- `arrears_amount`
- `latest_visit_doctor`
- `latest_treatment`
- `consultation_record_id`

## Upload and Excel notes

- Patient image upload already exists:
  - backend: `/patient-images/upload`
  - storage: `~/.local/uploads/patient-images/`
  - frontend usage: `Patient360View.vue`, `StaffPatient360H5.vue`
- Generic multipart file transfer page also exists:
  - backend: `/file-transfer/upload`
- There is no business backend Excel parser.
- Current Excel import is front-end parsing with `xlsx`, then JSON POST:
  - inventory import: `InventoryView.vue` / `InventoryView2.vue` -> `/Inventory/addBatch`
- Current Excel export exists in:
  - `InventoryView.vue`
  - `InventoryView2.vue`
  - `FinancialView2.vue`
  - `ConsultationView.vue`

## Front-end navigation notes

- Left menu config file: `saas-vue-src/src/views/Manager.vue`
- Route config file: `saas-vue-src/src/router/index.js`
- Current route style:
  - static top-level imports
  - `Manager` shell with child routes
  - global `beforeEach` auth guard
  - no lazy-loaded business routes in current active config
