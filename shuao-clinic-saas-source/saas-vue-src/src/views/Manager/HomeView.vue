<template>
  <div class="home-wrap">
    <template v-if="isDoctor">
      <div class="doctor-hero-card">
        <div>
          <div class="hero-kicker">医生工作台</div>
          <h1>{{ username || '医生' }}，今日待办</h1>
          <p>仅展示当前医生名下的接诊、病历、回访与明日预约确认。</p>
        </div>
        <div class="hero-clock">
          <div class="clock-label">当前时间</div>
          <div class="clock-value">{{ currentTime }}</div>
          <div class="clock-date">{{ todayLabel }}</div>
        </div>
      </div>

      <div class="doctor-todo-grid">
        <div class="todo-stat-card primary clickable" @click="goPage('/Appointment2')">
          <div class="todo-stat-card__label">待接诊</div>
          <div class="todo-stat-card__value">{{ doctorTodos.pendingConsultations }}</div>
          <div class="todo-stat-card__desc">今日未完成预约</div>
        </div>
        <div class="todo-stat-card success clickable" @click="goPage('/MedicalRecord')">
          <div class="todo-stat-card__label">待写病历</div>
          <div class="todo-stat-card__value">{{ doctorTodos.pendingRecords }}</div>
          <div class="todo-stat-card__desc">已就诊未留病历</div>
        </div>
        <div class="todo-stat-card warn clickable" @click="goPage('/Followup')">
          <div class="todo-stat-card__label">待回访</div>
          <div class="todo-stat-card__value">{{ doctorTodos.pendingFollowups }}</div>
          <div class="todo-stat-card__desc">今日到期或逾期</div>
        </div>
        <div class="todo-stat-card info clickable" @click="goPage('/Appointment2')">
          <div class="todo-stat-card__label">待确认明日预约</div>
          <div class="todo-stat-card__value">{{ doctorTodos.tomorrowAppointments }}</div>
          <div class="todo-stat-card__desc">明日有效预约</div>
        </div>
      </div>

      <div class="doctor-panel-grid">
        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-title">待办口径</div>
          <div class="doctor-note-list">
            <div class="doctor-note-item">
              <span class="dot blue"></span>
              <span>待接诊：今日状态为“待治疗 / 已预约 / 待就诊”的预约。</span>
            </div>
            <div class="doctor-note-item">
              <span class="dot green"></span>
              <span>待写病历：今日已就诊，但当前医生名下还没有对应病历的患者。</span>
            </div>
            <div class="doctor-note-item">
              <span class="dot orange"></span>
              <span>待回访：计划回访时间在今天及之前，且尚未填写回访结果的患者。</span>
            </div>
            <div class="doctor-note-item">
              <span class="dot slate"></span>
              <span>待确认明日预约：明天未取消、未改约的有效预约。</span>
            </div>
          </div>
        </el-card>

        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-title">快捷入口</div>
          <div class="doctor-action-grid">
            <div class="doctor-action-item" @click="goPage('/Appointment2')">
              <i class="el-icon-date"></i>
              <span>预约日程</span>
            </div>
            <div class="doctor-action-item" @click="goPage('/MedicalRecord')">
              <i class="el-icon-document"></i>
              <span>病历列表</span>
            </div>
            <div class="doctor-action-item" @click="goPage('/Followup')">
              <i class="el-icon-phone-outline"></i>
              <span>回访管理</span>
            </div>
            <div class="doctor-action-item" @click="loadDashboard">
              <i class="el-icon-refresh"></i>
              <span>刷新待办</span>
            </div>
          </div>
          <div class="last-updated">最后刷新：{{ lastUpdatedAt || '--' }}</div>
          <div v-if="lastUpdatedFromCache" class="last-updated-cache">当前展示为最近缓存</div>
          <div class="auto-refresh-tip">医生首页每 60 秒自动刷新一次提醒</div>
        </el-card>
      </div>

      <el-card v-if="missingNextAppointmentPatients.length" class="panel-card doctor-reminder-card" shadow="never">
        <div slot="header" class="panel-title">未挂新预约提醒</div>
        <div class="doctor-reminder-list">
          <div v-for="item in missingNextAppointmentPatients" :key="item.key" class="doctor-reminder-item">
            <div>
              <div class="doctor-reminder-title">{{ item.patient_name || '未命名患者' }}</div>
              <div class="doctor-reminder-desc">最近完成治疗：{{ item.latest_treatment_date || '-' }} · {{ item.latest_treatment || '未填写项目' }}</div>
            </div>
            <div class="doctor-reminder-actions">
              <el-button size="mini" type="primary" plain @click="openReminderAppointment(item)">新增预约</el-button>
              <el-button size="mini" @click="markReminderCompleted(item)">已全部完成</el-button>
            </div>
          </div>
        </div>
      </el-card>
    </template>

    <template v-else>
      <div class="hero-card">
        <div>
          <div class="hero-kicker">今日门诊运营概览</div>
          <h1>欢迎回来，{{ username || '医生/管理员' }}</h1>
          <p>聚焦预约、患者、治疗、收费四条主线，快速查看当日经营状态。</p>
        </div>
        <div class="hero-clock">
          <div class="clock-label">当前时间</div>
          <div class="clock-value">{{ currentTime }}</div>
        </div>
      </div>

      <div class="stats-grid">
        <div class="stat-card primary clickable" @click="goPage('/Appointment2')">
          <div class="stat-label">今日预约</div>
          <div class="stat-value">{{ stats.todayAppointments }}</div>
          <div class="stat-desc">点击查看今日日程</div>
        </div>
        <div class="stat-card clickable" @click="goPage('/Patient')">
          <div class="stat-label">患者总数</div>
          <div class="stat-value">{{ stats.totalPatients }}</div>
          <div class="stat-desc">点击查看患者列表</div>
        </div>
        <div class="stat-card success clickable" @click="goPatientGroup('highValue', 'totalSpentDesc')">
          <div class="stat-label">高价值客户</div>
          <div class="stat-value">{{ stats.highValueCount }}</div>
          <div class="stat-desc">按累计消费进入名单</div>
        </div>
        <div class="stat-card attention clickable" @click="goPatientGroup('lostRisk', 'lastVisitDesc')">
          <div class="stat-label">流失风险客户</div>
          <div class="stat-value">{{ stats.lostRiskCount }}</div>
          <div class="stat-desc">按最近到店倒序查看</div>
        </div>
        <div class="stat-card teal clickable" @click="goPatientGroup('all', 'recent', { sourceFilter: '转介绍' })">
          <div class="stat-label">本月新增转介绍</div>
          <div class="stat-value">{{ stats.monthNewReferralCount }}</div>
          <div class="stat-desc">点击查看转介绍患者列表</div>
        </div>
        <div class="stat-card success clickable" @click="goPage('/Financial2')">
          <div class="stat-label">本月已收费</div>
          <div class="stat-value">¥{{ stats.monthIncome }}</div>
          <div class="stat-desc">点击查看收费明细</div>
        </div>
        <div class="stat-card danger clickable" @click="goPendingLabOrders">
          <div class="stat-label">待登记加工</div>
          <div class="stat-value">{{ stats.pendingLabRegistrations }}</div>
          <div class="stat-desc">点击处理待登记操作</div>
          <div class="stat-note">基于病历操作的待登记加工</div>
        </div>
        <div class="stat-card teal clickable" @click="goPage('/lab-bills')">
          <div class="stat-label">本月加工费</div>
          <div class="stat-value">¥{{ stats.monthLabExpense }}</div>
          <div class="stat-desc">待对账 {{ stats.pendingLabBills }} 份账单</div>
        </div>
        <div class="stat-card violet clickable" @click="goPage('/materials')">
          <div class="stat-label">本月耗材支出</div>
          <div class="stat-value">¥{{ stats.monthMaterialExpense }}</div>
          <div class="stat-desc">库存预警 {{ stats.materialAlerts }} 条</div>
        </div>
        <div class="stat-card warn clickable" @click="goPage('/MedicalRecord')">
          <div class="stat-label">病历总数</div>
          <div class="stat-value">{{ stats.totalRecords }}</div>
          <div class="stat-desc">点击查看病历列表</div>
        </div>
      </div>

      <div class="content-grid">
        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-title">今日运营提醒</div>
          <div class="panel-list">
            <div class="panel-item">
              <span class="dot blue"></span>
              <span>今日预约 {{ stats.todayAppointments }} 条，点击上方卡片可直达预约页。</span>
            </div>
            <div class="panel-item">
              <span class="dot green"></span>
              <span>当前患者总数 {{ stats.totalPatients }} 人，可点击患者卡片查看详情。</span>
            </div>
            <div class="panel-item">
              <span class="dot orange"></span>
              <span>本月已收费 ¥{{ stats.monthIncome }}，病历总数 {{ stats.totalRecords }} 份。</span>
            </div>
            <div class="panel-item">
              <span class="dot red"></span>
              <span>待登记加工 {{ stats.pendingLabRegistrations }} 条，按病历操作维度统计。</span>
            </div>
            <div class="panel-item">
              <span class="dot teal"></span>
              <span>本月加工费 ¥{{ stats.monthLabExpense }}，待对账账单 {{ stats.pendingLabBills }} 份。</span>
            </div>
            <div class="panel-item">
              <span class="dot violet"></span>
              <span>本月耗材支出 ¥{{ stats.monthMaterialExpense }}，库存预警 {{ stats.materialAlerts }} 条。</span>
            </div>
          </div>
        </el-card>

        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-title">快捷入口</div>
          <div class="quick-grid">
            <div class="quick-item" @click="$router.push('/Patient')">
              <i class="el-icon-user"></i>
              <span>患者管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/Appointment2')">
              <i class="el-icon-date"></i>
              <span>预约管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/Financial')">
              <i class="el-icon-document-copy"></i>
              <span>经营报表</span>
            </div>
            <div class="quick-item" @click="$router.push('/Financial2')">
              <i class="el-icon-s-data"></i>
              <span>门诊分析</span>
            </div>
            <div class="quick-item" @click="$router.push('/BusinessAnalysis')">
              <i class="el-icon-magic-stick"></i>
              <span>AI经营日报</span>
            </div>
            <div class="quick-item" @click="$router.push('/lab-bills')">
              <i class="el-icon-office-building"></i>
              <span>义齿账单</span>
            </div>
            <div class="quick-item" @click="goPendingLabOrders">
              <i class="el-icon-warning-outline"></i>
              <span>待登记加工</span>
            </div>
            <div class="quick-item" @click="$router.push('/materials')">
              <i class="el-icon-collection-tag"></i>
              <span>耗材库存</span>
            </div>
          </div>
        </el-card>
      </div>

      <div class="content-grid secondary-grid">
        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-title">待办事项</div>
          <div class="todo-list">
            <div class="todo-item">
              <div>
                <div class="todo-title">预约总览</div>
                <div class="todo-desc">今日共有 {{ stats.todayAppointments }} 条预约，请及时跟进到诊情况。</div>
              </div>
              <el-tag size="mini" type="warning">预约</el-tag>
            </div>
            <div class="todo-item">
              <div>
                <div class="todo-title">经营与病历同步查看</div>
                <div class="todo-desc">当前病历 {{ stats.totalRecords }} 份，患者 {{ stats.totalPatients }} 人。</div>
              </div>
              <el-tag size="mini">概览</el-tag>
            </div>
          </div>
        </el-card>

        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-title">预约趋势</div>
          <div class="trend-placeholder">
            <div class="trend-bars">
              <span style="height: 42%"></span>
              <span style="height: 68%"></span>
              <span style="height: 55%"></span>
              <span style="height: 82%"></span>
              <span style="height: 74%"></span>
              <span style="height: 91%"></span>
              <span style="height: 64%"></span>
            </div>
            <div class="trend-labels">
              <span>一</span><span>二</span><span>三</span><span>四</span><span>五</span><span>六</span><span>日</span>
            </div>
          </div>
        </el-card>
      </div>

      <el-card class="calendar-card" shadow="never">
        <div slot="header" class="panel-title">门诊日历</div>
        <el-calendar>
          <template slot="dateCell" slot-scope="{ data }">
            <p :class="data.isSelected ? 'is-selected' : ''">
              {{ data.day.split('-').slice(1).join('-') }} {{ data.isSelected ? '✔️' : '' }}
            </p>
          </template>
        </el-calendar>
      </el-card>
    </template>
  </div>
