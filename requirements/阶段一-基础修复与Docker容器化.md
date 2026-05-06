# 阶段一：基础修复与 Docker 容器化

> 目标：让系统具备稳定的开发/部署基座，修复底层数据一致性与安全问题
> 预计工期：1-2 周
> 前置条件：本地开发环境可正常运行（Vue 前端 7070 + Spring Boot 后端 8080 + MySQL）

---

## 一、任务清单

### 1.1 Docker 全套容器化

#### 1.1.1 前端 Dockerfile
- **文件路径**：`shuao-clinic-saas-source/saas-vue-src/Dockerfile`
- **需求**：
  - 多阶段构建：Stage1 使用 `node:18-alpine` 执行 `npm install && npm run build`
  - Stage2 使用 `nginx:alpine` 运行，复制 `dist/` 到 `/usr/share/nginx/html`
  - 提供默认 `nginx.conf`：支持 History 模式路由（`try_files $uri $uri/ /index.html`）
  - 支持通过环境变量 `VUE_APP_API_BASE_URL` 注入后端 API 地址（用于 Nginx 反向代理或前端 Axios baseURL）
  - `.dockerignore` 排除 `node_modules/`、`dist/`

#### 1.1.2 后端 Dockerfile
- **文件路径**：`shuao-clinic-saas-source/saas-springboot-src/Dockerfile`
- **需求**：
  - 基于 `eclipse-temurin:17-jre-alpine` 构建轻量镜像
  - 先复制 `pom.xml` 和 `src/` 执行 `mvn clean package -DskipTests`
  - 或使用多阶段构建：Stage1 `maven:3.9-eclipse-temurin-17` 打包，Stage2 仅复制 JAR
  - 暴露端口 `8080`
  - `ENTRYPOINT` 使用 `java -jar` 启动
  - 支持通过环境变量注入所有 `application.yml` 配置项

#### 1.1.3 docker-compose.yml
- **文件路径**：`shuao-clinic-saas-source/docker-compose.yml`
- **需求**：
  - 服务定义：`mysql`、`backend`、`frontend`
  - 网络：自定义 bridge 网络 `clinic-network`
  - **mysql 服务**：
    - 镜像：`mysql:8.0`（兼容现有 Flyway）
    - 端口映射：`3306:3306`
    - 环境变量：`MYSQL_ROOT_PASSWORD`、`MYSQL_DATABASE=clinic_system`
    - 数据卷：`mysql_data` 持久化 + `./init.sql` 初始化脚本（可选）
    - `healthcheck`：`mysqladmin ping`
  - **backend 服务**：
    - 构建上下文：`saas-springboot-src/`
    - 依赖：`mysql` 健康检查通过后才启动
    - 环境变量：`DB_URL`、`DB_USER`、`DB_PASSWORD`、`OPENAI_API_KEY` 等
    - 端口：`8080:8080`
    - 数据卷：映射 `~/.local/uploads/patient-images` 用于影像存储
  - **frontend 服务**：
    - 构建上下文：`saas-vue-src/`
    - 依赖：`backend`
    - 端口：`7070:80`（Nginx 80 映射到主机 7070）
    - 或者通过 Nginx 反向代理 `/api` 到 backend
  - `.env.example`：提供开发环境默认配置模板
  - `.env`：实际环境变量文件（加入 `.gitignore`）

#### 1.1.4 部署脚本
- **文件路径**：`shuao-clinic-saas-source/scripts/docker-deploy.sh`
- **需求**：
  - 一键执行 `docker-compose down && docker-compose up -d --build`
  - 等待 backend 健康检查通过后输出访问地址
  - 支持 `--prod` 参数使用生产环境 `.env.prod`

### 1.2 数据一致性修复

#### 1.2.1 patient_name 级联更新
- **文件路径**：
  - `saas-springboot-src/src/main/java/com/example/springboot/service/PatientService.java`
  - `saas-springboot-src/src/main/java/com/example/springboot/mapper/PatientMapper.java`（或对应的 XML）
- **需求**：
  1. 在 `PatientService.updatePatient()` 方法中，若患者姓名发生变更，级联更新以下表中的 `patient_name`：
     - `appointments`
     - `medical_records`
     - `treatment`
     - `finance`
     - `lab_orders`
     - `patient_followups`
     - `patient_timeline`
     - `patient_consents`
     - `patient_images`
  2. 各 Mapper 中新增 `updatePatientNameByPatientId(@Param("patientId") Long patientId, @Param("patientName") String patientName)` 方法
  3. 级联更新操作放在同一个 Spring 事务中（`@Transactional`），确保原子性
  4. **决策点**：若数据量极大（单表百万级），需评估锁表风险，可考虑改为异步消息队列或批次处理

#### 1.2.2 级联删除补全
- **文件路径**：`saas-springboot-src/src/main/java/com/example/springboot/service/PatientService.java`
- **需求**：
  1. 在 `PatientService.deletePatient()` 中补充删除：
     - `patient_referral_records`（通过 `referrer_patient_id` 或 `referred_patient_id` 关联）
     - `patient_insight_summary`（通过 `patient_id` 关联）
  2. 新增对应的 Mapper 删除方法
  3. 编写 Flyway 迁移脚本 `V33__CleanOrphanReferralAndInsightData.sql` 清理历史孤儿数据

