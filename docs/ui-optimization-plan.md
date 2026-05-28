# 一隐口腔 SaaS 管理系统 — UI 全局优化计划

## 项目概述

- **品牌名**：一隐口腔
- **技术栈**：Vue 2.6.14 + Element UI 2.15 + Vue Router 3
- **优化范围**：全局所有页面（登录页、导航栏、首页仪表盘、所有管理后台页面）
- **原则**：只改 UI 视觉与交互，不改后端 API 调用逻辑、不改数据流、不改 Vue 组件结构

---

## 一、设计概念与方向

### 选定概念（待用户从 3 个预览方案中选择后确定）

| 方案 | 名称 | 核心特征 | 氛围 |
|------|------|----------|------|
| 方案一 | 「隐曜」 | 极简黑白灰 + 暖琥珀金点缀 | 内敛奢华、克制的精致 |
| 方案二 | 「青瓷」 | 东方青瓷色 + 宣纸暖白 | 温润如玉、东方雅韵 |
| 方案三 | 「深海」 | 深色背景 + 深海蓝 + 珊瑚橙 | 深邃科技、专业力量 |

---

## 二、全局色彩系统

> 最终色号以用户选定的方案为准，以下为方案一（隐曜）的参考：

```css
:root {
  --yinyin-text-primary: #141414;
  --yinyin-text-secondary: #6B6B6B;
  --yinyin-text-tertiary: #A0A0A0;
  --yinyin-bg: #F7F5F2;
  --yinyin-surface: #FFFFFF;
  --yinyin-surface-hover: #FAF9F7;
  --yinyin-accent: #D4A574;
  --yinyin-accent-hover: #C49460;
  --yinyin-accent-light: rgba(212, 165, 116, 0.08);
  --yinyin-success: #5A8F7B;
  --yinyin-warning: #C9A227;
  --yinyin-danger: #C75B5B;
  --yinyin-info: #6B8FA8;
  --yinyin-border: rgba(0, 0, 0, 0.06);
  --yinyin-divider: rgba(0, 0, 0, 0.05);
}
```

---

## 三、字体系统

- **品牌标题**：Playfair Display (英文) + Noto Serif SC (中文衬线)
- **正文/界面**：Noto Sans SC
- **加载方式**：Google Fonts CDN (`public/index.html`)

---

## 四、全局交互规范

| 交互类型 | 规范 |
|----------|------|
| 页面进入 | fade-in-up 0.5s，缓动 cubic-bezier(0.22, 1, 0.36, 1) |
| 按钮悬停 | translateY(-1px) + 阴影加深 |
| 按钮按下 | scale(0.98) + 涟漪扩散 |
| 按钮加载 | 收缩为圆形 + 旋转环 |
| 输入框聚焦 | 底部边框从中心展开 + 标签上浮 + 柔和光晕 |
| 输入框错误 | 水平震动 + 底部线变砖红 |
| 卡片悬停 | translateY(-2px) + 阴影加深 |
| 表格行悬停 | 背景微变 #FAF9F7 |
| 下拉菜单 | scale + translateY + backdrop blur |
| 弹窗 | scale(0.95→1) + backdrop blur 遮罩 |
| 消息提示 | 底部居中 + translateY 滑入 |

---

## 五、实施阶段

### Phase 1：全局设计系统（第 1 轮）
- 重写 `styles/apple-design/design-tokens.css`
- 重写 `styles/apple-design/element-overrides.css`
- `public/index.html` 引入 Google Fonts
- **验证**：前端编译通过 + 任意页面打开无样式崩坏

### Phase 2：登录页重构（第 2 轮）
- 重写 `views/login1.vue` 的 `<style>` 部分
- 保留所有 `<script>` 登录逻辑不变
- 新增：背景粒子动画、浮动标签、入场动画、记住密码、加载状态
- **验证**：登录功能正常 + 视觉符合设计稿

### Phase 3：导航栏优化（第 3 轮）
- 优化 `components/apple-design/AppTopNav.vue`
- 品牌 Logo、菜单激活态、用户下拉、移动端菜单
- **验证**：导航正常 + 路由跳转正常

### Phase 4：首页仪表盘（第 4 轮）
- 优化 `views/Manager/HomeView.vue`
- KPI 卡片图标替换、Greeting 区、状态 pill
- **验证**：数据加载正常 + 页面渲染正常

### Phase 5：通用页面批量优化（第 5 轮）
- 提取共性样式到全局
- 优化表格默认样式、表单默认样式、弹窗默认样式
- **验证**：任意表格页/表单页打开视觉统一

### Phase 6：高频页面逐个微调（并行多轮）
- 预约、患者、病历、财务、耗材、义齿等重点页面
- 根据每个页面的数据特点微调卡片/表格/图表呈现
- **验证**：每页数据正常 + 交互正常

---

## 六、文件变更清单

| 文件路径 | 操作 | 说明 |
|----------|------|------|
| `public/index.html` | 编辑 | 引入 Google Fonts |
| `styles/apple-design/design-tokens.css` | 重写 | 全局设计令牌 |
| `styles/apple-design/element-overrides.css` | 重写 | Element UI 全局覆盖 |
| `styles/apple-design/animations.css` | 编辑 | 新增/修改全局动画 |
| `views/login1.vue` | 编辑 | 仅重写 `<style>`，保留 `<script>` |
| `components/apple-design/AppTopNav.vue` | 编辑 | 品牌、颜色、交互微调 |
| `views/Manager/HomeView.vue` | 编辑 | KPI 卡片、Greeting、图标 |
| `views/Manager/*.vue` | 批量编辑 | Phase 6 逐个优化 |

---

## 七、风险控制

| 风险 | 应对措施 |
|------|----------|
| Element UI 主题覆盖导致某些页面崩坏 | 每阶段修改后立即编译验证，发现问题回滚 |
| 字体加载慢影响首屏 | 使用 `preconnect` + `display=swap` |
| 动画过多导致低端设备卡顿 | 使用 `prefers-reduced-motion` 媒体查询做降级 |
| 深色/浅色模式不适配 | 当前方案仅做浅色模式，深色模式作为二期 |

---

## 八、验收标准

- [ ] 所有页面打开后视觉风格统一（颜色、字体、圆角、阴影一致）
- [ ] 登录功能正常（不改交互逻辑）
- [ ] 所有 API 调用正常（不改数据流）
- [ ] 移动端基本可用（响应式适配）
- [ ] 前端编译无报错
- [ ] 浏览器控制台无样式/动画报错

---

> 计划创建时间：2026-05-28
> 等待用户从 3 个设计预览方案中选择后正式启动实施。
