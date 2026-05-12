<template>
  <div class="ad-page">
    <!-- 页面头部 -->
    <el-card class="hero-card" shadow="never">
      <div class="hero-head">
        <div>
          <div class="page-kicker">市场投放</div>
          <h2>广告投放</h2>
          <p>统一录入平台投放，自动同步财务支出，并在同页查看平台级 ROI。</p>
        </div>
        <div class="hero-actions">
          <el-button type="primary" plain icon="el-icon-magic-stick" @click="openAiPanel">
            AI 投放分析
          </el-button>
          <el-button v-if="canCreate" type="primary" icon="el-icon-plus" @click="openCreateDialog">新增投放</el-button>
          <el-button icon="el-icon-refresh" @click="loadAll">刷新</el-button>
        </div>
      </div>
    </el-card>

    <!-- 查询筛选 -->
    <el-card class="query-card" shadow="never">
      <div class="query-row">
        <el-select v-model="filters.platform" clearable placeholder="投放平台" class="query-select">
          <el-option v-for="item in platformOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-input v-model="filters.keyword" clearable class="query-input" placeholder="活动名称 / 备注" @keyup.enter.native="loadAll" />
        <el-select v-model="filters.rangePreset" class="query-select" @change="handleRangePresetChange">
          <el-option label="本月" value="month" />
          <el-option label="本周" value="week" />
          <el-option label="今天" value="today" />
          <el-option label="本季" value="quarter" />
          <el-option label="自定义" value="custom" />
        </el-select>
        <el-date-picker
          v-model="filters.startDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="开始日期"
          class="query-select"
          :disabled="filters.rangePreset !== 'custom'"
        />
        <el-date-picker
          v-model="filters.endDate"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="结束日期"
          class="query-select"
          :disabled="filters.rangePreset !== 'custom'"
        />
        <el-button type="primary" icon="el-icon-search" @click="loadAll">查询</el-button>
        <el-button icon="el-icon-refresh-left" @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <!-- 指标卡片 -->
    <div class="summary-grid">
      <div class="summary-card spend">
        <div class="summary-icon"><i class="el-icon-money"></i></div>
        <div class="summary-body">
          <div class="summary-value">¥{{ formatMoney(overview.total_spend_amount) }}</div>
          <div class="summary-label">投放金额</div>
          <div class="summary-sub">共 {{ overview.record_count || 0 }} 条记录</div>
        </div>
      </div>
      <div class="summary-card platform">
        <div class="summary-icon"><i class="el-icon-s-grid"></i></div>
        <div class="summary-body">
          <div class="summary-value">{{ platformShare.length }}</div>
          <div class="summary-label">投放平台</div>
          <div class="summary-sub">已统计平台级支出占比</div>
        </div>
      </div>
      <div class="summary-card roi" :class="{ 'roi-good': totalRoi >= 1, 'roi-bad': totalRoi > 0 && totalRoi < 1 }">
        <div class="summary-icon"><i class="el-icon-data-line"></i></div>
        <div class="summary-body">
          <div class="summary-value">{{ formatRoi(totalRoi) }}</div>
          <div class="summary-label">总 ROI</div>
          <div class="summary-sub">成交金额 / 投放金额</div>
        </div>
      </div>
      <div class="summary-card revenue">
        <div class="summary-icon"><i class="el-icon-coin"></i></div>
        <div class="summary-body">
          <div class="summary-value">¥{{ formatMoney(overview.total_deal_amount) }}</div>
          <div class="summary-label">成交回报</div>
          <div class="summary-sub">{{ overview.total_deal_count || 0 }} 人成交</div>
        </div>
      </div>
      <div class="summary-card mine">
        <div class="summary-icon"><i class="el-icon-user"></i></div>
        <div class="summary-body">
          <div class="summary-value">{{ myRecordCount }}</div>
          <div class="summary-label">我的录入</div>
          <div class="summary-sub">护士账号默认只看本人列表</div>
        </div>
      </div>
    </div>

    <!-- 图表区：趋势 + 占比 -->
    <div class="chart-grid">
      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">月度投放趋势</div>
        <div ref="trendChart" class="chart-box"></div>
      </el-card>
      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">平台投放占比</div>
        <div ref="pieChart" class="chart-box"></div>
      </el-card>
    </div>

    <!-- 漏斗 + ROI -->
    <div class="chart-grid">
      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">投放转化漏斗</div>
        <div ref="funnelChart" class="chart-box"></div>
      </el-card>
      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">平台 ROI 排行</div>
        <el-table :data="platformRoi" stripe size="small" :header-cell-style="tableHeaderStyle">
          <el-table-column prop="platform" label="平台" min-width="100">
            <template slot-scope="scope">
              <el-tag size="mini" :style="platformTagStyle(scope.row.platform)">{{ scope.row.platform }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="spend_amount" label="投放金额" min-width="120" align="right" sortable>
            <template slot-scope="scope">¥{{ formatMoney(scope.row.spend_amount) }}</template>
          </el-table-column>
          <el-table-column prop="consultation_count" label="咨询" width="80" align="center" sortable />
          <el-table-column prop="arrived_count" label="到店" width="80" align="center" sortable />
          <el-table-column prop="deal_count" label="成交" width="80" align="center" sortable />
          <el-table-column prop="deal_amount" label="成交回报" min-width="120" align="right" sortable>
            <template slot-scope="scope">¥{{ formatMoney(scope.row.deal_amount) }}</template>
          </el-table-column>
          <el-table-column prop="roi_ratio" label="ROI" width="100" align="center" sortable>
            <template slot-scope="scope">
              <span :class="roiTextClass(scope.row.roi_ratio)">
                <i v-if="isTopRoi(scope.row)" class="el-icon-medal-1" style="color:#f59e0b"></i>
                {{ formatRoi(scope.row.roi_ratio) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 历史记录 -->
    <el-card class="panel-card" shadow="never">
      <div slot="header" class="panel-title">
        <span>历史记录</span>
        <el-tag v-if="filters.platform" size="mini" closable @close="clearPlatformFilter" style="margin-left:8px">
          {{ filters.platform }}
        </el-tag>
      </div>
      <el-table
        :data="pagedRows"
        stripe
        v-loading="loading"
        size="small"
        :header-cell-style="tableHeaderStyle"
        :expand-row-keys="expandedRowKeys"
        row-key="id"
      >
        <el-table-column type="expand" width="40">
          <template slot-scope="scope">
            <div class="expand-row">
              <div class="expand-item"><span>目标人群：</span>{{ scope.row.target_audience || '-' }}</div>
              <div class="expand-item"><span>创建时间：</span>{{ scope.row.created_at || '-' }}</div>
              <div class="expand-item"><span>更新时间：</span>{{ scope.row.updated_at || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="platform" label="平台" min-width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :style="platformTagStyle(scope.row.platform)">{{ scope.row.platform }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="campaign_name" label="活动名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="投放周期" min-width="170">
          <template slot-scope="scope">{{ scope.row.start_date || '-' }} 至 {{ scope.row.end_date || '-' }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="110" align="right">
          <template slot-scope="scope"><b>¥{{ formatMoney(scope.row.amount) }}</b></template>
        </el-table-column>
        <el-table-column prop="target_project" label="目标项目" min-width="110" />
        <el-table-column prop="created_by_name" label="录入人" min-width="100" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="isAdmin" type="text" size="mini" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button v-if="isAdmin" type="text" size="mini" class="danger-link" @click="removeRow(scope.row)">删除</el-button>
            <span v-if="!isAdmin" class="readonly-text">仅查看</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-row">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalItems"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogMode === 'edit' ? '编辑投放' : '新增投放'" :visible.sync="dialogVisible" width="560px" append-to-body>
      <el-form :model="form" label-width="96px">
        <el-form-item label="投放平台">
          <el-select v-model="form.platform" style="width:100%">
            <el-option v-for="item in platformOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动名称">
          <el-input v-model="form.campaign_name" maxlength="100" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.start_date" type="date" value-format="yyyy-MM-dd" placeholder="开始日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.end_date" type="date" value-format="yyyy-MM-dd" placeholder="结束日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="投放金额">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="100" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="目标项目">
          <el-input v-model="form.target_project" maxlength="50" />
        </el-form-item>
        <el-form-item label="目标人群">
          <el-input v-model="form.target_audience" maxlength="100" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">{{ dialogMode === 'edit' ? '保存' : '新增' }}</el-button>
      </span>
    </el-dialog>

    <!-- AI 侧边浮层面板 -->
    <transition name="ai-panel-slide">
      <div v-if="aiPanelVisible" class="ai-panel-overlay" @click.self="closeAiPanel">
        <div class="ai-panel">
          <div class="ai-panel-head">
            <div class="ai-panel-title">
              <i class="el-icon-magic-stick"></i>
              AI 投放分析
            </div>
            <button class="ai-panel-close" @click="closeAiPanel">
              <i class="el-icon-close"></i>
            </button>
          </div>
          <div class="ai-panel-body">
            <div class="ai-section">
              <div class="ai-section-title"><i class="el-icon-s-finance"></i> 智能预算建议</div>
              <div class="ai-section-content">
                <p class="ai-placeholder">基于历史 ROI 推荐下月各平台预算分配</p>
                <div class="ai-coming-soon">功能即将上线，敬请期待</div>
              </div>
            </div>
            <div class="ai-section">
              <div class="ai-section-title"><i class="el-icon-warning-outline"></i> 投放效果诊断</div>
              <div class="ai-section-content">
                <p class="ai-placeholder">识别低 ROI 平台和异常波动</p>
                <div class="ai-coming-soon">功能即将上线，敬请期待</div>
              </div>
            </div>
            <div class="ai-section">
              <div class="ai-section-title"><i class="el-icon-s-marketing"></i> 趋势预测</div>
              <div class="ai-section-content">
                <p class="ai-placeholder">基于历史数据预测未来投放趋势</p>
                <div class="ai-coming-soon">功能即将上线，敬请期待</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'
import { ADVERTISING_PLATFORM_OPTIONS } from '@/utils/consultationOptions'
import { getAdminSession } from '@/utils/adminSession'
import { showApiError } from '@/utils/errorMessage'

const PLATFORM_COLORS = {
  '抖音': '#1a1a1a',
  '小红书': '#ff2442',
  '大众点评': '#ff6633',
  '美团': '#ffc300',
  '微信': '#07c160',
  '百度': '#2932e1',
  '快手': '#ff5000',
  '其他': '#94a3b8'
}

function createFilters() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const today = `${now.getFullYear()}-${month}-${day}`
  return {
    platform: '',
    keyword: '',
    startDate: `${now.getFullYear()}-${month}-01`,
    endDate: today,
    rangePreset: 'month'
  }
}

function createEmptyForm() {
  return {
    id: null,
    platform: '',
    campaign_name: '',
    start_date: '',
    end_date: '',
    amount: null,
    target_project: '',
    target_audience: '',
    remark: ''
  }
}

export default {
  name: 'AdvertisingSpendingView',
  data() {
    return {
      currentUser: getAdminSession() || {},
      filters: createFilters(),
      form: createEmptyForm(),
      dialogMode: 'create',
      dialogVisible: false,
      saving: false,
      loading: false,
      rows: [],
      overview: {},
      platformOptions: ADVERTISING_PLATFORM_OPTIONS,
      currentPage: 1,
      pageSize: 20,
      expandedRowKeys: [],
      aiPanelVisible: false,
      trendChart: null,
      pieChart: null,
      funnelChart: null
    }
  },
  computed: {
    normalizedRole() {
      const role = String((this.currentUser && this.currentUser.role) || '').trim()
      if (role === '管理员' || role === 'admin') return 'admin'
      if (role === '医生' || role === 'doctor') return 'doctor'
      if (role === '护士' || role === 'nurse') return 'nurse'
      return role
    },
    isAdmin() {
      return this.normalizedRole === 'admin'
    },
    canCreate() {
      return this.normalizedRole === 'admin' || this.normalizedRole === 'nurse'
    },
    platformShare() {
      return (this.overview && this.overview.platform_share) || []
    },
    platformRoi() {
      return (this.overview && this.overview.platform_roi) || []
    },
    trendRows() {
      return (this.overview && this.overview.trend) || []
    },
    totalRoi() {
      return Number(this.overview && this.overview.total_roi_ratio) || 0
    },
    myRecordCount() {
      const currentUserId = Number(this.currentUser && this.currentUser.id)
      return (this.rows || []).filter(item => Number(item.created_by || 0) === currentUserId).length
    },
    totalItems() {
      return this.rows.length
    },
    pagedRows() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.rows.slice(start, start + this.pageSize)
    }
  },
  mounted() {
    this.currentUser = getAdminSession() || {}
    this.trendChart = echarts.init(this.$refs.trendChart)
    this.pieChart = echarts.init(this.$refs.pieChart)
    this.funnelChart = echarts.init(this.$refs.funnelChart)
    this.loadAll()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.trendChart) this.trendChart.dispose()
    if (this.pieChart) this.pieChart.dispose()
    if (this.funnelChart) this.funnelChart.dispose()
  },
  methods: {
    handleResize() {
      if (this.trendChart) this.trendChart.resize()
      if (this.pieChart) this.pieChart.resize()
      if (this.funnelChart) this.funnelChart.resize()
    },
    handleRangePresetChange(value) {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const today = `${year}-${month}-${day}`
      if (value === 'today') {
        this.filters.startDate = today
        this.filters.endDate = today
      } else if (value === 'week') {
        const monday = new Date(now)
        const d = now.getDay() || 7
        monday.setDate(now.getDate() - d + 1)
        this.filters.startDate = `${monday.getFullYear()}-${String(monday.getMonth() + 1).padStart(2, '0')}-${String(monday.getDate()).padStart(2, '0')}`
        this.filters.endDate = today
      } else if (value === 'month') {
        this.filters.startDate = `${year}-${month}-01`
        this.filters.endDate = today
      } else if (value === 'quarter') {
        const quarterMonth = Math.floor(now.getMonth() / 3) * 3 + 1
        this.filters.startDate = `${year}-${String(quarterMonth).padStart(2, '0')}-01`
        this.filters.endDate = today
      }
      if (value !== 'custom') {
        this.loadAll()
      }
    },
    buildQueryParams() {
      const params = {
        platform: this.filters.platform || undefined,
        keyword: String(this.filters.keyword || '').trim() || undefined,
        startDate: this.filters.startDate || undefined,
        endDate: this.filters.endDate || undefined
      }
      if (this.normalizedRole === 'nurse' && Number(this.currentUser && this.currentUser.id) > 0) {
        params.createdBy = Number(this.currentUser.id)
      }
      return params
    },
    async loadAll() {
      this.loading = true
      try {
        const params = this.buildQueryParams()
        const [listRes, overviewRes] = await Promise.all([
          axios.get('/advertising-spending/search', { params: Object.assign({ page: 1, size: 500 }, params) }),
          axios.get('/advertising-spending/dashboard/overview', { params })
        ])
        const pageData = listRes.data && listRes.data.data ? listRes.data.data : {}
        this.rows = Array.isArray(pageData.list) ? pageData.list : []
        this.overview = (overviewRes.data && overviewRes.data.data) || {}
        this.currentPage = 1
        this.renderCharts()
      } catch (error) {
        showApiError(this, '加载广告投放数据', error)
      } finally {
        this.loading = false
      }
    },
    renderCharts() {
      // 月度趋势柱状图
      const trendData = this.trendRows
      this.trendChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 50, right: 20, top: 30, bottom: 30 },
        xAxis: { type: 'category', data: trendData.map(r => r.month), axisLine: { lineStyle: { color: '#e2e8f0' } }, axisLabel: { color: '#64748b' } },
        yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f1f5f9' } }, axisLabel: { color: '#64748b', formatter: v => '¥' + v } },
        series: [{
          type: 'bar',
          barWidth: 32,
          itemStyle: { borderRadius: [6, 6, 0, 0], color: '#3b82f6' },
          data: trendData.map(r => Number(r.amount || 0))
        }]
      })

      // 平台占比环形图
      const shareData = this.platformShare.map(r => ({
        name: r.platform,
        value: Number(r.amount || 0),
        itemStyle: { color: PLATFORM_COLORS[r.platform] || '#94a3b8' }
      }))
      this.pieChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
        legend: { bottom: 0, icon: 'circle', textStyle: { color: '#64748b' } },
        series: [{
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          label: { show: true, formatter: '{b}\n{d}%', color: '#475569' },
          emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
          data: shareData
        }]
      })
      this.pieChart.off('click')
      this.pieChart.on('click', params => {
        this.filters.platform = params.name
        this.loadAll()
      })

      // 漏斗图
      const funnel = (this.overview && this.overview.funnel) || []
      this.funnelChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c}人' },
        series: [{
          type: 'funnel',
          left: '10%', top: 20, bottom: 20, width: '80%',
          min: 0,
          max: Math.max(1, ...(funnel.map(r => Number(r.value || 0)))),
          minSize: '0%',
          maxSize: '100%',
          sort: 'descending',
          gap: 2,
          label: { show: true, position: 'inside', formatter: '{b}\n{c}人', color: '#fff', fontWeight: 'bold' },
          itemStyle: { borderColor: '#fff', borderWidth: 1 },
          emphasis: { label: { fontSize: 14 } },
          data: funnel.map((r, i) => ({
            name: r.name,
            value: Number(r.value || 0),
            itemStyle: { color: ['#3b82f6', '#10b981', '#f97316'][i] || '#94a3b8' }
          }))
        }]
      })
    },
    resetFilters() {
      this.filters = createFilters()
      this.loadAll()
    },
    clearPlatformFilter() {
      this.filters.platform = ''
      this.loadAll()
    },
    openCreateDialog() {
      if (!this.canCreate) {
        this.$message.warning('当前账号仅可查看投放记录')
        return
      }
      this.dialogMode = 'create'
      this.form = createEmptyForm()
      this.dialogVisible = true
    },
    openEditDialog(row) {
      if (!this.isAdmin) {
        this.$message.warning('只有管理员可以编辑投放记录')
        return
      }
      this.dialogMode = 'edit'
      this.form = Object.assign(createEmptyForm(), row, {
        amount: Number(row.amount || 0)
      })
      this.dialogVisible = true
    },
    validateForm() {
      if (!this.form.platform) return '投放平台必填'
      if (!this.form.start_date || !this.form.end_date) return '投放开始和结束日期必填'
      if (String(this.form.end_date).trim() < String(this.form.start_date).trim()) return '结束日期不能早于开始日期'
      if (!Number.isFinite(Number(this.form.amount)) || Number(this.form.amount) <= 0) return '投放金额必须大于0'
      return ''
    },
    buildPayload() {
      const currentUserId = Number(this.currentUser && this.currentUser.id)
      const currentUserName = String((this.currentUser && this.currentUser.name) || '').trim()
      return {
        id: this.form.id,
        platform: this.form.platform,
        campaign_name: String(this.form.campaign_name || '').trim(),
        start_date: this.form.start_date,
        end_date: this.form.end_date,
        amount: Number(this.form.amount || 0),
        target_project: String(this.form.target_project || '').trim(),
        target_audience: String(this.form.target_audience || '').trim(),
        remark: String(this.form.remark || '').trim(),
        created_by: Number.isFinite(currentUserId) && currentUserId > 0 ? currentUserId : null,
        created_by_name: currentUserName
      }
    },
    async submit() {
      const validationMessage = this.validateForm()
      if (validationMessage) {
        this.$message.warning(validationMessage)
        return
      }
      this.saving = true
      try {
        const payload = this.buildPayload()
        const response = this.dialogMode === 'edit'
          ? await axios.put('/advertising-spending/edit', payload)
          : await axios.post('/advertising-spending/add', payload)
        if (!response.data || response.data.code !== '200') {
          this.$message.error((response.data && response.data.msg) || '保存失败')
          return
        }
        this.$message.success(this.dialogMode === 'edit' ? '投放记录已更新' : '投放记录已新增')
        this.dialogVisible = false
        this.loadAll()
      } catch (error) {
        const message = error && error.response && error.response.data && error.response.data.msg
          ? error.response.data.msg
          : '保存失败'
        this.$message.error(message)
      } finally {
        this.saving = false
      }
    },
    removeRow(row) {
      if (!this.isAdmin) {
        this.$message.warning('只有管理员可以删除投放记录')
        return
      }
      this.$confirm('删除后会同步移除对应财务支出记录，是否继续？', '删除投放记录', {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }).then(async () => {
        const response = await axios.delete(`/advertising-spending/delete/${row.id}`)
        if (!response.data || response.data.code !== '200') {
          this.$message.error((response.data && response.data.msg) || '删除失败')
          return
        }
        this.$message.success('投放记录已删除')
        this.loadAll()
      }).catch(() => {})
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    handleCurrentChange(page) {
      this.currentPage = page
    },
    formatMoney(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    formatPercent(value) {
      const amount = Number(value || 0)
      return `${amount.toFixed(2).replace(/\.00$/, '')}%`
    },
    formatRoi(value) {
      const v = Number(value || 0)
      return Number.isFinite(v) ? v.toFixed(2) : '0.00'
    },
    roiTextClass(value) {
      const v = Number(value || 0)
      if (v >= 1) return 'roi-good-text'
      if (v > 0) return 'roi-bad-text'
      return ''
    },
    isTopRoi(row) {
      if (!this.platformRoi.length) return false
      return this.platformRoi[0] === row
    },
    platformTagStyle(platform) {
      const color = PLATFORM_COLORS[platform] || '#94a3b8'
      return {
        backgroundColor: color + '15',
        color: color,
        borderColor: color + '40',
        borderWidth: '1px',
        borderStyle: 'solid'
      }
    },
    tableHeaderStyle() {
      return { backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }
    },
    openAiPanel() {
      this.aiPanelVisible = true
    },
    closeAiPanel() {
      this.aiPanelVisible = false
    }
  }
}
</script>

