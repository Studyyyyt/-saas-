# 阶段二：UI 现代化改造

> 目标：建立统一设计规范，完成高频核心页面的视觉升级，提升系统高级感与现代感
> 预计工期：2-3 周
> 前置条件：阶段一完成，系统可稳定运行

---

## 一、任务清单

### 1.1 建立 Design System

#### 1.1.1 Design Token 定义
- **文件路径**：`saas-vue-src/src/styles/design-tokens.css`
- **需求**：
  - **颜色系统**：
    - Primary: `#2563eb` → `#1d4ed8` (渐变起点)
    - Success: `#10b981`
    - Warning: `#f59e0b`
    - Danger: `#ef4444`
    - 背景层级：`--bg-page: #f1f5f9`、`--bg-card: #ffffff`、`--bg-hover: #f8fafc`
    - 文字层级：`--text-primary: #0f172a`、`--text-secondary: #475569`、`--text-tertiary: #94a3b8`
    - 边框：`--border-color: #e2e8f0`
  - **间距系统**：
    - `--space-xs: 4px`、`--space-sm: 8px`、`--space-md: 12px`、`--space-lg: 16px`、`--space-xl: 24px`、`--space-2xl: 32px`
  - **圆角系统**：
    - `--radius-sm: 8px`、`--radius-md: 12px`、`--radius-lg: 16px`、`--radius-xl: 20px`、`--radius-full: 9999px`
  - **阴影系统**：
    - `--shadow-sm: 0 1px 2px rgba(0,0,0,0.05)`
    - `--shadow-md: 0 4px 6px rgba(0,0,0,0.07)`
    - `--shadow-lg: 0 10px 15px rgba(0,0,0,0.1)`
    - `--shadow-card: 0 8px 24px rgba(31,71,136,.08)`
  - **字体系统**：复用现有 `typography-scale.css`，补充行高和字重变量

#### 1.1.2 全局 Element UI 主题覆写
- **文件路径**：`saas-vue-src/src/styles/element-overrides.css`
- **需求**：
  - 按钮：统一圆角 `14px`，主按钮使用渐变背景，hover 时增加阴影上移效果
  - 输入框/选择框：圆角 `10px`，focus 状态边框颜色使用 Primary，增加微弱发光
  - 表格：表头背景 `#f8fafc`，字体 `#475569`，行 hover 背景 `#f1f5f9`，移除纵向边框
  - 卡片：`border-radius: 18px`，`box-shadow: var(--shadow-card)`
  - 标签/徽章：圆角 `6px`，增加 padding
  - 弹窗/抽屉：圆角 `20px`，标题字体加粗，增加进入/退出过渡动画
  - 分页器：圆角按钮，当前页使用 Primary 渐变背景
  - 消息提示：`border-radius: 12px`，增加阴影

#### 1.1.3 全局布局组件升级
- **文件路径**：`saas-vue-src/src/App.vue`、`src/views/Manager.vue`
- **需求**：
  - **Manager.vue 侧边栏**：
    - 背景色从纯色改为 `#ffffff` 带右侧微阴影
    - 菜单项增加圆角 `8px`，hover 背景 `#f1f5f9`
    - 当前选中项左侧增加 `4px` Primary 色竖线指示器
    - 支持折叠为图标模式，折叠后宽度 `64px`
    - 菜单分组标题使用 `text-tertiary` 小字
  - **顶部导航栏**（如有）：增加毛玻璃效果（`backdrop-filter: blur(10px)`）
  - **页面通用布局**：统一 hero-card + query-card + table-card 三段式结构

### 1.2 核心页面升级

#### 1.2.1 HomeView（首页仪表盘）
- **文件路径**：`saas-vue-src/src/views/Manager/HomeView.vue`
- **当前问题**：传统后台统计卡片，信息密度低，缺乏数据可视化
- **优化方向**：
  1. **Hero 区域**：
     - 大标题"今日工作台" + 日期 + 当前登录医生姓名
     - 关键指标 KPI 卡片横排：今日预约数、今日就诊数、今日收入、待随访数
     - 每个 KPI 卡片带趋势箭头（较昨日环比）和微图标
  2. **图表区域**：
     - 左侧：近 7 天收入趋势折线图（ECharts，使用渐变填充面积）
     - 右侧：今日预约状态分布饼图（已到诊/待就诊/已取消）
  3. **待办列表**：
     - 今日预约列表（带患者姓名、时间、项目、状态标签）
     - 最近 3 天需随访患者提醒
     - 耗材库存预警（低于安全线）
  4. **快捷操作**：新增患者、新增预约、查看经营日报快捷入口按钮
  5. **响应式**：平板端图表上下堆叠，手机端隐藏图表仅保留 KPI 和待办

