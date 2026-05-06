<template>
  <div class="expense-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">财务管理</div>
        <h2>财务支出</h2>
        <p>统一汇总耗材采购、加工账单和非耗材支出；其中非耗材支出可在此直接录入维护。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" plain @click="openDialog()">录入非耗材支出</el-button>
        <el-button @click="loadAll">刷新</el-button>
      </div>
    </div>

    <el-alert
      title="耗材采购和加工账单由业务流程自动写入财务；本页只允许新增、编辑和删除“非耗材支出”记录。"
      type="info"
      :closable="false"
      show-icon
    />

    <el-card shadow="never" class="query-card">
      <div class="query-row">
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          value-format="yyyy-MM-dd"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="range-picker"
        />
        <el-input
          v-model="filters.keyword"
          class="query-input"
          clearable
          placeholder="搜索非耗材支出项目 / 备注"
          @keyup.enter.native="loadAll"
        />
        <el-button type="primary" icon="el-icon-search" @click="loadAll">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <div class="summary-grid">
      <el-card shadow="never" class="summary-card">
        <div class="summary-label">支出总额</div>
        <div class="summary-value">¥{{ formatMoney(overview.total_expense) }}</div>
        <div class="summary-desc">{{ periodText }}</div>
      </el-card>
      <el-card shadow="never" class="summary-card amber">
        <div class="summary-label">门店耗材</div>
        <div class="summary-value">¥{{ formatMoney(overview.material_expense) }}</div>
        <div class="summary-desc">{{ overview.material_count || 0 }} 笔自动入账</div>
      </el-card>
      <el-card shadow="never" class="summary-card blue">
        <div class="summary-label">加工账单</div>
        <div class="summary-value">¥{{ formatMoney(overview.lab_expense) }}</div>
        <div class="summary-desc">{{ overview.lab_count || 0 }} 笔自动入账</div>
      </el-card>
      <el-card shadow="never" class="summary-card green">
        <div class="summary-label">非耗材支出</div>
        <div class="summary-value">¥{{ formatMoney(overview.other_expense) }}</div>
        <div class="summary-desc">{{ overview.other_count || 0 }} 笔人工/历史支出</div>
      </el-card>
    </div>

    <el-card shadow="never" class="composition-card">
      <div class="section-head">
        <div>
          <h3>支出构成</h3>
          <p>当前统计区间内，经营支出按三类拆分展示。</p>
        </div>
      </div>
      <div class="composition-list">
        <div v-for="item in compositionRows" :key="item.key" class="composition-item">
          <div class="composition-meta">
            <span class="composition-name">{{ item.label }}</span>
            <span class="composition-amount">¥{{ formatMoney(item.amount) }}</span>
          </div>
          <el-progress :percentage="item.percent" :stroke-width="12" :color="item.color" />
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="trend-card">
      <div class="section-head">
        <div>
          <h3>按日支出趋势</h3>
          <p>展示统计区间内每日耗材、加工、非耗材支出的变化趋势。</p>
        </div>
      </div>
      <div ref="trendChart" class="trend-chart"></div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="section-head">
        <div>
          <h3>非耗材支出流水</h3>
          <p>例如房租、水电、物业、营销、办公等支出。</p>
        </div>
      </div>
      <el-table
        :data="rows"
        stripe
        v-loading="loadingRows"
        :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }"
      >
        <el-table-column prop="date" label="支出日期" width="120" />
        <el-table-column prop="name" label="支出项目" min-width="180" />
        <el-table-column label="金额" width="120" align="right">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.amount) }}</template>
        </el-table-column>
        <el-table-column label="分类" width="120" align="center">
          <template>
            <el-tag size="mini" type="success">非耗材支出</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="240" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="mini" style="color:#ef4444" @click="deleteRow(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loadingRows && !rows.length" description="暂无非耗材支出"></el-empty>
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

    <el-dialog :title="form.id ? '编辑非耗材支出' : '录入非耗材支出'" :visible.sync="dialogVisible" width="480px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="支出项目">
          <el-input v-model="form.name" placeholder="如：房租 / 水电 / 办公用品" />
        </el-form-item>
        <el-form-item label="支出金额">
          <el-input-number v-model="form.amount" :min="0" :precision="2" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="支出日期">
          <el-date-picker v-model="form.date" type="date" value-format="yyyy-MM-dd" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'
import { showApiError } from '@/utils/errorMessage'

function currentMonthRange() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return [`${year}-${month}-01`, `${year}-${month}-${day}`]
}

