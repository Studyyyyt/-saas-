# 口腔门诊 SaaS 管理系统 — UI 开发规范文档

> 版本：v1.0
> 日期：2026-05-07
> 参考来源：e看牙系统（工作台视图、预约视图、患者详情页）
> 技术栈：Vue 2.6.14 + Element UI 2.15

---

## 1. 概述

### 1.1 设计目标
本规范基于 e看牙 系统的成熟医疗 SaaS 界面风格，提取其布局、色彩、组件、交互等设计特征，统一指导本系统各页面的 UI 改造。

### 1.2 核心特征
- **医疗专业感**：青色主色调，清爽、干净、可信
- **高信息密度**：紧凑排版，一屏展示更多内容
- **效率优先**：减少视觉噪音，突出操作路径
- **一致性**：所有页面遵循同一套设计语言

### 1.3 适用页面
| 优先级 | 页面 | 参考来源 |
|--------|------|----------|
| P0 | 预约视图 (`AppointmentView.vue`) | 预约视图.html |
| P0 | 患者列表/患者360 (`PatientView.vue` / `Patient360View.vue`) | 患者详情页.html |
| P0 | 工作台/首页 (`HomeView.vue`) | 工作台视图.html |
| P1 | 病历视图 (`MedicalRecordView.vue`) | 患者详情页.html（信息区块风格） |
| P1 | 治疗/处置 (`TreatmentView.vue`) | 预约视图.html（卡片风格） |
| P2 | 财务/收费 (`FinancialView.vue`) | 工作台视图.html（数据面板风格） |

---

## 2. 设计原则

### 2.1 少即是多
- 减少不必要的装饰元素（渐变背景、过重阴影）
- 用留白和分割线代替边框来分隔区域
- 颜色只用于表达状态和层级，不用于装饰

### 2.2 信息层级清晰
- 通过字号、字重、颜色建立明确的信息层级
- 同一层级的内容保持视觉一致性
- 重要操作按钮始终可见，次要操作折叠或弱化

### 2.3 紧凑但不拥挤
- 行高、间距适度紧凑，提高信息密度
- 但保证元素之间有足够的呼吸空间
- 鼠标可操作区域不小于 32x32px

---

## 3. 颜色系统

### 3.1 主色

| 用途 | 色值 | 使用场景 |
|------|------|----------|
| 主色 | `#00a6c9` | 按钮、Tab active、链接、强调、hover |
| 主色 Hover | `#0095b5` | 按钮悬停状态 |
| 主色 Light | `rgba(0, 166, 201, 0.08)` | 选中背景、淡色填充 |
| 主色 Border | `rgba(0, 166, 201, 0.3)` | 选中边框、标签边框 |

### 3.2 文字色

| 用途 | 色值 | 使用场景 |
|------|------|----------|
| 主标题 | `#1d222a` | 页面标题、患者姓名、重要数据 |
| 正文 | `#3e3e3c` | 普通文本内容 |
| 次要文字 | `#636a74` | 标签、描述、辅助信息 |
| 弱化文字 | `#9397a2` | 占位符、禁用状态、时间戳 |
| 白色文字 | `#ffffff` | 主色背景上的文字 |

### 3.3 背景与边框

| 用途 | 色值 | 使用场景 |
|------|------|----------|
| 页面背景 | `#f5f5f5` | 整体页面底层背景 |
| 卡片背景 | `#ffffff` | 内容卡片、面板背景 |
| 悬停背景 | `#f5f7fa` | 表格行悬停、列表项悬停 |
| 边框 | `#d9d9d9` | 分割线、卡片边框、表单边框 |
| 深边框 | `#c0c4cc` | 聚焦状态边框 |

### 3.4 状态色

| 状态 | 色值 | 使用场景 |
|------|------|----------|
| 成功 | `#52c41a` | 已完成、正常状态 |
| 警告 | `#faad14` | 待处理、提醒 |
| 错误 | `#f86359` | 欠费、取消、异常 |
| 信息 | `#00a6c9` | 进行中、默认状态 |

### 3.5 CSS 变量定义

