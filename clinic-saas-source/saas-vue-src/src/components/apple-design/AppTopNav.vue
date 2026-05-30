<template>
  <nav class="app-top-nav" :class="{ 'is-scrolled': isScrolled }">
    <div class="nav-inner">
      <!-- 左侧：汉堡按钮 + Logo -->
      <div class="nav-left">
        <div class="sidebar-toggle" @click="$emit('toggle-sidebar')" title="展开/折叠菜单">
          <i class="el-icon-s-fold"></i>
        </div>
        <div class="nav-brand" @click="$router.push('/home')">
          <div class="brand-mark">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2C7.5 2 4 6.5 4 10c0 3.5 3 7 8 12 5-5 8-8.5 8-12 0-3.5-3.5-8-8-8z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
          </div>
          <span class="brand-text">一隐口腔</span>
        </div>
      </div>

      <!-- 桌面核心菜单 -->
      <div class="nav-menu">
        <router-link
          v-for="item in coreMenuItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: $route.path === item.path }"
        >
          <i v-if="item.icon" :class="item.icon" class="menu-item-icon"></i>
          <span>{{ item.label }}</span>
          <div v-if="$route.path === item.path" class="active-indicator"></div>
        </router-link>
      </div>

      <!-- 右侧操作区 -->
      <div class="nav-actions">
        <div class="action-btn" @click="handleFull" title="全屏">
          <i class="el-icon-full-screen"></i>
        </div>
        <div class="action-btn" @click="$router.push('/SystemSettings')" title="系统设置">
          <i class="el-icon-s-tools"></i>
        </div>
        <div class="user-menu" @mouseenter="showUserMenu = true" @mouseleave="showUserMenu = false">
          <el-avatar :size="32" icon="el-icon-user-solid" class="user-avatar"></el-avatar>
          <span class="user-name">{{ username || '未登录' }}</span>
          <i class="el-icon-arrow-down user-arrow"></i>

          <transition name="apple-dropdown">
            <div v-show="showUserMenu" class="user-dropdown">
              <div class="user-dropdown-header">
                <el-avatar :size="44" icon="el-icon-user-solid"></el-avatar>
                <div class="user-dropdown-info">
                  <div class="user-dropdown-name">{{ username || '未登录用户' }}</div>
                  <div class="user-dropdown-role">{{ userRoleLabel }}</div>
                </div>
              </div>
              <div class="user-dropdown-divider"></div>
              <div class="user-dropdown-item user-dropdown-item--normal" @click="$router.push('/SystemSettings')">
                <i class="el-icon-s-tools"></i>
                <span>系统设置</span>
              </div>
              <div class="user-dropdown-item" @click="handleLogout">
                <i class="el-icon-switch-button"></i>
                <span>退出登录</span>
              </div>
            </div>
          </transition>
        </div>
      </div>

      <!-- 移动端汉堡菜单 -->
      <div class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen">
        <i :class="mobileMenuOpen ? 'el-icon-close' : 'el-icon-s-unfold'"></i>
      </div>
    </div>

    <!-- 移动端菜单面板 -->
    <transition name="apple-slide">
      <div v-show="mobileMenuOpen" class="mobile-menu-panel">
        <div
          v-for="item in visibleMenuItems"
          :key="item.path || item.group"
          class="mobile-menu-group"
        >
          <router-link
            v-if="!item.children"
            :to="item.path"
            class="mobile-menu-link"
            :class="{ active: $route.path === item.path }"
            @click.native="mobileMenuOpen = false"
          >
            {{ item.label }}
          </router-link>
          <div v-else>
            <div class="mobile-menu-group-title">{{ item.label }}</div>
            <router-link
              v-for="child in item.children"
              :key="child.path"
              :to="child.path"
              class="mobile-menu-link mobile-menu-sublink"
              :class="{ active: $route.path === child.path }"
              @click.native="mobileMenuOpen = false"
            >
              {{ child.label }}
            </router-link>
          </div>
        </div>
      </div>
    </transition>
  </nav>
</template>

