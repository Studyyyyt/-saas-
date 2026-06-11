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
        <!-- 诊所切换器 -->
        <div v-if="currentClinicName" class="clinic-switcher" @mouseenter="showClinicMenu = true" @mouseleave="showClinicMenu = false">
          <div class="clinic-switcher-trigger">
            <i class="el-icon-office-building" style="font-size: 14px; color: var(--apple-accent);"></i>
            <span class="clinic-switcher-name">{{ currentClinicName }}</span>
            <i class="el-icon-arrow-down" style="font-size: 10px; color: var(--apple-text-tertiary);"></i>
          </div>
          <transition name="apple-dropdown">
            <div v-show="showClinicMenu" class="clinic-dropdown">
              <div class="clinic-dropdown-header">切换诊所</div>
              <div
                v-for="clinic in userClinics"
                :key="clinic.clinicId"
                class="clinic-dropdown-item"
                :class="{ active: clinic.clinicId === currentClinicId }"
                @click="switchClinic(clinic.clinicId)"
              >
                <span class="clinic-dropdown-item-name">{{ clinic.clinicName }}</span>
                <el-tag size="mini" :type="clinicRoleTagType(clinic.role)">{{ clinicRoleLabel(clinic.role) }}</el-tag>
              </div>
              <div v-if="isAdmin" class="clinic-dropdown-divider"></div>
              <div v-if="isAdmin" class="clinic-dropdown-item clinic-dropdown-item--manage" @click="$router.push('/SystemSettings/basic/clinics')">
                <i class="el-icon-s-tools"></i>
                <span>诊所管理</span>
              </div>
            </div>
          </transition>
        </div>

        <!-- 授权信息按钮 -->
        <div
          v-if="licenseInfo"
          class="action-btn license-btn"
          :class="{ 'license-expired': isLicenseExpired }"
          @mouseenter="showLicenseInfo = true"
          @mouseleave="showLicenseInfo = false"
          title="授权信息"
        >
          <i :class="isLicenseExpired ? 'el-icon-warning' : 'el-icon-key'"></i>

          <transition name="apple-dropdown">
            <div v-show="showLicenseInfo" class="license-dropdown">
              <div class="license-dropdown-header">
                <div class="license-dropdown-title">
                  <i :class="isLicenseExpired ? 'el-icon-warning' : 'el-icon-key'"></i>
                  <span>系统授权</span>
                </div>
                <div class="license-refresh-btn" @click.stop="refreshLicense" title="刷新授权状态">
                  <i :class="licenseRefreshing ? 'el-icon-loading' : 'el-icon-refresh'"></i>
                </div>
              </div>
              <div class="license-dropdown-body">
                <div class="license-dropdown-row">
                  <span class="license-dropdown-label">激活码</span>
                  <span class="license-dropdown-code">{{ licenseInfo.activationCode || '-' }}</span>
                </div>
                <div class="license-dropdown-row">
                  <span class="license-dropdown-label">授权状态</span>
                  <el-tag :type="licenseStatus.type" size="mini">{{ licenseStatus.text }}</el-tag>
                </div>
                <div class="license-dropdown-row">
                  <span class="license-dropdown-label">过期时间</span>
                  <span class="license-dropdown-value">{{ formatLicenseDate(licenseInfo.expiresAt) }}</span>
                </div>
              </div>
            </div>
          </transition>
        </div>
        <div class="action-btn" @click="$router.push('/SystemSettings')" title="系统设置">
          <i class="el-icon-s-tools"></i>
        </div>
        <div class="user-menu" @mouseenter="showUserMenu = true" @mouseleave="showUserMenu = false">
          <el-avatar v-if="avatar" :size="32" :src="avatar" class="user-avatar"></el-avatar>
          <el-avatar v-else :size="32" icon="el-icon-user-solid" class="user-avatar"></el-avatar>
          <span class="user-name">{{ userName || username || '未登录' }}</span>
          <i class="el-icon-arrow-down user-arrow"></i>

          <transition name="apple-dropdown">
            <div v-show="showUserMenu" class="user-dropdown">
              <div class="user-dropdown-header">
                <div class="avatar-wrapper" @click="triggerAvatarUpload">
                  <el-avatar v-if="avatar" :size="44" :src="avatar"></el-avatar>
                  <el-avatar v-else :size="44" icon="el-icon-user-solid"></el-avatar>
                  <div class="avatar-overlay">
                    <i class="el-icon-camera"></i>
                  </div>
                </div>
                <div class="user-dropdown-info">
                  <div class="user-dropdown-name">{{ userName || username || '未登录用户' }}</div>
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

    <!-- 头像上传（隐藏） -->
    <el-upload
      ref="avatarUpload"
      class="avatar-uploader-hidden"
      :action="uploadAction"
      :show-file-list="false"
      :on-success="handleAvatarSuccess"
      :before-upload="beforeAvatarUpload"
      accept="image/*"
    >
    </el-upload>

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
import axios from 'axios'
import {
  ADMIN_SESSION_EVENT,
  clearAdminSession,
  getAdminSession,
  saveAdminSession
} from '@/utils/adminSession'
import { canAccessRoleMenu } from '@/utils/roleMenuCatalog'

