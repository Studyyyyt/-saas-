# 口腔门诊 SaaS 项目系统说明书

> 基于已实际检查的源码整理，覆盖项目定位、目录结构、模块说明、主要接口、数据层、页面-接口映射、运行部署说明与风险清单。

---

## 1. 项目概述

这是一个面向**口腔门诊/牙科诊所**的 SaaS 管理系统，核心目标是把门诊日常运营中的患者、预约、治疗、病历、收费、库存与微信触点串起来，形成一套兼顾**门诊经营管理**与**患者服务/员工移动办公**的系统。

### 1.1 技术栈

#### 前端
- Vue 2.6.14
- Vue Router 3.5.1
- Element UI 2.15.14
- Axios
- ECharts
- XLSX

#### 后端
- Spring Boot 3.2.5
- MyBatis Spring Boot Starter 3.0.3
- MySQL
- PageHelper 分页插件
- Lombok
- Java 17

### 1.2 业务定位
系统不是单一的电子病历系统，而是一个偏**门诊经营后台 + 患者关系运营 + 微信门户**的一体化 SaaS 原型。

主要业务链路：
1. 患者录入
2. 预约安排
3. 到诊治疗
4. 病历沉淀
5. 收费记录
6. 库存/采购管理
7. 患者随访与风险标签
8. 患者微信门户访问
9. 员工微信移动端查看工作内容

---

## 2. 项目目录结构

## 2.1 前端目录
源码包：`saas-vue-src.tgz`
解压目录：`saas-vue-src/`

关键文件：
- `saas-vue-src/package.json`
- `saas-vue-src/vue.config.js`
- `saas-vue-src/src/main.js`
- `saas-vue-src/src/router/index.js`
- `saas-vue-src/src/views/`

前端页面文件数（已统计）：
- `src/views` 下约 **33 个页面文件**

主要目录说明：
- `src/main.js`：前端入口
- `src/router/index.js`：路由总表
- `src/views/Manager.vue`：后台管理壳页面
- `src/views/Manager/*.vue`：后台功能页
- `src/views/PatientPortalHome.vue`：患者微信门户
- `src/views/StaffPortalHome.vue`：员工微信工作台
- `src/views/StaffAppointmentH5.vue` / `StaffPatientH5.vue` / `StaffPatient360H5.vue`：员工 H5 子页面

## 2.2 后端目录
源码包：`saas-springboot-src.tgz`
解压目录：`saas-springboot-src/`

关键文件：
- `saas-springboot-src/pom.xml`
- `saas-springboot-src/src/main/resources/application.yml`
- `saas-springboot-src/src/main/resources/schema-extra.sql`
- `saas-springboot-src/sql/patient360_init.sql`
- `saas-springboot-src/src/main/java/com/example/springboot/`

主要代码目录：
- `controller/`：控制器层
- `service/`：服务层
- `mapper/`：MyBatis 数据访问层
- `entity/`：实体类
- `config/`：配置类
- `common/`：公共返回类等

控制器文件数（已统计）：
- `controller` 下约 **19 个 Controller**

---

## 3. 前端系统结构说明

## 3.1 前端启动与构建
根据 `package.json`：

```bash
npm install
npm run serve
```

开发脚本：
- `serve`: `vue-cli-service serve`
- `build`: `vue-cli-service build`

`vue.config.js` 指定前端开发端口：
- **7070**

## 3.2 前端入口
`src/main.js`
- 注册 Element UI
- 挂载 Vue Router
- 渲染根组件 `App.vue`

## 3.3 路由总体结构
`src/router/index.js` 显示系统可分为三大块：

### A. 管理后台（桌面端）
入口：
- `/` -> `Manager.vue`
- 默认重定向 `/login1`

后台主要页面：
- `/home`：门诊首页概览
- `/Patient`：患者列表
- `/Patient360`：患者 360 视图
- `/MedicalRecord`：病历管理
- `/Appointment`、`/Appointment2`：预约管理
- `/Doctor`：医生管理/排班
- `/Treatment`、`/Treatment2`：治疗计划/治疗记录
- `/TreatmentCatalog`：处置收费项目库
- `/Financial`、`/Financial2`：财务信息与财务分析
- `/Inventory`、`/Inventory2`、`/Inventory3`：库存、提醒、采购
- `/Account`：账号信息管理
- `/Person`：个人信息

