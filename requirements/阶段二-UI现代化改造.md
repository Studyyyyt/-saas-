# 阶段二（重制版 v2）：精致 Apple 风格 UI 现代化改造计划

## Context

用户反馈当前 Apple 风格实现太简陋（"不是真正的 Apple 风格"），且首页布局未达预期。上一版执行了设计令牌、毛玻璃、阴影等"形"，但整体仍缺乏**高级感、空间感与视觉层次**。用户明确要求参考 [21st.dev](https://21st.dev/) 等现代组件库的设计手法，做出**看得出效果的 Apple 风格**。

核心约束不变：
- Vue 2.6.14 + Element UI 2.15 + ECharts 5.5.0
- 口腔门诊 SaaS，医生是主要用户
- 顶部导航 + AI 对话窗口是首页核心

---

## 问题诊断（上一版不足）

| 维度 | 问题描述 |
|------|---------|
| **背景** | 纯色 `#f2f4f6` 过于平淡，缺乏空间深度，像"未完成的页面" |
| **卡片质感** | 毛玻璃效果在大多数显示器上几乎不可见；阴影太淡，卡片浮不起来 |
| **字体层级** | 标题与正文对比不够强烈，数据数字缺乏"大屏感" |
| **首页布局** | AI 全宽过于笨重，缺乏信息节奏；KPI 胶囊与 AI 区之间没有视觉过渡 |
| **动效** | 仅有基础 fade-in，缺乏 Apple 标志性的弹性、阻尼、景深动效 |
| **装饰** | 无任何环境光、渐变光晕、纹理，页面"干净到空洞" |

---

## 设计方向：Apple 空间界面（Apple Spatial UI）

参考 Apple Vision Pro 的空间设计 + 21st.dev 的现代玻璃拟态组件，核心理念：

- **空间感**：元素不是贴在平面上，而是悬浮在不同深度的空间中
- **环境光**：背景有微妙的渐变光晕（Ambient Glow），营造氛围
- **玻璃厚度**：卡片像有厚度的光学玻璃，带内发光边缘（Inner Glow Border）
- **字体张力**：Display 标题极大、极紧凑；数据数字使用等宽字体，有强烈的"仪表盘"感
- **动效叙事**：每个元素的进入都有编排（Staggered Entrance），hover 有物理弹性

### 配色系统（v2 升级）

```css
/* 背景层：极深的冷灰蓝，带微妙的环境光 */
--apple-bg-primary: #eef1f5;
--apple-bg-gradient: radial-gradient(ellipse 80% 50% at 50% -20%, rgba(37, 99, 235, 0.08), transparent);

/* 表层卡片：真正的厚玻璃 */
--apple-surface: rgba(255, 255, 255, 0.65);
--apple-surface-blur: blur(24px) saturate(150%);
--apple-surface-border: 1px solid rgba(255, 255, 255, 0.5);
--apple-surface-shadow-inset: inset 0 1px 1px rgba(255, 255, 255, 0.6);

/* 主文字 */
--apple-text-primary: #0f172a;
--apple-text-secondary: #475569;
--apple-text-tertiary: #94a3b8;

/* Accent */
--apple-accent: #2563eb;
--apple-accent-glow: rgba(37, 99, 235, 0.15);
```

### 阴影系统（v2：更立体）

```css
--shadow-sm: 0 1px 2px rgba(0,0,0,0.05), 0 1px 3px rgba(0,0,0,0.03);
--shadow-md: 0 4px 12px rgba(0,0,0,0.05), 0 2px 4px rgba(0,0,0,0.03);
--shadow-lg: 0 12px 24px rgba(0,0,0,0.06), 0 4px 8px rgba(0,0,0,0.02);
--shadow-xl: 0 24px 48px rgba(0,0,0,0.08), 0 8px 16px rgba(0,0,0,0.03);
--shadow-glow: 0 0 40px rgba(37, 99, 235, 0.08);
```

### 全局背景纹理

在 `.manager-shell` 上叠加一层微妙的 SVG 网格/点阵纹理（opacity 0.015），增加质感而不干扰内容：
```css
background-image: url("data:image/svg+xml,..."); /* 6x6 点阵 */
```

---

## 核心页面重设计规格

### 1. 首页（HomeView.vue）——医生的空间指挥中心

**整体布局（v2）：**
- 背景：主色 `#eef1f5` + 顶部中央蓝色环境光晕（radial-gradient）
- 内容区 `max-width: 1200px; margin: 0 auto; padding: 32px 24px;`
- **双栏回归，但比例更激进**：左侧 55%（数据 + 待办），右侧 45%（AI 助手），gap: 24px
- 左侧内容采用**垂直堆叠卡片**，右侧 AI 面板 **sticky 定位**

**顶部标题区（更有冲击力）：**
- 问候语：`font-size: 32px; font-weight: 700; letter-spacing: -0.03em; color: #0f172a;`
- 日期时间：`font-size: 14px; color: #64748b; margin-top: 8px;`
- 左侧下方增加一句**今日状态摘要**（如"今日 8 条预约，3 条待接诊"），小字灰底胶囊

**KPI 卡片（2x2 网格，更大更有存在感）：**
- 网格：`grid-template-columns: repeat(2, 1fr); gap: 16px;`
- 卡片：`background: var(--apple-surface); backdrop-filter: var(--apple-surface-blur); border: var(--apple-surface-border); box-shadow: var(--shadow-md), var(--apple-surface-shadow-inset); border-radius: 20px; padding: 24px;`
- 每个卡片内部：
  - 顶部：图标（24px，彩色）+ 标签（13px，secondary）
  - 中间：数字 `font-size: 36px; font-weight: 700; letter-spacing: -0.02em;`
  - 底部：趋势指示（上升/下降箭头 + 百分比，小字）
- Hover：`transform: translateY(-4px) scale(1.01); box-shadow: var(--shadow-lg), var(--shadow-glow); transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);`
- **取消左侧竖条**，改为**顶部彩色细线**（2px，更现代）或**图标彩色**

**快捷操作（彻底删除，用户已明确要求）**

**左侧下方：今日预约卡片列表（增加信息密度）**
- 卡片式列表，每项 `padding: 16px; background: rgba(255,255,255,0.8); border-radius: 14px; margin-bottom: 10px;`
- 时间 + 患者名 + 项目标签 + 状态点，紧凑排列
- Hover：`transform: translateX(4px); background: #fff; box-shadow: var(--shadow-sm);`

**右侧 AI 助手区（v2 升级）：**
- 面板：`background: var(--apple-surface); backdrop-filter: blur(32px) saturate(160%); border-radius: 24px; border: var(--apple-surface-border); box-shadow: var(--shadow-xl);`
- **高度**：`calc(100vh - 100px); position: sticky; top: 80px;`
- 头部：Agent 头像（40px）+ 名称 + 下拉切换 + 在线状态脉冲动画
- 对话区背景：极淡的渐变 `linear-gradient(180deg, rgba(255,255,255,0.5) 0%, rgba(241,245,249,0.5) 100%)`
- 消息气泡升级：
  - AI：纯白 + `box-shadow: 0 2px 8px rgba(0,0,0,0.04); border-radius: 18px 18px 18px 4px;`
  - User：渐变蓝 `linear-gradient(135deg, #2563eb, #1d4ed8); border-radius: 18px 18px 4px 18px;`
- 输入区：悬浮在面板底部，`background: rgba(255,255,255,0.9); backdrop-filter: blur(12px); border-radius: 16px; border: 1px solid rgba(0,0,0,0.06);`
- **新增**：AI 回复时头像旁有呼吸光效（模拟"思考中"）

---

### 2. 顶部导航栏（AppTopNav.vue）——空间导航

- 高度：`60px`（略增）
- 背景升级：`rgba(255,255,255,0.7)` + `backdrop-filter: blur(32px) saturate(180%)`，底部 1px `rgba(0,0,0,0.04)`
- Logo：保持当前 SVG 牙齿图标，但增加**微妙的渐变呼吸动画**（`background-position` 动画）
- 菜单项：`font-size: 14px; font-weight: 500; padding: 10px 16px; border-radius: 10px;`
- Active 状态：不仅底部指示线，而是**整个背景变为纯白 + 微妙阴影**（像 Safari 标签页）
- 拖拽排序：保持现有逻辑，但拖拽手柄改为**6 点网格图标**，更精致

---

### 3. 患者 360（Patient360View.vue）

- 顶部档案卡：从渐变背景改为**纯白厚玻璃卡片**，带顶部彩色渐变条（患者姓名色块）
- Tab 导航：改为**胶囊 Segmented Control**，选中项有纯白背景和阴影，非选中透明
- 时间轴：垂直中线使用渐变（从上到下颜色渐淡），节点使用彩色圆环 + 脉冲动画
- 影像画廊：hover 时图片放大 + 阴影加深，带平滑过渡

---

### 4. 预约（AppointmentView.vue）

- 日历头部：厚玻璃卡片，周导航按钮圆形化
- 时间轴：左侧时间标签使用 `font-variant-numeric: tabular-nums; color: #94a3b8;`
- 预约卡片：不同状态使用**顶部彩色条**（4px）+ 对应颜色的极淡背景（如蓝色状态背景 `rgba(37,99,235,0.03)`）
- Hover：`transform: scale(1.03); box-shadow: var(--shadow-md); z-index: 10;`

---

### 5. 登录页（login1.vue）

- 背景：从纯色改为**微妙的动态渐变网格**（极淡的蓝紫渐变光晕在角落）
- 卡片：`border-radius: 24px; padding: 56px 48px; box-shadow: var(--shadow-xl), 0 0 0 1px rgba(255,255,255,0.5) inset;`
- 输入框：Focus 时不仅有边框变色，还有**从内向外的光晕扩散**（`box-shadow: 0 0 0 4px rgba(37,99,235,0.1);`）
- 登录按钮：增加**点击时的涟漪效果**（Ripple），通过伪元素实现

---

## 全局质感升级清单

| 序号 | 升级项 | 具体做法 |
|------|--------|---------|
| 1 | 背景环境光 | `body` 增加 radial-gradient 蓝色光晕（顶部中央） |
| 2 | 全局纹理 | 叠加 6x6 SVG 点阵纹理，opacity 0.012 |
| 3 | 卡片玻璃厚度 | 所有卡片统一使用 `backdrop-filter: blur(24px)` + 内发光边框 |
| 4 | Hover 物理感 | 所有可交互卡片 hover 时 `translateY(-4px)` + 阴影加深 + 0.4s 缓动 |
| 5 | 字体张力 | 标题最大 32px，数据数字 36px+，字重 700，letter-spacing -0.03em |
| 6 | 进入动效编排 | 页面元素按序 staggered fade-in-up，delay 递增 0.05s |
| 7 | 脉冲/呼吸动画 | AI 思考中、在线状态、待办提醒等使用 pulse 动画 |
| 8 | 聚焦光晕 | 输入框 focus 时增加扩散光晕，按钮点击时有 scale(0.97) + 阴影变化 |

---

## 技术实现要点

1. **CSS 变量重定义**：更新 `design-tokens.css`，引入 `--apple-surface` 系列和 `--shadow-glow`
2. **全局背景**：在 `Manager.vue` 的 `.manager-shell` 上叠加渐变 + 纹理
3. **Element UI 覆写微调**：`element-overrides.css` 中卡片、按钮、输入框的 shadow 和 radius 进一步升级
4. **动画系统扩充**：`animations.css` 增加 `apple-pulse`、`apple-glow-expand`、`apple-ripple` 等 keyframes
5. **导航栏拖拽**：保持现有 HTML5 DnD 实现，仅优化视觉
6. **ECharts 主题**：保持现有配色，但 tooltip 背景增强毛玻璃

---

## 实施顺序（v2）

### 第一阶段：全局质感（基础升级）
1. 升级 `design-tokens.css`（v2 变量）
2. 升级 `element-overrides.css`（更强的 shadow、glass、radius）
3. 升级 `animations.css`（pulse、glow、ripple）
4. 升级 `Manager.vue`（全局背景光晕 + 纹理）

### 第二阶段：首页重设计（核心）
5. 重写 `HomeView.vue` 模板 + 样式（双栏 55/45、KPI 2x2、AI 面板升级）
6. 保留所有 script 逻辑（dashboard 加载、AI 对话）

### 第三阶段：其他核心页面
7. `login1.vue`（背景光晕 + 涟漪 + 光晕输入框）
8. `Patient360View.vue`（玻璃档案卡 + Segmented Control + 时间轴升级）
9. `AppointmentView.vue`（日历卡片升级 + 预约卡片 hover 效果）
10. `BusinessAnalysisView.vue`（AI 对话区升级）
11. `MedicalRecordView.vue`（列表卡片化 + 折叠区块）

### 第四阶段：全局组件
12. `KpiCard.vue`（2x2 网格用）
13. `PageHeader.vue`

### 第五阶段：验证
14. 浏览器测试每个页面（Playwright 有头模式）
15. 交互测试（hover、动画、拖拽、下拉）
16. 构建验证
17. 响应式验证（1400px / 768px）

---

## 关键文件路径

| 文件 | 说明 |
|------|------|
| `src/styles/apple-design/design-tokens.css` | Design Token 变量（v2 升级） |
| `src/styles/apple-design/element-overrides.css` | Element UI 覆写（更强 shadow/glass） |
| `src/styles/apple-design/animations.css` | 动画 keyframes（pulse、glow、ripple） |
| `src/views/Manager.vue` | 全局布局壳（背景光晕 + 纹理） |
| `src/components/apple-design/AppTopNav.vue` | 顶部导航栏（Active 状态升级） |
| `src/views/login1.vue` | 登录页（光晕 + 涟漪） |
| `src/views/Manager/HomeView.vue` | 首页（双栏 55/45 + KPI 2x2 + AI 升级） |
| `src/views/Manager/Patient360View.vue` | 患者 360（玻璃档案卡 + Segmented Control） |
| `src/views/Manager/AppointmentView.vue` | 预约（日历卡片 + 预约卡片 hover） |
| `src/views/Manager/BusinessAnalysisView.vue` | AI 经营日报（对话区升级） |
| `src/views/Manager/MedicalRecordView.vue` | 病历（列表卡片化） |
| `src/components/apple-design/KpiCard.vue` | KPI 卡片组件 |
| `src/components/apple-design/PageHeader.vue` | 页面标题组件 |

---

## 验证方法

1. 启动前端开发服务器：`npm run serve`
2. 使用 Playwright（有头模式）打开 `http://localhost:7072`
3. 逐项验证：
   - 首页：背景光晕可见、KPI 2x2 网格、AI 面板玻璃质感、hover 物理感
   - 导航栏：毛玻璃强度、菜单 hover 背景高亮、拖拽手柄
   - 登录页：背景光晕、输入框 focus 光晕扩散、按钮涟漪
   - 各页面卡片是否有"浮起来"的感觉
4. 构建验证：`npm run build`
5. 响应式验证：`1400px` 宽屏和 `768px` 平板
