<template>
  <div class="analysis-page">
    <!-- 左栏：会话历史列表 -->
    <aside class="sidebar-left">
      <div class="sidebar-left__header">
        <el-button type="primary" icon="el-icon-plus" class="new-chat-btn" @click="initChatSession">
          新建会话
        </el-button>
      </div>
      <div class="sidebar-left__list">
        <div
          v-for="(session, index) in chatSessions"
          :key="index"
          class="session-item"
          :class="{ active: currentSessionIndex === index }"
          @click="switchSession(index)"
        >
          <div class="session-item__icon">
            <i class="el-icon-chat-dot-round" />
          </div>
          <div class="session-item__info">
            <div class="session-item__title">{{ session.title }}</div>
            <div class="session-item__time">{{ session.time }}</div>
          </div>
          <div class="session-item__actions">
            <i class="el-icon-edit" @click.stop="renameSession(index)" />
            <i class="el-icon-delete" @click.stop="deleteSession(index)" />
          </div>
        </div>
        <el-empty v-if="!chatSessions.length" description="暂无会话" />
      </div>
    </aside>

    <!-- 中栏：对话区域 -->
    <main class="chat-main">
      <div class="chat-main__header">
        <div class="chat-main__title">
          <span>AI 经营分析助手</span>
          <el-tag size="mini" :type="chatRestarted ? 'warning' : 'success'">
            {{ chatRestarted ? '新会话' : '进行中' }}
          </el-tag>
        </div>
        <div class="chat-main__meta">
          <span>会话ID：{{ chatSessionId || '-' }}</span>
          <span>最后交流：{{ chatLastActivityAt || '-' }}</span>
        </div>
      </div>

      <div v-if="chatMemoryPreview" class="memory-preview">
        <div class="memory-preview__head">
          <strong>长期记忆摘录</strong>
          <span>{{ chatMemoryUpdatedAt || '未写入' }}</span>
        </div>
        <pre>{{ chatMemoryPreview }}</pre>
      </div>

      <div ref="chatMessageList" class="chat-message-list" v-loading="chatLoading">
        <ChatMessage
          v-for="(item, index) in chatMessages"
          :key="`chat-${index}-${item.created_at}`"
          :role="item.role"
          :content="item.content"
          :time="item.created_at"
          :is-streaming="item.isStreaming"
        />
        <el-empty v-if="!chatLoading && !chatMessages.length" description="暂无对话，开始提问吧" />
      </div>

      <div class="chat-input-area">
        <ChatInput
          v-model="chatInput"
          :sending="chatSending"
          placeholder="例如：本月非耗材支出上升说明什么？接下来我应该重点盯哪三件事？"
          @send="sendChatMessage"
        />
      </div>
    </main>

    <!-- 右栏：快捷功能面板 -->
    <aside class="sidebar-right">
      <div class="sidebar-right__section">
        <div class="sidebar-right__title">快捷功能</div>
        <div class="quick-actions">
          <el-button
            type="success"
            icon="el-icon-data-analysis"
            :loading="todayLoading"
            class="quick-btn"
            @click="runTodayAnalysis"
          >
            生成日报
          </el-button>
          <el-button
            type="warning"
            icon="el-icon-document"
            :loading="weeklyLoading"
            class="quick-btn"
            @click="runWeeklyReport"
          >
            生成周报
          </el-button>
          <el-button
            type="danger"
            plain
            icon="el-icon-warning-outline"
            :loading="alertLoading"
            class="quick-btn"
            @click="scanAlerts"
          >
            扫描异常
          </el-button>
          <el-button
            icon="el-icon-connection"
            :loading="probeLoading"
            class="quick-btn"
            @click="probeModel"
          >
            测试连接
          </el-button>
        </div>
      </div>

      <div class="sidebar-right__section">
        <div class="sidebar-right__title">分析日期</div>
        <el-date-picker
          v-model="runDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="选择分析日期"
          size="small"
          style="width: 100%"
        />
        <el-button
          type="primary"
          icon="el-icon-magic-stick"
          :loading="runLoading"
          class="quick-btn"
          style="margin-top: 10px"
          @click="runAnalysis"
        >
          补跑指定日期
        </el-button>
        <el-button
          type="warning"
          plain
          icon="el-icon-tickets"
          :loading="monthlyLoading"
          class="quick-btn"
          style="margin-top: 8px"
          @click="runMonthlyReport"
        >
          生成本月月报
        </el-button>
      </div>

      <div v-if="taskNotices.length" class="sidebar-right__section">
        <div class="sidebar-right__title">任务状态</div>
        <div class="task-notice-group">
          <div v-for="item in taskNotices" :key="item.key" class="task-notice">
            <div class="task-notice__head">
              <strong>{{ item.label }}</strong>
              <el-tag :type="statusTagType(item.status)" size="mini">{{ item.status }}</el-tag>
            </div>
            <div class="task-notice__text">{{ item.message }}</div>
          </div>
        </div>
      </div>

      <div class="sidebar-right__section">
        <div class="sidebar-right__title">最新周报</div>
        <div v-if="weeklyLatest" class="period-card">
          <div class="period-head">
            <strong>{{ weeklyLatest.period_label || '-' }}</strong>
            <el-tag :type="statusTagType(weeklyLatest.report_status)" size="mini">{{ weeklyLatest.report_status }}</el-tag>
          </div>
          <div class="period-title">{{ weeklyLatest.headline || '暂无周报' }}</div>
          <div class="period-summary">{{ weeklyLatest.summary || '暂无摘要' }}</div>
          <div class="period-meta">评分 {{ weeklyLatest.operating_score ?? '--' }} · {{ trendLabel(weeklyLatest.trend) }}</div>
        </div>
        <div v-else class="empty-text">暂无周报</div>
      </div>

      <div class="sidebar-right__section">
        <div class="sidebar-right__title">最新月报</div>
        <div v-if="monthlyLatest" class="period-card">
          <div class="period-head">
            <strong>{{ monthlyLatest.period_label || '-' }}</strong>
            <el-tag :type="statusTagType(monthlyLatest.report_status)" size="mini">{{ monthlyLatest.report_status }}</el-tag>
          </div>
          <div class="period-title">{{ monthlyLatest.headline || '暂无月报' }}</div>
          <div class="period-summary">{{ monthlyLatest.summary || '暂无摘要' }}</div>
          <div class="period-meta">评分 {{ monthlyLatest.operating_score ?? '--' }} · {{ trendLabel(monthlyLatest.trend) }}</div>
        </div>
        <div v-else class="empty-text">暂无月报</div>
      </div>

      <div class="sidebar-right__section">
        <div class="sidebar-right__title">长期记忆</div>
        <el-button size="small" plain style="width: 100%" :loading="memoryLoading" @click="openMemoryDialog">
          查看长期记忆
        </el-button>
        <div class="memory-dialog-meta" style="margin-top: 8px">
          最近更新：{{ chatMemoryUpdatedAt || '未写入' }}
        </div>
      </div>
    </aside>

    <!-- 弹窗：长期记忆 -->
    <el-dialog title="长期记忆文档" :visible.sync="memoryDialogVisible" width="760px">
      <div class="memory-dialog-meta">
        <span>最近更新时间：{{ chatMemoryUpdatedAt || '未写入' }}</span>
      </div>
      <pre class="memory-dialog-content">{{ chatMemoryContent || '暂无长期记忆内容' }}</pre>
      <span slot="footer">
        <el-button @click="memoryDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 弹窗：模型连接测试结果 -->
    <el-dialog title="模型连接测试" :visible.sync="probeDialogVisible" width="560px">
      <div v-if="probeResult" class="probe-body">
        <div><strong>模型：</strong>{{ probeResult.model || '-' }}</div>
        <div><strong>接口：</strong>{{ probeResult.base_url || '-' }}/responses</div>
        <div><strong>结果：</strong>{{ probeResult.message || '-' }}</div>
        <div v-if="probeResult.response_sample"><strong>返回样例：</strong>{{ probeResult.response_sample }}</div>
        <div><strong>检测时间：</strong>{{ probeResult.checked_at || '-' }}</div>
      </div>
      <span slot="footer">
        <el-button @click="probeDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'
