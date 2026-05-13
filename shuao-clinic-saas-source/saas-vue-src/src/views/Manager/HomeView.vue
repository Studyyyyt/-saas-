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
              <span class="kpi-card-icon">🩺</span>
            </div>
            <span class="kpi-card-label">待接诊</span>
          </div>
          <div class="kpi-card-value">{{ doctorTodos.pendingConsultations }}</div>
        </div>
        <div class="kpi-card kpi-green" @click="goPage('/MedicalRecord')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-green">
              <span class="kpi-card-icon">📝</span>
            </div>
            <span class="kpi-card-label">待写病历</span>
          </div>
          <div class="kpi-card-value">{{ doctorTodos.pendingRecords }}</div>
        </div>
        <div class="kpi-card kpi-purple" @click="goPage('/Followup')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-purple">
              <span class="kpi-card-icon">📞</span>
            </div>
            <span class="kpi-card-label">待回访</span>
          </div>
          <div class="kpi-card-value">{{ doctorTodos.pendingFollowups }}</div>
        </div>
        <div class="kpi-card kpi-amber" @click="goPage('/Appointment')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-amber">
              <span class="kpi-card-icon">📅</span>
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
              <span class="kpi-card-icon">📅</span>
            </div>
            <span class="kpi-card-label">今日预约</span>
          </div>
          <div class="kpi-card-value">{{ stats.todayAppointments }}</div>
        </div>
        <div class="kpi-card kpi-green" @click="goPage('/Patient')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-green">
              <span class="kpi-card-icon">👥</span>
            </div>
            <span class="kpi-card-label">患者总数</span>
          </div>
          <div class="kpi-card-value">{{ stats.totalPatients }}</div>
        </div>
        <div class="kpi-card kpi-amber" @click="goPage('/Financial2')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-amber">
              <span class="kpi-card-icon">💰</span>
            </div>
            <span class="kpi-card-label">本月已收费</span>
          </div>
          <div class="kpi-card-value">¥{{ stats.monthIncome }}</div>
        </div>
        <div class="kpi-card kpi-purple" @click="goPage('/MedicalRecord')">
          <div class="kpi-card-top">
            <div class="kpi-icon-wrap kpi-icon-purple">
              <span class="kpi-card-icon">📋</span>
            </div>
            <span class="kpi-card-label">病历总数</span>
          </div>
          <div class="kpi-card-value">{{ stats.totalRecords }}</div>
        </div>
      </template>
    </div>

    <!-- AI 对话区 -->
    <div v-if="isAiEnabled('home-assistant')" class="ai-section">
      <div class="ai-panel">
        <!-- 头部 Tabs -->
        <div class="ai-panel-header">
          <div class="ai-tabs">
            <div
              v-for="agent in agents"
              :key="agent.id"
              class="ai-tab"
              :class="{ active: currentAgent.id === agent.id }"
              @click="switchAgent(agent)"
            >
              <span class="ai-tab-dot" :style="{ background: agent.gradient }"></span>
              <span class="ai-tab-name">{{ agent.name }}</span>
            </div>
            <div class="ai-tab ai-tab-add" title="管理 AI 助手" @click="goPage('/AIAgentConfig')">
              <span class="ai-tab-dot" style="background: #e5e7eb;"></span>
              <span class="ai-tab-name"><i class="el-icon-setting"></i> 管理</span>
            </div>
          </div>
          <div class="ai-header-actions">
            <div class="ai-header-action" :class="{ active: showDebugPanel }" title="调试日志" @click="toggleDebugPanel">
              <i class="el-icon-warning-outline"></i>
            </div>
            <div class="ai-header-action" title="清空对话" @click="clearChat">
              <i class="el-icon-delete"></i>
            </div>
          </div>
        </div>

        <!-- 消息区 -->
        <div class="ai-messages" ref="chatBody">
          <div
            v-for="(msg, index) in chatMessages"
            :key="index"
            class="msg-item"
            :class="[msg.role, { 'rich-media': msg.type === 'chart' || msg.type === 'appointments' }]"
          >
            <!-- 富媒体 -->
            <template v-if="msg.type === 'chart' || msg.type === 'appointments'">
              <div class="msg-card">
                <div v-if="msg.content" class="msg-card-text">{{ msg.content }}</div>

                <div v-if="msg.type === 'appointments'" class="msg-appointments">
                  <div class="doctor-filter">
                    <span
                      v-for="doc in doctorOptions"
                      :key="doc.value"
                      class="filter-tag"
                      :class="{ active: selectedDoctor === doc.value }"
                      @click="selectedDoctor = doc.value"
                    >
                      {{ doc.label }}
                    </span>
                  </div>
                  <div class="appointment-grid">
                    <div
                      v-for="appt in filteredAppointments"
                      :key="appt.id"
                      class="appointment-card"
                      :class="appt.status"
                    >
                      <div class="apptime">{{ appt.time }}</div>
                      <div class="appname">{{ appt.patient_name }}</div>
                      <div class="appproject">{{ appt.project || '未填写项目' }}</div>
                      <div class="appstatus">{{ appt.status_label }}</div>
                    </div>
                  </div>
                </div>

                <div v-if="msg.type === 'chart'" class="msg-chart">
                  <div :id="'ai-chart-' + index" class="ai-chart-container" />
                </div>

                <div v-if="msg.time" class="msg-time">{{ msg.time }}</div>
              </div>
            </template>

            <!-- 普通消息 -->
            <template v-else>
              <div class="msg-bubble">
                <div v-if="msg.type === 'text'" class="msg-text" :class="{ 'markdown-body': msg.role === 'assistant' && msg.content }">
                  <template v-if="msg.content">
                    <div v-if="msg.role === 'assistant'" v-html="renderMarkdown(msg.content)"></div>
                    <template v-else>{{ msg.content }}</template>
                  </template>
                  <template v-else-if="msg.isStreaming">
                    <span class="typing-dots"><span></span><span></span><span></span></span>
                  </template>
                  <span v-if="msg.isStreaming && msg.content" class="stream-cursor" />
                </div>
                <div v-if="msg.type === 'typing'" class="msg-text typing">
                  <span class="typing-dots"><span></span><span></span><span></span></span>
                </div>
                <div v-if="msg.time" class="msg-time">{{ msg.time }}</div>
              </div>
            </template>
          </div>
        </div>

        <!-- 调试日志面板 -->
        <div v-if="showDebugPanel" class="ai-debug-panel">
          <div class="debug-panel-header">
            <span class="debug-title"><i class="el-icon-warning-outline"></i> 调试日志</span>
            <div class="debug-actions">
              <span class="debug-action" @click="clearDebugPanelLogs"><i class="el-icon-delete"></i> 清空</span>
              <span class="debug-action" @click="toggleDebugPanel"><i class="el-icon-close"></i> 关闭</span>
            </div>
          </div>
          <div class="debug-log-list">
            <div v-if="debugLogs.length === 0" class="debug-empty">暂无日志，发送一条 AI 消息后将在此显示请求与响应详情。</div>
            <div
              v-for="(log, idx) in debugLogs"
              :key="idx"
              class="debug-log-item"
              :class="log.type"
            >
              <span class="debug-time">{{ log.time }}</span>
              <span class="debug-type">{{ log.type }}</span>
              <span class="debug-data">{{ JSON.stringify(log.data) }}</span>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="ai-input-area">
          <div class="input-shell">
            <input
              v-model="chatInput"
              class="input-field"
              placeholder="有什么可以帮您的？"
              @keyup.enter="sendChat"
            />
            <button class="input-send" :disabled="!chatInput.trim()" @click="sendChat">
              <i class="el-icon-s-promotion"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'
