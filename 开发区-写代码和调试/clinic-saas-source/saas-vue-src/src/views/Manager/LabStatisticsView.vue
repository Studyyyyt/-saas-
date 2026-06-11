<template>
  <div class="lab-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">义齿加工</div>
        <h2>加工费统计</h2>
        <p>查看最近 12 个月加工费趋势、加工厂分布和产品结构。</p>
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
      v-if="!canViewStatistics"
      title="当前账号仅支持查看业务列表，统计页面仅 admin 可使用。"
      type="warning"
      :closable="false"
      show-icon
    />

    <template v-else>
      <el-row :gutter="14" class="summary-row">
        <el-col :xs="24" :sm="8">
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">统计月份范围</div>
            <div class="summary-value small">{{ overview.startMonth || '-' }} ~ {{ overview.endMonth || '-' }}</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">本页加工厂数</div>
            <div class="summary-value">{{ factoryCount }}</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card shadow="never" class="summary-card">
            <div class="summary-label">产品类型数</div>
            <div class="summary-value">{{ productCount }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="14">
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <div class="chart-title">最近 12 个月加工费总额</div>
            <div ref="monthlyChart" class="chart-box"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <div class="chart-title">按加工厂分组</div>
            <div ref="factoryChart" class="chart-box"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="14">
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <div class="chart-title">按产品类型分组</div>
            <div ref="productChart" class="chart-box"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card table-card">
            <div class="chart-title">加工厂详细对比</div>
            <el-table :data="overview.factoryComparison || []" size="small" border max-height="320">
              <el-table-column prop="factory_name" label="加工厂" min-width="140" />
              <el-table-column prop="order_count" label="订单数" width="90" align="right" />
              <el-table-column label="加工费" width="110" align="right">
                <template slot-scope="scope">¥{{ formatMoney(scope.row.total_amount) }}</template>
              </el-table-column>
              <el-table-column label="平均单价" width="110" align="right">
                <template slot-scope="scope">¥{{ formatMoney(scope.row.average_unit_price) }}</template>
              </el-table-column>
              <el-table-column label="退修率" width="90" align="center">
                <template slot-scope="scope">{{ scope.row.repair_rate == null ? '-' : scope.row.repair_rate }}</template>
              </el-table-column>
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
import { canViewLabStatistics, formatMoney, normalizeLabRole } from '@/utils/labConstants'
import { showApiError } from '@/utils/errorMessage'
export default {
  name: 'LabStatisticsView',
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
        factoryDistribution: [],
        productDistribution: [],
        factoryComparison: [],
        startMonth: '',
        endMonth: ''
      },
      chartInstances: {}
    }
  },
  computed: {
    canViewStatistics() {
      return canViewLabStatistics(normalizeLabRole(this.currentUser && this.currentUser.role))
    },
    factoryCount() {
      return Array.isArray(this.overview.factoryDistribution) ? this.overview.factoryDistribution.length : 0
    },
    productCount() {
      return Array.isArray(this.overview.productDistribution) ? this.overview.productDistribution.length : 0
    }
  },
  mounted() {
    if (this.canViewStatistics) {
      this.loadOverview()
      window.addEventListener('resize', this.resizeCharts)
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    Object.keys(this.chartInstances).forEach(key => {
      if (this.chartInstances[key]) {
        this.chartInstances[key].dispose()
      }
    })
  },
  methods: {
    formatMoney,
    handlePresetChange() {
      if (this.filters.rangePreset !== 'custom') {
        this.filters.customRange = []
      }
      this.loadOverview()
    },
    async loadOverview() {
      if (!this.canViewStatistics) return
      const params = { rangePreset: this.filters.rangePreset }
      if (this.filters.rangePreset === 'custom' && Array.isArray(this.filters.customRange)) {
        params.startDate = this.filters.customRange[0] || undefined
        params.endDate = this.filters.customRange[1] || undefined
      }
      try {
        const res = await axios.get('/lab-statistics/overview', { params })
        this.overview = res.data && res.data.data ? res.data.data : this.overview
        this.$nextTick(() => {
          this.renderMonthlyChart()
          this.renderFactoryChart()
          this.renderProductChart()
        })
      } catch (error) {
        showApiError(this, '获取技工统计数据', error)
      }
    },
    ensureChart(refName) {
      if (!this.$refs[refName]) return null
      if (!this.chartInstances[refName]) {
        this.chartInstances[refName] = echarts.init(this.$refs[refName])
      }
      return this.chartInstances[refName]
    },
    renderMonthlyChart() {
      const chart = this.ensureChart('monthlyChart')
      if (!chart) return
      const list = Array.isArray(this.overview.monthlyTotals) ? this.overview.monthlyTotals : []
      chart.setOption({
        color: ['#5A8F7B'],
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
    renderFactoryChart() {
      const chart = this.ensureChart('factoryChart')
      if (!chart) return
      const list = Array.isArray(this.overview.factoryDistribution) ? this.overview.factoryDistribution : []
      chart.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: 0 },
        series: [{
          type: 'pie',
          radius: ['38%', '70%'],
          center: ['50%', '42%'],
          data: list.map(item => ({ name: item.name, value: Number(item.value || 0) }))
        }]
      })
    },
    renderProductChart() {
      const chart = this.ensureChart('productChart')
      if (!chart) return
      const list = Array.isArray(this.overview.productDistribution) ? this.overview.productDistribution : []
      chart.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: 0 },
        series: [{
          type: 'pie',
          radius: ['32%', '68%'],
          center: ['50%', '42%'],
          data: list.map(item => ({ name: item.name, value: Number(item.value || 0) }))
        }]
      })
    },
    resizeCharts() {
      Object.keys(this.chartInstances).forEach(key => {
        if (this.chartInstances[key]) {
          this.chartInstances[key].resize()
        }
      })
    },
  }
}
</script>

<style scoped>
.lab-page { display:flex; flex-direction:column; gap:14px; }
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
