# 阶段三：业务闭环与 AI 场景扩展

> 目标：打通预约→病历→处置→收费核心业务流，重构 AI 对话 UI 并扩展 AI 到临床场景
> 预计工期：2-3 周
> 前置条件：阶段二完成，UI 组件库和 Patient360 按需加载已就绪

---

## 一、任务清单

### 1.1 业务闭环优化

#### 1.1.1 预约 → 病历快捷流转
- **涉及文件**：
  - `AppointmentView.vue`
  - `MedicalRecordView.vue` / `MedicalRecordView.vue`（新增/编辑组件）
  - `AppointmentController.java`
  - `MedicalRecordController.java`
- **需求**：
  1. **"开始就诊"按钮**：在 AppointmentView 的列表/日历中，每个"待就诊"预约增加"开始就诊"操作按钮
  2. **自动带入患者信息**：点击后跳转至病历新增页面（`/medical-records/add`），URL 携带 `patientId` 和 `appointmentId` 参数
  3. **病历页面自动填充**：`MedicalRecordView` 新增模式读取 URL 参数，自动填充患者姓名、就诊日期（当天）、关联预约 ID
  4. **预约状态联动**：病历保存成功后，自动调用 `PUT /appointments/{id}/status` 将对应预约状态更新为"已到诊"
  5. **防重复**：已就诊的预约隐藏"开始就诊"按钮，显示"查看病历"按钮

#### 1.1.2 病历 → 处置自动同步
- **涉及文件**：
  - `MedicalRecordService.java`
  - `TreatmentController.java`
  - `TreatmentService.java`
  - `MedicalRecordView.vue`
- **需求**：
  1. **病历保存时可选"创建处置"**：在病历编辑页的"治疗计划"区块下方，增加复选框"同时创建处置记录"
  2. **自动创建处置**：勾选后，病历保存时根据治疗计划内容自动创建 `treatment` 记录：
     - `patient_id` / `patient_name` 从病历带入
     - `treatment_date` = 当天
     - `treatment_content` = 治疗计划摘要
     - `doctor_account_id` / `doctor_name` = 病历医生
     - `medical_record_id` = 刚保存的病历 ID
  3. **处置列表关联展示**：在病历详情页展示关联的处置记录卡片，可点击跳转

#### 1.1.3 处置 → 收费一键生成
- **涉及文件**：
  - `TreatmentView.vue`
  - `FinanceController.java`
  - `FinanceService.java`
  - `TreatmentService.java`
- **需求**：
  1. **"生成收费单"按钮**：在 TreatmentView 的处置列表中，每条未收费的处置记录增加"生成收费单"按钮
  2. **收费单弹窗**：点击后弹出收费单编辑抽屉/弹窗，自动带入：
     - 患者信息
     - 收费项目 = 处置内容对应的治疗项目（需匹配 `treatment_catalog`）
     - 金额 = 治疗项目单价（可修改）
     - 医生 = 处置医生
  3. **保存关联**：收费单保存时，更新对应 `treatment` 记录的 `finance_id`，并标记为"已收费"
  4. **状态联动**：处置列表中已收费的处置显示绿色"已收费"标签，未收费显示橙色"待收费"标签

#### 1.1.4 预约智能提醒
- **涉及文件**：
  - `AppointmentService.java`
  - `AppointmentController.java`（新增查询接口）
  - `Manager.vue` 或 `HomeView.vue`
- **需求**：
  1. **后端定时查询**：`AppointmentService` 增加方法查询"未来 30 分钟内待就诊的预约"
  2. **首页待办提醒**：HomeView 的待办列表增加"即将到达的预约"区块，展示：患者姓名、预约时间、项目、医生
  3. **系统通知**：使用 Element UI `Notification` 组件，在医生登录后弹出今日待办提醒（可关闭）
  4. **处置未收费提醒**：HomeView 增加"待收费处置"提醒，展示今日已完成但未生成收费单的处置记录

#### 1.1.5 耗材/义齿供应链联动（可选，时间紧张可延后）
- **涉及文件**：
  - `MedicalRecordOperationService.java`
  - `LabOrderService.java`
  - `InventoryController.java`
- **需求**：
  1. 病历中开具的义齿加工单（`operation_items` 中类型为义齿）保存时，自动创建 `lab_orders` 记录
  2. 技工账单确认后，遍历账单中的项目，若对应耗材库存存在，自动扣减库存数量
  3. 耗材库存低于安全线时，在 HomeView 和 MaterialView 中显示预警

### 1.2 AI 对话 UI 重构

#### 1.2.1 现代化 Chat UI 组件
- **文件路径**：
  - `saas-vue-src/src/components/ai/ChatContainer.vue`
  - `saas-vue-src/src/components/ai/ChatMessage.vue`
  - `saas-vue-src/src/components/ai/ChatInput.vue`
  - `saas-vue-src/src/components/ai/ChatSidebar.vue`