### B. 患者微信门户
- `/patient-portal-home`
- `/portal-auth-error`
- `/app/bind-success`
- `/appointment-notice`

功能目标：
- 患者在微信里查看预约、病历、影像
- 取消/修改预约
- 完成微信绑定后的成功页展示

### C. 员工微信 H5
- `/staff-portal-home`
- `/staff-h5/appointments`
- `/staff-h5/patients`
- `/staff-h5/patient360`
- `/staff-portal-bind`
- `/staff-portal-auth-error`

功能目标：
- 员工微信登录/绑定
- 医生手机端查看今日预约
- 查看患者信息与 360 档案

---

## 4. 前端功能模块说明

## 4.1 登录模块
页面：`src/views/login1.vue`

功能：
- 输入用户名、密码
- 调用 `POST /loginController/login`
- 登录成功后将用户信息写入 `localStorage.userData`
- 跳转到 `/home`

当前保存的本地用户字段：
- `id`
- `username`
- `name`
- `role`

## 4.2 后台主壳
页面：`src/views/Manager.vue`

功能：
- 左侧导航菜单
- 顶部用户卡片
- 动态内容区 `router-view`
- 基于 `localStorage.userData` 显示用户姓名与角色
- 根据 `user.role === '管理员'` 控制财务、库存、系统设置菜单显示

菜单分组：
- 首页概览
- 患者管理
- 预约管理
- 治疗管理
- 财务管理（管理员）
- 库存管理（管理员）
- 系统设置（管理员）

## 4.3 首页概览模块
页面：`src/views/Manager/HomeView.vue`

调用接口：
- `GET /appointments/selectAll?page=1&size=1000`
- `GET /patients/selectAll?page=1&size=1000`
- `GET /medical-records/selectAll?page=1&size=1000`
- `GET /finances/selectByMonth`

页面展示：
- 今日预约数
- 患者总数
- 本月已收费金额
- 病历总数
- 运营提醒
- 快捷入口
- 门诊日历

## 4.4 患者管理模块
页面：`src/views/Manager/PatientView.vue`

功能：
- 按 ID / 姓名检索
- 分页列表
- 新增患者
- 编辑患者
- 删除患者
- 批量删除
- 跳转患者 360

主要前端动作对应接口：
- 查询：`/patients/selectAll`、`/patients/selectByid`、`/patients/selectByname`
- 新增：`POST /patients/add`
- 编辑：`PUT /patients/edit`
- 删除：`DELETE /patients/delete/{id}`
- 批量删除：`DELETE /patients/deleteBatch`

## 4.5 患者 360 模块
页面：`src/views/Manager/Patient360View.vue`

这是系统最有业务价值的模块之一，聚合了患者全链路信息。

展示内容：
- 基本信息
- 统计卡片
  - 就诊次数
  - 治疗次数
  - 累计费用
  - 最近就诊
  - 下次随访
- 风险标签
- 多标签页：
  - 病历记录
  - 预约信息
  - 处置收费
  - 影像管理
  - 随访记录
  - 时间线

支持动作：
- 新增/编辑/删除病历
- 新增处置
- 上传/查看/删除影像
- 新增/删除随访
- 添加/删除风险标签

后端核心聚合接口：
- `GET /patient360/overview/{patientId}`

## 4.6 患者微信门户模块
页面：`src/views/PatientPortalHome.vue`

目标用户：患者

功能：
- 查看个人预约
- 查看病例/病历记录
- 查看影像
- 修改预约
- 取消预约

核心接口：
- `GET /patient-portal/overview?patientId=...`
- `POST /patient-portal/appointments/{id}/cancel`

## 4.7 员工微信 H5 模块
页面：
- `src/views/StaffPortalHome.vue`
- `src/views/StaffAppointmentH5.vue`
- `src/views/StaffPatientH5.vue`
- `src/views/StaffPatient360H5.vue`

目标用户：医生/员工

功能：
- 显示员工账号资料
- 显示当前角色
- 快速入口：今日预约 / 我的患者 / 患者360 / 门诊首页
- 根据员工身份筛选相关预约
- 在微信环境中快速查看工作内容