</template>

<script>
import axios from 'axios'
import { ADMIN_SESSION_EVENT, getAdminSession } from '@/utils/adminSession'
import { fetchCachedResource } from '@/utils/offline/apiClient'
import { savePendingAppointmentPatient } from '@/utils/appointmentPrefill'

const WAITING_APPOINTMENT_STATUSES = ['待治疗', '已预约', '待就诊']
const COMPLETED_APPOINTMENT_STATUSES = ['已就诊', '已治疗', '已完成', '已离开']
const INACTIVE_APPOINTMENT_STATUSES = ['已取消', '已改约']

function buildEmptyStats() {
  return {
    todayAppointments: 0,
    totalPatients: 0,
    highValueCount: 0,
    lostRiskCount: 0,
    monthNewReferralCount: 0,
    monthIncome: '0.00',
    totalRecords: 0,
    monthLabExpense: '0.00',
    pendingLabBills: 0,
    pendingLabRegistrations: 0,
    monthMaterialExpense: '0.00',
    materialAlerts: 0
  }
}

function buildEmptyDoctorTodos() {
  return {
    pendingConsultations: 0,
    pendingRecords: 0,
    pendingFollowups: 0,
    tomorrowAppointments: 0
  }
}

function buildSuccessResponse(data) {
  return {
    data: {
      code: '200',
      data
    }
  }
}

