<template>
  <div class="ai-response-card" :class="`display-type--${effectiveType}`">
    <!-- 文本模式（默认） -->
    <template v-if="effectiveType === 'text'">
      <div class="response-text" v-html="renderedText" />
    </template>

    <!-- KPI 指标卡片 -->
    <template v-else-if="effectiveType === 'kpi'">
      <div class="response-kpi-grid">
        <div v-for="(item, index) in kpiItems" :key="index" class="kpi-item">
          <div class="kpi-item-value">{{ item.value }}</div>
          <div class="kpi-item-label">{{ item.label }}</div>
        </div>
      </div>
    </template>

    <!-- 表格模式 -->
    <template v-else-if="effectiveType === 'table'">
      <div class="response-table-wrap">
        <el-table
          :data="tableData"
          size="mini"
          :max-height="300"
          border
          stripe
          style="width: 100%"
        >
          <el-table-column
            v-for="col in tableColumns"
            :key="col.prop"
            :prop="col.prop"
            :label="col.label"
            :min-width="col.minWidth || 100"
            show-overflow-tooltip
          />
        </el-table>
      </div>
    </template>

    <!-- 对比模式 -->
    <template v-else-if="effectiveType === 'comparison'">
      <div class="response-comparison">
        <div v-for="(group, gIdx) in comparisonGroups" :key="gIdx" class="comparison-group">
          <div class="comparison-title">{{ group.title }}</div>
          <div class="comparison-metrics">
            <div v-for="(m, mIdx) in group.metrics" :key="mIdx" class="comparison-metric">
              <span class="metric-label">{{ m.label }}</span>
              <span class="metric-value" :class="m.trend">{{ m.value }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 列表模式 -->
    <template v-else-if="effectiveType === 'list'">
      <div class="response-list">
        <div v-for="(item, index) in listItems" :key="index" class="list-item">
          <span class="list-item-bullet" />
          <span class="list-item-text">{{ item }}</span>
        </div>
      </div>
    </template>

    <!-- 图表模式（预留，需要 echarts） -->
    <template v-else-if="effectiveType === 'chart'">
      <div ref="chartRef" class="response-chart" style="width: 100%; height: 280px;" />
    </template>
  </div>
</template>

<script>
/**
 * AI 结构化响应渲染器
 * 根据 displayType 渲染不同形式的结果展示
 * @displayType text | kpi | table | chart | comparison | list
 */
export default {
  name: 'AiResponseCard',
  props: {
    displayType: { type: String, default: 'text' },
    content: { type: String, default: '' },
    payload: { type: [Object, Array], default: null }
  },
  data() {
    return {
      chartInstance: null
    }
  },
  computed: {
    effectiveType() {
      const supported = ['text', 'kpi', 'table', 'chart', 'comparison', 'list']
      return supported.includes(this.displayType) ? this.displayType : 'text'
    },
    renderedText() {
      if (!this.content) return ''
      let html = this.escapeHtml(this.content)
      html = html
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/`(.+?)`/g, '<code>$1</code>')
        .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
        .replace(/^- (.+)$/gm, '<li>$1</li>')
        .replace(/(<li>.+<\/li>\n?)+/g, '<ul>$&</ul>')
        .replace(/\n/g, '<br>')
      return html
    },
    kpiItems() {
      const p = this.payload
      if (Array.isArray(p)) return p
      if (p && typeof p === 'object') {
        return Object.entries(p).map(([k, v]) => ({
          label: k,
          value: typeof v === 'number' ? v.toLocaleString() : String(v)
        }))
      }
      return []
    },
    tableData() {
      const p = this.payload
      if (Array.isArray(p)) return p
      if (p && Array.isArray(p.data)) return p.data
      if (p && Array.isArray(p.list)) return p.list
      return []
    },
    tableColumns() {
      const data = this.tableData
      if (!data.length) return []
      const first = data[0]
      if (typeof first !== 'object') return []
      // 如果 payload 中定义了 columns，优先使用
      const p = this.payload
      if (p && Array.isArray(p.columns)) {
        return p.columns.map(c => ({
          prop: c.prop || c.key,
          label: c.label || c.title || c.prop || c.key,
          minWidth: c.minWidth || c.width
        }))
      }
      return Object.keys(first).map(k => ({
        prop: k,
        label: k,
        minWidth: 100
      }))
    },
    comparisonGroups() {
      const p = this.payload
      if (Array.isArray(p)) return p
      if (p && Array.isArray(p.groups)) return p.groups
      return []
    },
    listItems() {
      const p = this.payload
      if (Array.isArray(p)) {
        return p.map(i => (typeof i === 'string' ? i : JSON.stringify(i)))
      }
      if (p && Array.isArray(p.items)) {
        return p.items.map(i => (typeof i === 'string' ? i : JSON.stringify(i)))
      }
      return []
    }
  },
  watch: {
    effectiveType(val) {
      if (val === 'chart') {
        this.$nextTick(() => this.renderChart())
      }
    },
    payload() {
      if (this.effectiveType === 'chart') {
        this.$nextTick(() => this.renderChart())
      }
    }
  },
  mounted() {
    if (this.effectiveType === 'chart') {
      this.$nextTick(() => this.renderChart())
    }
  },
  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose()
      this.chartInstance = null
    }
  },
  methods: {
    escapeHtml(text) {
      if (text == null) return ''
      const div = document.createElement('div')
      div.textContent = text
      return div.innerHTML
    },
    renderChart() {
      if (!this.$refs.chartRef) return
      try {
        const echarts = require('echarts')
        if (this.chartInstance) {
          this.chartInstance.dispose()
        }
        this.chartInstance = echarts.init(this.$refs.chartRef)
        const option = this.buildChartOption()
        this.chartInstance.setOption(option)
        const resizeHandler = () => this.chartInstance && this.chartInstance.resize()
        window.addEventListener('resize', resizeHandler)
        this._chartResizeHandler = resizeHandler
      } catch (e) {
        console.warn('[AiResponseCard] ECharts 渲染失败:', e)
      }
    },
    buildChartOption() {
      const p = this.payload || {}
      const chartType = p.chartType || 'bar'
      const categories = p.categories || p.labels || p.xAxis || []
      const series = p.series || p.data || []

      const baseOption = {
        color: ['#5A8F7B', '#7EB5A2', '#A8D5BA', '#C9A227', '#C75B5B', '#7c3aed'],
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: categories, axisLine: { lineStyle: { color: '#e2e8f0' } }, axisLabel: { color: '#6B6B6B' } },
        yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f1f5f9' } }, axisLabel: { color: '#6B6B6B' } }
      }

      if (chartType === 'pie') {
        return {
          color: baseOption.color,
          tooltip: { trigger: 'item' },
          series: [{
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
            label: { show: false },
            emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
            data: series
          }]
        }
      }

      if (chartType === 'line') {
        return {
          ...baseOption,
          series: Array.isArray(series) ? series.map(s => ({
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: { width: 3 },
            areaStyle: { opacity: 0.1 },
            ...s
          })) : [{ type: 'line', data: series, smooth: true }]
        }
      }

      // 默认柱状图
      return {
        ...baseOption,
        series: Array.isArray(series) ? series.map(s => ({
          type: 'bar',
          barWidth: '50%',
          itemStyle: { borderRadius: [4, 4, 0, 0] },
          ...s
        })) : [{ type: 'bar', data: series, barWidth: '50%', itemStyle: { borderRadius: [4, 4, 0, 0] } }]
      }
    }
  }
}
</script>