import { showApiError } from '@/utils/errorMessage'
import { streamChat } from '@/utils/aiStreamClient'
import ChatMessage from '@/components/design-system/ChatMessage.vue'
import ChatInput from '@/components/design-system/ChatInput.vue'

export default {
  name: 'BusinessAnalysisView',
  components: {
    ChatMessage,
    ChatInput
  },
  data() {
    return {
      currentUser: getAdminSession() || {},
      latest: null,
      history: [],
      runLoading: false,
      todayLoading: false,
      weeklyLoading: false,
      monthlyLoading: false,
      alertLoading: false,
      probeLoading: false,
      detailLoading: false,
      runDate: '',
      probeResult: null,
      probeDialogVisible: false,
      weeklyLatest: null,
      monthlyLatest: null,
      alerts: [],
      dailyTask: null,
      weeklyTask: null,
      monthlyTask: null,
      pollTimers: {},
      chatLoading: false,
      chatSending: false,
      chatStreaming: false,
      chatSessionId: '',
      chatMessages: [],
      chatInput: '',
      chatRestarted: false,
      chatLastActivityAt: '',
      chatAbortController: null,
      chatMemoryPreview: '',
      chatMemoryUpdatedAt: '',
      memoryLoading: false,
      memoryDialogVisible: false,
      chatMemoryContent: '',
      chatSessions: [],
      currentSessionIndex: 0
    }
  },
  computed: {
    analysisData() {
      return this.latest && this.latest.analysis ? this.latest.analysis : null
    },
    weeklyMeetingContent() {
      const report = this.weeklyLatest || null
      const analysis = report && report.analysis ? report.analysis : null
      const metrics = report && report.metrics ? report.metrics : null
      if (!report || !analysis || !metrics) {
        return null
      }

      const communicatePoints = []
      communicatePoints.push(
        `先统一经营结果：本周预约 ${this.numberValue(metrics.total_appointments)} 次，独立患者 ${this.numberValue(metrics.total_unique_patients)} 人，治疗 ${this.numberValue(metrics.total_treatments)} 次，净收入 ¥${this.formatAmount(metrics.net_income)}。`
      )
      if (analysis.summary) {
        communicatePoints.push(analysis.summary)
      }
      ;(analysis.risks || []).slice(0, 2).forEach(item => {
        communicatePoints.push(`重点风险：${item.title}。现状是${item.finding}。本周要求：${item.recommendation}`)
      })
      ;(analysis.opportunities || []).slice(0, 1).forEach(item => {
        communicatePoints.push(`本周机会：${item.title}。团队要围绕"${item.finding}"推进，具体做法是：${item.recommendation}`)
      })

      const executionItems = (analysis.actions || []).slice(0, 5).map(item => ({
        priority: item.priority || 'P1',
        owner: item.owner || '负责人待明确',
        due: item.due || '本周',
        action: item.action || '待补充',
        expected_result: item.expected_result || '待补充'
      }))

      const checkQuestions = [
        '本周预约里，哪些患者还没有形成明确治疗计划？负责人分别是谁？',
        '本周前台、医生、护士三端的数据录入，哪里还不规范？今天怎么改？'
      ]
      if (this.numberValue(metrics.completed_treatments) === 0) {
        checkQuestions.push('为什么本周治疗没有形成完结闭环？是到诊问题、报价问题，还是复诊推进不到位？')
      }
      if (this.numberValue(metrics.total_unique_patients) <= 3) {
        checkQuestions.push('现有少量患者怎么做复诊召回和项目推进，才能把下周排班先填起来？')
      } else {
        checkQuestions.push('哪些患者最有机会在下周形成治疗或收费，谁负责跟进到结果？')
      }

      const closingRequirements = [
        '会后当天把责任人、截止时间、预期结果发到群里，不允许只有口头安排没有书面确认。',
        '下次周会先逐条复盘本周承诺动作，未完成必须说明原因、补救动作和完成时间。',
        '日终至少核对预约、到诊、病历、治疗、收费五项数据，保证下周周会讨论的是准数据。'
      ]

      return {
        theme: report.headline || `${report.period_label || '本周'}周会重点`,
        opening: analysis.management_brief || report.summary || '暂无周会摘要',
        communicatePoints,
        executionItems,
        checkQuestions,
        closingRequirements
      }
    },
    metricsData() {
      return this.latest && this.latest.metrics ? this.latest.metrics : {}
    },
    taskNotices() {
      return [
        { key: 'daily', label: '日报任务', task: this.dailyTask },
        { key: 'weekly', label: '周报任务', task: this.weeklyTask },
        { key: 'monthly', label: '月报任务', task: this.monthlyTask }
      ].filter(item => item.task && !item.task.done).map(item => ({
        key: item.key,
        label: item.label,
        status: item.task.task_status || 'PENDING',
        message: item.task.message || `${item.label}正在后台生成`
      }))
    }
  },
  created() {
    this.runDate = this.yesterday()
    this.loadLatest()
    this.loadHistory()
    this.loadWeeklyLatest()
    this.loadMonthlyLatest()
    this.loadAlerts()
    this.initChatSession()
  },
  beforeDestroy() {
    Object.keys(this.pollTimers).forEach(key => this.clearTaskPoller(key))
    if (this.chatAbortController) {
      this.chatAbortController.abort()
      this.chatAbortController = null
    }
  },
  methods: {
    yesterday() {
      const date = new Date()
      date.setDate(date.getDate() - 1)
      return this.formatDateValue(date)
    },
    today() {
      return this.formatDateValue(new Date())
    },
    formatDateValue(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    loadLatest() {
      axios.get('/business-analysis/latest').then(res => {
        if (res.data.code === '200') {
          this.latest = res.data.data || null
        }
      })
    },
    loadHistory() {
      axios.get('/business-analysis/history', { params: { limit: 20 } }).then(res => {
        if (res.data.code === '200') {
          this.history = Array.isArray(res.data.data) ? res.data.data : []
        }
      })
    },
    loadWeeklyLatest() {
      axios.get('/business-analysis/weekly/latest').then(res => {
        if (res.data.code === '200') {
          this.weeklyLatest = res.data.data || null
        }
      })
    },
    loadMonthlyLatest() {
      axios.get('/business-analysis/monthly/latest').then(res => {
        if (res.data.code === '200') {
          this.monthlyLatest = res.data.data || null
        }
      })
    },
    loadAlerts() {
      axios.get('/business-analysis/alerts/recent', { params: { limit: 20 } }).then(res => {
        if (res.data.code === '200') {
          this.alerts = Array.isArray(res.data.data) ? res.data.data : []
        }
      })
    },
    chatAccountParams() {
      return {
        accountId: this.currentUser && this.currentUser.id ? Number(this.currentUser.id) : undefined,
        accountName: this.currentUser && this.currentUser.name ? this.currentUser.name : ''
      }
    },
    initChatSession() {
      if (this.chatAbortController) {
        this.chatAbortController.abort()
        this.chatAbortController = null
      }
      this.chatStreaming = false
      this.chatLoading = true
      axios.get('/business-analysis/chat/session', { params: this.chatAccountParams() }).then(res => {
        if (res.data.code === '200') {
          this.applyChatPayload(res.data.data || {})
          this.addOrUpdateSession()
        } else {
          this.$message.error(res.data.msg || '初始化对话失败')
        }
      }).catch(() => {
        this.$message.error('初始化对话失败')
      }).finally(() => {
        this.chatLoading = false
      })
    },
    refreshChatSession() {
      this.initChatSession()
    },
    applyChatPayload(payload) {
      this.chatSessionId = payload.session_id || ''
      this.chatMessages = Array.isArray(payload.messages) ? payload.messages : []
      this.chatRestarted = payload.restarted === true
      this.chatLastActivityAt = payload.last_activity_at || ''
      this.chatMemoryPreview = payload.memory_preview || ''
      this.chatMemoryUpdatedAt = payload.memory_updated_at || ''
      this.$nextTick(() => this.scrollChatToBottom())
    },
    addOrUpdateSession() {
      const title = this.chatMessages.length
        ? (this.chatMessages[0].content || '新会话').slice(0, 20)
        : '新会话'
      const time = this.chatLastActivityAt || this.formatDateValue(new Date()) + ' ' + new Date().toLocaleTimeString()
      const existingIndex = this.chatSessions.findIndex(s => s.sessionId === this.chatSessionId)
      if (existingIndex >= 0) {
        this.chatSessions[existingIndex].title = title
        this.chatSessions[existingIndex].time = time
        this.currentSessionIndex = existingIndex
      } else {
        this.chatSessions.unshift({
          sessionId: this.chatSessionId,
          title,
          time
        })
        this.currentSessionIndex = 0
      }
    },
    switchSession(index) {
      this.currentSessionIndex = index
      // 这里可以根据 sessionId 加载历史会话，当前实现为刷新当前会话
      this.initChatSession()
    },
    renameSession(index) {
      this.$prompt('请输入新名称', '重命名会话', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: this.chatSessions[index].title
      }).then(({ value }) => {
        this.chatSessions[index].title = value
      }).catch(() => {})
    },
    deleteSession(index) {
      this.$confirm('确定删除该会话？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.chatSessions.splice(index, 1)
        if (this.currentSessionIndex >= this.chatSessions.length) {
          this.currentSessionIndex = Math.max(0, this.chatSessions.length - 1)
        }
        if (!this.chatSessions.length) {
          this.initChatSession()
        }
      }).catch(() => {})
    },
    openMemoryDialog() {
      this.memoryLoading = true
      axios.get('/business-analysis/chat/memory', { params: this.chatAccountParams() }).then(res => {
        if (res.data.code === '200') {
          const data = res.data.data || {}
          this.chatMemoryContent = data.content || ''
          this.chatMemoryUpdatedAt = data.updated_at || this.chatMemoryUpdatedAt
          this.memoryDialogVisible = true
        } else {
          this.$message.error((res.data.msg || '获取长期记忆失败') + '，请刷新页面重试。如问题持续，请联系管理员。')
        }
      }).catch(() => {
        this.$message.error('获取长期记忆失败')
      }).finally(() => {
        this.memoryLoading = false
      })
    },
    scrollChatToBottom() {
      const container = this.$refs.chatMessageList
      if (container && typeof container.scrollTop === 'number') {
        container.scrollTop = container.scrollHeight
      }
    },
    sendChatMessage() {
      const message = String(this.chatInput || '').trim()
      if (!message) {
        this.$message.warning('请输入对话内容')
        return
      }
      if (this.chatStreaming) return
      this.chatSending = true
      this.chatStreaming = true

      const now = this.formatDateValue(new Date()) + ' ' + new Date().toLocaleTimeString()
      this.chatMessages.push({
        role: 'user',
        content: message,
        created_at: now
      })
      this.chatInput = ''
      this.chatLastActivityAt = now
      this.$nextTick(() => this.scrollChatToBottom())

      const assistantIndex = this.chatMessages.length
      this.chatMessages.push({
        role: 'assistant',
        content: '',
        created_at: now,
        isStreaming: true
      })
      this.$nextTick(() => this.scrollChatToBottom())

      const { abort } = streamChat({
        message,
        sessionId: this.chatSessionId || '',
        functionKey: 'business-analysis',
        onToken: (token) => {
          const msg = this.chatMessages[assistantIndex]
          if (msg) {
            this.$set(msg, 'content', msg.content + token)
          }
          this.$nextTick(() => this.scrollChatToBottom())
        },
        onDone: () => {
          this.chatStreaming = false
          this.chatSending = false
          const msg = this.chatMessages[assistantIndex]
          if (msg) {
            this.$set(msg, 'isStreaming', false)
          }
          this.addOrUpdateSession()
        },
        onError: (errMsg) => {
          this.chatStreaming = false
          this.chatSending = false
          const msg = this.chatMessages[assistantIndex]
          if (msg) {
            this.$set(msg, 'isStreaming', false)
            this.$set(msg, 'content', msg.content + '\n\n[系统提示：' + errMsg + ']')
          }
        }
      })

      this.chatAbortController = { abort }
    },
    loadDetail(id) {
      if (!id) return
      this.detailLoading = true
      axios.get(`/business-analysis/${id}`).then(res => {
        if (res.data.code === '200' && res.data.data) {
          this.latest = res.data.data
        }
      }).finally(() => {
        this.detailLoading = false
      })
    },
    runAnalysis() {
      this.submitTask({
        submitUrl: '/business-analysis/run',
        statusUrl: '/business-analysis/run/status',
        loadingField: 'runLoading',
        taskField: 'dailyTask',
        date: this.runDate || '',
        taskLabel: '经营日报',
        onComplete: (report) => {
          this.latest = report || null
          this.loadLatest()
          this.loadHistory()
        }
      })
    },
    runTodayAnalysis() {
      this.submitTask({
        submitUrl: '/business-analysis/run',
        statusUrl: '/business-analysis/run/status',
        loadingField: 'todayLoading',
        taskField: 'dailyTask',
        date: this.today(),
        taskLabel: '经营日报',
        onComplete: (report) => {
          this.latest = report || null
          this.loadLatest()
          this.loadHistory()
        }
      })
    },
    runWeeklyReport() {
      this.submitTask({
        submitUrl: '/business-analysis/weekly/run',
        statusUrl: '/business-analysis/weekly/run/status',
        loadingField: 'weeklyLoading',
        taskField: 'weeklyTask',
        date: this.runDate || '',
        taskLabel: '周报',
        onComplete: (report) => {
          this.weeklyLatest = report || null
          this.loadWeeklyLatest()
        }
      })
    },
    runMonthlyReport() {
      this.submitTask({
        submitUrl: '/business-analysis/monthly/run',
        statusUrl: '/business-analysis/monthly/run/status',
        loadingField: 'monthlyLoading',
        taskField: 'monthlyTask',
        date: this.runDate || '',
        taskLabel: '月报',
        onComplete: (report) => {
          this.monthlyLatest = report || null
          this.loadMonthlyLatest()
        }
      })
    },
    scanAlerts() {
      this.runPeriod('/business-analysis/alerts/scan', 'alertLoading', () => {
        this.$message.success('异常波动已扫描')
        this.loadAlerts()
      })
    },
    submitTask({ submitUrl, statusUrl, loadingField, taskField, date, taskLabel, onComplete }) {
      this[loadingField] = true
      axios.post(submitUrl, null, {
        params: { date: date || '' }
      }).then(res => {
        if (res.data.code === '200' && res.data.data) {
          const task = this.normalizeTaskPayload(res.data.data)
          this[taskField] = task
          if (task.done) {
            this.finishTaskPolling({ loadingField, taskField, taskLabel, task, onComplete })
            return
          }
          this.$message.info(task.message || `${taskLabel}任务已提交，正在后台生成`)
          this.startTaskPolling({
            statusUrl,
            loadingField,
            taskField,
            date,
            taskLabel,
            onComplete
          })
        } else {
          this.$message.error(res.data.msg || `${taskLabel}提交失败`)
          this[loadingField] = false
        }
      }).catch(error => {
        this.$message.error((error.response && error.response.data && error.response.data.msg) || `${taskLabel}提交失败`)
        this[loadingField] = false
      })
    },
    runPeriod(url, loadingField, onSuccess) {
      this[loadingField] = true
      axios.post(url, null, {
        params: { date: this.runDate || '' }
      }).then(res => {
        if (res.data.code === '200') {
          if (typeof onSuccess === 'function') {
            onSuccess(res.data.data)
          }
        } else {
          this.$message.error(res.data.msg || '执行失败')
        }
      }).catch(error => {
        this.$message.error((error.response && error.response.data && error.response.data.msg) || '执行失败')
      }).finally(() => {
        this[loadingField] = false
      })
    },
    startTaskPolling({ statusUrl, loadingField, taskField, date, taskLabel, onComplete }) {
      this.clearTaskPoller(taskField)
      const poll = (attempt = 0) => {
        axios.get(statusUrl, {
          params: { date: date || '' }
        }).then(res => {
          if (res.data.code !== '200' || !res.data.data) {
            throw new Error(res.data.msg || `${taskLabel}状态查询失败`)
          }
          const task = this.normalizeTaskPayload(res.data.data)
          this[taskField] = task
          if (task.done) {
            this.finishTaskPolling({ loadingField, taskField, taskLabel, task, onComplete })
            return
          }
          if (attempt >= 39) {
            this.$message.warning(`${taskLabel}仍在生成中，请稍后手动刷新结果`)
            this[loadingField] = false
            this.clearTaskPoller(taskField)
            return
          }
          this.pollTimers[taskField] = window.setTimeout(() => poll(attempt + 1), 3000)
        }).catch(error => {
          if (attempt >= 39) {
            this.$message.error((error && error.message) || `${taskLabel}状态查询失败`)
            this[loadingField] = false
            this.clearTaskPoller(taskField)
            return
          }
          this.pollTimers[taskField] = window.setTimeout(() => poll(attempt + 1), 3000)
        })
      }
      poll()
    },
    finishTaskPolling({ loadingField, taskField, taskLabel, task, onComplete }) {
      this[loadingField] = false
      this.clearTaskPoller(taskField)
      if (task.task_status === 'SUCCESS') {
        this.$message.success(`${taskLabel}已生成`)
      } else if (task.task_status === 'FALLBACK') {
        this.$message.warning(`${taskLabel}已生成，当前为规则回退结果`)
      } else if (task.task_status === 'FAILED') {
        this.$message.error(`${taskLabel}生成失败`)
      }
      if (typeof onComplete === 'function') {
        onComplete(task.report || null, task)
      }
      this[taskField] = null
    },
    clearTaskPoller(taskField) {
      if (this.pollTimers[taskField]) {
        window.clearTimeout(this.pollTimers[taskField])
        delete this.pollTimers[taskField]
      }
    },
    normalizeTaskPayload(payload) {
      const task = payload || {}
      return {
        task_status: task.task_status || 'PENDING',
        done: Boolean(task.done),
        message: task.message || '',
        report: task.report || null
      }
    },
    probeModel() {
      this.probeLoading = true
      axios.get('/business-analysis/probe').then(res => {
        if (res.data.code === '200') {
          this.probeResult = res.data.data || null
          this.probeDialogVisible = true
          if (this.probeResult && this.probeResult.connected) {
            this.$message.success('模型连接正常')
          } else {
            this.$message.warning('模型连接未通过')
          }
        } else {
          this.$message.error(res.data.msg || '模型连接测试失败')
        }
      }).catch(error => {
        this.$message.error((error.response && error.response.data && error.response.data.msg) || '模型连接测试失败')
      }).finally(() => {
        this.probeLoading = false
      })
    },
    metricValue(key, fallback = '--') {
      const value = this.metricsData && this.metricsData[key]
      return value === null || value === undefined || value === '' ? fallback : value
    },
    moneyValue(key) {
      const value = Number(this.metricValue(key, 0) || 0)
      return value.toFixed(2)
    },
    formatAmount(value) {
      const num = Number(value || 0)
      return Number.isFinite(num) ? num.toFixed(2) : '0.00'
    },
    numberValue(value) {
      const num = Number(value || 0)
      return Number.isFinite(num) ? num : 0
    },
    trendLabel(value) {
      if (value === 'up') return '上升'
      if (value === 'down') return '下降'
      if (value === 'flat') return '持平'
      return value || '-'
    },
    trendClass(value) {
      return {
        'trend-up': value === 'up',
        'trend-down': value === 'down',
        'trend-flat': value === 'flat'
      }
    },
    statusTagType(status) {
      if (status === 'SUCCESS') return 'success'
      if (status === 'FALLBACK') return 'warning'
      if (status === 'FAILED') return 'danger'
      return 'info'
    },
    severityTagType(value) {
      if (value === 'high') return 'danger'
      if (value === 'medium') return 'warning'
      return 'info'
    },
    impactTagType(value) {
      if (value === 'high') return 'success'
      if (value === 'medium') return ''
      return 'info'
    },
    priorityTagType(value) {
      if (value === 'P0') return 'danger'
      if (value === 'P1') return 'warning'
      return 'success'
    },
    alertLevelTagType(value) {
      if (value === 'HIGH') return 'danger'
      if (value === 'MEDIUM') return 'warning'
      return 'info'
    },
    copyWeeklyMeetingContent() {
      if (!this.weeklyMeetingContent) return
      const content = this.formatWeeklyMeetingContentText()
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(content).then(() => {
          this.$message.success('周会内容已复制')
        }).catch(() => {
          this.fallbackCopy(content)
        })
        return
      }
      this.fallbackCopy(content)
    },
    fallbackCopy(content) {
      const input = document.createElement('textarea')
      input.value = content
      input.setAttribute('readonly', 'readonly')
      input.style.position = 'fixed'
      input.style.left = '-9999px'
      document.body.appendChild(input)
      input.select()
      try {
        document.execCommand('copy')
        this.$message.success('周会内容已复制')
      } catch (error) {
        this.$message.error('复制失败，请手动复制')
      } finally {
        document.body.removeChild(input)
      }
    },
    formatWeeklyMeetingContentText() {
      const content = this.weeklyMeetingContent
      if (!content) return ''
      const lines = [
        '周会内容',
        `主题：${content.theme}`,
        `开场：${content.opening}`,
        '',
        '一、本周先向员工沟通'
      ]
      content.communicatePoints.forEach((item, index) => {
        lines.push(`${index + 1}. ${item}`)
      })
      lines.push('', '二、本周必须落实')
      content.executionItems.forEach((item, index) => {
        lines.push(`${index + 1}. [${item.priority}] ${item.action}`)
        lines.push(`负责人：${item.owner}；时限：${item.due}；结果：${item.expected_result}`)
      })
      lines.push('', '三、周会现场要追问')
      content.checkQuestions.forEach((item, index) => {
        lines.push(`${index + 1}. ${item}`)
      })
      lines.push('', '四、会后要求')
      content.closingRequirements.forEach((item, index) => {
        lines.push(`${index + 1}. ${item}`)
      })
      return lines.join('\n')
    }
  }
}
</script>