function defaultForm() {
  const range = currentMonthRange()
  return {
    id: null,
    name: '',
    amount: 0,
    date: range[1],
    remark: ''
  }
}

export default {
  name: 'FinancialExpenseView',
  data() {
    return {
      loadingOverview: false,
      loadingRows: false,
      saving: false,
      chartInstance: null,
      dialogVisible: false,
      overview: {
        start_date: '',
        end_date: '',
        total_expense: 0,
        material_expense: 0,
        lab_expense: 0,
        other_expense: 0,
        material_count: 0,
        lab_count: 0,
        other_count: 0
      },
      filters: {
        dateRange: currentMonthRange(),
        keyword: ''
      },
      rows: [],
      totalItems: 0,
      currentPage: 1,
      pageSize: 10,
      form: defaultForm()
    }
  },
  computed: {
    periodText() {
      return `${this.overview.start_date || '--'} 至 ${this.overview.end_date || '--'}`
    },
    compositionRows() {
      const total = Number(this.overview.total_expense || 0)
      const buildPercent = amount => {
        if (total <= 0) return 0
        return Math.min(100, Math.round((Number(amount || 0) / total) * 10000) / 100)
      }
      return [
        { key: 'material', label: '门店耗材', amount: this.overview.material_expense || 0, percent: buildPercent(this.overview.material_expense), color: '#f59e0b' },
        { key: 'lab', label: '加工账单', amount: this.overview.lab_expense || 0, percent: buildPercent(this.overview.lab_expense), color: '#3b82f6' },
        { key: 'other', label: '非耗材支出', amount: this.overview.other_expense || 0, percent: buildPercent(this.overview.other_expense), color: '#10b981' }
      ]
    }
  },
  mounted() {
    this.loadAll()
  },
  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose()
      this.chartInstance = null
    }
  },
  methods: {
    formatMoney(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    async loadAll() {
      await Promise.all([this.loadOverview(), this.loadRows()])
    },
    async loadOverview() {
      this.loadingOverview = true
      const range = Array.isArray(this.filters.dateRange) ? this.filters.dateRange : []
      try {
        const res = await axios.get('/finances/expenseOverview', {
          params: {
            startDate: range[0] || undefined,
            endDate: range[1] || undefined
          }
        })
        if (res.data.code === '200') {
          this.overview = Object.assign({}, this.overview, res.data.data || {})
          this.$nextTick(() => this.renderTrendChart())
        } else {
          this.$message.error(res.data.msg || '获取支出汇总失败')
        }
      } catch (error) {
        showApiError(this, '获取支出汇总', error)
      } finally {
        this.loadingOverview = false
      }
    },
    async loadRows() {
      this.loadingRows = true
      const range = Array.isArray(this.filters.dateRange) ? this.filters.dateRange : []
      try {
        const res = await axios.get('/finances/manualExpenseSearch', {
          params: {
            startDate: range[0] || undefined,
            endDate: range[1] || undefined,
            keyword: this.filters.keyword || undefined,
            page: this.currentPage,
            size: this.pageSize
          }
        })
        const data = res.data && res.data.data ? res.data.data : {}
        this.rows = Array.isArray(data.list) ? data.list : []
        this.totalItems = Number(data.total || 0)
      } catch (error) {
        this.rows = []
        this.totalItems = 0
        this.$message.error('获取非耗材支出失败')
      } finally {
        this.loadingRows = false
      }
    },
    resetFilters() {
      this.filters = {
        dateRange: currentMonthRange(),
        keyword: ''
      }
      this.currentPage = 1
      this.loadAll()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadRows()
    },
    handleCurrentChange(page) {
      this.currentPage = page
      this.loadRows()
    },
    openDialog(row) {
      this.form = row
        ? {
            id: row.id,
            name: row.name || '',
            amount: Number(row.amount || 0),
            date: row.date || currentMonthRange()[1],
            remark: row.remark || ''
          }
        : defaultForm()
      this.dialogVisible = true
    },
    async submitForm() {
      if (!String(this.form.name || '').trim()) {
        this.$message.warning('支出项目不能为空')
        return
      }
      if (!Number(this.form.amount) || Number(this.form.amount) <= 0) {
        this.$message.warning('支出金额必须大于0')
        return
      }
      if (!this.form.date) {
        this.$message.warning('请选择支出日期')
        return
      }
      this.saving = true
      const payload = {
        id: this.form.id || 0,
        name: String(this.form.name || '').trim(),
        amount: Number(this.form.amount || 0),
        date: this.form.date,
        remark: this.form.remark || ''
      }
      const request = payload.id
        ? axios.put('/finances/manualExpense/edit', payload)
        : axios.post('/finances/manualExpense/add', payload)
      try {
        const res = await request
        if (res.data.code === '200') {
          this.$message.success(payload.id ? '支出更新成功' : '支出录入成功')
          this.dialogVisible = false
          this.loadAll()
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败')
      } finally {
        this.saving = false
      }
    },
    deleteRow(row) {
      this.$confirm(`确认删除支出“${row.name || '未命名支出'}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.delete(`/finances/manualExpense/delete/${row.id}`)
        if (res.data.code === '200') {
          this.$message.success('删除成功')
          this.loadAll()
        } else {
          this.$message.error(res.data.msg || '删除失败')
        }
      }).catch(() => {})
    },
    renderTrendChart() {
      if (!this.$refs.trendChart) {
        return
      }
      if (!this.chartInstance) {
        this.chartInstance = echarts.init(this.$refs.trendChart)
      }
      const trend = Array.isArray(this.overview.trend) ? this.overview.trend : []
      const dates = trend.map(item => item.date || '')
      const totalSeries = trend.map(item => Number(item.total_expense || 0))
      const materialSeries = trend.map(item => Number(item.material_expense || 0))
      const labSeries = trend.map(item => Number(item.lab_expense || 0))
      const otherSeries = trend.map(item => Number(item.other_expense || 0))
      this.chartInstance.setOption({
        tooltip: { trigger: 'axis' },
        legend: {
          data: ['总支出', '门店耗材', '加工账单', '非耗材支出']
        },
        grid: {
          left: 56,
          right: 24,
          top: 48,
          bottom: 40
        },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: {
          type: 'value'
        },
        series: [
          { name: '总支出', type: 'line', smooth: true, data: totalSeries, lineStyle: { width: 3, color: '#0f172a' } },
          { name: '门店耗材', type: 'line', smooth: true, data: materialSeries, lineStyle: { color: '#f59e0b' } },
          { name: '加工账单', type: 'line', smooth: true, data: labSeries, lineStyle: { color: '#3b82f6' } },
          { name: '非耗材支出', type: 'line', smooth: true, data: otherSeries, lineStyle: { color: '#10b981' } }
        ]
      })
    }
  }
}
</script>

<style scoped>
.expense-page { display:flex; flex-direction:column; gap:14px; }
.hero-card { display:flex; justify-content:space-between; align-items:flex-start; gap:16px; padding:18px; border-radius:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.hero-actions { display:flex; gap:10px; flex-wrap:wrap; }
.query-card,.composition-card,.table-card { border-radius:18px; }
.trend-card { border-radius:18px; }
.query-row { display:flex; gap:12px; flex-wrap:wrap; align-items:center; }
.range-picker { width:260px; }
.query-input { width:260px; }
.summary-grid { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:14px; }
.summary-card { border-radius:18px; }
.summary-card.amber { background:#fff7ed; }
.summary-card.blue { background:#eff6ff; }
.summary-card.green { background:#ecfdf5; }
.summary-label { color:#64748b; font-size:13px; }
.summary-value { margin-top:8px; color:#0f172a; font-size:28px; font-weight:800; }
.summary-desc { margin-top:8px; color:#94a3b8; font-size:12px; }
.section-head { display:flex; justify-content:space-between; align-items:flex-start; gap:16px; margin-bottom:14px; }
.section-head h3 { margin:0; color:#0f172a; font-size:18px; }
.section-head p { margin:6px 0 0; color:#94a3b8; font-size:12px; }
.composition-list { display:flex; flex-direction:column; gap:14px; }
.composition-item { display:flex; flex-direction:column; gap:8px; }
.composition-meta { display:flex; justify-content:space-between; gap:12px; color:#334155; font-size:13px; }
.composition-name { font-weight:600; }
.composition-amount { color:#0f172a; font-weight:700; }
.trend-chart { width:100%; height:360px; }
.pagination-row { display:flex; justify-content:flex-end; padding-top:16px; }
@media (max-width: 992px) {
  .summary-grid { grid-template-columns:repeat(2,minmax(0,1fr)); }
}
@media (max-width: 768px) {
  .hero-card { flex-direction:column; }
  .summary-grid { grid-template-columns:1fr; }
  .range-picker,.query-input { width:100%; }
  .pagination-row { justify-content:center; }
}
</style>