import { ADMIN_SESSION_EVENT, getAdminSession } from '@/utils/adminSession'
import { loadAgentsFromStorage } from '@/views/Manager/AIAgentConfigView'
import { streamChat, fetchAgentConfigs, getDebugLogs, clearDebugLogs, subscribeToDebugLogs, unsubscribeFromDebugLogs } from '@/utils/aiStreamClient'
import { isAiEnabled as checkAiEnabled } from '@/utils/aiConfig'
import { fetchCachedResource } from '@/utils/offline/apiClient'
import { savePendingAppointmentPatient } from '@/utils/appointmentPrefill'
import { marked } from 'marked'

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
      missingNextAppointmentPatients: [],
      // AI 相关
      chatInput: '',
      showAgentMenu: false,
      currentAgent: { id: 'default', name: '智能助手', gradient: 'linear-gradient(135deg, #2563eb 0%, #3b82f6 100%)' },
      agents: [],
      chatMessages: [
        {
          role: 'assistant',
          type: 'text',
          content: '早上好！我是您的门诊智能助手。您可以问我今日预约、待办事项、收入情况，或者让我帮您查找患者信息。',
          time: (() => {
            const now = new Date()
            const pad = n => String(n).padStart(2, '0')
            return `${pad(now.getHours())}:${pad(now.getMinutes())}`
          })()
        }
      ],
      chatStreaming: false,
      chatAbortController: null,
      showDebugPanel: false,
      aiDebugLogs: [],
      selectedDoctor: 'all',
      demoAppointments: [
        { id: 1, time: '08:00', patient_name: '张三', project: '种植牙复诊', status: 'waiting', status_label: '待接诊', doctor: '李医生' },
        { id: 2, time: '08:30', patient_name: '李四', project: '拔牙', status: 'completed', status_label: '已完成', doctor: '李医生' },
        { id: 3, time: '09:00', patient_name: '王五', project: '初诊检查', status: 'waiting', status_label: '待接诊', doctor: '李医生' },
        { id: 4, time: '09:30', patient_name: '赵六', project: '根管治疗', status: 'waiting', status_label: '待接诊', doctor: '李医生' },
        { id: 5, time: '10:00', patient_name: '钱七', project: '洗牙', status: 'cancelled', status_label: '已取消', doctor: '李医生' },
        { id: 6, time: '10:30', patient_name: '孙八', project: '正畸复诊', status: 'waiting', status_label: '待接诊', doctor: '张医生' },
        { id: 7, time: '11:00', patient_name: '周九', project: '补牙', status: 'completed', status_label: '已完成', doctor: '张医生' },
        { id: 8, time: '14:00', patient_name: '吴十', project: '种植牙手术', status: 'waiting', status_label: '待接诊', doctor: '王医生' },
        { id: 9, time: '14:30', patient_name: '郑一', project: '牙周治疗', status: 'waiting', status_label: '待接诊', doctor: '王医生' },
        { id: 10, time: '15:00', patient_name: '陈二', project: '取模', status: 'waiting', status_label: '待接诊', doctor: '李医生' }
      ]
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
    doctorOptions() {
      const doctors = new Set(this.demoAppointments.map(a => a.doctor))
      const list = Array.from(doctors).map(doc => ({ label: doc, value: doc }))
      return [{ label: '全部医生', value: 'all' }, ...list]
    },
    filteredAppointments() {
      if (this.selectedDoctor === 'all') return this.demoAppointments
      return this.demoAppointments.filter(a => a.doctor === this.selectedDoctor)
    },
    todayAppointmentsPreview() {
      return this.demoAppointments.slice(0, 5)
    },
    showPresetQuestions() {
      return this.chatMessages.length === 1 && this.chatMessages[0].role === 'assistant'
    },
    debugLogs() {
      return this.aiDebugLogs
    }
  },
  mounted() {
    this.syncUserFromStorage()
    this.updateTime()
    this.loadDashboard()
    this.loadAgents()
    this.loadChatMessages()
    this.configureDashboardRefresh()
    this.timer = setInterval(this.updateTime, 1000)
    window.addEventListener(ADMIN_SESSION_EVENT, this.handleIdentityRefresh)
    window.addEventListener('focus', this.handleIdentityRefresh)
    window.addEventListener('online', this.handleVisibilityRefresh)
    document.addEventListener('visibilitychange', this.handleVisibilityRefresh)
    this._debugLogHandler = (log) => {
      if (log === null) {
        this.aiDebugLogs = []
      } else {
        this.aiDebugLogs.unshift(log)
        if (this.aiDebugLogs.length > 100) this.aiDebugLogs.pop()
      }
    }
    subscribeToDebugLogs(this._debugLogHandler)
    this.aiDebugLogs = getDebugLogs()
  },
  beforeDestroy() {
    clearInterval(this.timer)
    clearInterval(this.dashboardTimer)
    window.removeEventListener(ADMIN_SESSION_EVENT, this.handleIdentityRefresh)
    window.removeEventListener('focus', this.handleIdentityRefresh)
    window.removeEventListener('online', this.handleVisibilityRefresh)
    document.removeEventListener('visibilitychange', this.handleVisibilityRefresh)
    if (this._debugLogHandler) {
      unsubscribeFromDebugLogs(this._debugLogHandler)
    }
  },
  methods: {
    isAiEnabled(key) {
      return checkAiEnabled(key)
    },
    renderMarkdown(text) {
      if (!text) return ''
      const renderer = new marked.Renderer()
      // 禁用多行代码块，不渲染为 pre/code
      renderer.code = () => ''
      // 内联代码渲染为普通文本，不加背景
      renderer.codespan = (code) => code
      return marked.parse(text, { breaks: true, gfm: true, renderer })
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
    },
    // AI 对话方法
    sendChat() {
      const text = this.chatInput.trim()
      if (!text || this.chatStreaming) return
      this.chatMessages.push({ role: 'user', type: 'text', content: text, time: this.formatChatTime() })
      this.chatInput = ''
      this.saveChatMessages()
      this.$nextTick(() => {
        this.scrollChatToBottom()
        this.callAIStream(text)
      })
    },
    callAIStream(text) {
      this.chatStreaming = true
      const typingIndex = this.chatMessages.length
      // 先显示 typing 等待动画，收到首个 token 后再替换为正式消息
      this.chatMessages.push({ role: 'assistant', type: 'typing', content: '', time: this.formatChatTime() })
      this.$nextTick(() => this.scrollChatToBottom())

      let hasReceivedToken = false

      const { abort } = streamChat({
        message: text,
        agentKey: this.currentAgent.id || 'default',
        functionKey: 'home-assistant',
        onToken: (token) => {
          if (!hasReceivedToken) {
            hasReceivedToken = true
            this.$set(this.chatMessages, typingIndex, {
              role: 'assistant',
              type: 'text',
              content: token,
              time: this.formatChatTime(),
              isStreaming: true
            })
          } else {
            const current = this.chatMessages[typingIndex].content
            // 针对 DeepSeek 等供应商不按 SSE 规范发送换行符的情况：
            // 如果当前内容不以换行结尾，且新 token 看起来像块级元素开头，则补一个换行
            const isBlockStart = /^\s*(#{1,6}\s|[-*]\s|\d+\.\s|>\s|```|\|)/.test(token)
            const separator = current && !current.endsWith('\n') && isBlockStart ? '\n' : ''
            this.$set(this.chatMessages[typingIndex], 'content', current + separator + token)
          }
          this.$nextTick(() => this.scrollChatToBottom())
        },
        onDone: () => {
          this.chatStreaming = false
          const msg = this.chatMessages[typingIndex]
          if (msg) {
            if (msg.type === 'typing') {
              // AI 未返回任何内容，将 typing 替换为提示文本
              this.$set(this.chatMessages, typingIndex, {
                role: 'assistant',
                type: 'text',
                content: '（AI 未返回任何内容，请检查模型配置或后端日志）',
                time: this.formatChatTime(),
                isStreaming: false
              })
            } else {
              this.$set(msg, 'isStreaming', false)
            }
          }
          this.saveChatMessages()
        },
        onError: (errMsg) => {
          this.chatStreaming = false
          const msg = this.chatMessages[typingIndex]
          if (msg) {
            if (msg.type === 'typing') {
              this.$set(this.chatMessages, typingIndex, {
                role: 'assistant',
                type: 'text',
                content: '[系统提示：' + errMsg + ']',
                time: this.formatChatTime(),
                isStreaming: false
              })
            } else {
              this.$set(msg, 'isStreaming', false)
              this.$set(msg, 'content', msg.content + '\n\n[系统提示：' + errMsg + ']')
            }
          }
          this.saveChatMessages()
        }
      })

      this.chatAbortController = { abort }
    },
    clearChat() {
      if (this.chatAbortController) {
        this.chatAbortController.abort()
        this.chatAbortController = null
      }
      this.chatStreaming = false
      this.chatMessages = [
        {
          role: 'assistant',
          type: 'text',
          content: `对话已清空。我是${this.currentAgent.name}，${this.currentAgent.desc}，有什么可以帮您的？`,
          time: this.formatChatTime()
        }
      ]
      this.saveChatMessages()
    },
    saveChatMessages() {
      try {
        const key = 'home_chat_messages_' + (this.user.id || 'guest')
        localStorage.setItem(key, JSON.stringify(this.chatMessages))
      } catch (e) { /* 忽略存储错误 */ }
    },
    loadChatMessages() {
      try {
        const key = 'home_chat_messages_' + (this.user.id || 'guest')
        const raw = localStorage.getItem(key)
        if (raw) {
          const parsed = JSON.parse(raw)
          if (Array.isArray(parsed) && parsed.length > 0) {
            this.chatMessages = parsed
          }
        }
      } catch (e) { /* 忽略读取错误 */ }
    },
    toggleDebugPanel() {
      this.showDebugPanel = !this.showDebugPanel
    },
    clearDebugPanelLogs() {
      clearDebugLogs()
      this.aiDebugLogs = []
    },
    async loadAgents() {
      try {
        const accountId = this.user.id || null
        const res = await fetchAgentConfigs(accountId)
        if (res.code === '200' && Array.isArray(res.data) && res.data.length > 0) {
          this.agents = res.data.map(item => ({
            id: item.agentKey || String(item.id),
            name: item.name,
            icon: item.icon,
            desc: item.description || '',
            gradient: item.gradient,
            chips: Array.isArray(item.chips) ? item.chips : [],
            systemPrompt: item.systemPrompt || '',
            enabledTools: Array.isArray(item.enabledTools) ? item.enabledTools : []
          }))
        } else {
          const { loadAgentsFromStorage } = await import('@/views/Manager/AIAgentConfigView')
          this.agents = loadAgentsFromStorage()
        }
        if (this.agents.length > 0 && (!this.currentAgent || this.currentAgent.id === 'default')) {
          this.currentAgent = this.agents[0]
        }
      } catch (e) {
        console.warn('加载 AI Agent 配置失败', e)
        const { loadAgentsFromStorage } = await import('@/views/Manager/AIAgentConfigView')
        this.agents = loadAgentsFromStorage()
        if (this.agents.length > 0) {
          this.currentAgent = this.agents[0]
        }
      }
    },
    switchAgent(agent) {
      if (this.currentAgent.id === agent.id) return
      this.currentAgent = agent
      this.showAgentMenu = false
      this.chatMessages.push({
        role: 'assistant',
        type: 'text',
        content: `已切换到【${agent.name}】。${agent.desc}，请问有什么可以帮您的？`,
        time: this.formatChatTime()
      })
      this.saveChatMessages()
      this.$nextTick(() => this.scrollChatToBottom())
    },
    formatChatTime() {
      const now = new Date()
      const pad = n => String(n).padStart(2, '0')
      return `${pad(now.getHours())}:${pad(now.getMinutes())}`
    },
    scrollChatToBottom() {
      const body = this.$refs.chatBody
      if (body) body.scrollTop = body.scrollHeight
    },
    simulateAIResponse(text) {
      // 已废弃，保留做兜底
      const lower = String(text || '').toLowerCase()
      const typingIndex = this.chatMessages.length
      this.chatMessages.push({ role: 'assistant', type: 'typing', content: '' })
      this.$nextTick(() => this.scrollChatToBottom())

      setTimeout(() => {
        this.chatMessages.splice(typingIndex, 1)
        let response = null

        if (lower.includes('预约')) {
          response = {
            role: 'assistant',
            type: 'appointments',
            content: '今日共有 10 位患者预约，其中 5 位待接诊，2 位已完成，1 位已取消。',
            time: this.formatChatTime()
          }
        } else if (lower.includes('待办') || lower.includes('工作')) {
          const todo = this.isDoctor ? this.doctorTodos : this.stats
          if (this.isDoctor) {
            response = {
              role: 'assistant',
              type: 'text',
              content: `您当前有 ${todo.pendingConsultations} 位待接诊患者、${todo.pendingRecords} 份待写病历、${todo.pendingFollowups} 位待回访患者，明日还有 ${todo.tomorrowAppointments} 条预约待确认。`,
              time: this.formatChatTime()
            }
          } else {
            response = {
              role: 'assistant',
              type: 'text',
              content: `今日预约 ${todo.todayAppointments} 条，患者总数 ${todo.totalPatients} 人，本月已收费 ¥${todo.monthIncome}，待登记加工 ${todo.pendingLabRegistrations} 条。`,
              time: this.formatChatTime()
            }
          }
        } else if (lower.includes('收入') || lower.includes('收费')) {
          response = {
            role: 'assistant',
            type: 'chart',
            content: '本月收入趋势如下：',
            time: this.formatChatTime()
          }
        } else if (lower.includes('患者') || lower.includes('查询')) {
          response = {
            role: 'assistant',
            type: 'text',
            content: '目前系统共有 ' + (this.stats.totalPatients || 0) + ' 位患者。您可以告诉我患者姓名或手机号，我帮您查找。',
            time: this.formatChatTime()
          }
        } else if (lower.includes('今日患者')) {
          response = {
            role: 'assistant',
            type: 'text',
            content: `今日已到诊 ${this.stats.todayAppointments || 0} 位患者。主要治疗项目包括：种植牙复诊 3 人、拔牙 2 人、初诊检查 2 人、根管治疗 1 人。`,
            time: this.formatChatTime()
          }
        } else if (lower.includes('待收费') || lower.includes('未收费')) {
          response = {
            role: 'assistant',
            type: 'text',
            content: '今日有 3 位患者待收费，总计约 ¥5,800。包括：张三（种植牙二期 ¥3,200）、李四（拔牙+缝合 ¥1,600）、王五（洗牙套餐 ¥1,000）。',
            time: this.formatChatTime()
          }
        } else {
          response = {
            role: 'assistant',
            type: 'text',
            content: '收到您的提问。我目前可以帮您查询：今日预约、我的待办、收入情况、患者信息、今日患者、待收费。请问您想了解哪方面？',
            time: this.formatChatTime()
          }
        }

        this.chatMessages.push(response)
        this.$nextTick(() => {
          if (response.type === 'chart') {
            this.renderAIChart()
          }
          this.scrollChatToBottom()
        })
      }, 800)
    },
    renderAIChart() {
      const index = this.chatMessages.length - 1
      const chartDom = document.getElementById('ai-chart-' + index)
      if (!chartDom || !echarts) return
      const chart = echarts.init(chartDom)
      const option = {
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['5/1', '5/2', '5/3', '5/4', '5/5', '5/6', '今日'],
          axisLine: { lineStyle: { color: '#e2e8f0' } },
          axisLabel: { color: '#94a3b8' }
        },
        yAxis: {
          type: 'value',
          axisLine: { show: false },
          splitLine: { lineStyle: { color: '#f1f5f9' } },
          axisLabel: { color: '#94a3b8' }
        },
        series: [{
          name: '收入',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: { width: 3, color: '#2563eb' },
          itemStyle: { color: '#2563eb', borderColor: '#fff', borderWidth: 2 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(37, 99, 235, 0.25)' },
              { offset: 1, color: 'rgba(37, 99, 235, 0.02)' }
            ])
          },
          data: [8200, 9320, 9010, 9340, 12900, 13300, 13200]
        }]
      }
      chart.setOption(option)
    }
  }
}
</script>