- **需求**：
  1. **ChatContainer.vue**：
     - 三栏布局（左会话列表 / 中对话区 / 右功能面板）
     - 支持调整左右栏宽度（拖拽分隔线）
     - 对话区自动滚动到底部
  2. **ChatMessage.vue**：
     - 用户消息：右侧对齐，白色背景气泡，圆角不对称（类似 iMessage）
     - AI 消息：左侧对齐，浅灰背景，带 AI 头像（品牌 Logo 或机器人图标）
     - 支持 Markdown 渲染（使用 `marked` 库或 `markdown-it`）
     - 代码块支持语法高亮（`highlight.js`）
     - 表格使用自定义样式包裹，增加横向滚动
     - 消息下方显示时间戳和"复制"按钮
  3. **ChatInput.vue**：
     - 多行文本输入框（`textarea`），自适应高度（最多 5 行）
     - 底部快捷问题胶囊（可横向滚动）
     - 发送按钮（Enter 发送，Shift+Enter 换行）
     - 上传按钮（预留，阶段三暂不上传功能）
  4. **ChatSidebar.vue**：
     - 顶部"新建对话"按钮
     - 会话列表：标题（取用户第一条消息前 20 字）、时间、删除按钮
     - 当前会话高亮
     - 空状态："开始新的对话"

#### 1.2.2 SSE 流式输出支持
- **涉及文件**：
  - `BusinessAnalysisChatService.java`
  - `BusinessDailyAnalysisController.java`（新增 SSE 端点）
  - `ChatContainer.vue`
- **需求**：
  1. **后端 SSE 接口**：新增 `GET /chat/stream?sessionId=xxx&message=xxx`，返回 `text/event-stream`
  2. **前端 EventSource**：使用原生 `EventSource` 或 `fetch` + `ReadableStream` 接收流式数据
  3. **打字机效果**：每收到一个 chunk，追加到当前 AI 消息中，光标闪烁
  4. **错误处理**：连接断开时显示"连接已断开，请重试"按钮
  5. **兼容非流式**：若浏览器不支持或后端未开启流式，自动 fallback 到普通 HTTP 请求

### 1.3 AI 场景扩展

#### 1.3.1 AI 病历摘要
- **涉及文件**：
  - `BusinessDailyAnalysisController.java`（新增端点）
  - `MedicalRecordView.vue`
  - `saas-vue-src/src/components/ai/AiSummaryCard.vue`
- **需求**：
  1. **后端接口**：`POST /ai/summarize-medical-record`，接收病历 JSON，调用 LLM 生成结构化摘要：
     - 主诉摘要（一句话）
     - 关键诊断（列表）
     - 治疗计划要点（列表）
     - 医嘱提醒（列表）
  2. **前端展示**：在病历详情页增加"AI 摘要"卡片，位于病历内容上方，可折叠
  3. **一键生成**：医生点击"生成 AI 摘要"按钮，显示 loading，生成后展示结果
  4. **Prompt 设计**：System Prompt 要求 LLM 以专业医疗语言输出，不添加免责声明

#### 1.3.2 AI 智能随访建议
- **涉及文件**：
  - `FollowupManagementView.vue`
  - `PatientFollowupController.java`（新增端点）
- **需求**：
  1. **后端接口**：`POST /ai/followup-suggestion`，接收患者 ID，查询患者最近病历和治疗记录，生成：
     - 建议随访时间（如"7 天后"）
     - 随访话术模板（可直接复制给患者发送）
     - 需要关注的要点（如伤口恢复、用药提醒）
  2. **前端展示**：在随访管理页面，每个患者行增加"AI 建议"按钮，点击后抽屉展示建议内容
  3. **一键应用**：可将 AI 建议的随访时间和话术直接填充到新增随访表单中

#### 1.3.3 AI 经营洞察问答
- **涉及文件**：
  - `BusinessDailyAnalysisController.java`（扩展）
  - `BusinessAnalysisChatService.java`（扩展）
- **需求**：
  1. **自然语言查询**：在 AI 经营日报的对话中，用户可以用自然语言提问，如：
     - "上周哪个医生的业绩最高？"
     - "本月耗材支出占比是多少？"
     - "最近 30 天预约取消率趋势？"
  2. **Function Calling**：后端引入 Tool/Function 机制，LLM 可调用预定义的函数查询数据：
     - `query_doctor_performance(startDate, endDate)`
     - `query_material_cost_ratio(month)`
     - `query_appointment_cancel_rate(days)`
  3. **数据可视化**：AI 返回数据后，前端自动渲染为 ECharts 迷你图表（如回答中包含趋势数据）
  4. **Prompt 设计**：System Prompt 要求 LLM 在无法回答时引导用户细化问题，不编造数据

#### 1.3.4 AI 患者风险评估
- **涉及文件**：
  - `Patient360View.vue`
  - `PatientInsightController.java`（新增或扩展）
