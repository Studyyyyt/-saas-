<template>
  <div class="dashboard-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-head">
        <div>
          <div class="page-kicker">经营分析</div>
          <h2>咨询数据看板</h2>
          <p>聚焦咨询来源质量、到店转化和成交漏斗。</p>
        </div>
        <div class="filter-group">
          <el-select v-model="filters.rangePreset" class="query-select" @change="handleRangePresetChange">
            <el-option label="今天" value="today" />
            <el-option label="本周" value="week" />
            <el-option label="本月" value="month" />
            <el-option label="本季" value="quarter" />
            <el-option label="自定义" value="custom" />
          </el-select>
          <el-date-picker
            v-model="filters.range"
            type="datetimerange"
            value-format="yyyy-MM-dd HH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :disabled="filters.rangePreset !== 'custom'"
            class="range-picker"
          />
          <el-button type="primary" icon="el-icon-search" @click="loadAll">刷新</el-button>
        </div>
      </div>
    </el-card>

    <div class="card-grid">
      <div class="metric-card">
        <div class="metric-title">咨询总数</div>
        <div class="metric-value">{{ metricValue(overview.consultationCount) }}</div>
        <div class="metric-sub">上期 {{ metricPrev(overview.consultationCount) }}</div>
        <div class="metric-trend" :class="trendClass(overview.consultationCount)">{{ metricTrend(overview.consultationCount) }}</div>
      </div>
      <div class="metric-card">
        <div class="metric-title">到店转化率</div>
        <div class="metric-value">{{ metricValue(overview.arrivalRate) }}%</div>
        <div class="metric-sub">上期 {{ metricPrev(overview.arrivalRate) }}%</div>
        <div class="metric-trend" :class="trendClass(overview.arrivalRate)">{{ metricTrend(overview.arrivalRate) }}</div>
      </div>
      <div class="metric-card">
        <div class="metric-title">成交转化率</div>
        <div class="metric-value">{{ metricValue(overview.dealRate) }}%</div>
        <div class="metric-sub">上期 {{ metricPrev(overview.dealRate) }}%</div>
        <div class="metric-trend" :class="trendClass(overview.dealRate)">{{ metricTrend(overview.dealRate) }}</div>
      </div>
      <div class="metric-card attention">
        <div class="metric-title">高意向待跟进</div>
        <div class="metric-value">{{ metricValue(overview.highIntentPendingCount) }}</div>
        <div class="metric-sub">上期 {{ metricPrev(overview.highIntentPendingCount) }}</div>
        <div class="metric-trend" :class="trendClass(overview.highIntentPendingCount)">{{ metricTrend(overview.highIntentPendingCount) }}</div>
      </div>
    </div>

    <div class="chart-grid">
      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">转化漏斗</div>
        <div ref="funnelChart" class="chart-box"></div>
      </el-card>
      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">咨询时段热力图</div>
        <div ref="heatmapChart" class="chart-box"></div>
      </el-card>
    </div>

    <el-card class="panel-card" shadow="never">
      <div slot="header" class="panel-title">渠道分析</div>
      <el-table :data="channelRows" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="name" label="渠道" min-width="140" />
        <el-table-column prop="consultation_count" label="咨询数" width="110" align="center" />
        <el-table-column prop="arrival_rate" label="到店率" width="120" align="center">
          <template slot-scope="scope">{{ formatPercent(scope.row.arrival_rate) }}</template>
        </el-table-column>
        <el-table-column prop="deal_rate" label="成交率" width="120" align="center">
          <template slot-scope="scope">{{ formatPercent(scope.row.deal_rate) }}</template>
        </el-table-column>
        <el-table-column prop="avg_ticket" label="平均客单价" min-width="140" align="right">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.avg_ticket) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="panel-card" shadow="never">
      <div slot="header" class="panel-title">项目分析</div>
      <el-table :data="projectRows" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="name" label="主诉项目" min-width="160" />
        <el-table-column prop="consultation_count" label="咨询数" width="110" align="center" />
        <el-table-column prop="arrival_rate" label="到店率" width="120" align="center">
          <template slot-scope="scope">{{ formatPercent(scope.row.arrival_rate) }}</template>
        </el-table-column>
        <el-table-column prop="deal_rate" label="成交率" width="120" align="center">
          <template slot-scope="scope">{{ formatPercent(scope.row.deal_rate) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="panel-card" shadow="never">
      <div slot="header" class="panel-title">护士绩效</div>
      <el-table :data="nurseRows" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="created_by_name" label="录入人" min-width="150" />
        <el-table-column prop="consultation_count" label="录入数" width="120" align="center" />
        <el-table-column prop="arrival_rate" label="到店转化率" width="150" align="center">
          <template slot-scope="scope">{{ formatPercent(scope.row.arrival_rate) }}</template>
        </el-table-column>
        <el-table-column prop="deal_rate" label="成交转化率" width="150" align="center">
          <template slot-scope="scope">{{ formatPercent(scope.row.deal_rate) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="panel-card" shadow="never">
      <div slot="header" class="panel-title">转介绍分析</div>
      <div class="card-grid referral-grid">
        <div class="metric-card">
          <div class="metric-title">转介绍咨询占比</div>
          <div class="metric-value">{{ formatPercent(referralSummary.referral_ratio) }}</div>
          <div class="metric-sub">转介绍咨询 {{ referralSummary.referral_consultation_count || 0 }} 条</div>
        </div>
        <div class="metric-card">
          <div class="metric-title">转介绍到店率</div>
          <div class="metric-value">{{ formatPercent(referralSummary.referral_arrival_rate) }}</div>
          <div class="metric-sub">非转介绍 {{ formatPercent(referralSummary.non_referral_arrival_rate) }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-title">转介绍成交率</div>
          <div class="metric-value">{{ formatPercent(referralSummary.referral_deal_rate) }}</div>
          <div class="metric-sub">非转介绍 {{ formatPercent(referralSummary.non_referral_deal_rate) }}</div>
        </div>
        <div class="metric-card attention">
          <div class="metric-title">转介绍成交金额</div>
          <div class="metric-value">¥{{ formatMoney(referralSummary.referral_deal_amount) }}</div>
          <div class="metric-sub">当前周期转介绍净成交</div>
        </div>
      </div>
      <el-table :data="referralRows" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="name" label="介绍来源类型" min-width="180" />
        <el-table-column prop="consultation_count" label="咨询数" width="110" align="center" />
        <el-table-column prop="arrival_rate" label="到店率" width="120" align="center">
          <template slot-scope="scope">{{ formatPercent(scope.row.arrival_rate) }}</template>
        </el-table-column>
        <el-table-column prop="deal_rate" label="成交率" width="120" align="center">
          <template slot-scope="scope">{{ formatPercent(scope.row.deal_rate) }}</template>
        </el-table-column>
        <el-table-column prop="deal_amount" label="成交金额" min-width="140" align="right">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.deal_amount) }}</template>
        </el-table-column>
      </el-table>
      <div class="referral-top-head">高频介绍人</div>
      <el-table :data="topReferrers" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="referrer_name" label="介绍人" min-width="180" />
        <el-table-column prop="consultation_count" label="带来咨询" width="120" align="center" />
        <el-table-column prop="deal_count" label="成交数" width="110" align="center" />
        <el-table-column prop="deal_amount" label="成交金额" min-width="140" align="right">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.deal_amount) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'
import { showApiError } from '@/utils/errorMessage'

function createFilters() {
  return {
    rangePreset: 'week',
    range: []
  }
}

export default {
  name: 'ConsultationDashboardView',
  data() {
    return {
      filters: createFilters(),
      overview: {},
      funnel: {},
      channelRows: [],
      projectRows: [],
      heatmap: {},
      nurseRows: [],
      referralAnalysis: {},
      funnelChart: null,
      heatmapChart: null
    }
  },
  computed: {
    referralSummary() {
      return (this.referralAnalysis && this.referralAnalysis.summary) || {}
    },
    referralRows() {
      return (this.referralAnalysis && this.referralAnalysis.detailList) || []
    },
    topReferrers() {
      return (this.referralAnalysis && this.referralAnalysis.topReferrers) || []
    }
  },
  mounted() {
    this.funnelChart = echarts.init(this.$refs.funnelChart)
    this.heatmapChart = echarts.init(this.$refs.heatmapChart)
    this.loadAll()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.funnelChart) this.funnelChart.dispose()
    if (this.heatmapChart) this.heatmapChart.dispose()
  },
  methods: {
    handleResize() {
      if (this.funnelChart) this.funnelChart.resize()
      if (this.heatmapChart) this.heatmapChart.resize()
    },
    handleRangePresetChange(value) {
      if (value !== 'custom') this.filters.range = []
    },
    buildParams() {
      const params = {
        rangePreset: this.filters.rangePreset
      }
      if (this.filters.rangePreset === 'custom' && Array.isArray(this.filters.range) && this.filters.range.length === 2) {
        params.startTime = this.filters.range[0]
        params.endTime = this.filters.range[1]
      }
      return params
    },
    async loadAll() {
      try {
        const params = this.buildParams()
        const [overviewRes, funnelRes, channelRes, projectRes, heatmapRes, nurseRes, referralRes] = await Promise.all([
          axios.get('/consultations/dashboard/overview', { params }),
          axios.get('/consultations/dashboard/funnel', { params }),
          axios.get('/consultations/dashboard/channelAnalysis', { params }),
          axios.get('/consultations/dashboard/projectAnalysis', { params }),
          axios.get('/consultations/dashboard/hourHeatmap', { params }),
          axios.get('/consultations/dashboard/nursePerformance', { params }),
          axios.get('/consultations/dashboard/referralAnalysis', { params })
        ])
        this.overview = (overviewRes.data && overviewRes.data.data) || {}
        this.funnel = (funnelRes.data && funnelRes.data.data) || {}
        this.channelRows = (channelRes.data && channelRes.data.data && channelRes.data.data.list) || []
        this.projectRows = (projectRes.data && projectRes.data.data && projectRes.data.data.list) || []
        this.heatmap = (heatmapRes.data && heatmapRes.data.data) || {}
        this.nurseRows = (nurseRes.data && nurseRes.data.data && nurseRes.data.data.list) || []
        this.referralAnalysis = (referralRes.data && referralRes.data.data) || {}
        this.renderCharts()
      } catch (error) {
        showApiError(this, '加载咨询看板', error)
      }
    },
    metricValue(metric) {
      const value = metric && metric.current_value
      return Number.isFinite(Number(value)) ? Number(value).toFixed(2).replace(/\.00$/, '') : '0'
    },
    metricPrev(metric) {
      const value = metric && metric.previous_value
      return Number.isFinite(Number(value)) ? Number(value).toFixed(2).replace(/\.00$/, '') : '0'
    },
    metricTrend(metric) {
      if (!metric) return '0%'
      const direction = metric.direction === 'up' ? '↑' : metric.direction === 'down' ? '↓' : '→'
      const rate = Number(metric.change_rate || 0).toFixed(2).replace(/\.00$/, '')
      const delta = Number(metric.change_value || 0).toFixed(2).replace(/\.00$/, '')
      return `${direction} ${delta} / ${rate}%`
    },
    trendClass(metric) {
      if (!metric || metric.direction === 'flat') return 'is-flat'
      return metric.direction === 'up' ? 'is-up' : 'is-down'
    },
    formatPercent(value) {
      const amount = Number(value || 0)
      return `${amount.toFixed(2).replace(/\.00$/, '')}%`
    },
    formatMoney(value) {
      const amount = Number(value || 0)
      return amount.toFixed(2)
    },
    renderCharts() {
      const funnelCurrent = (this.funnel && this.funnel.current) || {}
      this.funnelChart.setOption({
        grid: { left: 40, right: 20, top: 30, bottom: 30 },
        xAxis: { type: 'category', data: ['咨询数', '到店数', '成交数'] },
        yAxis: { type: 'value' },
        series: [{
          type: 'bar',
          barWidth: 48,
          itemStyle: {
            borderRadius: [10, 10, 0, 0],
            color: params => ['#1d4ed8', '#0f766e', '#f97316'][params.dataIndex]
          },
          data: [
            Number(funnelCurrent.consultation_count || 0),
            Number(funnelCurrent.arrived_count || 0),
            Number(funnelCurrent.deal_count || 0)
          ]
        }]
      })

      const heatmapRows = (this.heatmap && this.heatmap.current) || []
      const hours = Array.from({ length: 24 }).map((_, index) => `${String(index).padStart(2, '0')}:00`)
      const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      const seriesData = heatmapRows.map(item => [Number(item.hour || 0), Number(item.weekday_index || 1) - 1, Number(item.count || 0)])
      this.heatmapChart.setOption({
        tooltip: { position: 'top' },
        grid: { left: 50, right: 20, top: 30, bottom: 30 },
        xAxis: { type: 'category', data: hours },
        yAxis: { type: 'category', data: weekdays },
        visualMap: {
          min: 0,
          max: Math.max(1, ...seriesData.map(item => item[2])),
          calculable: true,
          orient: 'horizontal',
          left: 'center',
          bottom: 0
        },
        series: [{
          type: 'heatmap',
          data: seriesData,
          label: { show: false }
        }]
      })
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-card {
  border-radius: 22px;
}

.referral-grid {
  margin-bottom: 14px;
}

.referral-top-head {
  margin: 16px 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.hero-head {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.page-kicker {
  color: #64748b;
  font-size: 13px;
}

.hero-head h2 {
  margin: 8px 0;
  color: #0f172a;
}

.hero-head p {
  margin: 0;
  color: #64748b;
}

.filter-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.query-select {
  width: 150px;
}

.range-picker {
  width: 360px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric-card {
  padding: 18px;
  border-radius: 20px;
  background: #ffffff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.metric-card.attention {
  background: linear-gradient(135deg, #fff7ed 0%, #fffbeb 100%);
}

.metric-title {
  color: #64748b;
  font-size: 13px;
}

.metric-value {
  margin-top: 10px;
  color: #0f172a;
  font-size: 32px;
  font-weight: 700;
}

.metric-sub {
  margin-top: 8px;
  color: #94a3b8;
  font-size: 12px;
}

.metric-trend {
  margin-top: 10px;
  font-weight: 600;
}

.metric-trend.is-up {
  color: #b91c1c;
}

.metric-trend.is-down {
  color: #0f766e;
}

.metric-trend.is-flat {
  color: #64748b;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.panel-card {
  border-radius: 20px;
}

.panel-title {
  font-weight: 700;
  color: #0f172a;
}

.chart-box {
  width: 100%;
  height: 360px;
}

@media (max-width: 1024px) {
  .card-grid,
  .chart-grid {
    grid-template-columns: 1fr;
  }

  .query-select,
  .range-picker {
    width: 100%;
  }
}
</style>
