<template>
  <div class="home-wrap">
    <!-- 顶部标题区 -->
    <div class="home-header">
      <div class="home-header-left">
        <h1 class="home-greeting">
          <span class="greeting-text">{{ greeting }}</span>
          <span class="greeting-name">，{{ username || (isDoctor ? '医生' : '医生/管理员') }}</span>
        </h1>
        <div class="home-meta">
          <span class="meta-weekday">{{ weekdayLabel }}</span>
          <span class="meta-divider">·</span>
          <span class="meta-date">{{ todayLabel }}</span>
          <span class="meta-divider">·</span>
          <span class="meta-time" :class="{ 'time-tick': timeTick }">{{ currentTime }}</span>
        </div>
      </div>
      <div class="home-header-right">
        <div class="status-pill" :class="isDoctor ? 'pill-doctor' : 'pill-admin'">
          <span class="status-dot"></span>
          <span class="status-label">{{ isDoctor ? '医生工作台' : '管理后台' }}</span>
        </div>
      </div>
    </div>

    <!-- KPI 栏 -->
    <div class="kpi-bar">
      <template v-if="isDoctor">
        <div class="kpi-card kpi-blue" @click="goPage('/Appointment')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-blue">
              <i class="el-icon-first-aid-kit kpi-card-icon"></i>
            </div>
            <span class="kpi-card-label">待接诊</span>
          </div>
          <div class="kpi-card-value">{{ doctorTodos.pendingConsultations }}</div>
        </div>
        <div class="kpi-card kpi-green" @click="goPage('/MedicalRecord')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-green">
              <i class="el-icon-document kpi-card-icon"></i>
            </div>
            <span class="kpi-card-label">待写病历</span>
          </div>
          <div class="kpi-card-value">{{ doctorTodos.pendingRecords }}</div>
        </div>
        <div class="kpi-card kpi-purple" @click="goPage('/Followup')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-purple">
              <i class="el-icon-phone-outline kpi-card-icon"></i>
            </div>
            <span class="kpi-card-label">待回访</span>
          </div>
          <div class="kpi-card-value">{{ doctorTodos.pendingFollowups }}</div>
        </div>
        <div class="kpi-card kpi-amber" @click="goPage('/Appointment')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-amber">
              <i class="el-icon-date kpi-card-icon"></i>
            </div>
            <span class="kpi-card-label">明日预约</span>
          </div>
          <div class="kpi-card-value">{{ doctorTodos.tomorrowAppointments }}</div>
        </div>
      </template>
      <template v-else>
        <div class="kpi-card kpi-blue" @click="goPage('/Appointment')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-blue">
              <i class="el-icon-date kpi-card-icon"></i>
            </div>
            <span class="kpi-card-label">今日预约</span>
          </div>
          <div class="kpi-card-value">{{ stats.todayAppointments }}</div>
        </div>
        <div class="kpi-card kpi-green" @click="goPage('/Patient')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-green">
              <i class="el-icon-user kpi-card-icon"></i>
            </div>
            <span class="kpi-card-label">患者总数</span>
          </div>
          <div class="kpi-card-value">{{ stats.totalPatients }}</div>
        </div>
        <div class="kpi-card kpi-amber" @click="goPage('/Financial2')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-amber">
              <i class="el-icon-money kpi-card-icon"></i>
            </div>
            <span class="kpi-card-label">本月已收费</span>
          </div>
          <div class="kpi-card-value">¥{{ stats.monthIncome }}</div>
        </div>
        <div class="kpi-card kpi-purple" @click="goPage('/MedicalRecord')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-purple">
              <i class="el-icon-tickets kpi-card-icon"></i>
            </div>
            <span class="kpi-card-label">病历总数</span>
          </div>
          <div class="kpi-card-value">{{ stats.totalRecords }}</div>
        </div>
      </template>
    </div>

    <!-- AI 中心对话区 -->
    <AiCenter :user="user" />
  </div>
</template>

