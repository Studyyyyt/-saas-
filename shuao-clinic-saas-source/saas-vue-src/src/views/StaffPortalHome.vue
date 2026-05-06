<template>
  <div class="staff-portal-shell" v-loading="loading">
    <div class="staff-portal-mobile">
      <section class="hero-card">
        <div class="hero-top">
          <span class="hero-badge">员工微信工作台</span>
          <span class="hero-badge hero-badge--light">{{ summary.wechatBound ? '微信已绑定' : '未绑定' }}</span>
        </div>
        <div class="hero-content">
          <div>
            <div class="hero-title">{{ summary.displayName ? `${summary.displayName}，你好` : '员工中心' }}</div>
            <div class="hero-subtitle">在微信内快速查看个人账号信息与常用工作入口。</div>
          </div>
          <div class="hero-clinic">长沙舒澳口腔</div>
        </div>
        <div class="hero-stats" :class="{ 'hero-stats--single': heroStats.length === 1 }">
          <div v-for="item in heroStats" :key="item.label" class="hero-stat">
            <div class="hero-stat__value">{{ item.value }}</div>
            <div class="hero-stat__label">{{ item.label }}</div>
          </div>
        </div>
      </section>

      <section class="section-card" v-if="todoCards.length">
        <div class="section-header">
          <div>
            <div class="section-title">医生待办</div>
            <div class="section-subtitle">基于当前医生身份生成的微信快捷工作概览</div>
          </div>
        </div>
        <div class="todo-grid">
          <div v-for="item in todoCards" :key="item.label" class="todo-card">
            <div class="todo-card__value">{{ item.value }}</div>
            <div class="todo-card__label">{{ item.label }}</div>
          </div>
        </div>
      </section>

      <section class="quick-grid">
        <div class="quick-card" @click="goTo('/staff-h5/appointments')">
          <div class="quick-card__icon">📅</div>
          <div class="quick-card__title">今日预约</div>
          <div class="quick-card__desc">查看预约安排</div>
        </div>
        <div class="quick-card" @click="goTo('/staff-h5/consultations')">
          <div class="quick-card__icon">💬</div>
          <div class="quick-card__title">咨询录入</div>
          <div class="quick-card__desc">先记咨询，再进预约</div>
        </div>
        <div class="quick-card" @click="goTo('/staff-h5/patients')">
          <div class="quick-card__icon">🧑‍⚕️</div>
          <div class="quick-card__title">我的患者</div>
          <div class="quick-card__desc">进入患者列表</div>
        </div>
        <div class="quick-card" @click="goTo('/staff-h5/lab-processing')">
          <div class="quick-card__icon">🦷</div>
          <div class="quick-card__title">外加工</div>
          <div class="quick-card__desc">查看待登记加工</div>
        </div>
        <div v-if="canSeePurchase" class="quick-card" @click="goTo('/staff-h5/material-purchases')">
          <div class="quick-card__icon">🧾</div>
          <div class="quick-card__title">采购记录</div>
          <div class="quick-card__desc">新建采购单</div>
        </div>
      </section>

      <section class="section-card" v-if="error">
        <div class="portal-error">{{ error }}</div>
      </section>

      <template v-else-if="account.id">
        <section class="section-card">
          <div class="section-header">
            <div>
              <div class="section-title">员工资料</div>
              <div class="section-subtitle">当前微信已绑定的员工账号信息</div>
            </div>
          </div>
          <div class="card-list">
            <div class="info-card">
              <div class="info-card__meta"><strong>姓名：</strong>{{ account.name || '--' }}</div>
              <div class="info-card__meta"><strong>角色：</strong>{{ summary.roleLabel || account.role || '--' }}</div>
              <div class="info-card__meta"><strong>微信绑定：</strong>{{ summary.wechatBound ? '已绑定' : '未绑定' }}</div>
            </div>
          </div>
        </section>

        <section class="section-card">
          <div class="section-header">
            <div>
              <div class="section-title">使用说明</div>
              <div class="section-subtitle">员工端 H5 当前为第一版 MVP</div>
            </div>
          </div>
          <div class="card-list">
            <div class="info-card">
              <div class="info-card__meta">1. 从微信公众号菜单进入员工入口。</div>
              <div class="info-card__meta">2. 系统会根据当前微信 openid 自动匹配后台已绑定的员工账号。</div>
              <div class="info-card__meta">3. 如未绑定，请先在后台账号管理中为员工账号填写并保存 wechat_openid。</div>
            </div>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { getStaffPortalQuery, saveStaffPortalSessionFromQuery } from '@/utils/portalSession'