<style scoped>
.ai-response-card {
  width: 100%;
}

/* 文本模式 */
.response-text {
  font-size: 14px;
  line-height: 1.7;
  color: #334155;
  word-break: break-word;
}

.response-text ::v-deep strong {
  color: #0f172a;
  font-weight: 600;
}

.response-text ::v-deep code {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  color: #5A8F7B;
  font-family: 'SF Mono', Monaco, monospace;
}

.response-text ::v-deep pre {
  background: #f1f5f9;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.response-text ::v-deep pre code {
  background: none;
  padding: 0;
}

.response-text ::v-deep ul {
  margin: 8px 0;
  padding-left: 20px;
}

.response-text ::v-deep li {
  margin: 4px 0;
}

/* KPI 模式 */
.response-kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
}

.kpi-item {
  background: #fff;
  border: 1px solid rgba(90, 143, 123, 0.12);
  border-radius: 10px;
  padding: 12px;
  text-align: center;
}

.kpi-item-value {
  font-size: 20px;
  font-weight: 700;
  color: #5A8F7B;
  line-height: 1.3;
}

.kpi-item-label {
  font-size: 12px;
  color: #6B6B6B;
  margin-top: 4px;
}

/* 表格模式 */
.response-table-wrap {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(90, 143, 123, 0.12);
}

/* 对比模式 */
.response-comparison {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comparison-group {
  background: #fff;
  border: 1px solid rgba(90, 143, 123, 0.12);
  border-radius: 10px;
  padding: 12px 14px;
}

.comparison-title {
  font-size: 13px;
  font-weight: 600;
  color: #2C3E35;
  margin-bottom: 8px;
}

.comparison-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 8px;
}

.comparison-metric {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.metric-label {
  font-size: 11px;
  color: #A0A0A0;
}

.metric-value {
  font-size: 14px;
  font-weight: 600;
  color: #2C3E35;
}

.metric-value.up {
  color: #C75B5B;
}

.metric-value.down {
  color: #5A8F7B;
}

/* 列表模式 */
.response-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.list-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: #334155;
}

.list-item-bullet {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #5A8F7B;
  margin-top: 8px;
  flex-shrink: 0;
}

.list-item-text {
  flex: 1;
  word-break: break-word;
}

/* 图表模式 */
.response-chart {
  background: #fff;
  border-radius: 8px;
  border: 1px solid rgba(90, 143, 123, 0.12);
}

@media (max-width: 768px) {
  .response-kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