#### 1.2.2 Patient360View（患者 360 视图）
- **文件路径**：`saas-vue-src/src/views/Manager/Patient360View.vue`
- **当前问题**：单页聚合过多模块，加载慢，信息堆叠密集
- **优化方向**：
  1. **顶部患者档案卡**：
     - 左侧：患者头像（首字母占位符）、姓名、性别年龄、手机号
     - 右侧：标签云（客户来源、风险标签、分组）、欠款金额（如有）
     - 背景使用微渐变或品牌色卡片
  2. **Tab 导航**：
     - 概述 / 病历 / 预约 / 处置 / 收费 / 时间轴 / 影像 / 随访 / 知情同意
     - Tab 切换时带动画过渡
  3. **概述 Tab**：
     - 患者基本信息卡片（可展开/折叠）
     - 最近就诊记录摘要（最近一次病历、处置、收费）
     - AI 洞察摘要（如有）
  4. **时间轴 Tab**：
     - 使用 `el-timeline` 升级样式，事件类型用颜色区分（就诊-蓝、收费-绿、随访-橙）
     - 支持点击事件跳转到对应详情
  5. **影像 Tab**：
     - 网格画廊布局，hover 放大预览
     - 支持上传新影像拖拽区域
  6. **病历 Tab**：
     - 卡片式列表，每张卡片展示主诉、诊断、治疗计划摘要
     - 支持展开查看完整病历
  7. **所有 Tab 独立 loading**，使用骨架屏占位

#### 1.2.3 AppointmentView（预约管理）
- **文件路径**：`saas-vue-src/src/views/Manager/AppointmentView.vue`
- **当前问题**：纯表格展示，缺乏日历视图，医生排班不直观
- **优化方向**：
  1. **视图切换**：顶部增加"日历视图" / "列表视图"切换按钮组
  2. **日历视图**（新增）：
     - 周视图为主，左侧医生列，顶部时间轴（08:00-18:00）
     - 每个预约以卡片形式展示在对应时间段，带患者姓名和项目
     - 支持拖拽调整预约时间（需后端 API 支持 `PUT /appointments/{id}/reschedule`）
     - 不同状态使用不同颜色边框（已到诊-绿、待就诊-蓝、取消-灰）
  3. **列表视图**（现有升级）：
     - 表格行增加患者头像占位符
     - 状态列使用彩色圆点 + 文字
     - 操作列增加"开始就诊"快捷按钮（直接跳转到病历新增页并带入患者信息）
  4. **查询区域**：
     - 使用卡片包裹，圆角阴影
     - 日期范围选择器、医生选择器、状态选择器、患者搜索框
  5. **新增预约弹窗**：
     - 步骤化表单：选择患者 → 选择日期时间 → 选择医生和项目 → 确认
     - 患者选择支持搜索和最近就诊患者快捷列表

#### 1.2.4 BusinessAnalysisView（AI 经营日报）
- **文件路径**：`saas-vue-src/src/views/Manager/BusinessAnalysisView.vue`
- **当前问题**：Element UI 表单风格对话 UI，缺乏现代聊天体验
- **优化方向**：
  1. **整体布局改为三栏式**：
     - 左栏：会话历史列表（可新建、重命名、删除会话）
     - 中栏：对话区域（主区域）
     - 右栏：快捷功能面板（生成日报、生成周报、扫描异常、测试连接）
  2. **对话区域**：
     - 用户消息：右侧气泡，白色背景，圆角 `16px 16px 4px 16px`
     - AI 消息：左侧卡片，浅灰背景 `#f8fafc`，圆角 `16px 16px 16px 4px`
     - 支持 Markdown 渲染（表格、列表、加粗、代码块）
     - AI 消息顶部显示模型名称和生成时间
     - 流式输出时显示打字机光标动画
  3. **输入区域**：
     - 底部固定输入框，圆角 `24px`，带发送按钮和快捷问题推荐胶囊
     - 支持 Enter 发送，Shift+Enter 换行
     - 输入框上方显示"AI 正在输入..."状态
  4. **日报/周报展示卡片**：
     - 使用大卡片展示 headline、summary、risks、opportunities、actions
     - 支持一键导出为 PDF 或复制 Markdown
  5. **夜间模式支持**（可选）：增加暗色主题切换按钮

