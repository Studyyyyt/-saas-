<template>
  <div class="system-settings-layout">
    <!-- 左侧菜单栏 -->
    <aside class="settings-sidebar">
      <div class="sidebar-header">
        <div class="sidebar-title">系统设置</div>
        <div class="sidebar-subtitle">配置管理中心</div>
      </div>

      <div class="sidebar-menu">
        <div
          v-for="group in menuGroups"
          :key="group.key"
          class="menu-group"
        >
          <!-- 一级菜单 -->
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

          <!-- 二级菜单 -->
          <div
            v-show="isGroupExpanded(group)"
            class="menu-children"
          >
            <template v-for="item in group.children">
              <!-- 有三级菜单的情况 -->
              <div v-if="item.children" :key="item.key" class="menu-sub-group">
                <div
                  class="menu-sub-title"
                  :class="{ active: isSubGroupActive(item) }"
                  @click="toggleSubGroup(item)"
                >
                  <span>{{ item.title }}</span>
                  <i
                    class="el-icon-arrow-right menu-arrow"
                    :class="{ expanded: expandedSubGroups.includes(item.key) }"
                  ></i>
                </div>
                <div
                  v-show="expandedSubGroups.includes(item.key)"
                  class="menu-grand-children"
                >
                  <div
                    v-for="child in item.children"
                    :key="child.key"
                    class="menu-item level-3"
                    :class="{ active: isMenuActive(child.path) }"
                    @click="goTo(child.path, child.external)"
                  >
                    {{ child.title }}
                  </div>
                </div>
              </div>

              <!-- 普通二级菜单 -->
              <div
                v-else
                :key="item.key"
                class="menu-item level-2"
                :class="{ active: isMenuActive(item.path) }"
                @click="goTo(item.path, item.external)"
              >
                {{ item.title }}
              </div>
            </template>
          </div>
        </div>
      </div>
    </aside>

    </aside>

    <!-- 右侧内容区 -->
    <main class="settings-content">
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script>
const menuGroups = [
  {
    key: 'basic',
    title: '基础设置',
    icon: 'el-icon-s-tools',
    sortOrder: 1,
    children: [
      { key: 'clinics', title: '诊所管理', path: '/SystemSettings/basic/clinics', sortOrder: 1 },
      { key: 'treatment', title: '项目与治疗', path: '/SystemSettings/basic/treatment', sortOrder: 2 },
      { key: 'payment', title: '财务与收费', path: '/SystemSettings/basic/payment', sortOrder: 3 },
      { key: 'consent', title: '知情同意书', path: '/SystemSettings/basic/consent', sortOrder: 4 },
      { key: 'account', title: '账号管理', path: '/SystemSettings/basic/account', sortOrder: 5 },
      { key: 'account-permission', title: '账号权限', path: '/SystemSettings/basic/account-permission', sortOrder: 6 }
    ]
  },
  {
    key: 'open',
    title: '开放接口',
    icon: 'el-icon-link',
    sortOrder: 2,
    children: [
      { key: 'api-key', title: 'API Key 管理', path: '/SystemSettings/open/api-key', sortOrder: 1 },
      { key: 'api-docs', title: 'API 接口文档', path: 'http://localhost:8080/swagger-ui/index.html', external: true, sortOrder: 2 }
    ]
  },
  {
    key: 'ai',
    title: 'AI 智能中心',
    icon: 'el-icon-cpu',
    sortOrder: 2,
    children: [
      { key: 'ai-overview', title: 'AI 总览', path: '/SystemSettings/ai/overview', sortOrder: 1 }
    ]
  },
  {
    key: 'help',
    title: '帮助中心',
    icon: 'el-icon-question',
    sortOrder: 3,
    children: [
      { key: 'help-document', title: '帮助文档', path: '/SystemSettings/help/index', sortOrder: 1 }
    ]
  }
]

function sortMenuItems(items) {
  if (!Array.isArray(items)) return items
  const sorted = [...items].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  sorted.forEach(item => {
    if (item.children) {
      item.children = sortMenuItems(item.children)
    }
  })
  return sorted
}