export default {
  name: 'HomeView',
  data() {
    return {
      currentTime: '',
      timer: null,
      dashboardTimer: null,
      dashboardLoading: false,
      dashboardReloadQueued: false,
      user: getAdminSession() || {},
      username: '',
      lastUpdatedAt: '',
      lastUpdatedFromCache: false,
      stats: buildEmptyStats(),
      doctorTodos: buildEmptyDoctorTodos(),
      missingNextAppointmentPatients: []
    }
  },
  computed: {
    normalizedRole() {
      const role = String((this.user && this.user.role) || '').trim()
      if (role === '管理员' || role === 'admin') return 'admin'
      if (role === '医生' || role === 'doctor') return 'doctor'
      if (role === '护士' || role === 'nurse') return 'nurse'
      return role
    },
    isDoctor() {
      return this.normalizedRole === 'doctor'
    },
    currentDoctorId() {
      const id = Number(this.user && this.user.id)
      return Number.isFinite(id) && id > 0 ? id : 0
    },
    currentDoctorName() {
      return String((this.user && this.user.name) || '').trim()
    },
    todayLabel() {
      return this.formatDisplayDate(new Date())
    }
  },
  mounted() {
    this.syncUserFromStorage()
    this.updateTime()
    this.loadDashboard()
    this.configureDashboardRefresh()
    this.timer = setInterval(this.updateTime, 1000)
    window.addEventListener(ADMIN_SESSION_EVENT, this.handleIdentityRefresh)
    window.addEventListener('focus', this.handleIdentityRefresh)
    window.addEventListener('online', this.handleVisibilityRefresh)
    document.addEventListener('visibilitychange', this.handleVisibilityRefresh)
  },
  beforeDestroy() {
    clearInterval(this.timer)
    clearInterval(this.dashboardTimer)
    window.removeEventListener(ADMIN_SESSION_EVENT, this.handleIdentityRefresh)
    window.removeEventListener('focus', this.handleIdentityRefresh)
    window.removeEventListener('online', this.handleVisibilityRefresh)
    document.removeEventListener('visibilitychange', this.handleVisibilityRefresh)
  },
  methods: {
    handleIdentityRefresh() {
      this.syncUserFromStorage()
      this.configureDashboardRefresh()
      this.loadDashboard()
    },
    handleVisibilityRefresh() {
      if (typeof document !== 'undefined' && document.visibilityState === 'hidden') {
        return
      }
      this.loadDashboard()
    },
    syncUserFromStorage() {
      this.user = getAdminSession() || {}
      this.username = this.user.name || ''
    },
    updateTime() {
      const now = new Date()
      this.currentTime = now.toLocaleTimeString('zh-CN', { hour12: false })
    },
    configureDashboardRefresh() {
      clearInterval(this.dashboardTimer)
      this.dashboardTimer = null
      if (!this.isDoctor) return
      this.dashboardTimer = setInterval(() => {
        if (typeof document !== 'undefined' && document.visibilityState === 'hidden') {
          return
        }
        this.loadDashboard()
      }, 60000)
    },
    pad2(value) {
      return String(value).padStart(2, '0')
    },
    formatDate(date) {
      const d = new Date(date)
      if (Number.isNaN(d.getTime())) return ''
      const y = d.getFullYear()
      const m = this.pad2(d.getMonth() + 1)
      const day = this.pad2(d.getDate())
      return `${y}-${m}-${day}`
    },
    formatDisplayDate(date) {
      const d = new Date(date)
      if (Number.isNaN(d.getTime())) return ''
      return `${d.getFullYear()}年${this.pad2(d.getMonth() + 1)}月${this.pad2(d.getDate())}日`
    },
    formatDateTime(date) {
      const d = new Date(date)
      if (Number.isNaN(d.getTime())) return ''
      return `${this.formatDate(d)} ${this.pad2(d.getHours())}:${this.pad2(d.getMinutes())}:${this.pad2(d.getSeconds())}`
    },
    formatMoney(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    formatMonth(date) {
      const d = new Date(date)
      const y = d.getFullYear()
      const m = this.pad2(d.getMonth() + 1)
      return `${y}-${m}`
    },
    dateKey(value) {
      const text = String(value || '').trim()
      if (!text) return ''
      const matched = text.match(/^(\d{4}-\d{2}-\d{2})/)
      if (matched) {
        return matched[1]
      }
      const parsed = new Date(value)
      if (Number.isNaN(parsed.getTime())) return ''
      return this.formatDate(parsed)
    },
    goPage(path) {
      this.$router.push(path)
    },
    goPatientGroup(groupKey, sortMode, extraQuery = {}) {
      this.$router.push({ path: '/Patient', query: Object.assign({ groupKey, sortMode }, extraQuery || {}) })
    },
    goPendingLabOrders() {
      this.$router.push({ path: '/lab-orders', query: { pendingLab: '1' } })
    },
    statusText(value) {
      return String(value || '').trim()
    },
    followupResultText(item) {
      return String((item && item.summary) || '').trim()
    },
    followupDueDateKey(item) {
      return this.dateKey(item && (item.followup_date || item.next_followup_date))
    },
    isPendingFollowupItem(item) {
      return !this.followupResultText(item)
    },
    matchesCurrentDoctorFollowup(item, doctorPatientRefs) {
      const doctorId = Number(item && item.doctor_account_id)
      const doctorName = String((item && item.doctor_name) || '').trim()
      const sameId = this.currentDoctorId > 0 && Number.isFinite(doctorId) && doctorId > 0 && doctorId === this.currentDoctorId
      const sameName = !!this.currentDoctorName && !!doctorName && doctorName === this.currentDoctorName
      if (sameId || sameName) {
        return true
      }
      const patientRef = this.patientRefKey(item)
      return !!patientRef && doctorPatientRefs.has(patientRef)
    },
    matchesCurrentDoctor(item) {
      const doctorId = Number(item && item.doctor_account_id)
      const doctorName = String((item && item.doctor_name) || '').trim()
      const sameId = this.currentDoctorId > 0 && Number.isFinite(doctorId) && doctorId > 0 && doctorId === this.currentDoctorId
      const sameName = !!this.currentDoctorName && !!doctorName && doctorName === this.currentDoctorName
      return sameId || sameName
    },
    patientRefKey(item) {
      const patientId = Number(item && item.patient_id)
      if (Number.isFinite(patientId) && patientId > 0) {
        return `id:${patientId}`
      }
      const patientName = String((item && item.patient_name) || '').trim()
      return patientName ? `name:${patientName}` : ''
    },
    visitRefKey(item, fallbackDate = '') {
      const patientRef = this.patientRefKey(item)
      const visitDate = fallbackDate || this.dateKey(item && (item.visit_date || item.appointment_date))
      if (!patientRef || !visitDate) return ''
      return `${patientRef}|${visitDate}`
    },
    latestCompletedTreatmentRef(item) {
      const patientRef = this.patientRefKey(item)
      const treatmentDate = this.dateKey(item && item.treatment_date)
      if (!patientRef || !treatmentDate) return ''
      return `${patientRef}|${treatmentDate}`
    },
    extractList(response) {
      const data = (response && response.data && response.data.data) || {}
      if (Array.isArray(data)) return data
      if (Array.isArray(data.list)) return data.list
      return []
    },
    async loadDismissedReminderKeys() {
      if (!this.currentDoctorId) return new Set()
      const res = await axios.get('/doctor-home-reminders/dismissed', {
        params: { doctorAccountId: this.currentDoctorId }
      }).catch(() => ({ data: { data: [] } }))
      const list = Array.isArray(res && res.data && res.data.data) ? res.data.data : []
      return new Set(list.map(item => String(item || '').trim()).filter(Boolean))
    },
    async dismissReminder(reminder) {
      if (!this.currentDoctorId || !reminder || !reminder.key) return
      await axios.post('/doctor-home-reminders/dismiss', {
        doctor_account_id: this.currentDoctorId,
        doctor_name: this.currentDoctorName || this.username || '',
        patient_id: reminder.patient_id || null,
        patient_name: reminder.patient_name || '',
        reminder_key: reminder.key
      })
    },
    async clearReminderDismissal(reminder) {
      if (!this.currentDoctorId || !reminder || !reminder.key) return
      await axios.delete('/doctor-home-reminders/dismissed', {
        params: {
          doctorAccountId: this.currentDoctorId,
          reminderKey: reminder.key
        }
      })
    },
    async openReminderAppointment(reminder) {
      if (!reminder) return
      await this.clearReminderDismissal(reminder).catch(() => {})
      savePendingAppointmentPatient({
        patient_id: reminder.patient_id || null,
        patient_name: reminder.patient_name || '',
        appointment_purpose: reminder.latest_treatment || ''
      })
      this.$router.push('/Appointment2')
    },
    async markReminderCompleted(reminder) {
      if (!reminder || !reminder.key) return
      const previousList = this.missingNextAppointmentPatients.slice()
      this.missingNextAppointmentPatients = this.missingNextAppointmentPatients.filter(item => item.key !== reminder.key)
      try {
        await this.dismissReminder(reminder)
      } catch (error) {
        this.missingNextAppointmentPatients = previousList
        this.$message.error((error && error.message) || '提醒状态保存失败')
      }
    },
    async loadDashboard() {
      if (this.dashboardLoading) {
        this.dashboardReloadQueued = true
        return
      }
      this.dashboardLoading = true
      this.dashboardReloadQueued = false
      try {
        const result = this.isDoctor
          ? await this.loadDoctorTodos()
          : await this.loadManagementStats()
        if (result && result.success) {
          const displayTime = result.fromCache && result.cachedAt ? result.cachedAt : new Date()
          this.lastUpdatedAt = this.formatDateTime(displayTime)
          this.lastUpdatedFromCache = !!result.fromCache
        }
      } finally {
        this.dashboardLoading = false
        if (this.dashboardReloadQueued) {
          this.dashboardReloadQueued = false
          this.loadDashboard()
        }
      }
    },
    async buildManagementStatsPayload({ today, year, monthValue }) {
      const monthStart = `${year}-${monthValue}-01`
      const [appointmentsRes, patientsRes, recordsRes, financeRes, labBillsRes, materialPurchasesRes, materialAlertsRes, pendingLabRes, patientInsightRes] = await Promise.all([
        axios.get('/appointments/selectAll', { params: { page: 1, size: 1000 } }),
        axios.get('/patients/selectAll', { params: { page: 1, size: 1000 } }),
        axios.get('/medical-records/selectAll', { params: { page: 1, size: 1000 } }),
        axios.get('/finances/selectByMonth', {
          params: { year, month: monthValue }
        }).catch(() => ({ data: { data: [] } })),
        axios.get('/lab-bills/search', {
          params: { page: 1, size: 1000 }
        }).catch(() => ({ data: { data: { list: [] } } })),
        axios.get('/material-purchases/search', {
          params: {
            page: 1,
            size: 1000,
            status: '有效',
            startDate: monthStart,
            endDate: today
          }
        }).catch(() => ({ data: { data: { list: [] } } })),
        axios.get('/materials/search', {
          params: {
            page: 1,
            size: 1000,
            lowStockOnly: true
          }
        }).catch(() => ({ data: { data: { list: [], total: 0 } } })),
        axios.get('/medical-record-operations/pendingLabList', {
          params: { page: 1, size: 1000 }
        }).catch(() => ({ data: { data: { list: [], total: 0, pendingTotal: 0 } } })),
        axios.get('/patient-insights/overview').catch(() => ({ data: { data: {} } }))
      ])

      const appointmentList = this.extractList(appointmentsRes)
      const patientData = ((patientsRes.data || {}).data || {})
      const patientList = Array.isArray(patientData.list) ? patientData.list : []
      const recordData = ((recordsRes.data || {}).data || {})
      const recordList = Array.isArray(recordData.list) ? recordData.list : []
      const financeRaw = ((financeRes.data || {}).data || [])
      const financeList = Array.isArray(financeRaw) ? financeRaw : (Array.isArray(financeRaw.list) ? financeRaw.list : [])
      const labBillData = ((labBillsRes.data || {}).data || {})
      const labBillList = Array.isArray(labBillData.list) ? labBillData.list : []
      const materialPurchaseData = ((materialPurchasesRes.data || {}).data || {})
      const materialPurchaseList = Array.isArray(materialPurchaseData.list) ? materialPurchaseData.list : []
      const materialAlertData = ((materialAlertsRes.data || {}).data || {})
      const materialAlertList = Array.isArray(materialAlertData.list) ? materialAlertData.list : []
      const pendingLabData = ((pendingLabRes.data || {}).data || {})
      const pendingLabList = Array.isArray(pendingLabData.list) ? pendingLabData.list : []
      const patientInsightData = ((patientInsightRes.data || {}).data || {})

      const todayAppointments = appointmentList.filter(item => this.dateKey(item && item.appointment_date) === today).length
      const monthIncome = financeList.reduce((sum, item) => {
        const amount = Number(item.amount || 0)
        const type = String(item.type || '')
        const isIncome = type.includes('收入') || type.includes('收费')
        return isIncome ? sum + amount : sum
      }, 0)
      const currentBillMonth = `${year}-${monthValue}`
      const monthLabExpense = labBillList
        .filter(item => String((item && item.bill_month) || '').trim() === currentBillMonth)
        .filter(item => String((item && item.status) || '').trim() === '已完成对账')
        .reduce((sum, item) => sum + Number(item.total_amount || 0), 0)
      const pendingLabBills = labBillList
        .filter(item => String((item && item.status) || '').trim() !== '已完成对账')
        .length
      const monthMaterialExpense = materialPurchaseList
        .reduce((sum, item) => sum + Number(item.total_amount || 0), 0)
      const materialAlerts = Number(materialAlertData.total || materialAlertList.length || 0)

      return {
        stats: {
          todayAppointments,
          totalPatients: patientData.total || patientList.length || 0,
          highValueCount: Number(patientInsightData.high_value_count || 0),
          lostRiskCount: Number(patientInsightData.lost_risk_count || 0),
          monthNewReferralCount: Number(patientInsightData.month_new_referral_count || 0),
          monthIncome: monthIncome.toFixed(2),
          totalRecords: recordData.total || recordList.length || 0,
          monthLabExpense: monthLabExpense.toFixed(2),
          pendingLabBills,
          pendingLabRegistrations: Number(pendingLabData.pendingTotal || pendingLabData.total || pendingLabList.length || 0),
          monthMaterialExpense: monthMaterialExpense.toFixed(2),
          materialAlerts
        }
      }
    },
    async loadManagementStats() {
      const today = this.formatDate(new Date())
      const month = this.formatMonth(new Date())
      const [year, monthValue] = month.split('-')
      try {
        const result = await fetchCachedResource({
          cacheKey: 'page:home:management-dashboard',
          scope: 'homeDashboardManagement',
          params: {
            pageType: 'management',
            date: today,
            month,
            role: this.normalizedRole || ''
          },
          loader: async () => buildSuccessResponse(await this.buildManagementStatsPayload({ today, year, monthValue })),
          notifier: message => this.$message.warning(message),
          offlineMessage: '当前离线，已显示最近首页缓存'
        })
        const data = result && result.data ? result.data : {}
        this.stats = Object.assign(buildEmptyStats(), data.stats || {})
        return {
          success: true,
          fromCache: !!(result && result.fromCache),
          cachedAt: result && result.cachedAt ? result.cachedAt : ''
        }
      } catch (error) {
        console.error('Error loading dashboard stats:', error)
        this.stats = buildEmptyStats()
        return { success: false }
      }
    },
    async buildDoctorDashboardPayload({ today, tomorrow }) {
      const [appointmentsRes, recordsRes, followupsRes, treatmentsRes, dismissedReminderRes] = await Promise.all([
        axios.get('/appointments/selectAll', { params: { page: 1, size: 1000 } }),
        axios.get('/medical-records/selectAll', { params: { page: 1, size: 1000 } }),
        axios.get('/followup/selectAll', { params: { page: 1, size: 1000 } }).catch(() => ({ data: { data: { list: [] } } })),
        axios.get('/treatments/selectAll', { params: { page: 1, size: 1000 } }).catch(() => ({ data: { data: { list: [] } } })),
        this.loadDismissedReminderKeys().then(data => ({ data })).catch(() => ({ data: new Set() }))
      ])

      const appointmentList = this.extractList(appointmentsRes).filter(item => this.matchesCurrentDoctor(item))
      const recordList = this.extractList(recordsRes).filter(item => this.matchesCurrentDoctor(item))
      const followupList = this.extractList(followupsRes)
      const treatmentList = this.extractList(treatmentsRes)
        .filter(item => this.matchesCurrentDoctor(item))
        .filter(item => this.statusText(item && item.status) === '完成')
      const dismissedReminderKeys = dismissedReminderRes && dismissedReminderRes.data
        ? dismissedReminderRes.data
        : new Set()

      const todayAppointments = appointmentList.filter(item => this.dateKey(item && item.appointment_date) === today)
      const tomorrowAppointments = appointmentList.filter(item => this.dateKey(item && item.appointment_date) === tomorrow)
      const pendingConsultations = todayAppointments.filter(item => WAITING_APPOINTMENT_STATUSES.includes(this.statusText(item && item.status))).length

      const completedVisitKeys = new Set(
        todayAppointments
          .filter(item => COMPLETED_APPOINTMENT_STATUSES.includes(this.statusText(item && item.status)))
          .map(item => this.visitRefKey(item, today))
          .filter(Boolean)
      )

      const recordKeys = new Set(
        recordList
          .filter(item => this.dateKey(item && item.visit_date) === today)
          .map(item => this.visitRefKey(item, today))
          .filter(Boolean)
      )

      let pendingRecords = 0
      completedVisitKeys.forEach(key => {
        if (!recordKeys.has(key)) {
          pendingRecords += 1
        }
      })

      const doctorPatientRefs = new Set(
        appointmentList.concat(recordList).map(item => this.patientRefKey(item)).filter(Boolean)
      )

      const pendingFollowupRefs = new Set(
        followupList
          .filter(item => {
            const nextFollowupDate = this.followupDueDateKey(item)
            const patientRef = this.patientRefKey(item)
            return !!nextFollowupDate
              && nextFollowupDate <= today
              && !!patientRef
              && this.isPendingFollowupItem(item)
              && this.matchesCurrentDoctorFollowup(item, doctorPatientRefs)
          })
          .map(item => this.patientRefKey(item))
          .filter(Boolean)
      )

      const activeTomorrowAppointments = tomorrowAppointments.filter(item => !INACTIVE_APPOINTMENT_STATUSES.includes(this.statusText(item && item.status)))

      const remindersByPatient = new Map()
      treatmentList.forEach(item => {
        const key = this.patientRefKey(item)
        const reminderKey = this.latestCompletedTreatmentRef(item) || key
        if (!key) return
        const currentDate = this.dateKey(item && item.treatment_date)
        const existed = remindersByPatient.get(key)
        if (!existed || String(currentDate).localeCompare(String(existed.latest_treatment_date || '')) > 0) {
          remindersByPatient.set(key, {
            key: reminderKey,
            patient_ref: key,
            patient_id: item.patient_id || null,
            patient_name: item.patient_name || '未命名患者',
            latest_treatment_date: currentDate,
            latest_treatment: item.appointment_purpose || '',
            doctor_name: item.doctor_name || this.currentDoctorName
          })
        }
      })

      const appointmentNextMap = new Map()
      appointmentList.forEach(item => {
        const key = this.patientRefKey(item)
        const appointmentDate = this.dateKey(item && item.appointment_date)
        if (!key || !appointmentDate || INACTIVE_APPOINTMENT_STATUSES.includes(this.statusText(item && item.status))) {
          return
        }
        const existed = appointmentNextMap.get(key)
        if (!existed || String(appointmentDate).localeCompare(String(existed)) > 0) {
          appointmentNextMap.set(key, appointmentDate)
        }
      })

      const missingNextAppointmentPatients = Array.from(remindersByPatient.values())
        .filter(item => {
          const nextAppointmentDate = appointmentNextMap.get(item.patient_ref) || ''
          if (nextAppointmentDate && String(nextAppointmentDate).localeCompare(String(item.latest_treatment_date || '')) >= 0) {
            return false
          }
          return !dismissedReminderKeys.has(item.key)
        })
        .sort((left, right) => String(right.latest_treatment_date || '').localeCompare(String(left.latest_treatment_date || '')))

      return {
        doctorTodos: {
          pendingConsultations,
          pendingRecords,
          pendingFollowups: pendingFollowupRefs.size,
          tomorrowAppointments: activeTomorrowAppointments.length
        },
        missingNextAppointmentPatients
      }
    },
    async loadDoctorTodos() {
      const today = this.formatDate(new Date())
      const tomorrow = this.formatDate(Date.now() + (24 * 60 * 60 * 1000))
      try {
        const result = await fetchCachedResource({
          cacheKey: 'page:home:doctor-dashboard',
          scope: 'homeDashboardDoctor',
          params: {
            pageType: 'doctor',
            date: today,
            tomorrow,
            doctorAccountId: this.currentDoctorId || 0,
            doctorName: this.currentDoctorName || ''
          },
          loader: async () => buildSuccessResponse(await this.buildDoctorDashboardPayload({ today, tomorrow })),
          notifier: message => this.$message.warning(message),
          offlineMessage: '当前离线，已显示最近医生首页缓存'
        })
        const data = result && result.data ? result.data : {}
        this.doctorTodos = Object.assign(buildEmptyDoctorTodos(), data.doctorTodos || {})
        this.missingNextAppointmentPatients = Array.isArray(data.missingNextAppointmentPatients) ? data.missingNextAppointmentPatients : []
        return {
          success: true,
          fromCache: !!(result && result.fromCache),
          cachedAt: result && result.cachedAt ? result.cachedAt : ''
        }
      } catch (error) {
        console.error('Error loading doctor todo stats:', error)
        this.doctorTodos = buildEmptyDoctorTodos()
        this.missingNextAppointmentPatients = []
        return { success: false }
      }
    }
  }
}
</script>

<style scoped>
.home-wrap {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero-card,
.doctor-hero-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  color: #fff;
  padding: 28px;
  border-radius: 22px;
}

.hero-card {
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 45%, #60a5fa 100%);
  box-shadow: 0 18px 40px rgba(37, 99, 235, 0.22);
}

.doctor-hero-card {
  background: linear-gradient(135deg, #0f766e 0%, #0f9f8f 45%, #34d399 100%);
  box-shadow: 0 18px 40px rgba(15, 118, 110, 0.22);
}

.hero-kicker {
  font-size: 13px;
  opacity: 0.85;
  margin-bottom: 8px;
}

.hero-card h1,
.doctor-hero-card h1 {
  margin: 0;
  font-size: 28px;
}

.hero-card p,
.doctor-hero-card p {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.9);
}

.hero-clock {
  min-width: 180px;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
}

.clock-label {
  font-size: 12px;
  opacity: 0.8;
}

.clock-value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 700;
}