#### 1.2.5 MedicalRecordView（病历管理）
- **文件路径**：`saas-vue-src/src/views/Manager/MedicalRecordView.vue`
- **当前问题**：表单密集，可读性差，牙位信息不直观
- **优化方向**：
  1. **列表页升级**：
     - 卡片式列表替代表格，每张卡片展示：患者信息、就诊日期、主诉摘要、诊断标签、操作按钮
     - 支持按日期范围、医生、患者搜索筛选
  2. **病历详情/编辑页升级**：
     - 使用分区块布局（基础信息 / 主诉与现病史 / 检查与诊断 / 治疗计划 / 医嘱）
     - 每个区块使用卡片包裹，可折叠
     - **牙位图形化**（关键）：增加口腔牙位图组件（上颌/下颌 16 颗牙），点击牙齿可直接选择牙位，选中牙齿高亮显示
     - 已选牙位以标签形式展示在牙位图下方，可删除
  3. **病历新增快捷入口**：在 Patient360 的病历 Tab 中直接新增，自动带入患者信息

### 1.3 全局组件封装

#### 1.3.1 通用业务组件
- **文件路径**：`saas-vue-src/src/components/design-system/`
- **需求**：
  1. **KpiCard.vue**：KPI 指标卡片（标题、数值、趋势、图标）
  2. **PageHeader.vue**：页面头部（标题、副标题、操作按钮区）
  3. **QueryCard.vue**：查询条件卡片（统一圆角阴影包裹）
  4. **DataTable.vue**：数据表格封装（统一表头样式、空状态、loading、分页）
  5. **TimelineItem.vue**：时间轴事件项（图标、颜色、标题、时间、描述）
  6. **ToothSelector.vue**：牙位选择器组件（图形化牙位图）
  7. **ChatMessage.vue**：聊天消息项（用户/AI 两种样式，支持 Markdown）
  8. **ChatInput.vue**：聊天输入框（带发送、快捷推荐、上传）

#### 1.3.2 动画与过渡
- **文件路径**：`saas-vue-src/src/styles/animations.css`
- **需求**：
  - 页面切换：`fade-slide` 过渡（0.3s ease-out）
  - 卡片 hover：`translateY(-2px)` + 阴影增强，0.2s
  - 按钮点击：scale(0.98) 反馈
  - 表格行进入：stagger fade-in
  - Skeleton 加载：shimmer 扫光动画

---

## 二、关键修改文件汇总

| 类型 | 文件路径 | 说明 |
|------|---------|------|
| Design System | `src/styles/design-tokens.css` | 新增设计 Token |
| Design System | `src/styles/element-overrides.css` | 新增 Element UI 覆写 |
| Design System | `src/styles/animations.css` | 新增动画定义 |
| 全局布局 | `src/App.vue` | 可能调整根布局 |
| 全局布局 | `src/views/Manager.vue` | 侧边栏升级 |
| 全局入口 | `src/main.js` | 引入新样式文件 |
| 核心页面 | `src/views/Manager/HomeView.vue` | 重写仪表盘 |
| 核心页面 | `src/views/Manager/Patient360View.vue` | 重写患者 360 |
| 核心页面 | `src/views/Manager/AppointmentView.vue` | 升级预约管理 |
| 核心页面 | `src/views/Manager/BusinessAnalysisView.vue` | 重写 AI 经营日报 |
| 核心页面 | `src/views/Manager/MedicalRecordView.vue` | 升级病历管理 |
| 组件库 | `src/components/design-system/*.vue` | 新增通用组件 |

---

## 三、验收标准

- [ ] Design Token 文件完整定义颜色、间距、圆角、阴影、字体五类变量
- [ ] Element UI 全局覆写后，所有页面按钮、输入框、表格、卡片、弹窗风格统一
- [ ] HomeView 包含 KPI 卡片、ECharts 图表、待办列表三个区域，布局美观
- [ ] Patient360View 支持 Tab 切换懒加载，首屏 < 1s，时间轴和影像画廊可用
- [ ] AppointmentView 支持日历视图和列表视图切换，日历视图可查看医生排班
- [ ] BusinessAnalysisView 对话 UI 支持左右分栏、Markdown 渲染、流式输出视觉效果
- [ ] MedicalRecordView 列表页为卡片式，详情页包含牙位图形化选择器
- [ ] 所有核心页面在 1400px 宽屏和 768px 平板下布局正常（无重叠、无截断）

---

## 四、风险与注意事项

1. **Vue 2 限制**：Vue 2 的响应式和组件系统相比 Vue 3 有局限，部分现代动画效果（如 Teleport、Suspense）无法实现，需在现有能力内做到最好
2. **Element UI 版本**：当前使用 2.15，部分新组件（如 Skeleton）需确认版本支持，若不支持需手写 CSS 实现
3. **ECharts 版本**：确认项目中 ECharts 版本，5.x 支持更现代的渐变和动画效果
4. **牙位图组件**：口腔牙位图需要绘制 SVG 或 Canvas，工作量较大，若时间紧张可先使用下拉多选框作为 fallback
5. **日历视图复杂度**：日历视图（尤其是拖拽排班）涉及复杂的前端交互和新增后端 API，建议作为本阶段的"加分项"，基础版本可先展示不可拖拽的日历
