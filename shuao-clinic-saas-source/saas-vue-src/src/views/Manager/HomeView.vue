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

    <!-- AI 中心对话区 -->
    <div class="ai-section">
      <div class="ai-panel">
        <!-- AI 面板头部 -->
        <div class="ai-panel-header">
          <div class="ai-header-title">
            <i class="el-icon-cpu" style="color: #409eff; margin-right: 6px;"></i>
            <span>AI 智能中心</span>
            <el-tag v-if="currentAgent" size="mini" type="primary" style="margin-left: 8px;">
              {{ currentAgent.name }}
            </el-tag>
          </div>
          <div class="ai-header-actions">
            <el-select
              v-model="currentAgentKey"
              size="mini"
              placeholder="选择 AI Agent"
              style="width: 150px; margin-right: 8px;"
            >
              <el-option
                v-for="agent in enabledAgents"
                :key="agent.agentKey"
                :label="agent.name"
                :value="agent.agentKey"
              />
            </el-select>
            <div class="ai-header-action" title="清空对话" @click="clearAiChat">
              <i class="el-icon-delete"></i>
            </div>
          </div>
        </div>

        <!-- 消息展示区 -->
        <div class="ai-messages" ref="aiChatBody">
          <!-- 欢迎消息（无消息时显示） -->
          <div v-if="aiMessages.length === 0" class="ai-welcome">
            <div class="ai-welcome-avatar">
              <i class="el-icon-cpu"></i>
            </div>
            <div class="ai-welcome-title">{{ greeting }}，我是 AI 智能助手</div>
            <div class="ai-welcome-desc">我可以帮您分析经营数据、统计回访情况、辅助病历撰写，或分析患者信息</div>
          </div>

          <!-- 消息列表 -->
          <div
            v-for="(msg, index) in aiMessages"
            :key="index"
            class="msg-item"
            :class="msg.role"
          >
            <div class="msg-avatar-wrap" v-if="msg.role === 'assistant'">
              <div class="msg-avatar-ai">
                <i class="el-icon-cpu"></i>
              </div>
            </div>
            <div class="msg-bubble" :class="msg.role === 'user' ? 'msg-bubble--user' : 'msg-bubble--assistant'">
              <div class="msg-text">
                <template v-if="msg.role === 'assistant'">
                  <div v-if="msg.rawData && typeof msg.rawData === 'object'" class="json-card" v-html="renderJsonCard(msg.rawData)"></div>
                  <span v-else v-html="simpleMarkdown(msg.content)"></span>
                </template>
                <span v-else>{{ msg.content }}</span>
                <span v-if="msg.streaming" class="stream-cursor"></span>
              </div>
              <div v-if="msg.time" class="msg-time">{{ msg.time }}</div>
            </div>
          </div>
        </div>

        <!-- 快捷指令胶囊 -->
        <div class="ai-quick-chips">
          <el-tag
            v-for="(chip, index) in displayChips"
            :key="index"
            class="quick-chip"
            :class="{ active: chip.agentKey === currentAgentKey }"
            effect="plain"
            size="small"
            @click="selectAgentChip(chip)"
          >
            <i :class="chip.icon"></i>
            {{ chip.label }}
          </el-tag>
        </div>

        <!-- 输入区 -->
        <div class="ai-input-area">
          <el-input
            v-model="aiInput"
            type="textarea"
            :rows="2"
            placeholder="请输入您的问题，按回车发送..."
            resize="none"
            @keyup.enter.native="handleAiEnter"
          />
          <el-button
            type="primary"
            icon="el-icon-s-promotion"
            :disabled="!aiInput.trim() || aiLoading"
            :loading="aiLoading"
            @click="sendAiMessage"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
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
      // AI 中心数据模型
      aiMessages: [],
      aiInput: '',
      aiLoading: false,
      aiSessionId: '',
      // 动态 Agent 列表（从后端或本地加载）
      agentList: [],
      currentAgentKey: '',
      aiAgentsLoaded: false,
      // 默认快捷指令兜底（当后端未配置 Agent 时显示）
      defaultChips: [
        { label: '经营分析', icon: 'el-icon-data-line', value: '请帮我分析本月门诊经营情况', agentKey: 'business-analysis' },
        { label: '回访统计', icon: 'el-icon-phone-outline', value: '统计一下近期的回访完成情况', agentKey: 'followup-generate' },
        { label: '病历辅助', icon: 'el-icon-document', value: '请帮我辅助撰写一份病历模板', agentKey: 'medical-expand' },
        { label: '患者分析', icon: 'el-icon-user', value: '分析一下高价值患者和流失风险患者', agentKey: 'patient-insight' }
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
    /** 已启用的 Agent 列表（ai_agent_config 无 isEnabled 字段，所有返回配置均视为启用） */
    enabledAgents() {
      return this.agentList || []
    },
    /** 当前选中的 Agent 对象 */
    currentAgent() {
      return (this.agentList || []).find(a => a.agentKey === this.currentAgentKey) || null
    },
    /** 用于展示的快捷 chips（优先从后端 Agent 配置生成） */
    displayChips() {
      const agents = this.enabledAgents
      if (agents.length > 0) {
        return agents.map(agent => ({
          label: agent.name || agent.agentKey,
          icon: 'el-icon-cpu',
          value: (agent.chips && agent.chips[0]) || `请使用「${agent.name}」Agent`,
          agentKey: agent.agentKey
        }))
      }
      return this.defaultChips
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
    // 初始化 AI 会话 ID
    this.initAiSessionId()
    // 加载 AI Agent 列表
    this.loadAiAgents()
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
          html += `<div class="json-card-row"><div class="json-card-label">${this.escapeHtml(item.label)}</div><div class="json-card-value">${this.escapeHtml(String(data[item.key]))}</div></div>`
          renderedKeys.add(item.key)
        }
      })
      // 再渲染其他未匹配的字段
      Object.keys(data).forEach(key => {
        if (!renderedKeys.has(key) && data[key] != null) {
          html += `<div class="json-card-row"><div class="json-card-label">${this.escapeHtml(key)}</div><div class="json-card-value">${this.escapeHtml(String(data[key]))}</div></div>`
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
    },
    // ========== AI 中心交互方法 ==========
    /**
     * 初始化 AI 会话 ID，从 localStorage 读取或生成新的 UUID
     */
    initAiSessionId() {
      try {
        const stored = localStorage.getItem('ai_session_id')
        if (stored) {
          this.aiSessionId = stored
        } else {
          this.aiSessionId = this.generateUUID()
          localStorage.setItem('ai_session_id', this.aiSessionId)
        }
      } catch (e) {
        this.aiSessionId = this.generateUUID()
      }
    },
    /**
     * 生成简易 UUID
     * @returns {string} UUID 字符串
     */
    generateUUID() {
      return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0
        const v = c === 'x' ? r : (r & 0x3 | 0x8)
        return v.toString(16)
      })
    },
    /**
     * 获取当前时间字符串（HH:mm）
     * @returns {string} 时间字符串
     */
    formatAiTime() {
      const now = new Date()
      const pad = function(n) { return String(n).padStart(2, '0') }
      return pad(now.getHours()) + ':' + pad(now.getMinutes())
    },
    /**
     * 处理输入框回车事件（支持 Shift+Enter 换行）
     * @param {Event} e - 键盘事件
     */
    handleAiEnter(e) {
      if (e.shiftKey) {
        return
      }
      e.preventDefault()
      this.sendAiMessage()
    },
    /**
     * 从后端加载 AI Agent 配置列表（已切换为 /api/ai-agent-configs）
     */
    async loadAiAgents() {
      try {
        const session = getAdminSession()
        const accountId = session && session.id ? session.id : ''
        const res = await axios.get('/api/ai-agent-configs', { params: { accountId } })
        if (res.data && res.data.code === '200' && Array.isArray(res.data.data)) {
          this.agentList = res.data.data
          // 默认选中第一个 Agent
          const firstAgent = this.agentList[0]
          if (firstAgent && firstAgent.agentKey) {
            this.currentAgentKey = firstAgent.agentKey
          }
          this.aiAgentsLoaded = true
        } else {
          // 后端接口不存在，使用 localStorage 中可能缓存的配置
          this.loadAgentsFromLocalStorage()
        }
      } catch (error) {
        console.warn('[HomeView] 加载 Agent 列表失败，回退到本地缓存:', error)
        this.loadAgentsFromLocalStorage()
      }
    },
    /**
     * 从 localStorage 读取缓存的 Agent 配置
     */
    loadAgentsFromLocalStorage() {
      try {
        const cached = localStorage.getItem('ai_agents_config')
        if (cached) {
          const list = JSON.parse(cached)
          if (Array.isArray(list)) {
            this.agentList = list
            const firstEnabled = list.find(a => a.isEnabled !== false)
            if (firstEnabled && firstEnabled.agentKey) {
              this.currentAgentKey = firstEnabled.agentKey
            }
          }
        }
      } catch (e) {
        console.warn('[HomeView] 读取本地 Agent 缓存失败:', e)
      }
    },
    /**
     * 发送 AI 消息（用户输入）
     * 使用当前选中的 currentAgentKey 路由到对应 Agent
     */
    sendAiMessage() {
      const text = this.aiInput.trim()
      if (!text || this.aiLoading) return
      // 添加用户消息到列表
      this.aiMessages.push({
        role: 'user',
        content: text,
        time: this.formatAiTime()
      })
      this.aiInput = ''
      this.$nextTick(function() {
        this.scrollAiChatToBottom()
      })
      // 调用 SSE 流式接口，使用当前选中的 AgentKey
      const key = this.currentAgentKey || 'default'
      this.callAiStream(text, key)
    },
    /**
     * 点击快捷指令 chip：切换到对应 Agent，不自动发送消息
     * @param {Object} chip - chip 对象，包含 agentKey、label、value 等
     */
    selectAgentChip(chip) {
      if (!chip || !chip.agentKey) return
      this.currentAgentKey = chip.agentKey
      // 可选：把快捷指令文本填入输入框，方便用户直接发送或修改
      if (chip.value && chip.value !== `请使用「${chip.label}」Agent`) {
        this.aiInput = chip.value
      }
    },
    /**
     * 调用 AI 代理接口（JSON 同步模式，带 loading 动画）
     * @param {string} text - 用户发送的文本
     * @param {string} agentKey - Agent 标识，默认 'default'
     */
    async callAiStream(text, agentKey) {
      const self = this
      self.aiLoading = true
      const typingIndex = self.aiMessages.length
      // 先添加一个空的 AI 消息占位，显示加载中
      self.aiMessages.push({
        role: 'assistant',
        content: '',
        streaming: true,
        time: self.formatAiTime()
      })
      self.$nextTick(function() {
        self.scrollAiChatToBottom()
      })

      const session = getAdminSession()
      const accountId = session && session.id ? String(session.id) : ''
      const accountName = session && session.name ? String(session.name) : ''
      const key = agentKey || 'default'

      try {
        const res = await axios.post('/api/ai/proxy/' + encodeURIComponent(key), {
          message: text,
          session_id: self.aiSessionId || '',
          account_id: accountId,
          account_name: accountName,
          clinic_id: '1'
        })

        // 后端返回 HTTP 200 但业务码非 200 时，按错误处理
        const bizCode = res && res.data && res.data.code
        if (bizCode !== '200' && bizCode !== 200) {
          const errMsg = (res.data && res.data.msg) || 'AI 请求失败，请稍后重试'
          const msg = self.aiMessages[typingIndex]
          if (msg) {
            self.$set(msg, 'content', '[ERROR]' + errMsg)
            self.$set(msg, 'streaming', false)
          }
          return
        }

        const resultData = res && res.data && res.data.data
        const replyContent = self.extractReplyContent(resultData)

        const msg = self.aiMessages[typingIndex]
        if (msg) {
          self.$set(msg, 'content', replyContent)
          self.$set(msg, 'streaming', false)
          // 保存原始对象用于卡片式美化渲染
          if (resultData != null && typeof resultData === 'object') {
            self.$set(msg, 'rawData', resultData)
          }
        }
      } catch (err) {
        const errMsg = err && err.response && err.response.data && err.response.data.msg
          ? err.response.data.msg
          : 'AI 请求失败，请稍后重试'
        const msg = self.aiMessages[typingIndex]
        if (msg) {
          self.$set(msg, 'content', '[ERROR]' + errMsg)
          self.$set(msg, 'streaming', false)
        }
      } finally {
        self.aiLoading = false
        self.$nextTick(function() {
          self.scrollAiChatToBottom()
        })
      }
    },
    /**
     * 从 AI 响应数据中提取可展示的文本内容
     * 支持多种常见字段名和类型
     * @param {any} data - 后端返回的 data 字段
     * @return {string} 提取出的文本
     */
    extractReplyContent(data) {
      if (data == null) {
        return '（AI 未返回内容）'
      }
      // 字符串类型直接返回
      if (typeof data === 'string') {
        return data
      }
      // 对象类型：尝试常见字段名
      if (typeof data === 'object') {
        const candidateKeys = ['content', 'reply', 'message', 'text', 'answer', 'result', 'output']
        for (const k of candidateKeys) {
          if (data[k] != null) {
            return String(data[k])
          }
        }
        // 没有常见字段时，返回格式化 JSON
        try {
          return JSON.stringify(data, null, 2)
        } catch (e) {
          return String(data)
        }
      }
      return String(data)
    },
    /**
     * 清空 AI 对话
     */
    clearAiChat() {
      this.aiLoading = false
      this.aiMessages = []
    },
    /**
     * 滚动 AI 消息区到底部
     */
    scrollAiChatToBottom() {
      const body = this.$refs.aiChatBody
      if (body) body.scrollTop = body.scrollHeight
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
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* AI 面板头部 */
.ai-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.ai-header-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.ai-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-header-action {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-muted);
  transition: all 0.2s ease;
  background: var(--bg-hover);
}