<script>
import {
  ADMIN_SESSION_EVENT,
  clearAdminSession,
  getAdminSession
} from '@/utils/adminSession'
import { canAccessRoleMenu } from '@/utils/roleMenuCatalog'

export default {
  name: 'AppTopNav',
  data() {
    return {
      user: getAdminSession() || {},
      username: '',
      isScrolled: false,
      showUserMenu: false,
      mobileMenuOpen: false,
      menuItems: [
        { label: '首页概览', path: '/home', icon: 'el-icon-s-home' },
        { label: '患者列表', path: '/Patient', icon: 'el-icon-user' },
        { label: '今日工作', path: '/MedicalRecord', icon: 'el-icon-first-aid-kit' },
        { label: '预约视图', path: '/Appointment', icon: 'el-icon-date' },
        { label: '回访管理', path: '/Followup', icon: 'el-icon-phone-outline' },
        {
          group: 'consultation',
          label: '咨询管理',
          icon: 'el-icon-chat-dot-square',
          children: [
            { label: '咨询记录', path: '/Consultation' },
            { label: '咨询看板', path: '/ConsultationDashboard' }
          ]
        },
        {
          group: 'marketing',
          label: '市场投放',
          icon: 'el-icon-data-line',
          children: [
            { label: '广告投放', path: '/advertising-spending' }
          ]
        },
        {
          group: 'lab',
          label: '义齿加工',
          icon: 'el-icon-box',
          children: [
            { label: '加工厂', path: '/lab-factories' },
            { label: '加工订单', path: '/lab-orders' },
            { label: '月度账单', path: '/lab-bills' },
            { label: '加工统计', path: '/lab-statistics' }
          ]
        },
        {
          group: 'material',
          label: '耗材管理',
          icon: 'el-icon-shopping-bag-1',
          children: [
            { label: '耗材分类', path: '/material-categories' },
            { label: '耗材档案', path: '/materials' },
            { label: '采购记录', path: '/material-purchases' },
            { label: '耗材统计', path: '/material-statistics' }
          ]
        },
        { label: '医生排班', path: '/Doctor', icon: 'el-icon-time' },
        {
          group: 'financial',
          label: '财务管理',
          icon: 'el-icon-money',
          children: [
            { label: '财务信息', path: '/Financial' },
            { label: '财务分析', path: '/Financial2' },
            { label: '财务支出', path: '/financial-expenses' },
          ]
        },
        {
          group: 'insurance',
          label: '医保管理',
          icon: 'el-icon-document-checked',
          children: [
            { label: '医保总览', path: '/InsuranceOverview' },
            { label: '医保配置', path: '/InsuranceConfig' },
            { label: '患者医保档案', path: '/InsurancePatientProfile' },
            { label: '医保结算', path: '/InsuranceSettlement' },
            { label: '医保日志', path: '/InsuranceLog' },
            { label: 'mock报文', path: '/InsuranceMockPayload' }
          ]
        }
      ]
    }
  },
  computed: {
    visibleMenuItems() {
      return this.menuItems.filter(item => {
        if (!item.children) {
          return canAccessRoleMenu(this.user, item.path)
        }
        return item.children.some(child => canAccessRoleMenu(this.user, child.path))
      }).map(item => {
        if (!item.children) return item
        return {
          ...item,
          children: item.children.filter(child => canAccessRoleMenu(this.user, child.path))
        }
      })
    },
    coreMenuItems() {
      // 顶部只保留最高频的 5 个核心入口
      const corePaths = ['/home', '/Patient', '/MedicalRecord', '/Appointment', '/Followup']
      return corePaths
        .map(path => this.menuItems.find(item => item.path === path))
        .filter(item => item && canAccessRoleMenu(this.user, item.path))
    },
    normalizedRole() {
      const role = (this.user && this.user.role ? String(this.user.role) : '').trim()
      if (role === '管理员' || role === 'admin') return 'admin'
      if (role === '医生' || role === 'doctor') return 'doctor'
      if (role === '护士' || role === 'nurse') return 'nurse'
      return role
    },
    isAdmin() {
      return this.normalizedRole === 'admin'
    },
    isDoctor() {
      return this.normalizedRole === 'doctor'
    },
    userRoleLabel() {
      if (this.user && this.user.roleLabel) return this.user.roleLabel
      if (this.isAdmin) return '管理员'
      if (this.isDoctor) return '医生'
      if (this.normalizedRole === 'nurse') return '护士'
      return this.user && this.user.role ? this.user.role : '门诊成员'
    }
  },
  mounted() {
    window.addEventListener(ADMIN_SESSION_EVENT, this.syncUser)
    window.addEventListener('focus', this.syncUser)
    window.addEventListener('scroll', this.handleScroll, true)
    this.syncUser()
  },
  beforeDestroy() {
    window.removeEventListener(ADMIN_SESSION_EVENT, this.syncUser)
    window.removeEventListener('focus', this.syncUser)
    window.removeEventListener('scroll', this.handleScroll, true)
  },
  methods: {
    syncUser() {
      this.user = getAdminSession() || {}
      this.username = this.user.name || ''
    },
    handleScroll() {
      this.isScrolled = window.scrollY > 10
    },
    handleLogout() {
      clearAdminSession()
      this.$router.push('/login1').catch(() => {})
    },
    handleFull() {
      document.documentElement.requestFullscreen()
    }
  }
}
</script>