export default {
  name: 'AppTopNav',
  data() {
    return {
      user: getAdminSession() || {},
      username: '',
      userName: '',
      avatar: '',
      currentClinicId: '',
      currentClinicName: '',
      userClinics: [],
      isScrolled: false,
      showUserMenu: false,
      showClinicMenu: false,
      showLicenseInfo: false,
      mobileMenuOpen: false,
      licenseInfo: null,
      licenseRefreshing: false,
      uploadAction: '/patient-images/upload',
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
      const labelMap = { admin: '管理员', doctor: '医生', nurse: '护士' }
      return labelMap[this.normalizedRole] || (this.user && this.user.role ? this.user.role : '门诊成员')
    },
    isLicenseExpired() {
      if (!this.licenseInfo || !this.licenseInfo.expiresAt) return false
      return new Date(this.licenseInfo.expiresAt) < new Date()
    },
    licenseStatus() {
      if (!this.licenseInfo) return { text: '未获取', type: 'info' }
      if (!this.licenseInfo.expiresAt) return { text: '永久有效', type: 'success' }
      const expires = new Date(this.licenseInfo.expiresAt)
      const now = new Date()
      if (expires < now) return { text: '已过期', type: 'danger' }
      const daysLeft = Math.ceil((expires - now) / (1000 * 60 * 60 * 24))
      if (daysLeft <= 7) return { text: `剩余 ${daysLeft} 天`, type: 'warning' }
      return { text: `剩余 ${daysLeft} 天`, type: 'success' }
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
      this.username = this.user.username || ''
      this.userName = this.user.name || ''
      this.avatar = this.user.avatar || ''
      this.currentClinicId = this.user.currentClinicId || ''
      this.currentClinicName = this.user.currentClinicName || ''
      this.userClinics = this.user.clinics || []
      this.licenseInfo = this.user.licenseInfo || null
    },
    formatLicenseDate(dateStr) {
      if (!dateStr) return '-'
      const d = new Date(dateStr)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    refreshLicense() {
      if (this.licenseRefreshing) return
      this.licenseRefreshing = true
      axios.get('/auth/check-license')
        .then(res => {
          const code = res.data.code
          if (code === 200 || code === '200') {
            const data = res.data.data || {}
            this.licenseInfo = {
              activationCode: data.activationCode,
              expiresAt: data.expiresAt,
              isValid: data.isValid
            }
            // 同步更新 sessionStorage
            const session = getAdminSession() || {}
            session.licenseInfo = this.licenseInfo
            saveAdminSession(session)
            this.$message.success('授权状态已刷新')
          } else {
            this.$message.error(res.data.msg || '授权验证失败')
          }
        })
        .catch(err => {
          console.error('刷新授权失败:', err)
          this.$message.error('刷新授权失败')
        })
        .finally(() => {
          this.licenseRefreshing = false
        })
    },
    handleScroll() {
      this.isScrolled = window.scrollY > 10
    },
    triggerAvatarUpload() {
      this.$refs.avatarUpload && this.$refs.avatarUpload.$refs['upload-inner'].handleClick()
    },
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isImage) {
        this.$message.error('请上传图片文件')
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB')
      }
      return isImage && isLt2M
    },
    handleAvatarSuccess(res) {
      if (res && res.code === 200 && res.data) {
        const avatarUrl = res.data
        const userId = this.user.id
        axios.post(`/accounts/${userId}/avatar`, { avatar: avatarUrl })
          .then(r => {
            if (r.data && (r.data.code === 200 || r.data.code === '200')) {
              this.avatar = avatarUrl
              // 同步更新 sessionStorage
              const session = getAdminSession() || {}
              session.avatar = avatarUrl
              saveAdminSession(session)
              this.$message.success('头像更新成功')
            } else {
              this.$message.error(r.data.msg || '头像更新失败')
            }
          })
          .catch(err => {
            console.error('更新头像失败:', err)
            this.$message.error('头像更新失败')
          })
      } else {
        this.$message.error('上传失败')
      }
    },
    handleLogout() {
      clearAdminSession()
      this.$router.push('/login1').catch(() => {})
    },
    handleFull() {
      document.documentElement.requestFullscreen()
    },
    switchClinic(clinicId) {
      if (clinicId === this.currentClinicId) {
        this.showClinicMenu = false
        return
      }
      const payload = {
        userId: this.user.id,
        clinicId: clinicId
      }
      axios.post('/clinics/switch', payload).then(res => {
        const code = res.data.code
        if (code === 200 || code === '200') {
          const selected = this.userClinics.find(c => c.clinicId === clinicId)
          if (selected) {
            this.user.currentClinicId = clinicId
            this.user.currentClinicName = selected.clinicName
            saveAdminSession(this.user)
            this.syncUser()
            this.showClinicMenu = false
            this.$message.success(`已切换到「${selected.clinicName}」`)
            // 刷新当前页面
            window.location.reload()
          }
        } else {
          this.$message.error(res.data.msg || '切换失败')
        }
      }).catch(err => {
        console.error('切换诊所失败:', err)
        this.$message.error('切换诊所失败')
      })
    },
    clinicRoleTagType(role) {
      const map = { admin: 'danger', doctor: 'success', nurse: 'warning' }
      return map[role] || 'info'
    },
    clinicRoleLabel(role) {
      const map = { admin: '管理员', doctor: '医生', nurse: '护士' }
      return map[role] || role
    },
    // 动态角色标签映射：优先使用后端返回的 roleLabel，其次是本地映射
    resolveRoleLabel(roleCode) {
      const session = getAdminSession() || {}
      if (session.roleLabel && session.role === roleCode) {
        return session.roleLabel
      }
      const map = { admin: '管理员', doctor: '医生', nurse: '护士' }
      return map[roleCode] || roleCode
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

/* 诊所切换器 */
.clinic-switcher {
  position: relative;
  display: flex;
  align-items: center;
}

.clinic-switcher-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: var(--apple-radius-full);
  background: rgba(90, 143, 123, 0.06);
  border: 1px solid rgba(90, 143, 123, 0.12);
  cursor: pointer;
  transition: all var(--apple-transition-fast);
}

.clinic-switcher-trigger:hover {
  background: rgba(90, 143, 123, 0.1);
  border-color: rgba(90, 143, 123, 0.2);
}

.clinic-switcher-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--apple-accent);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.clinic-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 220px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.08), 0 8px 10px -6px rgba(0, 0, 0, 0.03);
  padding: 12px;
  z-index: 1001;
}

