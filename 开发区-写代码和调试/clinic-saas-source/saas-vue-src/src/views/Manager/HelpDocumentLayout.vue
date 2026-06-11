<template>
  <div class="help-document-layout">
    <!-- 左侧目录栏 -->
    <aside class="help-sidebar">
      <div class="sidebar-header">
        <div class="sidebar-title">帮助中心</div>
        <div class="sidebar-subtitle">功能使用指南与操作手册</div>
      </div>

      <div class="sidebar-menu">
        <div
          v-for="group in menuGroups"
          :key="group.key"
          class="menu-group"
        >
          <div
            class="menu-group-title"
            :class="{ active: isGroupActive(group) }"
            @click="toggleGroup(group)"
          >
            <i :class="group.icon"></i>
            <span>{{ group.title }}</span>
            <i
              class="el-icon-arrow-right menu-arrow"
              :class="{ expanded: isGroupExpanded(group) }"
            ></i>
          </div>

          <div v-show="isGroupExpanded(group)" class="menu-children">
            <div
              v-for="item in group.children"
              :key="item.key"
              class="menu-item"
              :class="{ active: isMenuActive(item.path), pending: item.status === 'pending' }"
              @click="goTo(item)"
            >
              <span class="menu-item-text">{{ item.title }}</span>
              <el-tag v-if="item.status === 'pending'" size="mini" type="info" class="status-tag">待补充</el-tag>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧内容区 -->
    <main class="help-content-area">
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script>
/**
 * 帮助文档菜单配置（前后端契约）
 *
 * 数据结构说明：
 * {
 *   key: String,       // 分组唯一标识，用于展开/收起状态
 *   title: String,     // 分组显示标题
 *   icon: String,      // Element UI 图标类名
 *   children: [        // 子菜单列表
 *     {
 *       key: String,   // 菜单唯一标识
 *       title: String, // 菜单显示标题
 *       path: String,  // 前端路由路径（相对 /SystemSettings/help）
 *       status: 'completed' | 'pending'  // 文档完成状态，pending 时显示「待补充」标签
 *     }
 *   ]
 * }
 *
 * 前后端统一约定：
 * 1. 前端路由 path 与后端菜单配置 path 保持一致
 * 2. 后端可通过 GET /api/help-documents/menu 返回相同结构的数据，前端优先使用后端数据
 * 3. status 字段用于标识该模块帮助文档是否已完成编写，pending 时前端展示占位页
 */

const defaultMenuGroups = [
  {
    key: 'ai',
    title: 'AI 智能中心',
    icon: 'el-icon-cpu',
    children: [
      { key: 'ai-overview', title: 'AI 总览', path: '/SystemSettings/help/ai/overview', status: 'completed' },
      { key: 'ai-medical', title: '病历 AI 扩写配置', path: '/SystemSettings/help/ai/medical', status: 'completed' },
      { key: 'ai-scene', title: '诊疗场景库', path: '/SystemSettings/help/ai/scene', status: 'completed' },
      { key: 'ai-patient', title: '患者洞察配置', path: '/SystemSettings/help/ai/patient', status: 'completed' },
      { key: 'ai-model', title: '模型供应商配置', path: '/SystemSettings/help/ai/model', status: 'completed' },
      { key: 'ai-agent', title: '首页助手配置', path: '/SystemSettings/help/ai/agent', status: 'pending' },
      { key: 'ai-link', title: 'Agent 链接配置', path: '/SystemSettings/help/ai/link', status: 'pending' }
    ]
  },
  {
    key: 'basic',
    title: '基础设置',
    icon: 'el-icon-s-tools',
    children: [
      { key: 'basic-treatment', title: '项目与治疗', path: '/SystemSettings/help/basic/treatment', status: 'pending' },
      { key: 'basic-payment', title: '财务与收费', path: '/SystemSettings/help/basic/payment', status: 'pending' },
      { key: 'basic-consent', title: '知情同意书', path: '/SystemSettings/help/basic/consent', status: 'pending' },
      { key: 'basic-lab', title: '义齿加工', path: '/SystemSettings/help/basic/lab', status: 'pending' },
      { key: 'basic-material', title: '耗材管理', path: '/SystemSettings/help/basic/material', status: 'pending' },
      { key: 'basic-account', title: '账号与权限', path: '/SystemSettings/help/basic/account', status: 'pending' }
    ]
  },
  {
    key: 'patient',
    title: '患者与病历',
    icon: 'el-icon-user',
    children: [
      { key: 'patient-list', title: '患者列表', path: '/SystemSettings/help/patient/list', status: 'pending' },
      { key: 'patient-360', title: '患者 360° 视图', path: '/SystemSettings/help/patient/360', status: 'pending' },
      { key: 'medical-record', title: '病历管理', path: '/SystemSettings/help/patient/record', status: 'pending' },
      { key: 'followup', title: '随访管理', path: '/SystemSettings/help/patient/followup', status: 'pending' },
      { key: 'consultation', title: '咨询管理', path: '/SystemSettings/help/patient/consultation', status: 'pending' }
    ]
  },
  {
    key: 'inventory',
    title: '库存与耗材',
    icon: 'el-icon-box',
    children: [
      { key: 'inventory-material', title: '材料管理', path: '/SystemSettings/help/inventory/material', status: 'pending' },
      { key: 'inventory-purchase', title: '采购管理', path: '/SystemSettings/help/inventory/purchase', status: 'pending' },
      { key: 'inventory-statistics', title: '库存统计', path: '/SystemSettings/help/inventory/statistics', status: 'pending' }
    ]
  },
  {
    key: 'lab',
    title: '技工与加工',
    icon: 'el-icon-office-building',
    children: [
      { key: 'lab-factory', title: '加工厂管理', path: '/SystemSettings/help/lab/factory', status: 'pending' },
      { key: 'lab-order', title: '加工订单', path: '/SystemSettings/help/lab/order', status: 'pending' },
      { key: 'lab-bill', title: '加工对账单', path: '/SystemSettings/help/lab/bill', status: 'pending' }
    ]
  }
]