<style scoped>
.app-top-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: var(--apple-nav-height);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  transition: background var(--apple-transition-normal), box-shadow var(--apple-transition-normal);
}

.app-top-nav.is-scrolled {
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.nav-inner {
  max-width: var(--apple-content-max-width);
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--apple-content-padding);
}

/* 左侧区域 */
.nav-left {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

/* 侧边栏切换按钮 */
.sidebar-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: var(--apple-radius-md);
  cursor: pointer;
  color: var(--apple-text-secondary);
  font-size: 16px;
  transition: all var(--apple-transition-fast);
}

.sidebar-toggle:hover {
  background: rgba(0, 0, 0, 0.04);
  color: var(--apple-accent);
}

/* Logo */
.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity var(--apple-transition-fast);
}

.nav-brand:hover {
  opacity: 0.75;
}

.brand-mark {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: linear-gradient(135deg, #5A8F7B, #4A7F6B);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(90, 143, 123, 0.25);
}

.brand-text {
  font-family: var(--apple-font-serif);
  font-size: 17px;
  font-weight: 600;
  color: var(--apple-text-primary);
  letter-spacing: 0.04em;
}

/* 菜单 */
.nav-menu {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  flex: 1;
  margin-left: 20px;
  margin-right: 20px;
  overflow-x: auto;
  scrollbar-width: none;
}

.nav-menu::-webkit-scrollbar {
  display: none;
}

.menu-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  font-size: 13.5px;
  font-weight: 500;
  color: var(--apple-text-secondary);
  text-decoration: none;
  border-radius: 10px;
  transition: all var(--apple-transition-fast);
  cursor: pointer;
  white-space: nowrap;
  letter-spacing: -0.01em;
}

.menu-item-icon {
  font-size: 15px;
  width: 18px;
  text-align: center;
  opacity: 0.75;
  transition: opacity 0.2s ease;
}

.menu-item:hover .menu-item-icon {
  opacity: 1;
}

.menu-item.active .menu-item-icon {
  opacity: 1;
  color: var(--apple-accent);
}

.menu-item:hover {
  color: var(--apple-text-primary);
  background: rgba(0, 0, 0, 0.03);
}

.menu-item.active {
  color: var(--apple-accent);
  font-weight: 600;
}

.active-indicator {
  position: absolute;
  bottom: 2px;
  left: 50%;
  transform: translateX(-50%);
  width: 16px;
  height: 2.5px;
  background: var(--apple-accent);
  border-radius: 2px;
  animation: apple-nav-indicator 0.3s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

/* 右侧操作区 */
.nav-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: var(--apple-radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--apple-text-secondary);
  transition: all var(--apple-transition-fast);
  font-size: 15px;
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.04);
  color: var(--apple-text-primary);
}

.user-menu {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 6px;
  border-radius: var(--apple-radius-full);
  cursor: pointer;
  transition: all var(--apple-transition-fast);
}

