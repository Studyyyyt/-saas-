<template>
  <div id="app" class="manager-shell">
    <el-container>
      <el-aside :width="asideWidth" class="manager-aside">
        <div class="brand-block">
          <div class="brand-mark">S</div>
          <div v-show="!isCollapse" class="brand-text">
            <div class="brand-title">舒澳口腔门诊 SaaS</div>
            <div class="brand-subtitle">诊疗 · 患者 · 预约 · 收费</div>
          </div>
        </div>

        <el-menu
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          background-color="transparent"
          text-color="#94a3b8"
          active-text-color="#1d4ed8"
          class="manager-menu"
          :default-active="$route.path"
        >
          <el-menu-item v-if="canAccessMenu('/home')" index="/home">
            <i class="el-icon-data-analysis"></i>
            <span slot="title">首页概览</span>
          </el-menu-item>

          <el-menu-item v-if="canAccessMenu('/Patient')" index="/Patient">
            <i class="el-icon-user"></i>
            <span slot="title">患者列表</span>
          </el-menu-item>

          <el-menu-item v-if="canAccessMenu('/MedicalRecord')" index="/MedicalRecord">
            <i class="el-icon-document"></i>
            <span slot="title">病历工作台</span>
          </el-menu-item>

          <el-menu-item v-if="canAccessMenu('/Followup')" index="/Followup">
            <i class="el-icon-phone-outline"></i>
            <span slot="title">回访管理</span>
          </el-menu-item>

          <el-submenu v-if="showConsultationGroup" index="/consultation-group">
            <template slot="title">
              <i class="el-icon-chat-line-square"></i>
              <span>咨询管理</span>
            </template>
            <el-menu-item v-if="canAccessMenu('/Consultation')" index="/Consultation">咨询记录</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/ConsultationDashboard')" index="/ConsultationDashboard">咨询看板</el-menu-item>
          </el-submenu>

          <el-submenu v-if="showMarketingGroup" index="/marketing-group">
            <template slot="title">
              <i class="el-icon-data-line"></i>
              <span>市场投放</span>
            </template>
            <el-menu-item v-if="canAccessMenu('/advertising-spending')" index="/advertising-spending">广告投放</el-menu-item>
          </el-submenu>

          <el-submenu v-if="showLabGroup" index="/lab-group">
            <template slot="title">
              <i class="el-icon-office-building"></i>
              <span>义齿加工</span>
            </template>
            <el-menu-item v-if="canAccessMenu('/lab-factories')" index="/lab-factories">加工厂/产品库</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/lab-orders')" index="/lab-orders">加工订单</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/lab-bills')" index="/lab-bills">月度账单</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/lab-statistics')" index="/lab-statistics">加工统计</el-menu-item>
          </el-submenu>

          <el-submenu v-if="showMaterialGroup" index="/material-group">
            <template slot="title">
              <i class="el-icon-collection-tag"></i>
              <span>耗材管理</span>
            </template>
            <el-menu-item v-if="canAccessMenu('/material-categories')" index="/material-categories">耗材分类</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/materials')" index="/materials">耗材档案</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/material-purchases')" index="/material-purchases">采购记录</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/material-statistics')" index="/material-statistics">耗材统计</el-menu-item>
          </el-submenu>

          <el-menu-item v-if="canAccessMenu('/Appointment2')" index="/Appointment2">
            <i class="el-icon-date"></i>
            <span slot="title">预约列表</span>
          </el-menu-item>

          <el-menu-item v-if="canAccessMenu('/Doctor')" index="/Doctor">
            <i class="el-icon-user-solid"></i>
            <span slot="title">医生排班</span>
          </el-menu-item>

          <el-submenu v-if="showFinancialGroup" index="/financial-group">
            <template slot="title">
              <i class="el-icon-money"></i>
              <span>财务管理</span>
            </template>
            <el-menu-item v-if="canAccessMenu('/Financial')" index="/Financial">财务信息</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/Financial2')" index="/Financial2">财务分析</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/financial-expenses')" index="/financial-expenses">财务支出</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/BusinessAnalysis')" index="/BusinessAnalysis">AI经营日报</el-menu-item>
          </el-submenu>

          <el-submenu v-if="showInsuranceGroup" index="/insurance-group">
            <template slot="title">
              <i class="el-icon-notebook-2"></i>
              <span>医保管理</span>
            </template>
            <el-menu-item v-if="canAccessMenu('/InsuranceOverview')" index="/InsuranceOverview">医保总览</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/InsuranceConfig')" index="/InsuranceConfig">医保配置</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/InsurancePatientProfile')" index="/InsurancePatientProfile">患者医保档案</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/InsuranceSettlement')" index="/InsuranceSettlement">医保结算</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/InsuranceLog')" index="/InsuranceLog">医保日志</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/InsuranceMockPayload')" index="/InsuranceMockPayload">mock报文</el-menu-item>
          </el-submenu>

          <el-submenu v-if="showSystemGroup" index="/account-group">
            <template slot="title">
              <i class="el-icon-setting"></i>
              <span>系统设置</span>
            </template>
            <el-menu-item v-if="canAccessMenu('/SystemTreatmentCatalog')" index="/SystemTreatmentCatalog">项目库</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/SystemTreatmentOperation')" index="/SystemTreatmentOperation">操作字典</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/SystemPaymentChannel')" index="/SystemPaymentChannel">收款渠道</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/SystemConsentTemplate')" index="/SystemConsentTemplate">知情同意书库</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/SystemAccountPermission')" index="/SystemAccountPermission">账号权限</el-menu-item>
            <el-menu-item v-if="canAccessMenu('/SystemAccountManage')" index="/SystemAccountManage">账号管理</el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="manager-header">
          <div class="header-left">
            <div class="page-title">门诊运营工作台</div>
            <div class="page-subtitle">聚焦接诊效率、患者运营与门诊经营数据</div>
          </div>

          <div class="header-right">
            <div class="header-action" @click="handleFull">
              <i class="el-icon-full-screen"></i>
            </div>
            <div class="user-card">
              <el-avatar icon="el-icon-user-solid" :size="40"></el-avatar>
              <div class="user-meta">
                <div class="user-name">{{ username || '未登录用户' }}</div>
                <div class="user-role">{{ userRoleLabel }}</div>
              </div>
              <el-dropdown placement="bottom-end">
                <i class="el-icon-arrow-down user-arrow"></i>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click.native="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <el-main class="manager-main">
          <router-view class="view-container"></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import {
  ADMIN_SESSION_EVENT,
  clearAdminSession,
  getAdminSession
} from '@/utils/adminSession'
import { canAccessRoleMenu } from '@/utils/roleMenuCatalog'