- **需求**：
  1. **后端接口**：`POST /ai/patient-risk-assessment/{patientId}`，聚合患者全部数据（病历、处置、影像描述、随访记录），生成风险评估：
     - 风险等级（低/中/高）
     - 风险标签（如"糖尿病史需关注伤口愈合"、"多次取消预约"）
     - 建议关注事项（列表）
  2. **前端展示**：在 Patient360 的"概述"Tab 顶部增加"AI 风险评估"徽章卡片：
     - 低风险：绿色
     - 中风险：橙色
     - 高风险：红色
     - 点击展开详细评估报告
  3. **动态更新**：每次患者新增病历或处置后，后台异步重新计算风险等级

---

## 二、关键修改文件汇总

| 模块 | 文件路径 | 修改类型 |
|------|---------|---------|
| 业务流转 | `AppointmentView.vue` | 修改 |
| 业务流转 | `MedicalRecordView.vue` | 修改 |
| 业务流转 | `TreatmentView.vue` | 修改 |
| 业务流转 | `HomeView.vue` | 修改 |
| 业务流转 | `AppointmentController.java` | 修改 |
| 业务流转 | `MedicalRecordController.java` | 修改 |
| 业务流转 | `TreatmentController.java` | 修改 |
| 业务流转 | `FinanceController.java` | 修改 |
| 业务流转 | `AppointmentService.java` | 修改 |
| 业务流转 | `MedicalRecordService.java` | 修改 |
| 业务流转 | `TreatmentService.java` | 修改 |
| AI Chat UI | `src/components/ai/ChatContainer.vue` | 新增 |
| AI Chat UI | `src/components/ai/ChatMessage.vue` | 新增 |
| AI Chat UI | `src/components/ai/ChatInput.vue` | 新增 |
| AI Chat UI | `src/components/ai/ChatSidebar.vue` | 新增 |
| AI Chat UI | `BusinessAnalysisView.vue` | 修改（接入新 Chat UI） |
| AI Chat UI | `BusinessDailyAnalysisController.java` | 修改（新增 SSE 端点） |
| AI Chat UI | `BusinessAnalysisChatService.java` | 修改（支持 SSE 流式） |
| AI 场景 | `BusinessDailyAnalysisController.java` | 修改（新增端点） |
| AI 场景 | `PatientFollowupController.java` | 修改 |
| AI 场景 | `PatientInsightController.java` | 修改 |
| AI 场景 | `AiSummaryCard.vue` | 新增 |
| AI 场景 | `FollowupManagementView.vue` | 修改 |
| AI 场景 | `Patient360View.vue` | 修改（接入 AI 风险评估） |

---

## 三、验收标准

- [ ] 在 AppointmentView 点击"开始就诊"可跳转至病历新增页，自动带入患者信息和预约 ID
- [ ] 病历保存后，对应预约状态自动变为"已到诊"
- [ ] 病历编辑页勾选"同时创建处置"后，保存时自动创建关联处置记录
- [ ] TreatmentView 中未收费处置可一键生成收费单，生成后处置标记为"已收费"
- [ ] HomeView 展示"即将到达的预约"和"待收费处置"提醒
- [ ] AI 对话 UI 支持左右分栏、Markdown 渲染、代码高亮、流式输出
- [ ] 在病历详情页可生成 AI 摘要，展示结构化主诉/诊断/治疗计划/医嘱
- [ ] 随访管理页可获取 AI 随访建议（时间 + 话术），并一键应用到表单
- [ ] AI 经营日报对话支持自然语言查询，LLM 可调用后端函数获取实时数据
- [ ] Patient360 概述页展示 AI 风险评估徽章（绿/橙/红），可展开查看详情
- [ ] 所有新增 AI 功能均有 loading 状态和错误处理，API 失败时不阻断页面其他功能

---

## 四、风险与注意事项

1. **Function Calling 实现复杂度**：OpenAI Responses API 的 Function Calling 与标准 Chat Completion 不同，需确认 `gpt-5.4-mini` 的 Tool Use 支持情况和调用格式
2. **SSE 跨域/代理问题**：前端 devServer 代理可能不支持 SSE 流式传输，需在 `vue.config.js` 中配置 `eventSource` 代理或直接使用后端地址
3. **AI 幻觉风险**：LLM 可能生成不准确的医疗建议，所有 AI 输出必须标注"仅供参考，请以医生判断为准"
4. **数据隐私**：AI 接口传输患者病历数据，需确保 API 提供商（OpenAI/百炼）符合医疗数据隐私要求，或考虑本地部署模型
5. **性能影响**：Patient360 的 AI 风险评估涉及大量数据聚合，建议异步计算并缓存结果，避免阻塞页面加载
6. **供应链联动复杂度**：耗材/义齿自动同步涉及多个模块的交叉逻辑，若时间紧张建议拆分到阶段四或作为独立迭代