<script>
import axios from 'axios'
import { ADMIN_SESSION_EVENT, getAdminSession } from '@/utils/adminSession'
import AiCenter from '@/components/ai/AiCenter.vue'
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
  components: {
    AiCenter
  },
  data() {
    return {
      currentTime: '',
      timer: null,
      timeTick: false,
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
    },
    weekdayLabel() {
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return weekdays[new Date().getDay()]
    },
    greeting() {
      const hour = new Date().getHours()
      if (hour < 6) return '夜深了'
      if (hour < 9) return '早上好'
      if (hour < 12) return '上午好'
      if (hour < 14) return '中午好'
      if (hour < 18) return '下午好'
      return '晚上好'
    },
    enabledAgents() {
      return []
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
    /**
     * 简单 Markdown 渲染（仅支持加粗和换行）
     * @param {string} text - 原始文本
     * @returns {string} HTML 字符串
     */
    simpleMarkdown(text) {
      if (!text) return ''
      // 将 **文本** 替换为 <strong>文本</strong>
      let html = text.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      // 将换行符替换为 <br>
      html = html.replace(/\n/g, '<br>')
      return html
    },
    /**
     * 将 JSON 对象渲染为卡片式 HTML
     * 优先按病历字段顺序展示，其他字段追加到末尾
     * @param {Object} data - 后端返回的原始数据对象
     * @returns {string} HTML 字符串
     */
    /**
     * 渲染 JSON 卡片的值，支持字符串、数字、数组和嵌套对象
     */
    renderJsonCardValue(value) {
      if (value == null) return ''
      if (typeof value === 'string') return value
      if (typeof value === 'number' || typeof value === 'boolean') return String(value)
      if (Array.isArray(value)) {
        return value.map(v => this.renderJsonCardValue(v)).filter(Boolean).join('、')
      }
      if (typeof value === 'object') {
        return Object.entries(value)
          .map(([k, v]) => `${k}：${this.renderJsonCardValue(v)}`)
          .filter(Boolean)
          .join('；')
      }
      return String(value)
    },
    renderJsonCard(data) {
      if (!data || typeof data !== 'object') return ''
      // 病历字段中文映射（按临床习惯排序）
      const fieldOrder = [
        { key: 'chief_complaint', label: '主诉' },
        { key: 'present_illness_history', label: '现病史' },
        { key: 'past_medical_history', label: '既往史' },
        { key: 'infectious_history', label: '传染病史' },
        { key: 'allergy_history', label: '过敏史' },
        { key: 'general_condition', label: '一般情况' },
        { key: 'examination_findings', label: '检查所见' },
        { key: 'auxiliary_examination', label: '辅助检查' },
        { key: 'diagnosis', label: '诊断' },
        { key: 'treatment_plan', label: '治疗方案' },
        { key: 'treatment', label: '治疗记录' },
        { key: 'medical_advice', label: '医嘱' },
        { key: 'prescription', label: '处方' },
        { key: 'record_tags', label: '标签' },
        { key: 'image_summary', label: '影像摘要' },
        { key: 'notes', label: '备注' }
      ]
      let html = '<div class="json-card-inner">'
      const renderedKeys = new Set()
      // 先渲染已知字段（按固定顺序）
      fieldOrder.forEach(item => {
        if (data[item.key] != null) {
          html += `<div class="json-card-row"><div class="json-card-label">${this.escapeHtml(item.label)}</div><div class="json-card-value">${this.escapeHtml(this.renderJsonCardValue(data[item.key]))}</div></div>`
          renderedKeys.add(item.key)
        }
      })
      // 再渲染其他未匹配的字段
      Object.keys(data).forEach(key => {
        if (!renderedKeys.has(key) && data[key] != null) {
          html += `<div class="json-card-row"><div class="json-card-label">${this.escapeHtml(key)}</div><div class="json-card-value">${this.escapeHtml(this.renderJsonCardValue(data[key]))}</div></div>`
        }
      })
      html += '</div>'
      return html
    },
    /**
     * HTML 转义，防止 XSS
     * @param {string} text - 原始文本
     * @returns {string} 转义后的文本
     */
    escapeHtml(text) {
      if (text == null) return ''
      const div = document.createElement('div')
      div.textContent = text
      return div.innerHTML
    },
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
      const prevSecond = this.currentTime ? this.currentTime.split(':')[2] : null
      this.currentTime = now.toLocaleTimeString('zh-CN', { hour12: false })
      const newSecond = this.currentTime.split(':')[2]
      if (prevSecond !== null && prevSecond !== newSecond) {
        this.timeTick = true
        setTimeout(() => { this.timeTick = false }, 300)
      }
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
    goPatientGroup(groupKey, sortMode, extraQuery) {
      extraQuery = extraQuery || {}
      this.$router.push({ path: '/Patient', query: Object.assign({ groupKey, sortMode }, extraQuery) })
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
    visitRefKey(item, fallbackDate) {
      fallbackDate = fallbackDate || ''
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
      this.$router.push('/Appointment')
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
    async buildManagementStatsPayload(params) {
      const today = params.today
      const year = params.year
      const monthValue = params.monthValue
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
        axios.get('/materials/selectAll', {
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
      const parts = month.split('-')
      const year = parts[0]
      const monthValue = parts[1]
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
    async buildDoctorDashboardPayload(params) {
      const today = params.today
      const tomorrow = params.tomorrow
      const [appointmentsRes, recordsRes, followupsRes, treatmentsRes, dismissedReminderRes] = await Promise.all([
        axios.get('/appointments/selectAll', { params: { page: 1, size: 1000 } }),
        axios.get('/medical-records/selectAll', { params: { page: 1, size: 1000 } }),
        axios.get('/followups/selectAll', { params: { page: 1, size: 1000 } }).catch(() => ({ data: { data: { list: [] } } })),
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
/* === 青瓷主题首页 === */
.home-wrap {
  --primary: #5A8F7B;
  --primary-hover: #4A7F6B;
  --primary-light: rgba(90, 143, 123, 0.08);
  --text-primary: #2C3E35;
  --text-regular: #3E3E3C;
  --text-secondary: #6B6B6B;
  --text-muted: #A0A0A0;
  --bg-page: #F5F0E8;
  --bg-card: #ffffff;
  --bg-hover: #FAF9F7;
  --border-color: rgba(90, 143, 123, 0.15);
  --border-light: rgba(90, 143, 123, 0.08);
  --success: #5A8F7B;
  --warning: #C9A227;
  --danger: #C75B5B;
  --shadow-card: 0 2px 8px rgba(0, 0, 0, 0.05);
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 14px;
}

/* ========== 首页整体布局 ========== */
.home-wrap {
  padding: 20px;
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
  background: var(--bg-page);
}

/* ========== 顶部标题栏 ========== */
.home-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.home-header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.home-greeting {
  margin: 0;
  font-family: var(--apple-font-serif);
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  letter-spacing: 0.04em;
}

.greeting-name {
  color: var(--primary);
}

.home-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-weekday {
  font-size: 13px;
  font-weight: 600;
  color: var(--primary);
}

.meta-date {
  font-size: 13px;
  color: var(--text-secondary);
}

.meta-divider {
  font-size: 13px;
  color: var(--border-color);
}

.meta-time {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

.meta-time.time-tick {
  color: var(--primary);
}

.home-header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
}

.status-pill.pill-doctor {
  background: var(--primary-light);
  color: var(--primary);
  border: 1px solid rgba(90, 143, 123, 0.2);
}

.status-pill.pill-admin {
  background: rgba(90, 143, 123, 0.06);
  color: var(--success);
  border: 1px solid rgba(90, 143, 123, 0.15);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

/* ========== KPI 栏 ========== */
.kpi-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.kpi-card {
  position: relative;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);
  border-radius: var(--radius-md);
  padding: 16px;
  cursor: pointer;
  overflow: hidden;
  transition: box-shadow 0.25s cubic-bezier(0.22, 1, 0.36, 1), transform 0.25s cubic-bezier(0.22, 1, 0.36, 1);
}

.kpi-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
}

.kpi-card.kpi-blue::before { background: var(--primary); }
.kpi-card.kpi-green::before { background: var(--success); }
.kpi-card.kpi-amber::before { background: var(--warning); }
.kpi-card.kpi-purple::before { background: #7c3aed; }

.kpi-card:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.kpi-card-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.kpi-icon-wrap.kpi-icon-blue { background: var(--primary-light); }
.kpi-icon-wrap.kpi-icon-green { background: rgba(90, 143, 123, 0.08); }
.kpi-icon-wrap.kpi-icon-amber { background: rgba(201, 162, 39, 0.08); }
.kpi-icon-wrap.kpi-icon-purple { background: rgba(124, 58, 237, 0.06); }

.kpi-icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.kpi-card-icon {
  font-size: 16px;
  line-height: 1;
  color: inherit;
}

.kpi-icon-blue .kpi-card-icon { color: var(--primary); }
.kpi-icon-green .kpi-card-icon { color: var(--success); }
.kpi-icon-amber .kpi-card-icon { color: var(--warning); }
.kpi-icon-purple .kpi-card-icon { color: #7c3aed; }

.kpi-card-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.kpi-card-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

/* ========== 动画 ========== */
@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .home-wrap {
    padding: 12px;
  }

  .home-greeting {
    font-size: 16px;
  }

  .kpi-bar {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .kpi-card {
    padding: 12px;
  }

  .kpi-card-value {
    font-size: 22px;
  }

}
</style>