.clock-date {
  margin-top: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.88);
}

.doctor-todo-grid,
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card,
.todo-stat-card {
  background: #fff;
  border-radius: 18px;
  padding: 18px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.05);
}

.clickable {
  cursor: pointer;
  transition: 0.2s ease;
}

.clickable:hover {
  transform: translateY(-2px);
}

.stat-card.clickable:hover {
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.1);
}

.todo-stat-card.clickable:hover {
  box-shadow: 0 14px 30px rgba(15, 118, 110, 0.14);
}

.stat-card.primary,
.todo-stat-card.primary {
  border-top: 4px solid #2563eb;
}

.stat-card.success,
.todo-stat-card.success {
  border-top: 4px solid #22c55e;
}

.stat-card.warn,
.todo-stat-card.warn {
  border-top: 4px solid #f59e0b;
}

.stat-card.danger {
  border-top: 4px solid #ef4444;
}

.stat-card.teal {
  border-top: 4px solid #0f766e;
}

.stat-card.violet {
  border-top: 4px solid #7c3aed;
}

.todo-stat-card.info {
  border-top: 4px solid #0f766e;
}

.stat-label,
.todo-stat-card__label {
  color: #64748b;
  font-size: 13px;
}

.stat-value,
.todo-stat-card__value {
  margin-top: 8px;
  font-size: 30px;
  font-weight: 800;
  color: #0f172a;
}