<style scoped>
/* === e看牙 医疗 SaaS 风格 === */
.home-wrap {
  --primary: #00a6c9;
  --primary-hover: #0095b5;
  --primary-light: rgba(0, 166, 201, 0.08);
  --text-primary: #1d222a;
  --text-regular: #3e3e3c;
  --text-secondary: #636a74;
  --text-muted: #9397a2;
  --bg-page: #f5f5f5;
  --bg-card: #ffffff;
  --bg-hover: #f5f7fa;
  --border-color: #d9d9d9;
  --border-light: #e8e8e8;
  --success: #52c41a;
  --warning: #faad14;
  --danger: #f86359;
  --shadow-card: 0 2px 8px rgba(0, 0, 0, 0.08);
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
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
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
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
  border: 1px solid rgba(0, 166, 201, 0.2);
}

.status-pill.pill-admin {
  background: rgba(82, 196, 26, 0.08);
  color: var(--success);
  border: 1px solid rgba(82, 196, 26, 0.2);
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
  transition: box-shadow 0.2s ease;
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
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.kpi-card-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.kpi-icon-wrap.kpi-icon-blue { background: var(--primary-light); }
.kpi-icon-wrap.kpi-icon-green { background: rgba(82, 196, 26, 0.1); }
.kpi-icon-wrap.kpi-icon-amber { background: rgba(250, 173, 20, 0.1); }
.kpi-icon-wrap.kpi-icon-purple { background: rgba(124, 58, 237, 0.08); }

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
}

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