export default {
  name: 'ManagerShell',
  data() {
    return {
      isCollapse: false,
      asideWidth: '248px',
      username: '',
      user: getAdminSession() || {}
    }
  },
  mounted() {
    window.addEventListener(ADMIN_SESSION_EVENT, this.syncUserFromStorage)
    window.addEventListener('focus', this.syncUserFromStorage)
    this.syncUserFromStorage()
  },
  beforeDestroy() {
    window.removeEventListener(ADMIN_SESSION_EVENT, this.syncUserFromStorage)
    window.removeEventListener('focus', this.syncUserFromStorage)
  },
  computed: {
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
    showConsultationGroup() {
      return this.canAccessAny(['/Consultation', '/ConsultationDashboard'])
    },
    showMarketingGroup() {
      return this.canAccessAny(['/advertising-spending'])
    },
    showLabGroup() {
      return this.canAccessAny(['/lab-factories', '/lab-orders', '/lab-bills', '/lab-statistics'])
    },
    showMaterialGroup() {
      return this.canAccessAny(['/material-categories', '/materials', '/material-purchases', '/material-statistics'])
    },
    showFinancialGroup() {
      return this.canAccessAny(['/Financial', '/Financial2', '/financial-expenses', '/BusinessAnalysis'])
    },
    showInsuranceGroup() {
      return this.canAccessAny([
        '/InsuranceOverview',
        '/InsuranceConfig',
        '/InsurancePatientProfile',
        '/InsuranceSettlement',
        '/InsuranceLog',
        '/InsuranceMockPayload'
      ])
    },
    showSystemGroup() {
      return this.canAccessAny([
        '/SystemTreatmentCatalog',
        '/SystemTreatmentOperation',
        '/SystemPaymentChannel',
        '/SystemConsentTemplate',
        '/SystemAccountPermission',
        '/SystemAccountManage'
      ])
    },
    userRoleLabel() {
      if (this.user && this.user.roleLabel) {
        return this.user.roleLabel
      }
      if (this.isAdmin) return '管理员'
      if (this.isDoctor) return '医生'
      if (this.normalizedRole === 'nurse') return '护士'
      return this.user && this.user.role ? this.user.role : '门诊成员'
    }
  },
  created() {
    this.username = this.user.name || ''
  },
  methods: {
    syncUserFromStorage() {
      this.user = getAdminSession() || {}
      this.username = this.user.name || ''
    },
    canAccessMenu(key) {
      return canAccessRoleMenu(this.user, key)
    },
    canAccessAny(keys) {
      return keys.some(key => this.canAccessMenu(key))
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

<style>
html, body, #app {
  height: 100%;
  margin: 0;
}

body {
  background: #f3f6fb;
}

.manager-shell {
  min-height: 100vh;
  background: #f3f6fb;
}

.manager-aside {
  min-height: 100vh;
  background: linear-gradient(180deg, #0f172a 0%, #111827 100%);
  box-shadow: 6px 0 30px rgba(15, 23, 42, 0.18);
  padding: 18px 14px;
  box-sizing: border-box;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px 18px;
}

.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 20px;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.35);
}

.brand-title {
  color: #f8fafc;
  font-size: 16px;
  font-weight: 700;
}

.brand-subtitle {
  color: #94a3b8;
  font-size: 12px;
  margin-top: 4px;
}

.manager-menu {
  border: none !important;
  background: transparent !important;
}

.manager-menu .el-menu-item,
.manager-menu .el-submenu__title {
  height: 46px !important;
  line-height: 46px !important;
  border-radius: 12px;
  margin-bottom: 6px;
}

.manager-menu .el-menu-item:hover,
.manager-menu .el-submenu__title:hover {
  background: rgba(255, 255, 255, 0.06) !important;
  color: #fff !important;
}

.manager-menu .el-menu-item.is-active {
  background: #eff6ff !important;
  color: #1d4ed8 !important;
  font-weight: 600;
}

.manager-menu .el-submenu .el-menu {
  background: rgba(2, 6, 23, 0.18) !important;
}

.manager-menu .el-submenu .el-menu-item {
  min-width: 0 !important;
  margin: 4px 0 4px 8px;
  width: calc(100% - 8px);
}

.manager-menu i {
  color: inherit !important;
}

.manager-header {
  height: 72px !important;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  box-shadow: 0 6px 24px rgba(15, 23, 42, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px !important;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.page-subtitle {
  font-size: 13px;
  color: #64748b;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-action {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #334155;
  border: 1px solid #e2e8f0;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.user-name {
  font-size: 14px;
  color: #0f172a;
  font-weight: 600;
}

.user-role {
  font-size: 12px;
  color: #64748b;
}

.user-arrow {
  color: #94a3b8;
  cursor: pointer;
}

.manager-main {
  padding: 20px !important;
  height: calc(100vh - 72px);
  overflow: auto;
  background: #f3f6fb;
}

.view-container {
  width: 100%;
  min-height: 100%;
}
</style>