核心接口：
- `GET /staff-portal/overview?accountId=...`
- `GET /appointments/selectAll`
- `GET /patients/selectAllForH5`
- 患者 360 相关接口

---

## 5. 后端系统结构说明

## 5.1 后端入口
文件：`src/main/java/com/example/springboot/SpringbootApplication.java`

说明：
- Spring Boot 主启动类
- 打开了 `@EnableScheduling`
- 说明系统内置定时任务

## 5.2 返回结构统一规范
文件：`common/Result.java`

接口返回统一格式：
- `code`
- `msg`
- `data`

成功码：
- `200`

错误码：
- `500`
- 登录失败处还使用了 `401`

## 5.3 配置模块
### `application.yml`
已确认内容包括：
- MySQL 连接配置
- 微信 AppId / AppSecret
- 微信模板消息配置
- 微信 OAuth 配置
- 绑定跳转地址

默认数据库：
- `clinic_system_new`

默认数据库连接：
- `jdbc:mysql://localhost:3306/clinic_system_new...`
- 用户名：`root`
- 密码：`root`

### `CorsConfig.java`
- 开放 `/**`
- 允许 GET/POST/PUT/DELETE/OPTIONS
- Origin pattern 为 `*`

### `SpaForwardController.java`
职责：
- 让 Spring Boot 能把前端 history 路由统一转发到 `/index.html`

### `SchemaInitializer.java`
职责：
- 启动时自动执行 `schema-extra.sql`
- 自动补充附加表结构

---

## 6. 核心后端模块说明

## 6.1 登录与用户认证
控制器：`loginController.java`

接口：
- `POST /loginController/login`

处理逻辑：
- 根据用户名查询用户
- 直接比对明文密码
- 返回用户基础信息

返回数据包含：
- `id`
- `username`
- `name`
- `role`

> 当前是最基础的登录实现，没有 JWT、RBAC、中间件鉴权链。

## 6.2 患者管理
控制器：`PatientController.java`

接口：
- `GET /patients/selectAll`
- `GET /patients/selectAllForH5`
- `GET /patients/selectByid`
- `GET /patients/selectByname`
- `POST /patients/add`
- `PUT /patients/edit`
- `PUT /patients/bindWechat`
- `DELETE /patients/delete/{id}`
- `DELETE /patients/deleteBatch`

说明：
- 后台分页查询与搜索
- 支持微信 openid 绑定
- H5 端提供无分页拉全量接口

## 6.3 预约管理
控制器：`AppointmentController.java`

接口：
- `GET /appointments/selectAll`
- `GET /appointments/selectByid`
- `GET /appointments/selectByname`
- `GET /appointments/public/detail`
- `PUT /appointments/updateStatus/{id}`
- `POST /appointments/add`
- `PUT /appointments/edit`
- `POST /appointments/cancel/{id}`
- `DELETE /appointments/delete/{id}`
- `DELETE /appointments/deleteBatch`
- `POST /appointments/manual-next-day-reminder`

业务特点：
- 支持按状态过滤
- 支持患者主动取消预约
- 支持手动触发次日提醒

## 6.4 患者 360 聚合
控制器：`Patient360Controller.java`

接口：
- `GET /patient360/overview/{patientId}`

聚合内容：
- 患者基本信息
- 病历列表
- 随访列表
- 风险标签
- 时间线
- 治疗记录
- 影像
- 预约
- 统计字段：
  - 就诊次数
  - 最近就诊
  - 下次随访
  - 累计费用

这一层是典型的**聚合视图接口**，把多个表的数据拼成一个 360 页面数据包。

## 6.5 患者微信门户
控制器：`PatientPortalController.java`

接口：
- `GET /patient-portal/entry`
- `GET /patient-portal/callback`
- `GET /patient-portal/overview`
- `POST /patient-portal/appointments/{id}/cancel`

业务流程：
1. 患者从微信公众号菜单进入
2. 跳微信 OAuth
3. 通过 openid 找到已绑定患者
4. 跳转到患者门户首页
5. 患者查看自己的预约/病历/影像
6. 患者可取消预约