```css
:root {
  /* 主色 */
  --primary: #00a6c9;
  --primary-hover: #0095b5;
  --primary-light: rgba(0, 166, 201, 0.08);
  --primary-border: rgba(0, 166, 201, 0.3);

  /* 文字 */
  --text-primary: #1d222a;
  --text-regular: #3e3e3c;
  --text-secondary: #636a74;
  --text-muted: #9397a2;
  --text-white: #ffffff;

  /* 背景与边框 */
  --bg-page: #f5f5f5;
  --bg-card: #ffffff;
  --bg-hover: #f5f7fa;
  --border-color: #d9d9d9;
  --border-dark: #c0c4cc;

  /* 状态 */
  --success: #52c41a;
  --warning: #faad14;
  --danger: #f86359;
  --info: #00a6c9;

  /* 阴影 */
  --shadow-card: 0 2px 8px rgba(0, 0, 0, 0.08);
  --shadow-dropdown: 0 4px 12px rgba(0, 0, 0, 0.1);
  --shadow-modal: 0 8px 24px rgba(0, 0, 0, 0.12);

  /* 圆角 */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;

  /* 间距 */
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 12px;
  --space-lg: 16px;
  --space-xl: 24px;
}
```

---

## 4. 字体系统

### 4.1 字体栈

```css
font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "Helvetica Neue", Helvetica, Arial, sans-serif;
```

> Mac 优先使用 PingFang SC（苹方），Windows 使用 Microsoft YaHei（微软雅黑）

### 4.2 字号规范

| 层级 | 字号 | 字重 | 行高 | 颜色 | 使用场景 |
|------|------|------|------|------|----------|
| H1 | 18px | 600 | 1.4 | `#1d222a` | 页面大标题 |
| H2 | 16px | 600 | 1.4 | `#1d222a` | 卡片标题、患者姓名 |
| H3 | 14px | 600 | 1.5 | `#1d222a` | 区块标题、列表标题 |
| Body | 14px | 400 | 1.5 | `#3e3e3c` | 正文内容 |
| Small | 13px | 400 | 1.5 | `#636a74` | 次要说明、标签 |
| Caption | 12px | 400 | 1.4 | `#9397a2` | 时间戳、辅助信息、Badge |

### 4.3 数字与金额

```css
/* 金额、统计数据使用 tabular-nums 保持等宽对齐 */
font-variant-numeric: tabular-nums;
```

---

## 5. 间距系统

### 5.1 基础间距

| Token | 值 | 使用场景 |
|-------|-----|----------|
| xs | 4px | 图标与文字间距、紧凑内边距 |
| sm | 8px | 按钮内边距、卡片小间距 |
| md | 12px | 表单控件间距、卡片padding |
| lg | 16px | 区块间距、卡片之间间距 |
| xl | 24px | 页面边距、大区块分隔 |

### 5.2 组件间距

- **卡片内边距**：12px ~ 16px
- **表格行高**：40px（紧凑模式）/ 48px（舒适模式）
- **表单控件间距**：12px
- **按钮间距**：8px
- **Tab 高度**：35px ~ 40px

---

## 6. 布局规范

### 6.1 全局布局

```
┌─────────────────────────────────────────────────────────┐
│  顶部导航栏 (60px)                                       │
├──────────┬──────────────────────────────────────────────┤
│          │                                                │
│  侧边栏   │              主内容区                          │
│ (可折叠)  │          (白色背景 #fff)                       │
│          │                                                │
│          │  ┌────────────────────────────────────────┐   │
│          │  │  Tab 导航栏 (35-40px)                   │   │
│          │  ├────────────────────────────────────────┤   │
│          │  │                                        │   │
│          │  │           内容区域                       │   │
│          │  │                                        │   │
│          │  └────────────────────────────────────────┘   │
│          │                                                │
└──────────┴──────────────────────────────────────────────┘
```

### 6.2 侧边栏