.ai-header-action:hover {
  background: var(--border-light);
  color: var(--danger);
}

/* 消息区 */
.ai-messages {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

/* 消息项 */
.msg-item {
  display: flex;
  animation: fade-in-up 0.25s ease forwards;
}

.msg-item.assistant {
  justify-content: flex-start;
  align-items: flex-start;
  gap: 10px;
}

.msg-item.user {
  justify-content: flex-end;
}

.msg-avatar-wrap {
  flex-shrink: 0;
  padding-top: 2px;
}

.msg-avatar-ai {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
}

.msg-bubble {
  max-width: 80%;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.msg-bubble--assistant {
  background: #fff;
  border-radius: 12px 12px 12px 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  color: var(--text-regular);
}

.msg-bubble--user {
  background: #409eff;
  color: #fff;
  border-radius: 12px 12px 0 12px;
}

.msg-text {
  min-height: 20px;
  display: block;
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
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* ========== AI 中心欢迎态 ========== */
.ai-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  animation: fade-in-up 0.4s ease forwards;
}

.ai-welcome-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.ai-welcome-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.ai-welcome-desc {
  font-size: 13px;
  color: var(--text-secondary);
  text-align: center;
}

/* ========== 快捷指令胶囊 ========== */
.ai-quick-chips {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
  flex-shrink: 0;
}

.quick-chip {
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.quick-chip:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.quick-chip.active {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
  font-weight: 600;
}

.quick-chip i {
  margin-right: 4px;
}

/* ========== Element UI 输入区 ========== */
.ai-input-area {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  flex-shrink: 0;
}

.ai-input-area .el-textarea {
  flex: 1;
}

.ai-input-area .el-textarea__inner {
  border-radius: 8px;
  resize: none;
}

.ai-input-area .el-button {
  margin-bottom: 1px;
  border-radius: 8px;
  padding: 10px 18px;
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
    padding: 12px;
  }

  .ai-messages {
    min-height: 200px;
    max-height: 350px;
    padding: 12px;
  }

  .msg-bubble {
    max-width: 90%;
    font-size: 14px;
  }
}
</style>

<style>
/* JSON 卡片美化展示 - 全局样式（对 v-html 内容生效） */
.json-card {
  width: 100%;
}
.json-card-inner {
  background: #f0f5ff;
  border: 1px solid #d6e4ff;
  border-radius: 8px;
  padding: 12px 16px;
  max-width: 520px;
}
.json-card-row {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px dashed #bfdbfe;
  align-items: flex-start;
}
.json-card-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.json-card-row:first-child {
  padding-top: 0;
}
.json-card-label {
  flex-shrink: 0;
  width: 72px;
  font-weight: 600;
  color: #1e40af;
  font-size: 13px;
  line-height: 1.6;
}
.json-card-value {
  flex: 1;
  margin-left: 12px;
  color: #1e293b;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}
</style>