.stat-desc,
.todo-stat-card__desc {
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.stat-note {
  margin-top: 8px;
  font-size: 11px;
  color: #64748b;
}

.doctor-panel-grid,
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.panel-card,
.calendar-card {
  border-radius: 18px;
}

.panel-card {
  border: 1px solid #e5e7eb;
}

.panel-title {
  font-weight: 700;
  color: #0f172a;
}

.panel-list,
.doctor-note-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-item,
.doctor-note-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #334155;
  font-size: 14px;
  line-height: 1.7;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  display: inline-block;
  flex-shrink: 0;
}

.dot.blue {
  background: #3b82f6;
}

.dot.green {
  background: #22c55e;
}

.dot.orange {
  background: #f59e0b;
}

.dot.red {
  background: #ef4444;
}

.dot.slate {
  background: #0f766e;
}

.dot.teal {
  background: #0f766e;
}

.dot.violet {
  background: #7c3aed;
}

.doctor-action-grid,
.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.quick-item,
.doctor-action-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
  cursor: pointer;
  transition: 0.2s ease;
}

.quick-item:hover,
.doctor-action-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.quick-item i,
.doctor-action-item i {
  font-size: 22px;
  color: #2563eb;
}

.doctor-action-item i {
  color: #0f766e;
}

.quick-item:hover span {
  color: #2563eb;
}