export default {
  name: 'SystemSettingsLayout',
  data() {
    return {
      menuGroups: sortMenuItems(menuGroups),
      expandedGroups: ['basic', 'ai'],
      expandedSubGroups: ['ai-pages']
    }
  },
  watch: {
    '$route.path': {
      immediate: true,
      handler(path) {
        // 根据当前路由自动展开对应的分组
        this.autoExpandByPath(path)
      }
    }
  },
  methods: {
    isGroupActive(group) {
      // 帮助中心分组在 /SystemSettings/help/* 下都高亮
      if (group.key === 'help') {
        return this.$route.path.startsWith('/SystemSettings/help')
      }
      return group.children.some(child => {
        if (child.children) {
          return child.children.some(c => c.path === this.$route.path)
        }
        return child.path === this.$route.path
      })
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
    isSubGroupActive(item) {
      return item.children.some(c => c.path === this.$route.path)
    },
    toggleSubGroup(item) {
      const idx = this.expandedSubGroups.indexOf(item.key)
      if (idx >= 0) {
        this.expandedSubGroups.splice(idx, 1)
      } else {
        this.expandedSubGroups.push(item.key)
      }
    },
    isMenuActive(path) {
      // 帮助文档菜单在 /SystemSettings/help/* 下都高亮
      if (path === '/SystemSettings/help/index') {
        return this.$route.path.startsWith('/SystemSettings/help')
      }
      return this.$route.path === path
    },
    goTo(path, external) {
      if (external) {
        window.open(path, '_blank')
        return
      }
      if (this.$route.path !== path) {
        this.$router.push(path)
      }
    },
    autoExpandByPath(path) {
      this.menuGroups.forEach(group => {
        let hasActive = false
        // 帮助中心分组在 /SystemSettings/help/* 下都自动展开
        if (group.key === 'help' && path.startsWith('/SystemSettings/help')) {
          hasActive = true
        } else {
          hasActive = group.children.some(child => {
            if (child.children) {
              const childActive = child.children.some(c => c.path === path)
              if (childActive && !this.expandedSubGroups.includes(child.key)) {
                this.expandedSubGroups.push(child.key)
              }
              return childActive
            }
            return child.path === path
          })
        }
        if (hasActive && !this.expandedGroups.includes(group.key)) {
          this.expandedGroups.push(group.key)
        }
      })
    }
  }
}
</script>

<style scoped>
.system-settings-layout {
  display: flex;
  min-height: calc(100vh - var(--apple-nav-height));
  background: var(--apple-bg-primary);
}

/* 左侧边栏 */
.settings-sidebar {
  width: 240px;
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
  z-index: 3000;
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

.menu-item.level-2,
.menu-sub-title {
  padding: 9px 20px 9px 50px;
  font-size: 13px;
  color: var(--apple-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 0;
  position: relative;
}

.menu-item.level-2:hover,
.menu-sub-title:hover {
  background: var(--apple-bg-hover);
  color: var(--apple-text-primary);
}

.menu-item.level-2.active,
.menu-sub-title.active {
  color: var(--apple-accent);
  background: var(--apple-accent-light);
  font-weight: 600;
}

.menu-item.level-2.active::before,
.menu-sub-title.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 3px;
  background: var(--apple-accent);
  border-radius: 0 3px 3px 0;
}

/* 三级菜单 */
.menu-sub-group {
  margin: 2px 0;
}

.menu-sub-title {
  display: flex;
  align-items: center;
  padding-right: 16px;
}

.menu-sub-title span {
  flex: 1;
}

.menu-grand-children {
  padding: 2px 0;
}

.menu-item.level-3 {
  padding: 8px 20px 8px 66px;
  font-size: 13px;
  color: var(--apple-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.menu-item.level-3:hover {
  background: var(--apple-bg-hover);
  color: var(--apple-text-primary);
}

.menu-item.level-3.active {
  color: var(--apple-accent);
  background: var(--apple-accent-light);
  font-weight: 600;
}

.menu-item.level-3.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 3px;
  background: var(--apple-accent);
  border-radius: 0 3px 3px 0;
}

/* 右侧内容区 */
.settings-content {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  height: calc(100vh - var(--apple-nav-height));
}

.content-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 28px;
  box-sizing: border-box;
}

/* 响应式 */
@media (max-width: 768px) {
  .settings-sidebar {
    width: 200px;
  }

  .content-wrapper {
    padding: 20px 16px;
  }
}
</style>
