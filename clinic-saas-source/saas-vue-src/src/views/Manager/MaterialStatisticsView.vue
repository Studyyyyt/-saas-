<template>
  <div class="material-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">耗材采购</div>
        <h2>耗材统计</h2>
        <p>查看采购总额、一级分类结构、前 10 大供应商和当前低库存预警。</p>
      </div>
      <div class="hero-actions">
        <el-select v-model="filters.rangePreset" class="query-select" @change="handlePresetChange">
          <el-option label="本月" value="month" />
          <el-option label="上月" value="lastMonth" />
          <el-option label="本季" value="quarter" />
          <el-option label="本年" value="year" />
          <el-option label="自定义" value="custom" />
        </el-select>
        <el-date-picker
          v-if="filters.rangePreset === 'custom'"
          v-model="filters.customRange"
          type="daterange"
          value-format="yyyy-MM-dd"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="range-picker"
        />
        <el-button type="primary" @click="loadOverview">刷新统计</el-button>
      </div>
    </div>

    <el-alert
      v-if="!canView"
      title="当前账号仅支持查看业务列表，耗材统计页面仅 admin 可使用。"
      type="warning"
      :closable="false"
      show-icon
    />

    <template v-else>
      <el-row :gutter="14" class="summary-row">
        <el-col :xs="24" :sm="8">
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">统计区间</div>
            <div class="summary-value small">{{ overview.startDate || '-' }} ~ {{ overview.endDate || '-' }}</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">低库存条数</div>
            <div class="summary-value">{{ lowStockCount }}</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">供应商数</div>
            <div class="summary-value">{{ supplierCount }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="14">
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <div class="chart-title">最近 12 个月采购总额</div>
            <div ref="monthlyChart" class="chart-box"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <div class="chart-title">按一级分类分组</div>
            <div ref="categoryChart" class="chart-box"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="14">
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <div class="chart-title">前 10 大供应商</div>
            <div ref="supplierChart" class="chart-box"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card table-card">
            <div class="chart-title">库存预警列表</div>
            <el-table :data="overview.lowStockList || []" size="small" border max-height="320">
              <el-table-column prop="name" label="耗材名称" min-width="140" />
              <el-table-column prop="category_name" label="分类" min-width="110" />
              <el-table-column prop="current_stock" label="当前库存" width="90" align="right" />
              <el-table-column prop="min_stock_alert" label="阈值" width="80" align="right" />
              <el-table-column prop="alert_gap" label="差额" width="80" align="right" />
              <el-table-column label="操作" width="80" align="center">
                <template slot-scope="scope">
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'
import { getAdminSession } from '@/utils/adminSession'
import { canViewMaterialStatistics, formatMaterialMoney, normalizeMaterialRole } from '@/utils/materialConstants'
import { showApiError } from '@/utils/errorMessage'
export default {
  name: 'MaterialStatisticsView',
  components: {},
  data() {
    return {
      currentUser: getAdminSession() || {},
      filters: {
        rangePreset: 'month',
        customRange: []
      },
      overview: {
        monthlyTotals: [],
        categoryDistribution: [],
        supplierDistribution: [],
        lowStockList: [],
        startDate: '',
        endDate: ''
      },
      charts: {}
    }
  },
  computed: {
    canView() {
      return canViewMaterialStatistics(normalizeMaterialRole(this.currentUser && this.currentUser.role))
    },
    lowStockCount() {
      return Array.isArray(this.overview.lowStockList) ? this.overview.lowStockList.length : 0
    },
    supplierCount() {
      return Array.isArray(this.overview.supplierDistribution) ? this.overview.supplierDistribution.length : 0
    }
  },
  mounted() {
    if (this.canView) {
      this.loadOverview()
      window.addEventListener('resize', this.resizeCharts)
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    Object.keys(this.charts).forEach(key => {
      if (this.charts[key]) {
        this.charts[key].dispose()
      }
    })
  },
  methods: {
    handlePresetChange() {
      if (this.filters.rangePreset !== 'custom') {
        this.filters.customRange = []
      }
      this.loadOverview()
    },
    async loadOverview() {
      if (!this.canView) return
      const params = { rangePreset: this.filters.rangePreset }
      if (this.filters.rangePreset === 'custom') {
        params.startDate = this.filters.customRange[0] || undefined
        params.endDate = this.filters.customRange[1] || undefined
      }
      try {
        const res = await axios.get('/material-statistics/overview', { params })
        this.overview = res.data && res.data.data ? res.data.data : this.overview
        this.$nextTick(() => {
          this.renderMonthlyChart()
          this.renderCategoryChart()
          this.renderSupplierChart()
        })
      } catch (error) {
        showApiError(this, '获取耗材统计数据', error)
      }
    },
    ensureChart(refName) {
      if (!this.$refs[refName]) return null
      if (!this.charts[refName]) {
        this.charts[refName] = echarts.init(this.$refs[refName])
      }
      return this.charts[refName]
    },
    renderMonthlyChart() {
      const chart = this.ensureChart('monthlyChart')
      if (!chart) return
      const list = this.overview.monthlyTotals || []
      chart.setOption({
        color: ['#0f766e'],
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 16, top: 24, bottom: 36, containLabel: true },
        xAxis: { type: 'category', data: list.map(item => item.month || item.name) },
        yAxis: { type: 'value' },
        series: [{
          type: 'bar',
          data: list.map(item => Number(item.value || 0)),
          barMaxWidth: 34,
          itemStyle: { borderRadius: [8, 8, 0, 0] }
        }]
      })
    },
    renderCategoryChart() {
      const chart = this.ensureChart('categoryChart')
      if (!chart) return
      const list = this.overview.categoryDistribution || []
      chart.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: 0 },
        series: [{
          type: 'pie',
          radius: ['36%', '70%'],
          center: ['50%', '42%'],
          data: list.map(item => ({ name: item.name, value: Number(item.value || 0) }))
        }]
      })
    },
    renderSupplierChart() {
      const chart = this.ensureChart('supplierChart')
      if (!chart) return
      const list = this.overview.supplierDistribution || []
      chart.setOption({
        color: ['#2563eb'],
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 16, top: 24, bottom: 54, containLabel: true },
        xAxis: {
          type: 'category',
          data: list.map(item => item.name),
          axisLabel: { rotate: 24 }
        },
        yAxis: { type: 'value' },
        series: [{
          type: 'bar',
          data: list.map(item => Number(item.value || 0)),
          barMaxWidth: 34,
          itemStyle: { borderRadius: [8, 8, 0, 0] }
        }]
      })
    },
    resizeCharts() {
      Object.keys(this.charts).forEach(key => {
        if (this.charts[key]) {
          this.charts[key].resize()
        }
      })
    },
    formatMaterialMoney,
  }
}
</script>

<style scoped>
.material-page { display:flex; flex-direction:column; gap:14px; }
.hero-card { display:flex; justify-content:space-between; align-items:flex-start; gap:16px; padding:18px; border-radius:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.hero-actions { display:flex; gap:10px; flex-wrap:wrap; align-items:center; }
.query-select { width:150px; }
.range-picker { width:260px; }
.summary-row { margin:0 !important; }
.summary-card,.chart-card { border-radius:18px; }
.summary-label { color:#94a3b8; font-size:13px; }
.summary-value { margin-top:8px; font-size:26px; font-weight:700; color:#0f172a; }
.summary-value.small { font-size:18px; }
.chart-title { margin-bottom:12px; color:#0f172a; font-size:15px; font-weight:700; }
.chart-box { width:100%; height:320px; }
.table-card { height:100%; }
@media (max-width: 768px) {
  .hero-card { flex-direction:column; }
  .query-select,.range-picker { width:100%; }
}
</style>
