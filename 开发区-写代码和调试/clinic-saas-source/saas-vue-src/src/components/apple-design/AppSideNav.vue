<template>
  <aside class="app-side-nav" :class="{ 'is-collapsed': collapsed }">
    <div class="side-nav-inner">
      <!-- 展开/折叠切换按钮 -->
      <div class="side-nav-toggle" @click="$emit('toggle')" title="展开/折叠菜单">
        <i :class="collapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
      </div>

      <!-- 菜单分组列表 -->
      <div class="side-nav-scroll">
        <div
          v-for="group in visibleGroups"
          :key="group.title"
          class="side-nav-group"
        >
          <!-- 分组标题 -->
          <div v-if="!collapsed" class="group-title">{{ group.title }}</div>
          <div v-else class="group-divider"></div>

          <!-- 分组内菜单项 -->
          <div class="group-items">
            <router-link
              v-for="item in group.items"
              :key="item.path"
              :to="item.path"
              class="side-nav-item"
              :class="{ active: isActive(item) }"
            >
              <div class="item-icon-wrap">
                <i :class="item.icon" class="side-nav-icon"></i>
              </div>
              <span v-if="!collapsed" class="side-nav-label">{{ item.label }}</span>
              <!-- 折叠状态下的 tooltip -->
              <div v-if="collapsed" class="item-tooltip">
                <span>{{ item.label }}</span>
              </div>
            </router-link>
          </div>
        </div>
      </div>

      <!-- 底部 -->
      <div class="side-nav-footer">
        <div v-if="!collapsed" class="footer-text">一隐口腔 SaaS</div>
        <div v-else class="footer-dot"></div>
      </div>
    </div>
  </aside>
</template>

<script>
import { getAdminSession } from '@/utils/adminSession'
import { canAccessRoleMenu } from '@/utils/roleMenuCatalog'

export default {
  name: 'AppSideNav',
  props: {
    collapsed: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      user: getAdminSession() || {}
    }
  },
  computed: {
    sideMenuGroups() {
      return [
        {
          title: '临床诊疗',
          items: [
            { label: '首页概览', path: '/home', icon: 'el-icon-s-home' },
            { label: '今日工作', path: '/MedicalRecord', icon: 'el-icon-first-aid-kit' },
            { label: '患者列表', path: '/Patient', icon: 'el-icon-user' },
            { label: '预约视图', path: '/Appointment', icon: 'el-icon-date' },
            { label: '回访管理', path: '/Followup', icon: 'el-icon-phone-outline' }
          ]
        },
        {
          title: '财务管理',
          items: [
            { label: '财务信息', path: '/Financial', icon: 'el-icon-money' },
            { label: '财务分析', path: '/Financial2', icon: 'el-icon-money' },
            { label: '财务支出', path: '/financial-expenses', icon: 'el-icon-money' }
          ]
        },
        {
          title: '耗材管理',
          items: [
            { label: '耗材分类', path: '/material-categories', icon: 'el-icon-shopping-bag-1' },
            { label: '耗材档案', path: '/materials', icon: 'el-icon-shopping-bag-1' },
            { label: '采购记录', path: '/material-purchases', icon: 'el-icon-shopping-bag-1' },
            { label: '耗材统计', path: '/material-statistics', icon: 'el-icon-shopping-bag-1' }
          ]
        },
        {
          title: '义齿加工',
          items: [
            { label: '加工厂', path: '/lab-factories', icon: 'el-icon-box' },
            { label: '加工订单', path: '/lab-orders', icon: 'el-icon-box' },
            { label: '月度账单', path: '/lab-bills', icon: 'el-icon-box' },
            { label: '加工统计', path: '/lab-statistics', icon: 'el-icon-box' }
          ]
        },
        {
          title: '医保管理',
          items: [
            { label: '医保总览', path: '/InsuranceOverview', icon: 'el-icon-document-checked' },
            { label: '医保配置', path: '/InsuranceConfig', icon: 'el-icon-document-checked' },
            { label: '患者医保档案', path: '/InsurancePatientProfile', icon: 'el-icon-document-checked' },
            { label: '医保结算', path: '/InsuranceSettlement', icon: 'el-icon-document-checked' },
            { label: '医保日志', path: '/InsuranceLog', icon: 'el-icon-document-checked' },
            { label: 'mock报文', path: '/InsuranceMockPayload', icon: 'el-icon-document-checked' }
          ]
        },
        {
          title: '市场咨询',
          items: [
            { label: '咨询记录', path: '/Consultation', icon: 'el-icon-chat-dot-square' },
            { label: '咨询看板', path: '/ConsultationDashboard', icon: 'el-icon-chat-dot-square' },
            { label: '广告投放', path: '/advertising-spending', icon: 'el-icon-data-line' }
          ]
        },
        {
          title: '其他',
          items: [
            { label: '医生排班', path: '/Doctor', icon: 'el-icon-time' },
            { label: '系统设置', path: '/SystemSettings', icon: 'el-icon-s-tools' }
          ]
        }
      ]
    },
    visibleGroups() {
      return this.sideMenuGroups
        .map(group => {
          const visibleItems = group.items.filter(item =>
            canAccessRoleMenu(this.user, item.path)
          )
          return visibleItems.length > 0
            ? { title: group.title, items: visibleItems }
            : null
        })
        .filter(Boolean)
    }
  },
  methods: {
    isActive(item) {
      if (this.$route.path === item.path) return true
      if (item.path === '/') return false
      return this.$route.path.startsWith(item.path + '/')
    }
  }
}
</script>