- **宽度**：展开 200px，折叠 60px（仅图标）
- **背景**：`#1d222a`（深色）或 `#ffffff`（白色，带右边框）
- **菜单项高度**：44px
- **图标大小**：18px
- **Active 状态**：左侧 3px 主色竖线 + 背景色变化
- **Hover 状态**：背景 `#f5f7fa`（白色主题）或 `rgba(255,255,255,0.08)`（深色主题）

### 6.3 页面内布局模式

**模式 A：左右分栏（预约视图）**
- 左侧 268px 侧边面板（日历 + 筛选）
- 右侧主内容区（日历/列表）
- 中间可折叠锚点按钮

**模式 B：上下结构（患者详情）**
- 顶部档案卡（64px 高，flex 横向排列）
- 中部 Tab 导航
- 底部内容区（可分左右）

**模式 C：Dashboard（工作台）**
- 多列卡片网格
- 关键数据 KPI 卡片
- 快捷操作入口

---

## 7. 组件规范

### 7.1 按钮

```css
/* 主按钮 */
.btn-primary {
  background: var(--primary);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-sm);
  padding: 0 16px;
  height: 32px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-primary:hover {
  background: var(--primary-hover);
}

/* 次要按钮 */
.btn-secondary {
  background: var(--bg-card);
  color: var(--text-regular);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  padding: 0 16px;
  height: 32px;
  font-size: 13px;
  cursor: pointer;
}
.btn-secondary:hover {
  border-color: var(--primary);
  color: var(--primary);
}

/* 文字按钮 */
.btn-text {
  background: transparent;
  color: var(--primary);
  border: none;
  padding: 0 8px;
  height: 32px;
  font-size: 13px;
  cursor: pointer;
}
```

**按钮尺寸**：
- 大：height 40px，padding 0 24px，font-size 14px
- 中（默认）：height 32px，padding 0 16px，font-size 13px
- 小：height 24px，padding 0 12px，font-size 12px

### 7.2 卡片/面板

```css
.card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  padding: var(--space-md);
}

/* 带阴影的浮动卡片 */
.card-elevated {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: var(--space-md);
}

/* 无边框卡片（用于列表项） */
.card-plain {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: var(--space-md);
}
```

### 7.3 表格

```css
.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.table th {
  height: 40px;
  padding: 0 12px;
  text-align: left;
  font-weight: 600;
  color: var(--text-secondary);
  background: #fafafa;
  border-bottom: 1px solid var(--border-color);
}

.table td {
  height: 40px;
  padding: 0 12px;
  color: var(--text-regular);
  border-bottom: 1px solid var(--border-color);
}

.table tr:hover td {
  background: var(--bg-hover);
}
```

**表格行高**：默认 40px（紧凑），重要页面可 48px

### 7.4 Tab 导航

```css
.tab-bar {
  display: flex;
  border-bottom: 1px solid var(--border-color);
  height: 40px;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  height: 40px;
  padding: 0 16px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 3px solid transparent;
  margin-bottom: -1px;
  transition: all 0.3s;
}

.tab-item:hover {
  color: var(--primary);
}

.tab-item.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
  font-weight: 500;
}
```

### 7.5 表单控件

```css
/* 输入框 */
.input {
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--text-regular);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px var(--primary-light);
  outline: none;
}

.input::placeholder {
  color: var(--text-muted);
}

/* 选择框 */
.select {
  height: 32px;
  padding: 0 8px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 13px;
}
```

### 7.6 标签/徽章（Tag / Badge）

```css
/* 状态标签 */
.tag {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.tag-primary {
  background: var(--primary-light);
  color: var(--primary);
  border: 1px solid var(--primary-border);
}

.tag-success {
  background: rgba(82, 196, 26, 0.1);
  color: var(--success);
  border: 1px solid rgba(82, 196, 26, 0.3);
}

.tag-warning {
  background: rgba(250, 173, 20, 0.1);
  color: var(--warning);
  border: 1px solid rgba(250, 173, 20, 0.3);
}

.tag-danger {
  background: rgba(248, 99, 89, 0.1);
  color: var(--danger);
  border: 1px solid rgba(248, 99, 89, 0.3);
}

/* 圆形徽章 */
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--bg-card);
  color: var(--text-secondary);
  font-size: 11px;
  font-weight: 500;
}
```

