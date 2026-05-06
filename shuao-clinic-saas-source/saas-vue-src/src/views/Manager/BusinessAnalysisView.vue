<template>
  <div class="analysis-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-main">
        <div>
          <div class="hero-kicker">AI 经营日报</div>
          <h2>每日自动分析门诊经营状态</h2>
          <p>系统每天自动汇总预约、病历、治疗与收费数据，并结合大模型输出经营结论、风险预警和行动建议。</p>
        </div>
        <div class="hero-actions">
          <el-date-picker
            v-model="runDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="选择分析日期"
            size="small"
          />
          <el-button icon="el-icon-connection" :loading="probeLoading" @click="probeModel">
            测试模型连接
          </el-button>
          <el-button type="success" icon="el-icon-data-analysis" :loading="todayLoading" @click="runTodayAnalysis">
            立即生成今日日报
          </el-button>
          <el-button type="warning" icon="el-icon-document" :loading="weeklyLoading" @click="runWeeklyReport">
            生成本周周报
          </el-button>
          <el-button type="warning" plain icon="el-icon-tickets" :loading="monthlyLoading" @click="runMonthlyReport">
            生成本月月报
          </el-button>
          <el-button type="danger" plain icon="el-icon-warning-outline" :loading="alertLoading" @click="scanAlerts">
            扫描异常波动
          </el-button>
          <el-button type="primary" icon="el-icon-magic-stick" :loading="runLoading" @click="runAnalysis">
            补跑指定日期
          </el-button>
        </div>
      </div>
      <div v-if="taskNotices.length" class="task-notice-group">
        <div v-for="item in taskNotices" :key="item.key" class="task-notice">
          <div class="task-notice__head">
            <strong>{{ item.label }}</strong>
            <el-tag :type="statusTagType(item.status)" size="mini">{{ item.status }}</el-tag>
          </div>
          <div class="task-notice__text">{{ item.message }}</div>
        </div>
      </div>
    </el-card>

    <el-alert
      v-if="probeResult"
      :title="probeResult.connected ? '模型连接测试成功' : '模型连接测试失败'"
      :type="probeResult.connected ? 'success' : 'warning'"
      :closable="false"
      class="probe-alert"
    >
      <div class="probe-body">
        <div><strong>模型：</strong>{{ probeResult.model || '-' }}</div>
        <div><strong>接口：</strong>{{ probeResult.base_url || '-' }}/responses</div>
        <div><strong>结果：</strong>{{ probeResult.message || '-' }}</div>
        <div v-if="probeResult.response_sample"><strong>返回样例：</strong>{{ probeResult.response_sample }}</div>
        <div><strong>检测时间：</strong>{{ probeResult.checked_at || '-' }}</div>
      </div>
    </el-alert>

    <el-card class="panel-card chat-card" shadow="never">
      <div slot="header" class="panel-title panel-title-row">
        <span>AI 财务分析对话</span>
        <div class="panel-title-actions">
          <span class="panel-title-tip">30 分钟无交流会自动总结到长期记忆；下次新会话会先读取长期记忆。</span>
          <el-button size="mini" plain @click="openMemoryDialog" :loading="memoryLoading">查看长期记忆</el-button>
          <el-button size="mini" plain @click="refreshChatSession" :loading="chatLoading">刷新会话</el-button>
        </div>
      </div>
      <div class="chat-meta-row">
        <el-tag size="mini" :type="chatRestarted ? 'warning' : 'success'">{{ chatRestarted ? '新会话已启动' : '当前会话进行中' }}</el-tag>
        <span class="chat-meta-text">会话ID：{{ chatSessionId || '-' }}</span>
        <span class="chat-meta-text">最后交流：{{ chatLastActivityAt || '-' }}</span>
      </div>
      <div v-if="chatMemoryPreview" class="memory-preview">
        <div class="memory-preview__head">
          <strong>长期记忆摘录</strong>
          <span>{{ chatMemoryUpdatedAt || '未写入' }}</span>
        </div>
        <pre>{{ chatMemoryPreview }}</pre>
      </div>
      <div ref="chatMessageList" class="chat-message-list" v-loading="chatLoading">
        <div v-for="(item, index) in chatMessages" :key="`chat-${index}-${item.created_at}`" class="chat-message" :class="`chat-message--${item.role}`">
          <div class="chat-message__head">
            <strong>{{ item.role === 'user' ? '你' : 'AI助手' }}</strong>
            <span>{{ item.created_at || '-' }}</span>
          </div>
          <div class="chat-message__body">{{ item.content }}</div>
        </div>
        <el-empty v-if="!chatLoading && !chatMessages.length" description="暂无对话"></el-empty>
      </div>
      <div class="chat-input-row">
        <el-input
          v-model="chatInput"
          type="textarea"
          :rows="3"
          resize="none"
          placeholder="例如：本月非耗材支出上升说明什么？接下来我应该重点盯哪三件事？"
          @keyup.ctrl.enter.native="sendChatMessage"
        />
        <div class="chat-actions">
          <span class="chat-actions__tip">`Ctrl + Enter` 发送</span>
          <el-button type="primary" :loading="chatSending" @click="sendChatMessage">发送</el-button>
        </div>
      </div>
    </el-card>

    <el-dialog title="长期记忆文档" :visible.sync="memoryDialogVisible" width="760px">
      <div class="memory-dialog-meta">
        <span>最近更新时间：{{ chatMemoryUpdatedAt || '未写入' }}</span>
      </div>
      <pre class="memory-dialog-content">{{ chatMemoryContent || '暂无长期记忆内容' }}</pre>
      <span slot="footer">
        <el-button @click="memoryDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <div class="period-grid">
      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">最新周报</div>
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
      </el-card>

      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">最新月报</div>
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
      </el-card>
    </div>

    <el-card class="panel-card weekly-meeting-card" shadow="never">
      <div slot="header" class="panel-title panel-title-row">
        <span>周会内容</span>
        <div class="panel-title-actions">
          <span class="panel-title-tip">基于最新周报自动整理</span>
          <el-button
            v-if="weeklyMeetingContent"
            type="primary"
            plain
            size="mini"
            icon="el-icon-document-copy"
            @click="copyWeeklyMeetingContent"
          >
            复制周会内容
          </el-button>
        </div>
      </div>
      <div v-if="weeklyMeetingContent" class="meeting-wrap">
        <div class="meeting-hero">
          <div class="meeting-kicker">建议用时 10-15 分钟</div>
          <div class="meeting-title">{{ weeklyMeetingContent.theme }}</div>
          <div class="meeting-summary">{{ weeklyMeetingContent.opening }}</div>
        </div>

        <div class="meeting-grid">
          <div class="meeting-section">
            <div class="meeting-section__title">本周先向员工沟通</div>
            <div
              v-for="(item, index) in weeklyMeetingContent.communicatePoints"
              :key="`meeting-talk-${index}`"
              class="meeting-bullet"
            >
              <span class="dot blue"></span>
              <span>{{ item }}</span>
            </div>
          </div>

          <div class="meeting-section">
            <div class="meeting-section__title">本周必须落实</div>
            <div
              v-for="(item, index) in weeklyMeetingContent.executionItems"
              :key="`meeting-action-${index}`"
              class="meeting-action-item"
            >
              <div class="meeting-action-top">
                <el-tag size="mini" :type="priorityTagType(item.priority)">{{ item.priority }}</el-tag>
                <span class="meeting-action-owner">{{ item.owner }}</span>
                <span class="meeting-action-due">{{ item.due }}</span>
              </div>
              <div class="meeting-action-title">{{ item.action }}</div>
              <div class="meeting-action-result">{{ item.expected_result }}</div>
            </div>
          </div>
        </div>

        <div class="meeting-grid meeting-grid-secondary">
          <div class="meeting-section">
            <div class="meeting-section__title">周会现场要追问</div>
            <div
              v-for="(item, index) in weeklyMeetingContent.checkQuestions"
              :key="`meeting-check-${index}`"
              class="meeting-bullet"
            >
              <span class="dot orange"></span>
              <span>{{ item }}</span>
            </div>
          </div>

          <div class="meeting-section">
            <div class="meeting-section__title">会后要求</div>
            <div
              v-for="(item, index) in weeklyMeetingContent.closingRequirements"
              :key="`meeting-close-${index}`"
              class="meeting-bullet"
            >
              <span class="dot green"></span>
              <span>{{ item }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-text">请先生成本周周报，系统会自动整理周会沟通与落实内容。</div>
    </el-card>

    <div v-if="latest" class="top-grid">
      <el-card class="summary-card" shadow="never">
        <div class="summary-head">
          <div>
            <div class="summary-date">{{ latest.analysis_date || '-' }}</div>
            <div class="summary-title">{{ latest.headline || '暂无经营分析结果' }}</div>
          </div>
          <div class="summary-tags">
            <el-tag :type="statusTagType(latest.analysis_status)" size="small">{{ latest.analysis_status || 'UNKNOWN' }}</el-tag>
            <el-tag size="small" effect="plain">{{ latest.source_type || '-' }}</el-tag>
          </div>
        </div>
        <div class="score-row">
          <div class="score-card">
            <div class="score-label">经营评分</div>
            <div class="score-value">{{ latest.operating_score ?? '--' }}</div>
          </div>
          <div class="score-card">
            <div class="score-label">趋势</div>
            <div class="score-value" :class="trendClass(latest.trend)">{{ trendLabel(latest.trend) }}</div>
          </div>
          <div class="score-card">
            <div class="score-label">触发方式</div>
            <div class="score-value score-text">{{ latest.trigger_type || '-' }}</div>
          </div>
          <div class="score-card">
            <div class="score-label">分析模型</div>
            <div class="score-value score-text">{{ latest.model_name || '-' }}</div>
          </div>
        </div>
        <div class="summary-text">{{ latest.summary || '暂无摘要' }}</div>
        <div v-if="latest.error_message" class="summary-error">
          {{ latest.error_message }}
        </div>
      </el-card>

      <el-card class="metric-card" shadow="never">
        <div slot="header" class="panel-title">核心经营指标</div>
        <div class="metric-grid">
          <div class="metric-item">
            <span>当日预约</span>
            <strong>{{ metricValue('today_appointments') }}</strong>
          </div>
          <div class="metric-item">
            <span>病历数</span>
            <strong>{{ metricValue('today_medical_records') }}</strong>
          </div>
          <div class="metric-item">
            <span>治疗数</span>
            <strong>{{ metricValue('today_treatments') }}</strong>
          </div>
          <div class="metric-item">
            <span>未来7日预约</span>
            <strong>{{ metricValue('future_7_day_appointments') }}</strong>
          </div>
          <div class="metric-item">
            <span>当日收入</span>
            <strong>¥{{ moneyValue('today_income') }}</strong>
          </div>
          <div class="metric-item">
            <span>净现金流</span>
            <strong :class="{ danger: Number(metricValue('today_net_income', 0)) < 0 }">¥{{ moneyValue('today_net_income') }}</strong>
          </div>
        </div>
      </el-card>
    </div>

    <el-row :gutter="14" v-if="analysisData">
      <el-col :span="12">
        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-title">关键亮点</div>
          <div v-if="analysisData.highlights && analysisData.highlights.length" class="bullet-list">
            <div v-for="(item, index) in analysisData.highlights" :key="`h-${index}`" class="bullet-item">
              <span class="dot blue"></span>
              <span>{{ item }}</span>
            </div>
          </div>
          <div v-else class="empty-text">暂无亮点分析</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-title">管理简报</div>
          <div class="brief-text">{{ analysisData.management_brief || '暂无管理简报' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14" v-if="analysisData">
      <el-col :span="12">
        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-title">风险预警</div>
          <div v-if="analysisData.risks && analysisData.risks.length" class="stack-list">
            <div v-for="(item, index) in analysisData.risks" :key="`r-${index}`" class="stack-item danger">
              <div class="stack-head">
                <strong>{{ item.title }}</strong>
                <el-tag size="mini" :type="severityTagType(item.severity)">{{ item.severity }}</el-tag>
              </div>
              <div class="stack-line">{{ item.finding }}</div>
              <div class="stack-line action">{{ item.recommendation }}</div>
            </div>
          </div>
          <div v-else class="empty-text">暂无风险预警</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-title">增长机会</div>
          <div v-if="analysisData.opportunities && analysisData.opportunities.length" class="stack-list">
            <div v-for="(item, index) in analysisData.opportunities" :key="`o-${index}`" class="stack-item success">
              <div class="stack-head">
                <strong>{{ item.title }}</strong>
                <el-tag size="mini" :type="impactTagType(item.impact)">{{ item.impact }}</el-tag>
              </div>
              <div class="stack-line">{{ item.finding }}</div>
              <div class="stack-line action">{{ item.recommendation }}</div>
            </div>
          </div>
          <div v-else class="empty-text">暂无机会分析</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="never" v-if="analysisData">
      <div slot="header" class="panel-title">建议动作</div>
      <div v-if="analysisData.actions && analysisData.actions.length" class="action-grid">
        <div v-for="(item, index) in analysisData.actions" :key="`a-${index}`" class="action-item">
          <div class="action-top">
            <el-tag size="mini" :type="priorityTagType(item.priority)">{{ item.priority }}</el-tag>
            <span class="action-owner">{{ item.owner }}</span>
            <span class="action-due">{{ item.due }}</span>
          </div>
          <div class="action-title">{{ item.action }}</div>
          <div class="action-result">{{ item.expected_result }}</div>
        </div>
      </div>
      <div v-else class="empty-text">暂无行动建议</div>
    </el-card>

    <el-card class="panel-card" shadow="never">
      <div slot="header" class="panel-title">最近日报记录</div>
      <el-table :data="history" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="analysis_date" label="分析日期" width="120" />
        <el-table-column prop="headline" label="标题" min-width="260" show-overflow-tooltip />
        <el-table-column prop="operating_score" label="评分" width="90" />
        <el-table-column prop="trend" label="趋势" width="90">
          <template slot-scope="scope">{{ trendLabel(scope.row.trend) }}</template>
        </el-table-column>
        <el-table-column prop="analysis_status" label="状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="statusTagType(scope.row.analysis_status)" size="mini">{{ scope.row.analysis_status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source_type" label="来源" width="110" />
        <el-table-column prop="updated_at" label="更新时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="loadDetail(scope.row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="panel-card" shadow="never">
      <div slot="header" class="panel-title">最近异常波动告警</div>
      <el-table :data="alerts" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="alert_date" label="日期" width="110" />
        <el-table-column prop="alert_title" label="告警标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="alert_level" label="级别" width="100">
          <template slot-scope="scope">
            <el-tag :type="alertLevelTagType(scope.row.alert_level)" size="mini">{{ scope.row.alert_level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="metric_name" label="指标" width="150" />
        <el-table-column label="当前/基线" min-width="180">
          <template slot-scope="scope">
            {{ scope.row.current_value ?? '-' }} / {{ scope.row.baseline_value ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="变动幅度" width="120">
          <template slot-scope="scope">
            {{ scope.row.change_rate ?? '-' }}%
          </template>
        </el-table-column>
        <el-table-column prop="alert_message" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="suggested_action" label="建议动作" min-width="220" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'
import { showApiError } from '@/utils/errorMessage'

export default {
  name: 'BusinessAnalysisView',
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
      weeklyLatest: null,
      monthlyLatest: null,
      alerts: [],
      dailyTask: null,
      weeklyTask: null,
      monthlyTask: null,
      pollTimers: {},
      chatLoading: false,
      chatSending: false,
      chatSessionId: '',
      chatMessages: [],
      chatInput: '',
      chatRestarted: false,
      chatLastActivityAt: '',
      chatMemoryPreview: '',
      chatMemoryUpdatedAt: '',
      memoryLoading: false,
      memoryDialogVisible: false,
      chatMemoryContent: ''
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
        communicatePoints.push(`本周机会：${item.title}。团队要围绕“${item.finding}”推进，具体做法是：${item.recommendation}`)
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
      this.chatLoading = true
      axios.get('/business-analysis/chat/session', { params: this.chatAccountParams() }).then(res => {
        if (res.data.code === '200') {
          this.applyChatPayload(res.data.data || {})
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
      this.chatSending = true
      axios.post('/business-analysis/chat/message', {
        account_id: this.currentUser && this.currentUser.id ? Number(this.currentUser.id) : null,
        account_name: this.currentUser && this.currentUser.name ? this.currentUser.name : '',
        session_id: this.chatSessionId || '',
        message
      }).then(res => {
        if (res.data.code === '200') {
          this.chatInput = ''
          this.applyChatPayload(res.data.data || {})
        } else {
          this.$message.error(res.data.msg || '发送失败')
        }
      }).catch(() => {
        this.$message.error('发送失败')
      }).finally(() => {
        this.chatSending = false
      })
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
.analysis-page { display:flex; flex-direction:column; gap:14px; }
.hero-card, .panel-card, .summary-card, .metric-card { border-radius:18px; }
.hero-main { display:flex; justify-content:space-between; align-items:flex-start; gap:20px; }
.hero-kicker { color:#3b82f6; font-size:13px; font-weight:600; }
.hero-main h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-main p { margin:0; color:#64748b; line-height:1.7; max-width:760px; }
.hero-actions { display:flex; gap:10px; align-items:center; flex-wrap:wrap; justify-content:flex-end; }
.task-notice-group { margin-top:14px; display:flex; gap:10px; flex-wrap:wrap; }
.task-notice { min-width:220px; padding:12px 14px; border-radius:14px; background:#f8fafc; border:1px solid #dbeafe; }
.task-notice__head { display:flex; align-items:center; justify-content:space-between; gap:10px; color:#0f172a; }
.task-notice__text { margin-top:8px; color:#475569; line-height:1.6; font-size:13px; }
.probe-alert { border-radius:16px; }
.probe-body { display:flex; flex-direction:column; gap:6px; line-height:1.7; color:#334155; }
.chat-card { overflow:hidden; }
.chat-meta-row { display:flex; gap:10px; align-items:center; flex-wrap:wrap; margin-bottom:12px; }
.chat-meta-text { color:#64748b; font-size:12px; }
.memory-preview { margin-bottom:12px; padding:12px 14px; border-radius:14px; border:1px solid #dbeafe; background:#f8fafc; }
.memory-preview__head { display:flex; justify-content:space-between; gap:12px; align-items:center; color:#334155; font-size:12px; }
.memory-preview pre { margin:10px 0 0; white-space:pre-wrap; word-break:break-word; line-height:1.7; color:#475569; max-height:180px; overflow:auto; }
.memory-dialog-meta { color:#64748b; font-size:12px; margin-bottom:10px; }
.memory-dialog-content { margin:0; max-height:520px; overflow:auto; padding:14px; border-radius:14px; background:#f8fafc; border:1px solid #e2e8f0; white-space:pre-wrap; word-break:break-word; line-height:1.8; color:#334155; }
.chat-message-list { max-height:360px; overflow:auto; display:flex; flex-direction:column; gap:12px; padding-right:4px; }
.chat-message { padding:12px 14px; border-radius:16px; border:1px solid #e2e8f0; background:#f8fafc; }
.chat-message--user { background:#eff6ff; border-color:#bfdbfe; }
.chat-message--assistant { background:#f8fafc; border-color:#e2e8f0; }
.chat-message__head { display:flex; justify-content:space-between; gap:12px; color:#64748b; font-size:12px; }
.chat-message__body { margin-top:8px; color:#0f172a; line-height:1.8; white-space:pre-wrap; word-break:break-word; }
.chat-input-row { margin-top:14px; display:flex; flex-direction:column; gap:10px; }
.chat-actions { display:flex; justify-content:space-between; align-items:center; gap:12px; }
.chat-actions__tip { color:#94a3b8; font-size:12px; }
.period-grid { display:grid; grid-template-columns:1fr 1fr; gap:14px; }
.panel-title-row { display:flex; align-items:center; justify-content:space-between; gap:12px; }
.panel-title-actions { display:flex; align-items:center; gap:10px; flex-wrap:wrap; }
.panel-title-tip { color:#64748b; font-size:12px; font-weight:500; }
.period-card { display:flex; flex-direction:column; gap:10px; }
.period-head { display:flex; justify-content:space-between; align-items:center; gap:8px; }
.period-title { font-size:16px; font-weight:700; color:#0f172a; line-height:1.6; }
.period-summary { color:#475569; line-height:1.8; }
.period-meta { color:#64748b; font-size:12px; }
.weekly-meeting-card { overflow:hidden; }
.meeting-wrap { display:flex; flex-direction:column; gap:14px; }
.meeting-hero {
  padding:18px 20px;
  border-radius:16px;
  background:linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
  border:1px solid #dbeafe;
}
.meeting-kicker { color:#2563eb; font-size:12px; font-weight:700; letter-spacing:.04em; }
.meeting-title { margin-top:8px; color:#0f172a; font-size:20px; font-weight:800; line-height:1.5; }
.meeting-summary { margin-top:10px; color:#334155; line-height:1.9; white-space:pre-wrap; }
.meeting-grid { display:grid; grid-template-columns:1.1fr 1fr; gap:14px; }
.meeting-grid-secondary { grid-template-columns:1fr 1fr; }
.meeting-section {
  padding:16px 18px;
  border-radius:16px;
  border:1px solid #e2e8f0;
  background:#f8fafc;
  display:flex;
  flex-direction:column;
  gap:12px;
}
.meeting-section__title { color:#0f172a; font-size:15px; font-weight:800; }
.meeting-bullet { display:flex; align-items:flex-start; gap:10px; color:#334155; line-height:1.8; }
.dot.orange { background:#f59e0b; }
.dot.green { background:#16a34a; }
.meeting-action-item {
  padding:12px 14px;
  border-radius:14px;
  border:1px solid #dbeafe;
  background:#fff;
}
.meeting-action-top { display:flex; align-items:center; gap:8px; color:#64748b; font-size:12px; flex-wrap:wrap; }
.meeting-action-owner { font-weight:700; color:#334155; }
.meeting-action-due { margin-left:auto; }
.meeting-action-title { margin-top:10px; color:#0f172a; font-weight:700; line-height:1.7; }
.meeting-action-result { margin-top:8px; color:#475569; line-height:1.7; }
.top-grid { display:grid; grid-template-columns:1.6fr 1fr; gap:14px; }
.summary-head { display:flex; justify-content:space-between; align-items:flex-start; gap:12px; }
.summary-date { color:#64748b; font-size:13px; }
.summary-title { margin-top:6px; font-size:22px; line-height:1.4; color:#0f172a; font-weight:800; }
.summary-tags { display:flex; gap:8px; flex-wrap:wrap; }
.score-row { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:10px; margin-top:18px; }
.score-card { padding:14px; background:#f8fafc; border-radius:14px; border:1px solid #e2e8f0; }
.score-label { color:#64748b; font-size:12px; }
.score-value { margin-top:8px; font-size:24px; font-weight:800; color:#0f172a; }
.score-value.score-text { font-size:14px; font-weight:700; word-break:break-word; }
.trend-up { color:#16a34a; }
.trend-down { color:#dc2626; }
.trend-flat { color:#2563eb; }
.summary-text { margin-top:16px; color:#334155; line-height:1.8; }
.summary-error { margin-top:12px; color:#b45309; background:#fff7ed; border:1px solid #fed7aa; border-radius:12px; padding:12px 14px; line-height:1.6; }
.panel-title { font-weight:700; color:#0f172a; }
.metric-grid { display:grid; grid-template-columns:repeat(2, minmax(0,1fr)); gap:10px; }
.metric-item { padding:14px; border-radius:14px; background:#f8fafc; border:1px solid #e2e8f0; display:flex; flex-direction:column; gap:8px; }
.metric-item span { color:#64748b; font-size:12px; }
.metric-item strong { color:#0f172a; font-size:22px; font-weight:800; }
.metric-item strong.danger { color:#dc2626; }
.bullet-list, .stack-list { display:flex; flex-direction:column; gap:10px; }
.bullet-item { display:flex; align-items:flex-start; gap:10px; color:#334155; line-height:1.7; }
.dot { width:10px; height:10px; border-radius:999px; display:inline-block; margin-top:8px; flex-shrink:0; }
.dot.blue { background:#3b82f6; }
.brief-text { color:#334155; line-height:1.9; white-space:pre-wrap; }
.stack-item { border-radius:16px; padding:14px; border:1px solid #e2e8f0; background:#f8fafc; }
.stack-item.danger { background:#fff7f7; border-color:#fecaca; }
.stack-item.success { background:#f3fff7; border-color:#bbf7d0; }
.stack-head { display:flex; justify-content:space-between; align-items:center; gap:8px; }
.stack-line { margin-top:8px; color:#475569; line-height:1.7; }
.stack-line.action { color:#0f172a; font-weight:600; }
.action-grid { display:grid; grid-template-columns:repeat(3, minmax(0,1fr)); gap:12px; }
.action-item { padding:14px; border-radius:16px; background:#f8fafc; border:1px solid #e2e8f0; }
.action-top { display:flex; align-items:center; gap:8px; color:#64748b; font-size:12px; flex-wrap:wrap; }
.action-owner { font-weight:600; color:#334155; }
.action-due { margin-left:auto; }
.action-title { margin-top:10px; color:#0f172a; font-weight:700; line-height:1.7; }
.action-result { margin-top:10px; color:#475569; line-height:1.7; }
.empty-text { color:#94a3b8; padding:10px 0; }
@media (max-width: 1100px) {
  .hero-main, .summary-head { flex-direction:column; }
  .hero-actions { width:100%; flex-wrap:wrap; }
  .top-grid, .action-grid, .period-grid, .meeting-grid, .meeting-grid-secondary { grid-template-columns:1fr; }
  .score-row { grid-template-columns:repeat(2, minmax(0,1fr)); }
  .panel-title-row { align-items:flex-start; }
  .meeting-action-due { margin-left:0; }
  .chat-actions { flex-direction:column; align-items:flex-start; }
}
</style>