<style scoped>
/* ============================================
   Apple 风格变量
   ============================================ */
.analysis-page {
  --apple-bg-primary: #f5f5f7;
  --apple-bg-secondary: #ffffff;
  --apple-text-primary: #1d1d1f;
  --apple-text-secondary: #86868b;
  --apple-blue: #0071e3;
  --apple-blue-hover: #0077ed;
  --apple-gray: #f5f5f7;
  --apple-border: #d2d2d7;
  --apple-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  --apple-radius-sm: 8px;
  --apple-radius-md: 12px;
  --apple-radius-lg: 16px;
  --apple-radius-xl: 24px;

  display: flex;
  height: calc(100vh - 84px);
  background: var(--apple-bg-primary);
  overflow: hidden;
}

/* ============================================
   左栏：会话历史列表
   ============================================ */
.sidebar-left {
  width: 260px;
  flex-shrink: 0;
  background: var(--apple-bg-secondary);
  border-right: 1px solid var(--apple-border);
  display: flex;
  flex-direction: column;
}

.sidebar-left__header {
  padding: 16px;
  border-bottom: 1px solid var(--apple-border);
}

.new-chat-btn {
  width: 100%;
  border-radius: var(--apple-radius-xl);
  font-weight: 500;
}

.sidebar-left__list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--apple-radius-md);
  cursor: pointer;
  transition: background 0.2s ease;
  margin-bottom: 4px;
  position: relative;
}