### 1.3 Patient360 按需加载拆分

#### 1.3.1 后端 API 拆分
- **文件路径**：
  - `saas-springboot-src/src/main/java/com/example/springboot/controller/Patient360Controller.java`
  - 新增/修改相关 Service 和 Mapper
- **需求**：
  1. 将原 `GET /patient360/{patientId}` 巨型接口拆分为：
     - `GET /patient360/{patientId}/basic` — 患者基本信息 + 客户来源 + 转诊信息
     - `GET /patient360/{patientId}/medical-records` — 病历列表（分页）
     - `GET /patient360/{patientId}/appointments` — 预约列表（分页）
     - `GET /patient360/{patientId}/treatments` — 处置记录（分页）
     - `GET /patient360/{patientId}/timeline` — 时间轴事件（分页）
     - `GET /patient360/{patientId}/images` — 影像资料（分页）
     - `GET /patient360/{patientId}/followups` — 随访记录
     - `GET /patient360/{patientId}/risk-tags` — 风险标签
     - `GET /patient360/{patientId}/consents` — 知情同意书
     - `GET /patient360/{patientId}/insight` — AI 洞察摘要
  2. 原接口保留但标记 `@Deprecated`，兼容旧前端调用
  3. 各子接口使用独立查询，避免一个模块异常导致整体失败

#### 1.3.2 前端异步加载改造
- **文件路径**：`saas-vue-src/src/views/Manager/Patient360View.vue`
- **需求**：
  1. 页面结构改为 Tab 卡片式布局，每个 Tab 对应一个子模块
  2. 进入页面时先加载 `basic` 信息，其余 Tab 按需懒加载（点击 Tab 时才请求）
  3. 各区块独立 loading 状态，某个区块失败不影响其他区块展示
  4. 使用骨架屏（Element UI `v-loading` + `el-skeleton`）提升首屏体验

### 1.4 安全加固

#### 1.4.1 二级密码外置
- **文件路径**：
  - `saas-springboot-src/src/main/java/com/example/springboot/controller/PatientController.java`
  - `saas-springboot-src/src/main/resources/application.yml`
- **需求**：
  1. 移除硬编码的 `PATIENT_ADMIN_SECONDARY_PASSWORD = "246810"`
  2. 在 `application.yml` 中新增配置项：`app.security.secondary-password: ${SECONDARY_PASSWORD:246810}`
  3. 通过 `@Value("${app.security.secondary-password}")` 注入到 Controller
  4. 提供环境变量覆盖能力，生产环境必须修改默认值

---

## 二、关键修改文件汇总

| 模块 | 文件路径 | 修改类型 |
|------|---------|---------|
| Docker | `saas-vue-src/Dockerfile` | 新增 |
| Docker | `saas-vue-src/nginx.conf` | 新增 |
| Docker | `saas-vue-src/.dockerignore` | 新增 |
| Docker | `saas-springboot-src/Dockerfile` | 新增 |
| Docker | `docker-compose.yml` | 新增 |
| Docker | `.env.example` | 新增 |
| Docker | `scripts/docker-deploy.sh` | 新增 |
| 数据修复 | `PatientService.java` | 修改 |
| 数据修复 | `PatientMapper.java` / XML | 修改 |
| 数据修复 | `db/migration/V33__CleanOrphan...` | 新增 |
| Patient360 | `Patient360Controller.java` | 修改 |
| Patient360 | `Patient360Service.java` | 修改 |
| Patient360 | `Patient360View.vue` | 修改 |
| 安全 | `PatientController.java` | 修改 |
| 安全 | `application.yml` | 修改 |

---

## 三、验收标准

- [ ] 执行 `docker-compose up -d` 后，3 分钟内可通过 `http://localhost:7070` 访问系统并正常登录
- [ ] MySQL 数据持久化正常（`docker-compose down` 后重新 `up`，数据不丢失）
- [ ] 修改患者姓名后，所有关联表中的 `patient_name` 同步更新
- [ ] 删除患者后，`patient_referral_records` 和 `patient_insight_summary` 中无孤儿数据
- [ ] Patient360 页面首屏加载时间 < 1s（仅加载 basic 信息）
- [ ] 二级密码可通过环境变量 `SECONDARY_PASSWORD` 动态配置

---

## 四、风险与注意事项

1. **Docker 镜像体积**：前端 `node_modules` 和后端 Maven 依赖可能导致镜像过大，需确保多阶段构建仅复制产物
2. **MySQL 8.0 兼容性**：现有 Flyway 脚本在 MySQL 8.0 下需验证（原使用 MySQL 9.6，但 Docker 中 8.0 更稳定）
3. **Flyway 迁移顺序**：新增 V33 脚本必须在 V32 之后执行，不能修改已执行的历史脚本
4. **Patient360 旧接口兼容**：拆分 API 后，需确认 H5/微信门户是否有调用原接口，避免破坏
5. **级联更新性能**：若 clinic_system 数据量较大，级联更新可能耗时较长，建议在事务内执行并设置合理超时