<style scoped>
.ad-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-card,
.query-card,
.panel-card {
  border-radius: 20px;
}

.hero-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.hero-actions {
  display: flex;
  gap: 10px;
}

.page-kicker {
  color: #2563eb;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-head h2 {
  margin: 8px 0 6px;
  font-size: 28px;
  color: #0f172a;
}

.hero-head p {
  margin: 0;
  color: #64748b;
}

.query-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.query-input {
  width: 220px;
}

.query-select {
  width: 160px;
}

/* 指标卡片 */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid rgba(148, 163, 184, 0.18);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

.summary-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.summary-card.spend .summary-icon { background: #eff6ff; color: #2563eb; }
.summary-card.platform .summary-icon { background: #f0fdf4; color: #16a34a; }
.summary-card.roi .summary-icon { background: #fef3c7; color: #d97706; }
.summary-card.revenue .summary-icon { background: #f5f3ff; color: #7c3aed; }
.summary-card.mine .summary-icon { background: #fff7ed; color: #ea580c; }

.summary-card.roi-good { border-color: #86efac; }
.summary-card.roi-bad { border-color: #fca5a5; }

.summary-body {
  min-width: 0;
}

.summary-value {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
}

.summary-label {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.summary-sub {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
}

/* 图表区 */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.chart-box {
  width: 100%;
  height: 300px;
}

.panel-title {
  font-weight: 600;
  color: #0f172a;
  font-size: 15px;
}

/* ROI 表格 */
.roi-good-text {
  color: #16a34a;
  font-weight: 600;
}

.roi-bad-text {
  color: #dc2626;
  font-weight: 600;
}

/* 历史记录 */
.expand-row {
  display: flex;
  gap: 24px;
  padding: 8px 24px;
  background: #f8fafc;
  border-radius: 8px;
  margin: 4px 0;
}

.expand-item {
  font-size: 13px;
  color: #475569;
}

.expand-item span {
  color: #94a3b8;
  margin-right: 4px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.readonly-text {
  color: #94a3b8;
  font-size: 12px;
}

.danger-link {
  color: #ef4444;
}

/* AI 面板 */
.ai-panel-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  justify-content: flex-end;
}

.ai-panel {
  width: 420px;
  max-width: 90vw;
  height: 100vh;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.12);
}

.ai-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
}

.ai-panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  display: flex;
  align-items: center;
  gap: 6px;
}

.ai-panel-title i {
  color: #a855f7;
  font-size: 18px;
}

.ai-panel-close {
  width: 32px;
  height: 32px;
  border: 0;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-panel-close:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.ai-panel-body {
  flex: 1;
  overflow: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ai-section {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  border: 1px solid #e2e8f0;
}

.ai-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
}

.ai-section-title i {
  color: #a855f7;
}

.ai-placeholder {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 10px;
}

.ai-coming-soon {
  font-size: 12px;
  color: #94a3b8;
  background: #f8fafc;
  padding: 8px 12px;
  border-radius: 8px;
  text-align: center;
}

/* 动画 */
.ai-panel-slide-enter-active,
.ai-panel-slide-leave-active {
  transition: all 0.25s ease;
}

.ai-panel-slide-enter,
.ai-panel-slide-leave-to {
  opacity: 0;
}

.ai-panel-slide-enter .ai-panel,
.ai-panel-slide-leave-to .ai-panel {
  transform: translateX(100%);
}

/* 响应式 */
@media (max-width: 1080px) {
  .summary-grid,
  .chart-grid {
    grid-template-columns: 1fr;
  }

  .query-input,
  .query-select {
    width: 100%;
  }
}
</style>