.user-menu:hover {
  background: rgba(0, 0, 0, 0.04);
}

.user-avatar {
  flex-shrink: 0;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--apple-text-primary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-arrow {
  font-size: 10px;
  color: var(--apple-text-tertiary);
  transition: transform var(--apple-transition-fast);
}

.user-menu:hover .user-arrow {
  transform: rotate(180deg);
}

/* 用户下拉 */
.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 240px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.08), 0 8px 10px -6px rgba(0, 0, 0, 0.03);
  padding: 16px;
  z-index: 1001;
}

.user-dropdown::before {
  content: '';
  position: absolute;
  top: -10px;
  left: 0;
  right: 0;
  height: 10px;
}

.user-dropdown-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-dropdown-info {
  flex: 1;
  min-width: 0;
}

.user-dropdown-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text-primary);
}

.user-dropdown-role {
  font-size: 12px;
  color: var(--apple-text-secondary);
  margin-top: 2px;
}

.user-dropdown-divider {
  height: 1px;
  background: var(--apple-divider);
  margin: 12px 0;
}

.user-dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: var(--apple-radius-sm);
  font-size: 13px;
  font-weight: 500;
  color: var(--apple-danger);
  cursor: pointer;
  transition: all var(--apple-transition-fast);
}

.user-dropdown-item:hover {
  background: var(--apple-danger-light);
}

.user-dropdown-item--normal {
  color: var(--apple-text-primary);
}

.user-dropdown-item--normal:hover {
  background: var(--apple-bg-hover);
}

/* 移动端菜单按钮 */
.mobile-menu-btn {
  display: none;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--apple-text-primary);
  cursor: pointer;
  border-radius: var(--apple-radius-sm);
  transition: background var(--apple-transition-fast);
}

.mobile-menu-btn:hover {
  background: rgba(0, 0, 0, 0.04);
}

/* 移动端菜单面板 */
.mobile-menu-panel {
  display: none;
  position: fixed;
  top: var(--apple-nav-height);
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  padding: 16px var(--apple-content-padding);
  overflow-y: auto;
  z-index: 999;
}

.mobile-menu-group {
  margin-bottom: 8px;
}

.mobile-menu-link {
  display: block;
  padding: 12px 16px;
  font-size: 16px;
  font-weight: 500;
  color: var(--apple-text-primary);
  text-decoration: none;
  border-radius: var(--apple-radius-md);
  transition: all var(--apple-transition-fast);
}

.mobile-menu-link:hover,
.mobile-menu-link.active {
  background: var(--apple-accent-light);
  color: var(--apple-accent);
}

.mobile-menu-group-title {
  padding: 8px 16px;
  font-size: 11px;
  font-weight: 600;
  color: var(--apple-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.mobile-menu-sublink {
  padding: 10px 16px 10px 28px;
  font-size: 14px;
}

/* 动画 */
.apple-dropdown-enter-active {
  animation: apple-dropdown-in 0.2s cubic-bezier(0.22, 1, 0.36, 1);
}

.apple-dropdown-leave-active {
  animation: apple-dropdown-in 0.15s reverse;
}

.apple-slide-enter-active {
  animation: apple-sheet-in 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}

.apple-slide-leave-active {
  animation: apple-sheet-in 0.2s reverse;
}

/* 导航指示器动画 */
@keyframes apple-nav-indicator {
  from {
    transform: translateX(-50%) scaleX(0);
    opacity: 0;
  }
  to {
    transform: translateX(-50%) scaleX(1);
    opacity: 1;
  }
}

/* 下拉菜单进入动画 */
@keyframes apple-dropdown-in {
  from {
    opacity: 0;
    transform: translateY(-6px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 移动端面板滑入动画 */
@keyframes apple-sheet-in {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式 */
@media (max-width: 1100px) {
  .nav-menu {
    display: none;
  }

  .sidebar-toggle {
    display: none;
  }

  .nav-actions .user-name {
    display: none;
  }

  .mobile-menu-btn {
    display: flex;
  }

  .mobile-menu-panel {
    display: block;
  }
}

@media (max-width: 768px) {
  .brand-text {
    display: none;
  }
}
</style>