### 7.7 预约卡片（核心组件）

```css
.appointment-card {
  border-radius: 8px 8px 0 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.24);
  overflow: hidden;
  cursor: pointer;
  position: relative;
}

/* 卡片标题区 */
.appointment-card .card-header {
  padding: 0 8px;
  line-height: 20px;
  font-size: 14px;
  font-weight: 400;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  color: var(--text-white);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 患者名 */
.appointment-card .patient-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-white);
}

/* 卡片内容区 */
.appointment-card .card-body {
  background: var(--bg-card);
  color: var(--text-regular);
  font-size: 13px;
  padding: 4px 0 4px 8px;
  border-bottom-left-radius: 8px;
}

/* Hover 遮罩 */
.appointment-card .event-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px 8px 0 8px;
  display: none;
}

.appointment-card:hover .event-mask {
  display: block;
}

/* 各状态标题色 */
.appointment-card.status-confirmed .card-header { background: var(--primary); }
.appointment-card.status-completed .card-header { background: var(--success); }
.appointment-card.status-cancelled .card-header { background: var(--danger); }
.appointment-card.status-pending .card-header { background: var(--warning); }
.appointment-card.status-placeholder .card-header { background: #9397a2; }
```

---

## 8. 交互与动效

### 8.1 过渡时长

| 场景 | 时长 | 缓动函数 |
|------|------|----------|
| 颜色变化（hover） | 0.2s | ease |
| 边框变化（focus） | 0.2s | ease |
| Tab 指示器滑动 | 0.3s | ease |
| 卡片阴影加深 | 0.2s | ease |
| 侧边栏展开/折叠 | 0.3s | ease-in-out |
| 弹窗出现 | 0.2s | ease-out |
| 页面加载淡入 | 0.4s | ease-out |

### 8.2 Hover 状态

- **按钮**：背景色变深 / 边框变主色
- **列表项**：背景变为 `#f5f7fa`
- **卡片**：阴影加深 `0 4px 12px rgba(0,0,0,0.12)`
- **链接**：颜色变为主色，出现下划线
- **预约卡片**：显示半透明白色遮罩

### 8.3 Focus 状态

```css
:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--primary-light);
}
```

### 8.4 禁用状态

```css
.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}
```

---

## 9. Vue 2 + Element UI 适配指南

### 9.1 Element UI 全局覆写

在 `element-overrides.css` 中添加以下覆写：

```css
/* 主色覆盖 */
.el-button--primary {
  background-color: var(--primary);
  border-color: var(--primary);
}
.el-button--primary:hover,
.el-button--primary:focus {
  background-color: var(--primary-hover);
  border-color: var(--primary-hover);
}

/* Tab 样式 */
.el-tabs__item {
  height: 40px;
  line-height: 40px;
  font-size: 14px;
  color: var(--text-secondary);
}
.el-tabs__item.is-active {
  color: var(--primary);
  font-weight: 500;
}
.el-tabs__active-bar {
  background-color: var(--primary);
  height: 3px;
}

/* 表格 */
.el-table th {
  background: #fafafa;
  color: var(--text-secondary);
  font-weight: 600;
  height: 40px;
  padding: 0;
}
.el-table td {
  height: 40px;
  padding: 0;
}
.el-table--medium th,
.el-table--medium td {
  padding: 0 12px;
}

/* 输入框 */
.el-input__inner {
  height: 32px;
  line-height: 32px;
  border-radius: var(--radius-sm);
  border-color: var(--border-color);
  font-size: 13px;
}
.el-input__inner:focus {
  border-color: var(--primary);
}

/* 标签 */
.el-tag {
  height: 20px;
  line-height: 18px;
  padding: 0 8px;
  border-radius: 10px;
  font-size: 12px;
}
.el-tag--primary {
  background-color: var(--primary-light);
  border-color: var(--primary-border);
  color: var(--primary);
}

/* 分页 */
.el-pagination.is-background .el-pager li:not(.disabled).active {
  background-color: var(--primary);
}
```