/* ========== AI 区域 ========== */
.ai-section {
  max-width: 1200px;
  margin: 0 auto;
}

.ai-panel {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  height: 520px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 Tabs */
.ai-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px 8px 14px;
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
}

.ai-tabs {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  scrollbar-width: none;
}

.ai-tabs::-webkit-scrollbar {
  display: none;
}

.ai-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  flex-shrink: 0;
  font-size: 13px;
  color: var(--text-secondary);
  border: 1px solid transparent;
  user-select: none;
}

.ai-tab:hover {
  background: var(--bg-hover);
  color: var(--text-regular);
}

.ai-tab.active {
  background: var(--primary-light);
  color: var(--primary);
  font-weight: 600;
  border-color: rgba(0, 166, 201, 0.25);
}

.ai-tab-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.ai-tab-name {
  line-height: 1;
}

.ai-header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.ai-header-action {
  width: 30px;
  height: 30px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-muted);
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.ai-header-action:hover {
  background: var(--bg-hover);
  color: var(--danger);
}

.ai-tab-add {
  color: var(--text-muted);
  border: 1px dashed var(--border-color);
}

.ai-tab-add:hover {
  background: var(--bg-hover);
  color: var(--text-regular);
  border-style: solid;
}

/* 消息区 */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: var(--bg-page);
}