export default {
  name: 'StaffPortalHome',
  data() {
    return {
      loading: false,
      error: '',
      payload: {},
      doctorPerformanceSummary: {
        received_amount: 0,
        project_count: 0
      }
    }
  },
  computed: {
    account() {
      return this.payload.account || {}
    },
    summary() {
      return this.payload.summary || {}
    },
    quickActions() {
      return this.payload.quickActions || {}
    },
    isDoctor() {
      return (this.summary.roleLabel || '') === '医生'
    },
    isAdmin() {
      return (this.summary.roleLabel || '') === '管理员'
    },
    isNurse() {
      return (this.summary.roleLabel || '') === '护士'
    },
    canSeePurchase() {
      return this.isAdmin || this.isNurse
    },
    heroStats() {
      const stats = [
        { label: '当前角色', value: this.summary.roleLabel || '员工' }
      ]
      if (this.isDoctor) {
        stats.push(
          { label: '当月业绩', value: `¥${this.formatMoney(this.doctorPerformanceSummary.received_amount)}` },
          { label: '业绩操作', value: String(Number(this.doctorPerformanceSummary.project_count || 0)) }
        )
      }
      return stats
    },
    todoCards() {
      const roleLabel = this.summary.roleLabel || ''
      if (roleLabel !== '医生') return []
      return [
        { label: '预约入口', value: '今日' },
        { label: '患者入口', value: '微信H5' },
        { label: '360管理', value: '已开启' }
      ]
    }
  },
  mounted() {
    saveStaffPortalSessionFromQuery(this.$route.query)
    this.loadPortalData()
  },
  methods: {
    formatDate(date) {
      const d = new Date(date)
      if (Number.isNaN(d.getTime())) return ''
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },
    formatMoney(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    resetDoctorPerformance() {
      this.doctorPerformanceSummary = {
        received_amount: 0,
        project_count: 0
      }
    },
    buildPortalQuery(extra = {}) {
      const query = getStaffPortalQuery(this.$route.query)
      const doctorName = this.summary.doctorName || query.doctorName || ''
      return Object.assign({}, query, doctorName ? { doctorName } : {}, extra)
    },
    loadDoctorPerformance(account, summary) {
      const doctorAccountId = Number(account && account.id)
      const doctorName = String((summary && summary.doctorName) || (account && account.name) || '').trim()
      if (!Number.isFinite(doctorAccountId) || doctorAccountId <= 0 || !doctorName) {
        this.resetDoctorPerformance()
        return Promise.resolve()
      }
      const today = this.formatDate(new Date())
      const monthStart = this.formatDate(new Date(new Date().getFullYear(), new Date().getMonth(), 1))
      return axios.get('/finances/doctorPerformance', {
        params: {
          startDate: monthStart,
          endDate: today,
          doctorAccountId,
          doctorName
        }
      }).then(res => {
        if (res.data.code !== '200') {
          this.resetDoctorPerformance()
          return
        }
        const summaryData = (res.data.data && res.data.data.summary) || {}
        this.doctorPerformanceSummary = {
          received_amount: Number(summaryData.received_amount || 0),
          project_count: Number(summaryData.project_count || 0)
        }
      }).catch(() => {
        this.resetDoctorPerformance()
      })
    },
    loadPortalData() {
      const query = getStaffPortalQuery(this.$route.query)
      if (!query.accountId || !query.staffToken) {
        this.error = '缺少员工身份信息'
        return
      }
      this.loading = true
      axios.get('/staff-portal/overview', {
        params: {
          accountId: query.accountId,
          staffToken: query.staffToken
        }
      }).then(res => {
        if (res.data.code !== '200') {
          this.error = res.data.msg || '加载失败'
          return
        }
        this.error = ''
        const data = res.data.data || {}
        this.payload = data
        if (String((data.summary || {}).roleLabel || '') === '医生') {
          return this.loadDoctorPerformance(data.account || {}, data.summary || {})
        }
        this.resetDoctorPerformance()
      }).catch(() => {
        this.error = '员工中心加载失败'
        this.resetDoctorPerformance()
      }).finally(() => {
        this.loading = false
      })
    },
    goTo(path) {
      this.$router.push({ path, query: this.buildPortalQuery() })
    }
  }
}
</script>

<style scoped>
.staff-portal-shell {
  min-height: 100vh;
  background: linear-gradient(180deg, #eef4ff 0%, #f8fbff 100%);
  overflow-x: hidden;
}
.staff-portal-mobile {
  max-width: 520px;
  margin: 0 auto;
  padding: 18px 14px 28px;
  box-sizing: border-box;
  min-width: 0;
}
.hero-card,
.section-card {
  background: #fff;
  border-radius: 20px;
  padding: 18px;
  box-shadow: 0 10px 28px rgba(31, 71, 136, 0.08);
  margin-bottom: 16px;
  min-width: 0;
}
.hero-top,
.hero-content,
.section-header,
.info-card__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.hero-top {
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1.5;
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
  word-break: break-word;
}
.hero-badge--light {
  background: #f4f7fb;
  color: #606266;
}
.hero-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2d3d;
  margin-bottom: 8px;
  line-height: 1.3;
  word-break: break-word;
}
.hero-subtitle,
.section-subtitle,
.info-card__meta,
.portal-error {
  color: #606266;
  line-height: 1.7;
  font-size: 14px;
  word-break: break-word;
}
.hero-clinic {
  font-size: 13px;
  color: #8b95a7;
  line-height: 1.6;
  word-break: break-word;
}
.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 18px;
}
.hero-stats--single {
  grid-template-columns: minmax(0, 1fr);
}
.hero-stat {
  background: #f8fbff;
  border-radius: 14px;
  padding: 12px 10px;
  text-align: center;
  min-width: 0;
}
.hero-stat__value {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  word-break: break-word;
}
.hero-stat__label {
  margin-top: 6px;
  font-size: 12px;
  color: #8b95a7;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}
.quick-card {
  background: #fff;
  border-radius: 18px;
  padding: 16px 14px;
  box-shadow: 0 8px 20px rgba(31, 71, 136, 0.06);
  cursor: pointer;
  min-width: 0;
}
.quick-card__icon {
  font-size: 24px;
  margin-bottom: 10px;
}
.quick-card__title,
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  line-height: 1.5;
  word-break: break-word;
}
.quick-card__desc {
  margin-top: 6px;
  font-size: 13px;
  color: #8b95a7;
  line-height: 1.6;
  word-break: break-word;
}
.todo-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}
.todo-card {
  border-radius: 16px;
  background: #f8fbff;
  padding: 14px 12px;
  text-align: center;
  min-width: 0;
}
.todo-card__value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  word-break: break-word;
}
.todo-card__label {
  margin-top: 6px;
  font-size: 12px;
  color: #8b95a7;
  line-height: 1.5;
  word-break: break-word;
}
.card-list {
  margin-top: 14px;
  display: grid;
  gap: 12px;
}
.info-card {
  border-radius: 16px;
  background: #f8fbff;
  padding: 14px;
  min-width: 0;
}
.portal-error {
  color: #f56c6c;
}

@media (max-width: 768px) {
  .hero-top,
  .hero-content,
  .section-header,
  .info-card__top {
    align-items: flex-start;
  }
}

@media (max-width: 420px) {
  .staff-portal-mobile {
    padding: 12px 10px 24px;
  }

  .hero-card,
  .section-card,
  .quick-card,
  .todo-card,
  .info-card {
    border-radius: 18px;
  }

  .hero-top,
  .hero-content,
  .section-header,
  .info-card__top {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-title {
    font-size: 21px;
  }

  .hero-stat__value,
  .todo-card__value {
    font-size: 17px;
  }
}

@media (max-width: 359px) {
  .hero-stats,
  .todo-grid,
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