### 9.2 Element UI 组件尺寸统一

所有 Element UI 组件统一使用 **medium** 尺寸（默认尺寸已接近目标）：
- 按钮：默认尺寸（medium）
- 输入框：默认尺寸（medium）
- 表格：`size="medium"` 或默认
- 选择框：默认尺寸

### 9.3 与现有 Apple 风格设计令牌的融合

现有系统已有 `design-tokens.css`，建议按以下方式融合：

| 现有 Token | 新规范值 | 说明 |
|-----------|---------|------|
| `--apple-accent` | `#00a6c9` | 从蓝色改为青色 |
| `--apple-text-primary` | `#1d222a` | 保持深色标题 |
| `--apple-text-secondary` | `#636a74` | 匹配次要文字 |
| `--apple-surface` | `#ffffff` | 纯白卡片 |
| `--apple-bg-primary` | `#f5f5f5` | 页面背景改为浅灰 |
| `--apple-border` | `#d9d9d9` | 边框色统一 |

**建议**：保留现有阴影系统（比 e看牙 更精致），但将主色和文字色调整为 e看牙 规范。

---

## 10. 各页面改造要点

### 10.1 预约视图 (`AppointmentView.vue`)

**布局改造：**
- 采用左右分栏：左侧 268px 侧边面板 + 右侧主内容区
- 左侧包含：视图切换 Tab + 日历 + 筛选列表 + 统计
- 右侧包含：操作按钮组 + 日历/列表视图
- 左侧可折叠，折叠后显示锚点箭头按钮

**组件规范：**
- 视图切换 Tab：底部 3px 指示器，active 青色
- 筛选列表项：40px 高，左 padding 12px，底部 1px 分割线，字体 13px
- 预约卡片：上圆角 8px，阴影 `0 2px 8px rgba(0,0,0,0.24)`
- 操作按钮组：13px 字体，圆角 4px

**状态色映射：**
| 状态 | 卡片标题背景 |
|------|-------------|
| 待确认 | `#faad14` (warning) |
| 已确认 | `#00a6c9` (primary) |
| 已就诊 | `#52c41a` (success) |
| 已取消 | `#f86359` (danger) |
| 未排班(占位) | `#9397a2` (muted) |

---

### 10.2 患者列表/患者360 (`PatientView.vue` / `Patient360View.vue`)

**布局改造：**
- 顶部档案卡：64px 高，flex 横向，头像 + 姓名 + 标签 + 基本信息
- 中部 Tab 导航：患者概览、病历、预约、收费、时间轴、影像
- 底部内容区：根据 Tab 切换

**档案卡规范：**
- 头像：圆形，40-48px
- 患者姓名：16px, PingFangSC-Semibold, weight 600, color `#1D222A`
- 患者 ID：13px, color `#636a74`
- 标签：17x17px 小圆形图标，margin 0 5px 8px 0
- 基本信息：13px, color `#636a74`，用分割线 `|` 分隔

**列表规范：**
- 表格行高：40px
- 关键字段：患者名 14px weight 500，手机号/年龄 13px
- 状态标签：使用 Tag 组件，小圆角

**患者360 Tab 导航：**
- 高度 40px
- 底部 3px 指示器
- active 状态：文字 `#00a6c9`，指示器 `#00a6c9`

---

### 10.3 工作台/首页 (`HomeView.vue`)

**布局改造：**
- 采用 Dashboard 布局：多列卡片网格
- 顶部：问候语 + 日期 + 今日状态摘要
- KPI 卡片：2x2 或 4 列网格
- 下方：今日预约列表 + AI 助手面板

**KPI 卡片规范：**
- 卡片：白色背景，圆角 8px，边框 1px solid `#d9d9d9`
- 图标：18-20px，主色
- 数字：24-28px，weight 600，color `#1d222a`
- 标签：13px，color `#636a74`
- 趋势：12px，上升绿色，下降红色

**快捷操作：**
- 按钮组：次要按钮样式，圆角 4px
- 图标 + 文字，紧凑排列