.session-item:hover {
  background: var(--apple-gray);
}

.session-item.active {
  background: rgba(0, 113, 227, 0.08);
}

.session-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--apple-blue);
  border-radius: 0 2px 2px 0;
}

.session-item__icon {
  width: 32px;
  height: 32px;
  border-radius: var(--apple-radius-sm);
  background: var(--apple-blue);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  flex-shrink: 0;
}

.session-item__info {
  flex: 1;
  min-width: 0;
}

.session-item__title {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-item__time {
  font-size: 11px;
  color: var(--apple-text-secondary);
  margin-top: 2px;
}

.session-item__actions {
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.2s ease;
  color: var(--apple-text-secondary);
  font-size: 13px;
}

.session-item:hover .session-item__actions {
  opacity: 1;
}

.session-item__actions i:hover {
  color: var(--apple-blue);
}

/* ============================================
   中栏：对话区域
   ============================================ */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--apple-bg-secondary);
}

.chat-main__header {
  padding: 16px 24px;
  border-bottom: 1px solid var(--apple-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.chat-main__title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 700;
  color: var(--apple-text-primary);
}

.chat-main__meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--apple-text-secondary);
}

.memory-preview {
  margin: 12px 24px 0;
  padding: 12px 14px;
  border-radius: var(--apple-radius-lg);
  border: 1px solid var(--apple-border);
  background: var(--apple-bg-primary);
  flex-shrink: 0;
}

