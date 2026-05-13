<template>
  <div class="lab-page">
    <!-- 页面头部 -->
    <el-card class="hero-card" shadow="never">
      <div class="hero-head">
        <div>
          <div class="page-kicker">义齿加工</div>
          <h2>加工订单</h2>
          <p>统一记录义齿下单、加工、收货与对账状态，支撑后续账单核对。</p>
        </div>
        <div class="hero-actions">
          <el-button v-if="isAiEnabled('lab-order-analysis')" type="primary" plain icon="el-icon-magic-stick" @click="openAiPanel">
            AI 订单分析
          </el-button>
          <el-button v-if="canEditOrders" type="primary" icon="el-icon-plus" @click="openCreateDialog">新建订单</el-button>
          <el-button v-if="canEditOrders" icon="el-icon-s-operation" :disabled="!selectedRows.length" @click="batchDialogVisible = true">批量改状态</el-button>
          <el-button icon="el-icon-refresh" @click="loadAll">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-alert
      v-if="showPendingLabTip"
      title="当前从“待登记加工”入口进入。新建订单时请优先选择“关联病历操作”，口径基于病历操作记录。"
      type="warning"
      :closable="false"
      show-icon
    />

    <!-- 指标卡片 -->
    <div class="summary-grid">
      <div class="summary-card total">
        <div class="summary-icon"><i class="el-icon-s-order"></i></div>
        <div class="summary-body">
          <div class="summary-value">{{ overview.total_count || 0 }}</div>
          <div class="summary-label">订单总数</div>
        </div>
      </div>
      <div class="summary-card amount">
        <div class="summary-icon"><i class="el-icon-money"></i></div>
        <div class="summary-body">
          <div class="summary-value">¥{{ formatMoney(overview.total_amount) }}</div>
          <div class="summary-label">订单总金额</div>
        </div>
      </div>
      <div class="summary-card created">
        <div class="summary-icon"><i class="el-icon-document-add"></i></div>
        <div class="summary-body">
          <div class="summary-value">{{ statusCount('已下单') }}</div>
          <div class="summary-label">已下单</div>
        </div>
      </div>
      <div class="summary-card processing">
        <div class="summary-icon"><i class="el-icon-loading"></i></div>
        <div class="summary-body">
          <div class="summary-value">{{ statusCount('加工中') }}</div>
          <div class="summary-label">加工中</div>
        </div>
      </div>
      <div class="summary-card done">
        <div class="summary-icon"><i class="el-icon-circle-check"></i></div>
        <div class="summary-body">
          <div class="summary-value">{{ statusCount('已完成') + statusCount('已收货') + statusCount('已对账') }}</div>
          <div class="summary-label">已完成/收货/对账</div>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">订单状态分布</div>
        <div ref="statusChart" class="chart-box"></div>
      </el-card>
      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">月度下单趋势</div>
        <div ref="trendChart" class="chart-box"></div>
      </el-card>
    </div>

    <!-- 加工厂排行 -->
    <el-card class="panel-card" shadow="never">
      <div slot="header" class="panel-title">加工厂订单排行</div>
      <el-table :data="factoryBreakdown" stripe size="small" :header-cell-style="tableHeaderStyle">
        <el-table-column type="index" label="排名" width="70" align="center" />
        <el-table-column prop="factory_name" label="加工厂" min-width="160" />
        <el-table-column prop="count" label="订单数" width="100" align="center" />
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.amount) }}</template>
        </el-table-column>
        <el-table-column label="占比" width="120" align="center">
          <template slot-scope="scope">
            <el-progress :percentage="factoryPercent(scope.row.amount)" :show-text="true" :stroke-width="10" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 查询筛选 -->
    <el-card class="query-card" shadow="never">
      <div class="query-row">
        <el-select v-model="filters.rangePreset" class="query-select" @change="handleRangePresetChange">
          <el-option label="本月" value="month" />
          <el-option label="本周" value="week" />
          <el-option label="今天" value="today" />
          <el-option label="本季" value="quarter" />
          <el-option label="自定义" value="custom" />
        </el-select>
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          value-format="yyyy-MM-dd"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="range-picker"
          :disabled="filters.rangePreset !== 'custom'"
        />
        <el-select v-model="filters.factoryId" clearable filterable class="query-select" placeholder="加工厂">
          <el-option v-for="item in factories" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select v-model="filters.status" clearable class="query-select" placeholder="订单状态">
          <el-option v-for="item in LAB_ORDER_STATUS_OPTIONS" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select
          v-model="filters.patientId"
          remote
          filterable
          clearable
          reserve-keyword
          class="query-select wide"
          placeholder="筛选患者"
          :remote-method="searchPatients"
          :loading="patientLoading"
        >
          <el-option v-for="item in patientOptions" :key="item.id" :label="formatPatientLabel(item)" :value="item.id" />
        </el-select>
      </div>
      <div class="query-row second-row">
        <el-input
          v-model="filters.keyword"
          class="query-input"
          clearable
          placeholder="搜索患者姓名 / 手机号"
          @keyup.enter.native="loadAll"
        />
        <el-button type="primary" icon="el-icon-search" @click="loadAll">查询</el-button>
        <el-button icon="el-icon-refresh-left" @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <!-- 历史记录表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="rows"
        stripe
        v-loading="loading"
        size="small"
        @selection-change="handleSelectionChange"
        :header-cell-style="tableHeaderStyle"
        :expand-row-keys="expandedRowKeys"
        row-key="id"
      >
        <el-table-column v-if="canEditOrders" type="selection" width="48" />
        <el-table-column type="expand" width="40">
          <template slot-scope="scope">
            <div class="expand-row">
              <div class="expand-item"><span>项目：</span>{{ scope.row.project_name || '-' }}</div>
              <div class="expand-item"><span>操作：</span>{{ scope.row.operation_name || '-' }}</div>
              <div class="expand-item"><span>牙位：</span>{{ scope.row.tooth_positions || '-' }}</div>
              <div class="expand-item"><span>备注：</span>{{ scope.row.remark || '-' }}</div>
              <div class="expand-item"><span>录入人：</span>{{ scope.row.created_by_name || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="factory_name" label="加工厂" min-width="140" />
        <el-table-column prop="patient_name" label="患者" min-width="110" />
        <el-table-column label="关联操作" min-width="170" show-overflow-tooltip>
          <template slot-scope="scope">
            {{ [scope.row.project_name, scope.row.operation_name, scope.row.tooth_positions ? `牙位:${scope.row.tooth_positions}` : ''].filter(Boolean).join('｜') || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="product_name" label="产品" min-width="130" />
        <el-table-column prop="product_spec" label="规格" min-width="110" />
        <el-table-column label="单价" width="90" align="right">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.unit_price) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="70" align="center" />
        <el-table-column label="总金额" width="100" align="right">
          <template slot-scope="scope"><b>¥{{ formatMoney(scope.row.total_amount) }}</b></template>
        </el-table-column>
        <el-table-column prop="order_date" label="下单日期" width="105">
          <template slot-scope="scope">{{ formatDate(scope.row.order_date) }}</template>
        </el-table-column>
        <el-table-column prop="expected_delivery_date" label="预计完成" width="105">
          <template slot-scope="scope">{{ formatDate(scope.row.expected_delivery_date) || '-' }}</template>
        </el-table-column>
        <el-table-column prop="actual_delivery_date" label="实际收货" width="105">
          <template slot-scope="scope">{{ formatDate(scope.row.actual_delivery_date) || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="orderStatusTagType(scope.row.status)" effect="light">
              {{ scope.row.status || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="openDetailDialog(scope.row)">详情</el-button>
            <el-button v-if="canEditOrders" type="text" size="mini" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button v-if="canEditOrders && scope.row.status !== '已对账'" type="text" size="mini" class="danger-link" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !rows.length" description="暂无加工订单"></el-empty>

      <div v-else class="pagination-row">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="totalItems"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <LabOrderDialog
      :visible.sync="dialogVisible"
      :mode="dialogMode"
      :order="activeOrder"
      :factories="factories"
      :current-user="currentUser"
      @saved="handleDialogSaved"
    />

    <el-dialog title="批量更新状态" :visible.sync="batchDialogVisible" width="420px" append-to-body>
      <el-form label-width="110px">
        <el-form-item label="目标状态">
          <el-select v-model="batchForm.status" style="width:100%">
            <el-option v-for="item in batchStatusOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="实际收货日期">
          <el-date-picker v-model="batchForm.actual_delivery_date" type="date" value-format="yyyy-MM-dd" style="width:100%" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSaving" @click="submitBatchStatus">确认</el-button>
      </span>
    </el-dialog>

    <!-- AI 侧边浮层面板 -->
    <transition name="ai-panel-slide">
      <div v-if="aiPanelVisible" class="ai-panel-overlay" @click.self="closeAiPanel">
        <div class="ai-panel">
          <div class="ai-panel-head">
            <div class="ai-panel-title">
              <i class="el-icon-magic-stick"></i>
              AI 订单分析
            </div>
            <button class="ai-panel-close" @click="closeAiPanel">
              <i class="el-icon-close"></i>
            </button>
          </div>
          <div class="ai-panel-body">
            <div class="ai-section">
              <div class="ai-section-title"><i class="el-icon-s-data"></i> 订单异常检测</div>
              <div class="ai-section-content">
                <p class="ai-placeholder">识别超期未收货、状态停滞过久的异常订单</p>
                <div class="ai-coming-soon">功能即将上线，敬请期待</div>
              </div>
            </div>
            <div class="ai-section">
              <div class="ai-section-title"><i class="el-icon-s-opportunity"></i> 加工厂效能对比</div>
              <div class="ai-section-content">
                <p class="ai-placeholder">基于交货周期、返工率对比各加工厂效能</p>
                <div class="ai-coming-soon">功能即将上线，敬请期待</div>
              </div>
            </div>
            <div class="ai-section">
              <div class="ai-section-title"><i class="el-icon-s-marketing"></i> 采购需求预测</div>
              <div class="ai-section-content">
                <p class="ai-placeholder">基于历史订单预测未来义齿加工需求</p>
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
import { getAdminSession } from '@/utils/adminSession'
import LabOrderDialog from '@/components/LabOrderDialog.vue'
import { showApiError } from '@/utils/errorMessage'
import { isAiEnabled as checkAiEnabled } from '@/utils/aiConfig'
import {
  LAB_MANUAL_ORDER_STATUS_OPTIONS,
  LAB_ORDER_STATUS_OPTIONS,
  canEditLabOrders,
  formatMoney,
  normalizeLabRole,
  orderStatusTagType
} from '@/utils/labConstants'

function defaultBatchForm() {
  return {
    status: '加工中',
    actual_delivery_date: ''
  }
}

const STATUS_COLORS = {
  '已下单': '#3b82f6',
  '加工中': '#f59e0b',
  '已完成': '#10b981',
  '已收货': '#6366f1',
  '已对账': '#8b5cf6'
}

export default {
  name: 'LabOrderView',
  components: { LabOrderDialog },
  data() {
    return {
      LAB_ORDER_STATUS_OPTIONS,
      currentUser: getAdminSession() || {},
      loading: false,
      rows: [],
      totalItems: 0,
      currentPage: 1,
      pageSize: 10,
      factories: [],
      patientOptions: [],
      patientLoading: false,
      filters: {
        factoryId: '',
        status: '',
        patientId: '',
        keyword: '',
        dateRange: [],
        rangePreset: 'month'
      },
      dialogVisible: false,
      dialogMode: 'create',
      activeOrder: {},
      selectedRows: [],
      batchDialogVisible: false,
      batchForm: defaultBatchForm(),
      batchSaving: false,
      overview: {},
      aiPanelVisible: false,
      statusChart: null,
      trendChart: null,
      expandedRowKeys: []
    }
  },
  computed: {
    canEditOrders() {
      return canEditLabOrders(normalizeLabRole(this.currentUser && this.currentUser.role))
    },
    showPendingLabTip() {
      return String((this.$route.query && this.$route.query.pendingLab) || '') === '1'
    },
    batchStatusOptions() {
      return LAB_MANUAL_ORDER_STATUS_OPTIONS.filter(item => item !== '已下单')
    },
    factoryBreakdown() {
      return (this.overview && this.overview.factory_breakdown) || []
    }
  },
  mounted() {
    this.initializePage()
    this.statusChart = echarts.init(this.$refs.statusChart)
    this.trendChart = echarts.init(this.$refs.trendChart)
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.statusChart) this.statusChart.dispose()
    if (this.trendChart) this.trendChart.dispose()
  },
  methods: {
    handleResize() {
      if (this.statusChart) this.statusChart.resize()
      if (this.trendChart) this.trendChart.resize()
    },
    async initializePage() {
      this.applyRoutePrefill()
      await this.loadFactories()
      this.handleRangePresetChange(this.filters.rangePreset)
      this.autoOpenCreateFromRoute()
    },
    applyRoutePrefill() {
      const query = this.$route.query || {}
      const patientId = Number(query.patientId || 0)
      const patientName = String(query.patientName || '').trim()
      if (patientId > 0) {
        this.filters.patientId = patientId
        if (patientName) {
          this.patientOptions = [{ id: patientId, name: patientName, phone: '' }]
        }
      }
    },
    autoOpenCreateFromRoute() {
      const query = this.$route.query || {}
      if (!this.canEditOrders || String(query.openCreate || '') !== '1') {
        return
      }
      const patientId = Number(query.patientId || 0)
      const medicalRecordId = Number(query.medicalRecordId || 0)
      const medicalRecordOperationId = Number(query.medicalRecordOperationId || 0)
      const patientName = String(query.patientName || '').trim()
      const prefill = {
        patient_id: patientId > 0 ? patientId : null,
        patient_name: patientName || '',
        medical_record_id: medicalRecordId > 0 ? medicalRecordId : null,
        medical_record_operation_id: medicalRecordOperationId > 0 ? medicalRecordOperationId : null
      }
      this.openCreateDialog(prefill)
      this.$router.replace({ path: this.$route.path, query: { pendingLab: query.pendingLab || '', patientId: patientId > 0 ? patientId : undefined, patientName: patientName || undefined } }).catch(() => {})
    },
    handleRangePresetChange(value) {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const today = `${year}-${month}-${day}`
      if (value === 'today') {
        this.filters.dateRange = [today, today]
      } else if (value === 'week') {
        const monday = new Date(now)
        const d = now.getDay() || 7
        monday.setDate(now.getDate() - d + 1)
        const mMonth = String(monday.getMonth() + 1).padStart(2, '0')
        const mDay = String(monday.getDate()).padStart(2, '0')
        this.filters.dateRange = [`${monday.getFullYear()}-${mMonth}-${mDay}`, today]
      } else if (value === 'month') {
        this.filters.dateRange = [`${year}-${month}-01`, today]
      } else if (value === 'quarter') {
        const quarterMonth = Math.floor(now.getMonth() / 3) * 3 + 1
        this.filters.dateRange = [`${year}-${String(quarterMonth).padStart(2, '0')}-01`, today]
      } else {
        this.filters.dateRange = []
      }
      if (value !== 'custom') {
        this.loadAll()
      }
    },
    formatMoney,
    orderStatusTagType,
    formatDate(value) {
      return value ? String(value).slice(0, 10) : ''
    },
    formatPatientLabel(item) {
      const phone = item && item.phone ? ` / ${item.phone}` : ''
      return `${item.name || '未命名患者'}${phone}`
    },
    statusCount(status) {
      const breakdown = (this.overview && this.overview.status_breakdown) || []
      const found = breakdown.find(item => item.status === status)
      return found ? found.count : 0
    },
    factoryPercent(amount) {
      const total = Number(this.overview.total_amount || 0)
      if (!total) return 0
      const p = Math.round((Number(amount || 0) / total) * 100)
      return Math.min(p, 100)
    },
    tableHeaderStyle() {
      return { backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }
    },
    async loadFactories() {
      try {
        const res = await axios.get('/lab-factories/selectEnabled')
        this.factories = Array.isArray(res.data.data) ? res.data.data : []
      } catch (error) {
        this.factories = []
      }
    },
    async loadAll() {
      this.loading = true
      const range = Array.isArray(this.filters.dateRange) ? this.filters.dateRange : []
      const params = {
        factoryId: this.filters.factoryId || undefined,
        status: this.filters.status || undefined,
        patientId: this.filters.patientId || undefined,
        keyword: this.filters.keyword || undefined,
        startDate: range[0] || undefined,
        endDate: range[1] || undefined
      }
      try {
        const [listRes, overviewRes] = await Promise.all([
          axios.get('/lab-orders/search', { params: Object.assign({ page: this.currentPage, size: this.pageSize }, params) }),
          axios.get('/lab-orders/dashboard/overview', { params })
        ])
        const data = listRes.data && listRes.data.data ? listRes.data.data : {}
        this.rows = Array.isArray(data.list) ? data.list : []
        this.totalItems = Number(data.total || 0)
        this.overview = (overviewRes.data && overviewRes.data.data) || {}
        this.renderCharts()
      } catch (error) {
        this.rows = []
        this.totalItems = 0
        showApiError(this, '获取技工订单', error)
      } finally {
        this.loading = false
      }
    },
    renderCharts() {
      // 状态分布饼图
      const statusData = (this.overview.status_breakdown || []).map(r => ({
        name: r.status,
        value: r.count,
        itemStyle: { color: STATUS_COLORS[r.status] || '#94a3b8' }
      }))
      this.statusChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c}单 ({d}%)' },
        legend: { bottom: 0, icon: 'circle', textStyle: { color: '#64748b' } },
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          label: { show: true, formatter: '{b}\n{d}%', color: '#475569' },
          emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
          data: statusData
        }]
      })
      this.statusChart.off('click')
      this.statusChart.on('click', params => {
        this.filters.status = params.name
        this.currentPage = 1
        this.loadAll()
      })

      // 月度趋势柱状图
      const trendData = (this.overview.monthly_trend || [])
      this.trendChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 50, right: 20, top: 30, bottom: 30 },
        xAxis: { type: 'category', data: trendData.map(r => r.month), axisLine: { lineStyle: { color: '#e2e8f0' } }, axisLabel: { color: '#64748b' } },
        yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f1f5f9' } }, axisLabel: { color: '#64748b', formatter: v => '¥' + v } },
        series: [{
          type: 'bar',
          barWidth: 28,
          itemStyle: { borderRadius: [6, 6, 0, 0], color: '#6366f1' },
          data: trendData.map(r => Number(r.amount || 0))
        }]
      })
    },
    resetFilters() {
      this.filters = { factoryId: '', status: '', patientId: '', keyword: '', dateRange: [], rangePreset: 'month' }
      this.currentPage = 1
      this.patientOptions = []
      this.handleRangePresetChange('month')
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadAll()
    },
    handleCurrentChange(page) {
      this.currentPage = page
      this.loadAll()
    },
    handleSelectionChange(rows) {
      this.selectedRows = Array.isArray(rows) ? rows.map(item => item.id) : []
    },
    async searchPatients(query) {
      const keyword = String(query || '').trim()
      if (!keyword) {
        this.patientOptions = []
        return
      }
      this.patientLoading = true
      try {
        const res = await axios.get('/patients/search', { params: { keyword, page: 1, size: 20 } })
        const data = res.data && res.data.data ? res.data.data : {}
        this.patientOptions = Array.isArray(data.list) ? data.list : []
      } catch (error) {
        this.patientOptions = []
      } finally {
        this.patientLoading = false
      }
    },
    openCreateDialog(prefill = {}) {
      this.dialogMode = 'create'
      this.activeOrder = Object.assign({}, prefill)
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.dialogMode = 'edit'
      this.activeOrder = Object.assign({}, row)
      this.dialogVisible = true
    },
    openDetailDialog(row) {
      this.dialogMode = 'detail'
      this.activeOrder = Object.assign({}, row)
      this.dialogVisible = true
    },
    handleDialogSaved() {
      this.loadAll()
    },
    handleDelete(row) {
      this.$confirm(`确认删除订单“${row.patient_name} / ${row.product_name}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.delete(`/lab-orders/delete/${row.id}`)
        if (res.data.code === '200') {
          this.$message.success('删除成功')
          this.loadAll()
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      }).catch(() => {})
    },
    async submitBatchStatus() {
      if (!this.selectedRows.length) {
        this.$message.warning('请先选择订单')
        return
      }
      if (!this.batchForm.status) {
        this.$message.warning('请选择目标状态')
        return
      }
      this.batchSaving = true
      try {
        const res = await axios.post('/lab-orders/batchStatus', {
          ids: this.selectedRows,
          status: this.batchForm.status,
          actual_delivery_date: this.batchForm.actual_delivery_date || ''
        })
        if (res.data.code === '200') {
          this.$message.success('批量更新成功')
          this.batchDialogVisible = false
          this.batchForm = defaultBatchForm()
          this.selectedRows = []
          this.loadAll()
        } else {
          this.$message.error(res.data.msg || '批量更新失败')
        }
      } catch (error) {
        this.$message.error('批量更新失败')
      } finally {
        this.batchSaving = false
      }
    },
    openAiPanel() {
      this.aiPanelVisible = true
    },
    closeAiPanel() {
      this.aiPanelVisible = false
    },
    isAiEnabled(key) {
      return checkAiEnabled(key)
    }
  }
}
</script>

<style scoped>
.lab-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-card {
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

.query-card,
.table-card,
.panel-card {
  border-radius: 20px;
}

.query-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.query-row.second-row {
  margin-top: 10px;
}

.query-input {
  width: 260px;
}

.query-select {
  width: 180px;
}

.query-select.wide {
  width: 260px;
}

.range-picker {
  width: 280px;
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

.summary-card.total .summary-icon { background: #eff6ff; color: #2563eb; }
.summary-card.amount .summary-icon { background: #f0fdf4; color: #16a34a; }
.summary-card.created .summary-icon { background: #fff7ed; color: #ea580c; }
.summary-card.processing .summary-icon { background: #fef3c7; color: #d97706; }
.summary-card.done .summary-icon { background: #f5f3ff; color: #7c3aed; }

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

/* 图表区 */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.chart-box {
  width: 100%;
  height: 280px;
}

.panel-title {
  font-weight: 600;
  color: #0f172a;
  font-size: 15px;
}

/* 表格 */
.expand-row {
  display: flex;
  gap: 24px;
  padding: 8px 24px;
  background: #f8fafc;
  border-radius: 8px;
  margin: 4px 0;
  flex-wrap: wrap;
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
  padding-top: 16px;
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

@media (max-width: 1080px) {
  .summary-grid,
  .chart-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .query-input,
  .query-select,
  .query-select.wide,
  .range-picker {
    width: 100%;
  }

  .pagination-row {
    justify-content: center;
  }
}
</style>