**AI 助手面板：**
- 圆角卡片，白色背景
- 头部：Agent 头像 + 名称 + 下拉切换
- 消息气泡：用户侧青色渐变，AI 侧白色带边框
- 输入框：圆角 8px，带发送按钮

---

### 10.4 病历视图 (`MedicalRecordView.vue`)

**布局改造：**
- 采用患者360的信息区块风格
- 病历列表：表格，行高 40px
- 病历详情：分区块卡片（基础信息、主诉、检查、诊断、治疗计划、医嘱）
- 每个区块：卡片标题 + 分割线 + 内容

**AI 病历辅助嵌入位置：**
- 在病历编辑页面右侧或底部嵌入 AI 面板
- 快捷键触发：选中文字后右键"AI 优化"
- 或固定面板：输入关键词 → AI 补全 → 一键填入

**区块卡片规范：**
- 标题：14px, weight 600, color `#1d222a`
- 分割线：1px solid `#d9d9d9`
- 内容：14px, color `#3e3e3c`
- 圆角：8px
- padding：12-16px

---

## 11. 文件结构规划

```
src/styles/
├── design-tokens.css          # CSS 变量定义（更新为 e看牙 规范）
├── element-overrides.css       # Element UI 全局覆写
├── animations.css              # 全局动画（保留现有）
└── components/
    ├── card.css                # 卡片组件样式
    ├── table.css               # 表格组件样式
    ├── tab.css                 # Tab 组件样式
    ├── button.css              # 按钮组件样式
    ├── tag.css                 # 标签/徽章样式
    ├── appointment-card.css    # 预约卡片专用样式
    └── patient-card.css        # 患者档案卡专用样式
```

---

## 12. 开发检查清单

每个页面改造完成后，对照以下清单检查：

- [ ] 颜色是否使用 CSS 变量，无硬编码色值
- [ ] 字体是否遵循层级规范（字号/字重/颜色）
- [ ] 按钮是否使用统一组件/样式类
- [ ] 卡片/面板是否统一圆角和阴影
- [ ] 表格行高是否统一 40px
- [ ] Tab 导航是否有 3px 底部指示器
- [ ] Hover 状态是否有过渡动画
- [ ] Focus 状态是否有主色光晕
- [ ] 页面是否能在 1366px 宽度正常显示
- [ ] 是否无控制台样式报错

---

## 13. 附录

### 13.1 参考截图建议
由于 HTML 文件是已渲染的 DOM，建议在浏览器中打开后截取以下关键区域的截图，作为开发时的视觉参照：

1. **预约视图**：整体布局、侧边栏、预约卡片（各状态）、操作按钮组
2. **工作台**：KPI 卡片、快捷入口、列表样式
3. **患者详情**：档案卡、Tab 导航、信息区块、标签样式

### 13.2 与现有代码的兼容策略

- **逐步替换**：不要一次性全局替换，按页面逐个改造
- **样式隔离**：每个页面的新样式放在 `<style scoped>` 中，避免影响其他页面
- **组件复用**：优先修改全局组件（如 `AppTopNav.vue`），再改页面级组件
- **保留回退**：关键样式保留原值注释，方便快速回退

### 13.3 e看牙 vs 现有 Apple 风格取舍

| 维度 | e看牙 风格 | 现有 Apple 风格 | 建议 |
|------|-----------|----------------|------|
| 主色 | `#00a6c9` 青色 | `#2563eb` 蓝色 | 采用 e看牙 青色 |
| 背景 | `#f5f5f5` 浅灰 | `#f2f4f6` 冷灰 | 采用 `#f5f5f5` |
| 阴影 | `0 2px 8px` 轻阴影 | 多层精致阴影 | 保留现有更精致的阴影 |
| 圆角 | 4px / 8px | 12px / 16px / 20px | 采用 4px/8px/12px 更紧凑 |
| 字体 | PingFang SC | system | 保留 PingFang SC 优先 |
| 动效 | 简单过渡 | 弹性阻尼 | 保留现有弹性动效 |
| 信息密度 | 高 | 中等 | 采用 e看牙 高密度 |