.clinic-dropdown::before {
  content: '';
  position: absolute;
  top: -10px;
  left: 0;
  right: 0;
  height: 10px;
}

.clinic-dropdown-header {
  font-size: 11px;
  font-weight: 600;
  color: var(--apple-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 4px 8px 8px;
}

.clinic-dropdown-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: var(--apple-radius-sm);
  font-size: 13px;
  color: var(--apple-text-primary);
  cursor: pointer;
  transition: all var(--apple-transition-fast);
}

.clinic-dropdown-item:hover {
  background: var(--apple-bg-hover);
}

.clinic-dropdown-item.active {
  background: rgba(90, 143, 123, 0.08);
  color: var(--apple-accent);
}

.clinic-dropdown-item-name {
  font-weight: 500;
}

.clinic-dropdown-item--manage {
  color: var(--apple-text-secondary);
  gap: 8px;
}

.clinic-dropdown-divider {
  height: 1px;
  background: var(--apple-divider);
  margin: 8px 0;
}

/* 授权信息下拉 */
.license-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 260px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.08), 0 8px 10px -6px rgba(0, 0, 0, 0.03);
  padding: 14px;
  z-index: 1001;
}
.license-dropdown::before {
  content: '';
  position: absolute;
  top: -10px;
  left: 0;
  right: 0;
  height: 10px;
}
.license-dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--apple-divider);
}
.license-dropdown-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text-primary);
}
.license-dropdown-title i {
  font-size: 16px;
  color: #5A8F7B;
}
.license-refresh-btn {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--apple-text-tertiary);
  transition: all 0.2s ease;
  font-size: 12px;
}
.license-refresh-btn:hover {
  background: var(--apple-bg-hover);
  color: var(--apple-accent);
}
.license-dropdown-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.license-dropdown-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}
.license-dropdown-label {
  color: var(--apple-text-tertiary);
}
.license-dropdown-code {
  font-family: monospace;
  font-weight: 600;
  color: var(--apple-text-primary);
  letter-spacing: 0.03em;
}
.license-dropdown-value {
  font-weight: 500;
  color: var(--apple-text-primary);
}
.license-btn {
  position: relative;
}
.license-btn.license-expired i {
  color: #e6a23c;
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

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
  border-radius: 50%;
}

.avatar-overlay i {
  color: #fff;
  font-size: 18px;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
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

/* 隐藏的头像上传组件 */
.avatar-uploader-hidden {
  display: none;
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
