<template>
  <div class="ad-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-head">
        <div>
          <div class="page-kicker">市场投放</div>
          <h2>广告投放</h2>
          <p>统一录入平台投放，自动同步财务支出，并在同页查看平台级 ROI。</p>
        </div>
        <div class="hero-actions">
          <el-button v-if="canCreate" type="primary" icon="el-icon-plus" @click="openCreateDialog">新增投放</el-button>
          <el-button icon="el-icon-refresh" @click="loadAll">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="query-card" shadow="never">
      <div class="query-row">
        <el-select v-model="filters.platform" clearable placeholder="投放平台" class="query-select">
          <el-option v-for="item in platformOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-input v-model="filters.keyword" clearable class="query-input" placeholder="活动名称 / 备注" @keyup.enter.native="loadAll" />
        <el-date-picker v-model="filters.startDate" type="date" value-format="yyyy-MM-dd" placeholder="开始日期" class="query-select" />
        <el-date-picker v-model="filters.endDate" type="date" value-format="yyyy-MM-dd" placeholder="结束日期" class="query-select" />
        <el-button type="primary" @click="loadAll">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <div class="card-grid">
      <div class="metric-card">
        <div class="metric-title">投放金额</div>
        <div class="metric-value">¥{{ formatMoney(overview.total_spend_amount) }}</div>
        <div class="metric-sub">当前条件下共 {{ overview.record_count || 0 }} 条记录</div>
      </div>
      <div class="metric-card">
        <div class="metric-title">平台数</div>
        <div class="metric-value">{{ platformShare.length }}</div>
        <div class="metric-sub">已统计平台级支出占比</div>
      </div>
      <div class="metric-card">
        <div class="metric-title">最高 ROI 平台</div>
        <div class="metric-value">{{ bestRoiPlatform.name || '-' }}</div>
        <div class="metric-sub">{{ bestRoiPlatform.name ? `ROI ${bestRoiPlatform.ratio}` : '暂无可用数据' }}</div>
      </div>
      <div class="metric-card attention">
        <div class="metric-title">我的录入</div>
        <div class="metric-value">{{ myRecordCount }}</div>
        <div class="metric-sub">护士账号默认只看本人列表</div>
      </div>
    </div>

    <el-card class="panel-card" shadow="never">
      <div slot="header" class="panel-title">月度趋势</div>
      <el-table :data="trendRows" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="month" label="月份" min-width="140" />
        <el-table-column prop="amount" label="投放金额" min-width="140" align="right">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.amount) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="double-grid">
      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">平台占比</div>
        <el-table :data="platformShare" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
          <el-table-column prop="platform" label="平台" min-width="120" />
          <el-table-column prop="amount" label="金额" min-width="120" align="right">
            <template slot-scope="scope">¥{{ formatMoney(scope.row.amount) }}</template>
          </el-table-column>
          <el-table-column prop="share_percent" label="占比" width="110" align="center">
            <template slot-scope="scope">{{ formatPercent(scope.row.share_percent) }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="panel-card" shadow="never">
        <div slot="header" class="panel-title">平台 ROI</div>
        <el-table :data="platformRoi" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
          <el-table-column prop="platform" label="平台" min-width="110" />
          <el-table-column prop="spend_amount" label="投放" width="110" align="right">
            <template slot-scope="scope">¥{{ formatMoney(scope.row.spend_amount) }}</template>
          </el-table-column>
          <el-table-column prop="consultation_count" label="咨询数" width="90" align="center" />
          <el-table-column prop="deal_amount" label="成交金额" width="120" align="right">
            <template slot-scope="scope">¥{{ formatMoney(scope.row.deal_amount) }}</template>
          </el-table-column>
          <el-table-column prop="roi_ratio" label="ROI" width="100" align="center">
            <template slot-scope="scope">{{ Number(scope.row.roi_ratio || 0).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-card class="panel-card" shadow="never">
      <div slot="header" class="panel-title">历史记录</div>
      <el-table :data="rows" stripe v-loading="loading" :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="platform" label="平台" min-width="100" />
        <el-table-column prop="campaign_name" label="活动名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="投放周期" min-width="180">
          <template slot-scope="scope">{{ scope.row.start_date || '-' }} 至 {{ scope.row.end_date || '-' }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="target_project" label="目标项目" min-width="120" />
        <el-table-column prop="created_by_name" label="录入人" min-width="120" />
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="isAdmin" type="text" size="mini" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button v-if="isAdmin" type="text" size="mini" class="danger-link" @click="removeRow(scope.row)">删除</el-button>
            <span v-if="!isAdmin" class="readonly-text">仅查看</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
  </div>
</template>

<script>
import axios from 'axios'
import { ADVERTISING_PLATFORM_OPTIONS } from '@/utils/consultationOptions'
import { getAdminSession } from '@/utils/adminSession'
import { showApiError } from '@/utils/errorMessage'

function createFilters() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const today = `${now.getFullYear()}-${month}-${day}`
  return {
    platform: '',
    keyword: '',
    startDate: `${now.getFullYear()}-${month}-01`,
    endDate: today
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
      platformOptions: ADVERTISING_PLATFORM_OPTIONS
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
    bestRoiPlatform() {
      const first = (this.platformRoi || [])[0] || null
      if (!first) return { name: '', ratio: '' }
      return {
        name: first.platform || '',
        ratio: Number(first.roi_ratio || 0).toFixed(2)
      }
    },
    myRecordCount() {
      const currentUserId = Number(this.currentUser && this.currentUser.id)
      return (this.rows || []).filter(item => Number(item.created_by || 0) === currentUserId).length
    }
  },
  mounted() {
    this.currentUser = getAdminSession() || {}
    this.loadAll()
  },
  methods: {
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
      } catch (error) {
        showApiError(this, '加载广告投放数据', error)
      } finally {
        this.loading = false
      }
    },
    resetFilters() {
      this.filters = createFilters()
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
    formatMoney(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    formatPercent(value) {
      const amount = Number(value || 0)
      return `${amount.toFixed(2).replace(/\.00$/, '')}%`
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

.hero-head,
.query-row,
.double-grid {
  display: flex;
  gap: 12px;
}

.hero-head,
.query-row {
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
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

.query-input {
  width: 220px;
}

.query-select {
  width: 160px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.metric-card.attention {
  background: linear-gradient(180deg, #fff7ed 0%, #ffffff 100%);
}

.metric-title {
  color: #64748b;
  font-size: 13px;
}

.metric-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}

.metric-sub {
  margin-top: 8px;
  color: #94a3b8;
  font-size: 12px;
}

.double-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.readonly-text {
  color: #94a3b8;
  font-size: 12px;
}

.danger-link {
  color: #ef4444;
}

@media (max-width: 1080px) {
  .card-grid,
  .double-grid {
    grid-template-columns: 1fr;
  }

  .query-input,
  .query-select {
    width: 100%;
  }
}
</style>
