<template>
  <div class="staff-h5-shell">
    <router-view />
    <div class="staff-h5-tabbar">
      <div class="tabbar-item" :class="{ active: activePath === '/staff-portal-home' }" @click="go('/staff-portal-home')">
        <div class="icon">🏠</div>
        <div class="label">首页</div>
      </div>
      <div class="tabbar-item" :class="{ active: activePath === '/staff-h5/appointments' }" @click="go('/staff-h5/appointments')">
        <div class="icon">📅</div>
        <div class="label">预约</div>
      </div>
      <div class="tabbar-item" :class="{ active: activePath === '/staff-h5/consultations' }" @click="go('/staff-h5/consultations')">
        <div class="icon">💬</div>
        <div class="label">咨询</div>
      </div>
      <div class="tabbar-item" :class="{ active: activePath === '/staff-h5/patients' }" @click="go('/staff-h5/patients')">
        <div class="icon">👥</div>
        <div class="label">患者</div>
      </div>
    </div>
  </div>
</template>

<script>
import { getStaffPortalQuery } from '@/utils/portalSession'

export default {
  name: 'StaffH5Shell',
  computed: {
    activePath() {
      return this.$route.path
    }
  },
  methods: {
    go(path) {
      this.$router.push({ path, query: getStaffPortalQuery(this.$route.query) })
    }
  }
}
</script>

<style scoped>
.staff-h5-shell {
  min-height: 100vh;
  background: #f5f7fb;
  padding-bottom: 72px;
  box-sizing: border-box;
}
.staff-h5-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: rgba(255,255,255,0.96);
  backdrop-filter: blur(8px);
  border-top: 1px solid #e5e7eb;
  z-index: 50;
}
.tabbar-item {
  text-align: center;
  padding: 10px 0 12px;
  color: #8b95a7;
  font-size: 12px;
}
.tabbar-item .icon {
  font-size: 20px;
  margin-bottom: 4px;
}
.tabbar-item.active {
  color: #409eff;
  font-weight: 700;
}
</style>