.doctor-action-item:hover span {
  color: #0f766e;
}

.quick-item span,
.doctor-action-item span {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.last-updated {
  margin-top: 14px;
  font-size: 12px;
  color: #94a3b8;
}

.last-updated-cache {
  margin-top: 4px;
  font-size: 12px;
  color: #fbbf24;
}

.auto-refresh-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #cbd5e1;
}

.doctor-reminder-card {
  border-color: #fde68a;
  background: #fffdf3;
}

.doctor-reminder-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.doctor-reminder-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #f3e8a5;
}

.doctor-reminder-title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.doctor-reminder-desc {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.doctor-reminder-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #f8fafc;
}

.todo-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.todo-desc {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.trend-placeholder {
  height: 220px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 18px 10px 8px;
  background: linear-gradient(180deg, #f8fbff 0%, #eef4ff 100%);
  border-radius: 18px;
}

.trend-bars {
  height: 180px;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  align-items: end;
  gap: 12px;
}

.trend-bars span {
  display: block;
  width: 100%;
  border-radius: 12px 12px 6px 6px;
  background: linear-gradient(180deg, #60a5fa 0%, #2563eb 100%);
}

.trend-labels {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-top: 10px;
  color: #64748b;
  font-size: 12px;
  text-align: center;
}

.calendar-card {
  border-radius: 18px;
}

.is-selected {
  color: #2563eb;
  font-weight: 700;
}

@media (max-width: 1200px) {
  .doctor-todo-grid,
  .stats-grid,
  .doctor-panel-grid,
  .content-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .hero-card,
  .doctor-hero-card {
    flex-direction: column;
  }

  .doctor-todo-grid,
  .stats-grid,
  .doctor-panel-grid,
  .content-grid,
  .doctor-action-grid,
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