<style scoped>
.app-side-nav {
  position: fixed;
  top: var(--apple-nav-height);
  left: 0;
  bottom: 0;
  width: var(--apple-sidebar-width);
  background: var(--apple-surface-solid);
  border-right: 1px solid var(--apple-border-light);
  z-index: 998;
  transition: width var(--apple-transition-normal);
  display: flex;
  flex-direction: column;
}

.app-side-nav.is-collapsed {
  width: var(--apple-sidebar-collapsed-width);
}

.side-nav-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* 展开/折叠切换按钮 */
.side-nav-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  margin: 8px 12px 4px;
  border-radius: var(--apple-radius-md);
  cursor: pointer;
  color: var(--apple-text-secondary);
  font-size: 16px;
  transition: all var(--apple-transition-fast);
  flex-shrink: 0;
}

.side-nav-toggle:hover {
  background: var(--apple-bg-hover);
  color: var(--apple-accent);
}

.is-collapsed .side-nav-toggle {
  margin: 8px 8px 4px;
}

/* 滚动区域 */
.side-nav-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0 12px 12px;
  scrollbar-width: thin;
  scrollbar-color: rgba(0,0,0,0.1) transparent;
}

.side-nav-scroll::-webkit-scrollbar {
  width: 4px;
}

.side-nav-scroll::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 2px;
}

.is-collapsed .side-nav-scroll {
  padding: 0 8px 12px;
}

/* 分组 */
.side-nav-group {
  margin-bottom: 8px;
}

.group-title {
  padding: 8px 10px 4px;
  font-size: 11px;
  font-weight: 600;
  color: var(--apple-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.group-divider {
  height: 1px;
  background: var(--apple-divider);
  margin: 8px 6px;
}

.group-items {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

/* 菜单项 */
.side-nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  border-radius: var(--apple-radius-md);
  text-decoration: none;
  color: var(--apple-text-secondary);
  font-size: 13px;
  font-weight: 500;
  transition: all var(--apple-transition-fast);
  white-space: nowrap;
  overflow: hidden;
}

.side-nav-item:hover {
  background: var(--apple-bg-hover);
  color: var(--apple-text-primary);
}

.side-nav-item.active {
  background: var(--apple-accent-light);
  color: var(--apple-accent);
  font-weight: 600;
}

/* 图标区域 */
.item-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--apple-radius-sm);
  flex-shrink: 0;
  transition: all var(--apple-transition-fast);
}

.side-nav-item:hover .item-icon-wrap {
  background: rgba(90, 143, 123, 0.06);
}

.side-nav-item.active .item-icon-wrap {
  background: rgba(90, 143, 123, 0.1);
}

.side-nav-icon {
  font-size: 15px;
  min-width: 18px;
  text-align: center;
  display: inline-block;
  line-height: 1;
}

.side-nav-label {
  overflow: hidden;
  text-overflow: ellipsis;
  transition: opacity var(--apple-transition-fast);
}

/* 折叠状态下的 tooltip */
.item-tooltip {
  position: absolute;
  left: calc(100% + 10px);
  top: 50%;
  transform: translateY(-50%);
  background: rgba(44, 62, 53, 0.92);
  color: #fff;
  padding: 6px 12px;
  border-radius: var(--apple-radius-md);
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.15s ease;
  z-index: 1002;
  box-shadow: var(--apple-shadow-md);
}

.item-tooltip::before {
  content: '';
  position: absolute;
  left: -4px;
  top: 50%;
  transform: translateY(-50%);
  border: 4px solid transparent;
  border-right-color: rgba(44, 62, 53, 0.92);
}

.is-collapsed .side-nav-item:hover .item-tooltip {
  opacity: 1;
}

/* 底部 */
.side-nav-footer {
  flex-shrink: 0;
  padding: 12px;
  border-top: 1px solid var(--apple-divider);
  text-align: center;
}

.footer-text {
  font-size: 11px;
  color: var(--apple-text-tertiary);
  letter-spacing: 0.02em;
}

.footer-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--apple-accent);
  margin: 0 auto;
  opacity: 0.5;
}

/* 响应式：移动端隐藏侧边栏 */
@media (max-width: 1100px) {
  .app-side-nav {
    display: none;
  }
}
</style>