.memory-preview__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  color: var(--apple-text-primary);
  font-size: 12px;
}

.memory-preview pre {
  margin: 10px 0 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
  color: var(--apple-text-secondary);
  max-height: 120px;
  overflow: auto;
  font-size: 12px;
}

.chat-message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 消息气泡（由 ChatMessage 组件渲染，此处提供全局覆盖） */
.chat-message-list ::v-deep .message--assistant .message-bubble {
  background: var(--apple-gray);
  color: var(--apple-text-primary);
  border-radius: 16px 16px 16px 4px;
  padding: 14px 18px;
  box-shadow: none;
}

.chat-message-list ::v-deep .message--user .message-bubble {
  background: var(--apple-blue);
  color: #ffffff;
  border-radius: 16px 16px 4px 16px;
  padding: 14px 18px;
  box-shadow: none;
}

/* Markdown 渲染区域 */
.chat-message-list ::v-deep pre {
  background: #1c1c1e;
  color: #f5f5f7;
  border-radius: var(--apple-radius-md);
  padding: 14px;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.6;
}

.chat-message-list ::v-deep code {
  background: rgba(0, 113, 227, 0.08);
  color: var(--apple-blue);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.chat-message-list ::v-deep blockquote {
  margin: 8px 0;
  padding: 8px 16px;
  border-left: 3px solid var(--apple-blue);
  background: var(--apple-bg-primary);
  border-radius: 0 var(--apple-radius-sm) var(--apple-radius-sm) 0;
  color: var(--apple-text-secondary);
}

.chat-input-area {
  padding: 16px 24px 24px;
  border-top: 1px solid var(--apple-border);
  flex-shrink: 0;
  background: var(--apple-bg-secondary);
}

/* 输入框胶囊形状（由 ChatInput 组件渲染，此处提供全局覆盖） */
.chat-input-area ::v-deep .chat-input-wrapper {
  border-radius: var(--apple-radius-xl);
  border: 1px solid var(--apple-border);
  background: var(--apple-bg-primary);
  padding: 10px 16px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.chat-input-area ::v-deep .chat-input-wrapper:focus-within {
  border-color: var(--apple-blue);
  box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.15);
}

.chat-input-area ::v-deep .chat-input-field {
  background: transparent;
  border: none;
  outline: none;
  color: var(--apple-text-primary);
  font-size: 15px;
}

.chat-input-area ::v-deep .chat-send-btn {
  border-radius: 50%;
  width: 32px;
  height: 32px;
  background: var(--apple-blue);
  color: #fff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}

.chat-input-area ::v-deep .chat-send-btn:hover {
  background: var(--apple-blue-hover);
}

/* ============================================
   右栏：快捷功能面板
   ============================================ */
.sidebar-right {
  width: 280px;
  flex-shrink: 0;
  background: var(--apple-bg-secondary);
  border-left: 1px solid var(--apple-border);
  overflow-y: auto;
  padding: 16px;
}

.sidebar-right__section {
  margin-bottom: 20px;
  padding: 14px;
  background: var(--apple-bg-secondary);
  border-radius: var(--apple-radius-lg);
  box-shadow: var(--apple-shadow);
  border: 1px solid var(--apple-border);
}

.sidebar-right__title {
  font-size: 13px;
  font-weight: 700;
  color: var(--apple-text-primary);
  margin-bottom: 10px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-btn {
  width: 100%;
  border-radius: var(--apple-radius-xl);
  text-align: left;
  font-weight: 500;
  border: 1px solid var(--apple-border);
  background: var(--apple-bg-secondary);
  color: var(--apple-text-primary);
  transition: background 0.2s ease, border-color 0.2s ease;
}

.quick-btn:hover {
  background: var(--apple-gray);
  border-color: var(--apple-blue);
}

.task-notice-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-notice {
  padding: 10px 12px;
  border-radius: var(--apple-radius-md);
  background: var(--apple-bg-primary);
  border: 1px solid var(--apple-border);
}

.task-notice__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: var(--apple-text-primary);
  font-size: 12px;
}

.task-notice__text {
  margin-top: 6px;
  color: var(--apple-text-secondary);
  line-height: 1.5;
  font-size: 12px;
}

.period-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  border-radius: var(--apple-radius-md);
  background: var(--apple-bg-primary);
  border: 1px solid var(--apple-border);
}

.period-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.period-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--apple-text-primary);
  line-height: 1.5;
}

.period-summary {
  color: var(--apple-text-secondary);
  line-height: 1.6;
  font-size: 12px;
}

.period-meta {
  color: var(--apple-text-secondary);
  font-size: 11px;
}

.empty-text {
  color: var(--apple-text-secondary);
  font-size: 12px;
  padding: 8px 0;
}

.memory-dialog-meta {
  color: var(--apple-text-secondary);
  font-size: 12px;
  margin-bottom: 10px;
}

.memory-dialog-content {
  margin: 0;
  max-height: 520px;
  overflow: auto;
  padding: 14px;
  border-radius: var(--apple-radius-lg);
  background: var(--apple-bg-primary);
  border: 1px solid var(--apple-border);
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
  color: var(--apple-text-primary);
}

.probe-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  line-height: 1.7;
  color: var(--apple-text-primary);
  font-size: 14px;
}

/* ============================================
   响应式
   ============================================ */
@media (max-width: 768px) {
  .sidebar-left,
  .sidebar-right {
    display: none;
  }

  .chat-main {
    width: 100%;
  }
}
</style>