## 6.6 员工微信门户
控制器：`StaffPortalController.java`

接口：
- `GET /staff-portal/entry`
- `GET /staff-portal/callback`
- `POST /staff-portal/bind`
- `GET /staff-portal/overview`

业务流程：
1. 员工从微信菜单进入员工入口
2. 微信 OAuth 回调
3. 如果 openid 已绑定员工账号，直接进入员工首页
4. 如果未绑定，跳去绑定页
5. 员工输入账号密码完成绑定
6. 进入员工工作台

返回摘要信息中包含：
- displayName
- doctorName
- roleLabel
- username
- wechatBound

并生成 quickActions：
- appointments
- patients
- patient360

## 6.7 患者微信绑定
控制器：`WechatBindController.java`

接口：
- `GET /wechat/bind/start?patientId=...`
- `GET /wechat/bind/callback`

业务用途：
- 给后台已录入患者绑定微信 openid
- 绑定后跳转成功页

## 6.8 影像管理
控制器：`PatientImageController.java`

接口：
- `GET /patient-images/selectByPatientId`
- `POST /patient-images/upload`
- `GET /patient-images/file/{id}`
- `DELETE /patient-images/delete/{id}`

存储方式：
- 文件保存到：`~/.local/uploads/patient-images/`

功能：
- 上传影像
- 根据 id 读取文件流
- 删除影像记录与本地文件

## 6.9 医生管理
控制器：`DoctorController.java`

接口：
- `GET /doctors/selectAll`
- `GET /doctors/selectByid`
- `GET /doctors/selectByname`
- `PUT /doctors/updateStatus/{id}`
- `POST /doctors/add`
- `PUT /doctors/edit`
- `DELETE /doctors/delete/{id}`
- `DELETE /doctors/deleteBatch`

## 6.10 财务管理
控制器：`FinanceController.java`

接口较多，主要包括：
- `GET /finances/all`
- `GET /finances/selectByid`
- `GET /finances/selectByname`
- `GET /finances/selectByamount`
- `GET /finances/selectBytype`
- `GET /finances/selectBydate`
- `GET /finances/selectByMonth`
- `GET /finances/select1Byid`
- `GET /finances/select1Byamount`
- `GET /finances/select1Byname`
- `GET /finances/select1Bydate`
- `GET /finances/select1Bytype`
- `POST /finances/add`
- `PUT /finances/edit`
- `PUT /finances/update`
- `DELETE /finances/delete/{id}`

功能：
- 财务流水查询
- 月份维度统计
- 收费/支出筛选
- 新增与编辑财务记录

## 6.11 库存管理
控制器：`InventoryController.java`

注意：控制器前缀是：
- `@RequestMapping("/Inventory")`

主要接口：
- `GET /Inventory/selectAll`
- `GET /Inventory/selectByid`
- `GET /Inventory/selectByname`
- `GET /Inventory/selectBycategory`
- `GET /Inventory/selectBybrand`
- `GET /Inventory/selectBysupplier`
- `POST /Inventory/add`
- `PUT /Inventory/edit`
- `DELETE /Inventory/delete/{id}`
- `DELETE /Inventory/deleteBatch`
- `PUT /Inventory/update/{id}`
- `POST /Inventory/addBatch`
- `GET /Inventory/selectLowStock`
- 以及一批 `select1By*` 查询接口

功能：
- 库存 CRUD
- 低库存查询
- 批量新增
- 按品名/分类/品牌/供应商查询

## 6.12 账号管理
控制器：`AccountController.java`

接口：
- `GET /accounts/search`
- `GET /accounts/selectByid`
- `GET /accounts/selectByname`
- `POST /accounts/add`
- `PUT /accounts/edit`
- `DELETE /accounts/delete/{id}`
- `DELETE /accounts/deleteBatch`

用途：
- 管理后台员工账号
- 与员工微信绑定逻辑关联

---

## 7. 定时任务说明

文件：`AppointmentReminderScheduler.java`

定时任务：
- 每天晚上 **20:00（Asia/Shanghai）** 执行

Cron：
```text
0 0 20 * * *
```

任务内容：
- 调用 `appointmentService.sendNextDayAppointmentReminders()`
- 发送次日预约提醒