export default {
  name: 'HelpDocumentLayout',
  data() {
    return {
      menuGroups: [],
      expandedGroups: []
    }
  },
  watch: {
    '$route.path': {
      immediate: true,
      handler(path) {
        this.autoExpandByPath(path)
      }
    }
  },
  created() {
    this.loadMenuConfig()
  },
  methods: {
    async loadMenuConfig() {
      // 优先尝试从后端加载菜单配置，失败时回退到本地静态数据
      // 前后端契约：GET /api/help-documents/menu 返回与 defaultMenuGroups 相同结构的数据
      try {
        const axios = (await import('axios')).default
        const res = await axios.get('/api/help-documents/menu')
        if (res && res.data && res.data.code === '200' && Array.isArray(res.data.data)) {
          this.menuGroups = res.data.data
        } else {
          this.menuGroups = JSON.parse(JSON.stringify(defaultMenuGroups))
        }
      } catch (e) {
        this.menuGroups = JSON.parse(JSON.stringify(defaultMenuGroups))
      }
      // 默认展开包含当前路由的分组
      this.autoExpandByPath(this.$route.path)
    },
    isGroupActive(group) {
      return group.children.some(c => c.path === this.$route.path)
    },
    isGroupExpanded(group) {
      return this.expandedGroups.includes(group.key)
    },
    toggleGroup(group) {
      const idx = this.expandedGroups.indexOf(group.key)
      if (idx >= 0) {
        this.expandedGroups.splice(idx, 1)
      } else {
        this.expandedGroups.push(group.key)
      }
    },
    isMenuActive(path) {
      return this.$route.path === path
    },
    goTo(item) {
      if (item.status === 'pending') {
        return
      }
      if (this.$route.path !== item.path) {
        this.$router.push(item.path)
      }
    },
    autoExpandByPath(path) {
      if (!this.menuGroups.length) return
      this.menuGroups.forEach(group => {
        const hasActive = group.children.some(c => c.path === path)
        if (hasActive && !this.expandedGroups.includes(group.key)) {
          this.expandedGroups.push(group.key)
        }
      })
    }
  }
}
</script>

<style scoped>
.help-document-layout {
  display: flex;
  min-height: calc(100vh - var(--apple-nav-height));
  background: var(--apple-bg-primary);
}

/* 左侧目录栏 */
.help-sidebar {
  width: 260px;
  flex-shrink: 0;
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border-right: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-sm);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  position: sticky;
  top: var(--apple-nav-height);
  height: calc(100vh - var(--apple-nav-height));
}

.sidebar-header {
  padding: 24px 20px 16px;
  border-bottom: 1px solid var(--apple-divider);
}

.sidebar-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--apple-text-primary);
  line-height: 1.3;
}

.sidebar-subtitle {
  font-size: 12px;
  color: var(--apple-text-tertiary);
  margin-top: 4px;
}

/* 菜单 */
.sidebar-menu {
  padding: 12px 0;
  flex: 1;
}

.menu-group {
  margin-bottom: 4px;
}

.menu-group-title {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text-primary);
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.menu-group-title:hover {
  background: var(--apple-bg-hover);
}

.menu-group-title.active {
  color: var(--apple-accent);
}

.menu-group-title i:first-child {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.menu-arrow {
  margin-left: auto;
  font-size: 12px;
  color: var(--apple-text-tertiary);
  transition: transform 0.25s ease;
}

.menu-arrow.expanded {
  transform: rotate(90deg);
}

/* 二级菜单 */
.menu-children {
  padding: 4px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 20px 9px 50px;
  font-size: 13px;
  color: var(--apple-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 0;
  position: relative;
}

.menu-item:hover {
  background: var(--apple-bg-hover);
  color: var(--apple-text-primary);
}

.menu-item.active {
  color: var(--apple-accent);
  background: var(--apple-accent-light);
  font-weight: 600;
}

.menu-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 3px;
  background: var(--apple-accent);
  border-radius: 0 3px 3px 0;
}

.menu-item.pending {
  color: var(--apple-text-tertiary);
  cursor: default;
}

.menu-item.pending:hover {
  color: var(--apple-text-tertiary);
  background: transparent;
}

.menu-item-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  margin-left: 6px;
  flex-shrink: 0;
}

/* 右侧内容区 */
.help-content-area {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  height: calc(100vh - var(--apple-nav-height));
}

.content-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 28px;
  box-sizing: border-box;
}

/* 响应式 */
@media (max-width: 768px) {
  .help-sidebar {
    width: 200px;
  }

  .content-wrapper {
    padding: 20px 16px;
  }
}
</style>