/* 消息项 */
.msg-item {
  display: flex;
  animation: fade-in-up 0.25s ease forwards;
}

.msg-item.assistant {
  justify-content: flex-start;
}

.msg-item.user {
  justify-content: flex-end;
}

.msg-bubble {
  max-width: 95%;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.msg-item.assistant .msg-bubble {
  background: #fff;
  color: var(--text-regular);
  border-radius: var(--radius-md) var(--radius-md) var(--radius-md) var(--radius-sm);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--border-light);
}

.msg-item.user .msg-bubble {
  background: var(--primary);
  color: #fff;
  border-radius: var(--radius-md) var(--radius-md) var(--radius-sm) var(--radius-md);
}

.msg-item.assistant.rich-media {
  width: 100%;
}

.msg-card {
  width: 100%;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 14px;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
}

.msg-card-text {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.6;
  margin-bottom: 10px;
}

.msg-text {
  min-height: 20px;
  display: block;
}

/* Markdown 渲染样式 */
.markdown-body {
  line-height: 1.7;
}

.markdown-body :first-child {
  margin-top: 0;
}

.markdown-body :last-child {
  margin-bottom: 0;
}

.markdown-body p {
  margin: 0 0 8px 0;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4 {
  margin: 12px 0 6px 0;
  font-weight: 600;
  line-height: 1.4;
}

.markdown-body h1 { font-size: 16px; }
.markdown-body h2 { font-size: 15px; }
.markdown-body h3 { font-size: 14px; }
.markdown-body h4 { font-size: 13px; }

.markdown-body ul,
.markdown-body ol {
  margin: 6px 0;
  padding-left: 20px;
}

.markdown-body li {
  margin: 3px 0;
}

.markdown-body strong {
  font-weight: 600;
}

.markdown-body code {
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 5px;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'SF Mono', Monaco, 'Cascadia Code', monospace;
}

.markdown-body pre {
  background: rgba(0, 0, 0, 0.04);
  padding: 10px 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.markdown-body pre code {
  background: none;
  padding: 0;
  font-size: 12px;
}

.markdown-body blockquote {
  margin: 8px 0;
  padding: 6px 12px;
  border-left: 3px solid var(--primary);
  background: rgba(37, 99, 235, 0.04);
  border-radius: 0 6px 6px 0;
}

.markdown-body table {
  width: 100%;
  border-collapse: collapse;
  margin: 8px 0;
  font-size: 13px;
}

.markdown-body th,
.markdown-body td {
  border: 1px solid var(--border-light);
  padding: 6px 10px;
  text-align: left;
}

.markdown-body th {
  background: var(--bg-hover);
  font-weight: 600;
}

.msg-time {
  margin-top: 4px;
  font-size: 11px;
  color: var(--text-muted);
  text-align: right;
}

/* 流式输出光标 */
.stream-cursor {
  display: inline-block;
  width: 2px;
  height: 1.1em;
  background: var(--primary);
  margin-left: 2px;
  vertical-align: text-bottom;
  animation: blink-cursor 1s step-end infinite;
}

@keyframes blink-cursor {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* 打字中 */
.typing-dots {
  display: inline-flex;
  gap: 4px;
  align-items: center;
  height: 20px;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--border-color);
  animation: typing-bounce 1.4s infinite ease-in-out both;
}

.typing-dots span:nth-child(1) { animation-delay: -0.32s; }
.typing-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

/* ========== 富媒体：预约卡片 ========== */
.msg-appointments {
  width: 100%;
}

.doctor-filter {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.filter-tag {
  padding: 3px 10px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--bg-hover);
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.filter-tag:hover {
  background: var(--border-light);
  color: var(--text-regular);
}

.filter-tag.active {
  background: var(--primary);
  color: #fff;
}

.appointment-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.appointment-card {
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  padding: 10px;
  border: 1px solid var(--border-light);
  transition: border-color 0.2s ease;
  cursor: pointer;
}

.appointment-card:hover {
  border-color: var(--border-color);
  box-shadow: var(--shadow-card);
}

.appointment-card.completed {
  border-left: 3px solid var(--success);
  background: rgba(82, 196, 26, 0.02);
}

.appointment-card.cancelled {
  border-left: 3px solid var(--border-color);
  opacity: 0.6;
}

.appointment-card.waiting {
  border-left: 3px solid var(--primary);
}

.apptime {
  font-size: 12px;
  font-weight: 600;
  color: var(--primary);
  margin-bottom: 2px;
}

.appname {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.appproject {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.appstatus {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 500;
  background: var(--bg-hover);
  color: var(--text-secondary);
}

.appointment-card.completed .appstatus {
  background: rgba(82, 196, 26, 0.1);
  color: var(--success);
}

/* 图表 */
.msg-chart {
  width: 100%;
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  padding: 6px;
  border: 1px solid var(--border-light);
}

.ai-chart-container {
  width: 100%;
  height: 200px;
}

/* ========== 底部输入区 ========== */
.ai-input-area {
  padding: 8px 14px 12px;
  flex-shrink: 0;
  background: var(--bg-card);
  border-top: 1px solid var(--border-light);
}

.input-shell {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-hover);
  border-radius: var(--radius-md);
  padding: 4px 4px 4px 12px;
  transition: all 0.2s ease;
}

.input-shell:focus-within {
  background: var(--bg-card);
  box-shadow: 0 0 0 2px rgba(0, 166, 201, 0.15);
}

.input-field {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--text-primary);
  height: 36px;
}

.input-field::placeholder {
  color: var(--text-muted);
}

.input-send {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  border: none;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s ease;
  flex-shrink: 0;
}

.input-send:hover:not(:disabled) {
  background: var(--primary-hover);
}

.input-send:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* 调试日志面板 */
.ai-debug-panel {
  background: #1e1e1e;
  border-top: 1px solid #333;
  color: #d4d4d4;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  display: flex;
  flex-direction: column;
  max-height: 200px;
}

.debug-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px;
  background: #2d2d2d;
  border-bottom: 1px solid #333;
  flex-shrink: 0;
}

.debug-title {
  font-weight: 600;
  color: #f0f0f0;
}

.debug-title i {
  color: #f59e0b;
  margin-right: 4px;
}

.debug-actions {
  display: flex;
  gap: 12px;
}

.debug-action {
  cursor: pointer;
  color: #aaa;
  transition: color 0.2s;
}

.debug-action:hover {
  color: #fff;
}

.debug-action i {
  margin-right: 2px;
}

.debug-log-list {
  overflow-y: auto;
  padding: 8px 12px;
  flex: 1;
}

.debug-empty {
  color: #666;
  text-align: center;
  padding: 16px 0;
}

.debug-log-item {
  display: flex;
  gap: 8px;
  padding: 3px 0;
  line-height: 1.5;
  word-break: break-all;
}

.debug-log-item .debug-time {
  color: #858585;
  flex-shrink: 0;
  width: 64px;
}

.debug-log-item .debug-type {
  flex-shrink: 0;
  width: 56px;
  text-align: center;
  border-radius: 3px;
  font-size: 11px;
  padding: 0 4px;
  line-height: 1.6;
}

.debug-log-item.request .debug-type { background: #3b82f6; color: #fff; }
.debug-log-item.response .debug-type { background: #10b981; color: #fff; }
.debug-log-item.token .debug-type { background: #8b5cf6; color: #fff; }
.debug-log-item.error .debug-type { background: #ef4444; color: #fff; }
.debug-log-item.done .debug-type { background: #6b7280; color: #fff; }
.debug-log-item.abort .debug-type { background: #f59e0b; color: #fff; }

.debug-log-item .debug-data {
  color: #ccc;
  flex: 1;
  white-space: pre-wrap;
}

.ai-header-action.active {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.1);
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

  .ai-panel {
    height: 480px;
    border-radius: var(--radius-md);
  }

  .ai-messages {
    padding: 12px;
  }

  .msg-bubble {
    max-width: 95%;
    font-size: 14px;
  }

  .appointment-grid {
    grid-template-columns: 1fr;
  }
}
</style>