说明：
- 系统同时提供了手动触发接口：`POST /appointments/manual-next-day-reminder`

---

## 8. 数据库与表结构说明

## 8.1 主数据库
`application.yml` 指向数据库：
- `clinic_system_new`

说明：
- 主业务表大概率在该库中已存在
- 附加表由 `schema-extra.sql` / `patient360_init.sql` 补充

## 8.2 自动建表脚本
### `schema-extra.sql`
应用启动时由 `SchemaInitializer` 自动执行。

已确认包含表：
- `treatment_catalog`
- `medical_records`
- `patient_followup`
- `patient_risk_tag`
- `patient_timeline`
- `patient_images`

同时包含：
- `ALTER TABLE patients ADD COLUMN IF NOT EXISTS wechat_openid ...`

### `sql/patient360_init.sql`
主要补充 Patient360 相关表：
- `patient_followup`
- `patient_risk_tag`
- `patient_timeline`

## 8.3 关键表职责概览
根据 entity、controller 与 SQL 文件，核心业务表大致为：

### 患者域
- `patients`：患者主档
- `medical_records`：病历记录
- `patient_followup`：随访记录
- `patient_risk_tag`：风险标签
- `patient_timeline`：患者事件时间线
- `patient_images`：影像资料

### 预约/治疗域
- `appointments`：预约记录
- `treatments`：治疗记录/收费
- `treatment_catalog`：处置收费项目库
- `treatment_plans` 或 `treatment_plans` 对应表：治疗计划

### 门诊运营域
- `finances`：财务流水
- `inventory`：库存
- `purchase`：采购
- `doctors`：医生信息
- `accounts`：后台员工账号
- `users`：登录用户表

## 8.4 Patient360 聚合关系
Patient360 聚合大致关系如下：

- 患者主档：`patients`
- 病历：按 `patient_id` 查 `medical_records`
- 随访：按 `patient_id` 查 `patient_followup`
- 风险标签：按 `patient_id` 查 `patient_risk_tag`
- 时间线：按 `patient_id` 查 `patient_timeline`
- 影像：按 `patient_id` 查 `patient_images`
- 预约：按患者姓名查 `appointments`
- 治疗：按患者姓名查 `treatments`

> 注意：预约与治疗目前有部分地方是通过**患者姓名**关联，而不是稳定主键，这会带来数据一致性风险。

---

## 9. 页面与接口映射

## 9.1 管理后台页面映射
- `login1.vue`
  - `POST /loginController/login`

- `HomeView.vue`
  - `GET /appointments/selectAll`
  - `GET /patients/selectAll`
  - `GET /medical-records/selectAll`
  - `GET /finances/selectByMonth`

- `PatientView.vue`
  - `GET /patients/selectAll`
  - `GET /patients/selectByid`
  - `GET /patients/selectByname`
  - `POST /patients/add`
  - `PUT /patients/edit`
  - `DELETE /patients/delete/{id}`
  - `DELETE /patients/deleteBatch`

- `Patient360View.vue`
  - `GET /patient360/overview/{patientId}`
  - 病历相关接口
  - 随访相关接口
  - 风险标签相关接口
  - 影像上传/读取/删除接口

## 9.2 患者微信门户映射
- `PatientPortalHome.vue`
  - `GET /patient-portal/overview`
  - `POST /patient-portal/appointments/{id}/cancel`

## 9.3 员工微信门户映射
- `StaffPortalHome.vue`
  - `GET /staff-portal/overview`

- `StaffAppointmentH5.vue`
  - `GET /staff-portal/overview`
  - `GET /appointments/selectAll`

- `StaffPatientH5.vue`
  - 依赖患者查询接口

- `StaffPatient360H5.vue`
  - 依赖 `patient360/overview` 或相关子接口

---

## 10. 运行与部署说明

## 10.1 前端本地运行
在 `saas-vue-src/` 目录：

```bash
npm install
npm run serve
```

默认访问：
- `http://localhost:7070`

## 10.2 后端本地运行
在 `saas-springboot-src/` 目录：

```bash
mvn spring-boot:run
```

或打包：

```bash
mvn clean package
java -jar target/*.jar
```

前提：
- 本地有 Java 17
- 本地有 MySQL
- 数据库 `clinic_system_new` 可访问
- 相关主表已存在
- 附加表可通过初始化脚本创建

## 10.3 需要准备的外部条件
- MySQL 服务
- 微信公众号 / OAuth 配置
- 回调域名可访问（配置中指向 `https://saas.shuao.cc`）
- 本地或服务器磁盘可写（影像上传目录）

## 10.4 前后端联调模式
当前代码默认模式看起来是：
- 前端 dev server：7070
- 后端本地接口直接访问
- 控制器里部分 CORS 明确允许 `http://localhost:7070`

---

## 11. 已识别的问题与风险清单

## 11.1 安全风险
### 1) 明文密码登录
`loginController.java` 直接做：
- `user.getPassword().equals(password)`

风险：
- 密码未加密存储/校验
- 无法满足正式环境安全要求

### 2) 配置中硬编码敏感信息
`application.yml` 中直接写了：
- 数据库账号密码
- 微信 `app-secret`

风险：
- 泄漏风险高
- 不利于多环境部署

### 3) 鉴权薄弱
当前看到的是：
- 登录后前端仅保存 localStorage 信息
- 未看到完整 JWT / Session 权限中间件
- 未看到统一鉴权过滤器

风险：
- 接口级权限控制不足
- 前端菜单控制不等于后端权限控制

## 11.2 架构与数据一致性风险
### 4) 患者关联部分依赖姓名而不是稳定主键
例如：
- `appointmentService.selectPatientAppointments(patient.getName())`
- `treatmentMapper.selectByPatientName(patient.getName())`

风险：
- 患者重名时串数据
- 患者改名后历史数据失联

### 5) 表结构脚本重复
`schema-extra.sql` 中 `treatment_catalog` 出现重复建表。

风险：
- 维护混乱
- 说明 SQL 演进过程未清理

### 6) 接口风格不统一
例如：
- `selectByid`
- `select1Byid`
- `/Inventory` 大写开头
- 某些删除/更新返回 `String`，某些返回 `Result`

风险：
- 前后端维护成本上升
- API 规范性较差

## 11.3 部署风险
### 7) SPA 路由转发可能与前端真实路径不完全一致
`SpaForwardController.java` 中手工维护了一批路径，存在：
- 大小写不统一
- 与前端真实路由可能不完全同步

风险：
- 刷新页面 404
- 新增页面后忘记同步转发配置

### 8) 影像文件落本地磁盘
目录：`~/.local/uploads/patient-images/`

风险：
- 不适合容器化/多实例部署
- 扩容困难
- 文件迁移与备份复杂

### 9) CORS 放得过宽
`allowedOriginPatterns("*")`

风险：
- 生产环境暴露面过大

---

## 12. 产品能力评价

从产品角度看，这个项目已经具备一个牙科门诊 SaaS MVP 的核心骨架：

### 12.1 已具备的亮点
- 患者 360 视图
- 患者病历、影像、随访、风险标签
- 预约与治疗联动
- 财务与库存基础模块
- 患者微信门户
- 员工微信 H5 工作台
- 次日预约提醒定时任务

### 12.2 仍偏 MVP/原型的地方
- 权限体系偏弱
- API 命名不规范
- 数据关联有历史设计痕迹
- 影像存储与部署方案偏单机化
- 配置管理不适合正式 SaaS 多环境

---

## 13. 结论

这套源码已经不是“纯演示页面”，而是一个**具备真实门诊业务逻辑雏形**的口腔门诊 SaaS 系统。

其最核心的业务价值集中在：
- 患者管理
- 患者 360
- 预约与治疗闭环
- 微信患者门户
- 微信员工工作台

如果后续要继续推进，建议优先围绕以下方向做升级：
1. 统一权限与登录安全
2. 统一 API 命名与返回结构
3. 修复患者主键关联问题
4. 清理路由转发与 SQL 初始化脚本
5. 将影像上传迁移到对象存储

---

## 14. 后续可继续补充的文档
如果继续深挖，可以继续产出：
- 全量接口清单（逐 Controller 逐 endpoint）
- 数据表关系图
- 页面-按钮-接口动作清单
- 本地运行排障手册
- SaaS 产品改造建议书
