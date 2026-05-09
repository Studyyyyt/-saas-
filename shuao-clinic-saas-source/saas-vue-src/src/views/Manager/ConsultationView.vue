<template>
  <div class="page-wrap consultation-page">
    <el-card class="top-card" shadow="never">
      <div class="page-head">
        <div>
          <div class="page-kicker">患者管理</div>
          <h2>咨询记录</h2>
          <p>统一沉淀微信/电话/平台咨询记录，支持后续转化分析和接诊前查阅。</p>
        </div>
        <div class="head-actions">
          <div class="mini-stat warn clickable" @click="applyQuickHighIntentFilter">
            <div class="mini-num">{{ highIntentPendingCount }}</div>
            <div class="mini-label">7天内高意向待跟进</div>
          </div>
          <div class="mini-stat info clickable" @click="applyTodayFollowupFilter">
            <div class="mini-num">{{ todayFollowupCount }}</div>
            <div class="mini-label">今日需跟进</div>
          </div>
          <el-button v-if="canCreate" type="primary" icon="el-icon-circle-plus-outline" @click="openCreateDialog">记录咨询</el-button>
          <el-button v-if="canExport" plain icon="el-icon-download" @click="exportExcel">导出 Excel</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="query-card" shadow="never">
      <div class="status-filter-row">
        <div class="status-filter-label">状态筛选</div>
        <div class="status-filter-group">
          <button
            v-for="item in consultationStageOptions"
            :key="item.value"
            type="button"
            class="status-filter-chip"
            :class="{ 'is-active': activeConsultationStage === item.value }"
            @click="applyConsultationStageFilter(item.value)"
          >
            {{ item.label }}
          </button>
        </div>
      </div>
      <div class="query-row">
        <el-input
          v-model="filters.keyword"
          class="query-input"
          clearable
          placeholder="支持姓名、手机号搜索"
          @keyup.enter.native="loadList"
        />
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
          class="range-picker"
          :disabled="filters.rangePreset !== 'custom'"
        />
      </div>
      <div class="query-row second-row">
        <el-select v-model="filters.channel" clearable placeholder="渠道" class="query-select">
          <el-option v-for="item in channelOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="filters.chiefProject" clearable placeholder="主诉项目" class="query-select">
          <el-option v-for="item in chiefProjectOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="filters.intentLevel" clearable placeholder="意向强度" class="query-select">
          <el-option v-for="item in intentOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="filters.handlingResult" clearable placeholder="处理结果" class="query-select">
          <el-option v-for="item in handlingResultOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="filters.hasDeal" clearable placeholder="是否成交" class="query-select">
          <el-option label="已成交" :value="true" />
          <el-option label="未成交" :value="false" />
        </el-select>
        <el-select v-model="filters.createdBy" clearable placeholder="录入人" class="query-select">
          <el-option v-for="item in creatorOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="loadList">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        ref="consultationTable"
        :data="rows"
        stripe
        border
        size="mini"
        v-loading="loading"
        class="consultation-table"
        :header-cell-style="tableHeaderStyle"
      >
        <el-table-column prop="consultation_time" label="咨询时间" min-width="165" />
        <el-table-column prop="consultation_channel" label="渠道" min-width="120" />
        <el-table-column prop="contact_name" label="姓名/昵称" min-width="130">
          <template slot-scope="scope">
            <span>{{ scope.row.contact_name || scope.row.patient_name || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="contact_phone" label="联系方式" min-width="130">
          <template slot-scope="scope">{{ scope.row.contact_phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="chief_project" label="主诉项目" min-width="120" />
        <el-table-column label="意向强度" width="120" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="intentTagType(scope.row.intent_level)">{{ scope.row.intent_level || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理结果" min-width="120" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="handlingResultTagType(scope.row.handling_result)">{{ scope.row.handling_result || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否成交" min-width="180">
          <template slot-scope="scope">
            <div class="deal-cell">
              <el-tag size="mini" :type="scope.row.deal_at ? 'success' : 'info'">{{ scope.row.deal_at ? '已成交' : '未成交' }}</el-tag>
              <span v-if="scope.row.deal_at" class="deal-text">¥{{ formatMoney(scope.row.total_deal_amount) }}</span>
              <span v-if="scope.row.deal_at" class="deal-sub">{{ scope.row.deal_at }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="预计金额" min-width="110" align="right">
          <template slot-scope="scope">
            <span v-if="scope.row.estimated_amount">¥{{ formatMoney(scope.row.estimated_amount) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="下次跟进" min-width="160">
          <template slot-scope="scope">
            <span v-if="scope.row.next_followup_time" :class="isOverdue(scope.row.next_followup_time) ? 'overdue-text' : ''">
              {{ scope.row.next_followup_time }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="跟进次数" width="90" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.followup_count > 0" size="mini" type="info">{{ scope.row.followup_count }}次</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="AI评分" width="90" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.ai_analysis_score" size="mini" :type="aiScoreTagType(scope.row.ai_analysis_score)">
              {{ scope.row.ai_analysis_score }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="created_by_name" label="录入人" min-width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="openDetailDialog(scope.row)">查看详情</el-button>
            <el-button
              v-if="canEditRecord(scope.row)"
              type="text"
              size="mini"
              @click="openEditDialog(scope.row)"
            >编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !rows.length" description="暂无咨询记录"></el-empty>

      <div v-else class="pagination-row">
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

    <ConsultationRecordDialog
      :visible.sync="dialogVisible"
      :mode="dialogMode"
      :record="activeRecord"
      :current-user="currentUser"
      @saved="handleDialogSaved"
    />
  </div>
</template>

<script>
import axios from 'axios'
import * as XLSX from 'xlsx'
import ConsultationRecordDialog from '@/components/ConsultationRecordDialog.vue'
import { getAdminSession } from '@/utils/adminSession'
import { offConsultationSaved, onConsultationSaved } from '@/utils/consultationDialog'
import { showApiError } from '@/utils/errorMessage'
import {
  CHIEF_PROJECT_OPTIONS,
  CONSULTATION_STAGE_FILTER_OPTIONS,
  CONSULTATION_CHANNEL_OPTIONS,
  detectConsultationStageFilter,
  HANDLING_RESULT_OPTIONS,
  INTENT_LEVEL_OPTIONS,
  resolveConsultationStageFilters
} from '@/utils/consultationOptions'

function createDefaultFilters() {
  return {
    keyword: '',
    rangePreset: 'week',
    range: [],
    channel: '',
    chiefProject: '',
    intentLevel: '',
    handlingResult: '',
    hasDeal: '',
    createdBy: ''
  }
}

export default {
  name: 'ConsultationView',
  components: { ConsultationRecordDialog },
  data() {
    return {
      currentUser: getAdminSession() || {},
      loading: false,
      rows: [],
      currentPage: 1,
      pageSize: 20,
      totalItems: 0,
      highIntentPendingCount: 0,
      todayFollowupCount: 0,
      dialogVisible: false,
      dialogMode: 'create',
      activeRecord: {},
      creatorOptions: [],
      consultationStageOptions: CONSULTATION_STAGE_FILTER_OPTIONS,
      channelOptions: CONSULTATION_CHANNEL_OPTIONS,
      chiefProjectOptions: CHIEF_PROJECT_OPTIONS,
      intentOptions: INTENT_LEVEL_OPTIONS,
      handlingResultOptions: HANDLING_RESULT_OPTIONS,
      filters: createDefaultFilters()
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
    canCreate() {
      return this.normalizedRole === 'admin' || this.normalizedRole === 'nurse'
    },
    canExport() {
      return this.normalizedRole === 'admin' || this.normalizedRole === 'nurse'
    },
    activeConsultationStage() {
      return detectConsultationStageFilter(this.filters.handlingResult, this.filters.hasDeal)
    }
  },
  mounted() {
    this.currentUser = getAdminSession() || {}
    this.applyRouteQuickFilter()
    this.loadCreators()
    this.loadQuickPendingCount()
    this.loadTodayFollowupCount()
    this.loadList()
    onConsultationSaved(this.handleExternalSaved)
  },
  beforeDestroy() {
    offConsultationSaved(this.handleExternalSaved)
  },
  methods: {
    handleExternalSaved() {
      this.loadQuickPendingCount()
      this.loadTodayFollowupCount()
      this.loadList()
    },
    applyRouteQuickFilter() {
      if (this.$route && this.$route.query) {
        if (this.$route.query.quickHighIntent === '1') {
          this.applyQuickHighIntentFilter()
        }
        if (this.$route.query.todayFollowup === '1') {
          this.applyTodayFollowupFilter()
        }
      }
    },
    formatMoney(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    intentTagType(value) {
      if (value === '高') return 'danger'
      if (value === '中') return 'warning'
      return 'info'
    },
    handlingResultTagType(value) {
      if (value === '已成交') return 'success'
      if (value === '已预约到店') return 'success'
      if (value === '待跟进') return 'warning'
      return 'info'
    },
    tableHeaderStyle() {
      return {
        backgroundColor: '#f5f7fb',
        color: '#4b5563',
        fontWeight: '600',
        fontSize: '12px',
        padding: '8px 0'
      }
    },
    isOverdue(dateStr) {
      if (!dateStr) return false
      return new Date(dateStr) < new Date()
    },
    aiScoreTagType(score) {
      const num = Number(score || 0)
      if (num >= 80) return 'danger'
      if (num >= 50) return 'warning'
      return 'info'
    },
    canEditRecord(row) {
      if (!(this.normalizedRole === 'admin' || this.normalizedRole === 'nurse')) return false
      if (this.normalizedRole === 'admin') return true
      return Number(row && row.created_by) === Number(this.currentUser && this.currentUser.id)
    },
    handleRangePresetChange(value) {
      if (value !== 'custom') {
        this.filters.range = []
      }
    },
    applyConsultationStageFilter(stage) {
      const nextStage = stage === this.activeConsultationStage && stage !== 'all' ? 'all' : stage
      const mappedFilters = resolveConsultationStageFilters(nextStage)
      this.filters.handlingResult = mappedFilters.handlingResult
      this.filters.hasDeal = mappedFilters.hasDeal
      this.currentPage = 1
      this.loadList()
    },
    buildQueryParams(page = this.currentPage, size = this.pageSize) {
      const params = {
        page,
        size,
        keyword: String(this.filters.keyword || '').trim() || undefined,
        rangePreset: this.filters.rangePreset || undefined,
        channel: this.filters.channel || undefined,
        chiefProject: this.filters.chiefProject || undefined,
        intentLevel: this.filters.intentLevel || undefined,
        handlingResult: this.filters.handlingResult || undefined,
        createdBy: this.filters.createdBy || undefined
      }
      if (this.filters.hasDeal === true || this.filters.hasDeal === false) {
        params.hasDeal = this.filters.hasDeal
      }
      if (this.filters.rangePreset === 'custom' && Array.isArray(this.filters.range) && this.filters.range.length === 2) {
        params.startTime = this.filters.range[0]
        params.endTime = this.filters.range[1]
      }
      return params
    },
    loadCreators() {
      axios.get('/accounts/search', { params: { page: 1, size: 1000 } }).then(response => {
        const data = response.data && response.data.data ? response.data.data : {}
        this.creatorOptions = Array.isArray(data.list) ? data.list.map(item => ({
          id: item.id,
          name: item.name || item.username || `成员${item.id}`
        })) : []
      }).catch(() => {
        this.creatorOptions = []
      })
    },
    loadQuickPendingCount() {
      const now = new Date()
      const start = new Date(now.getTime() - (7 * 24 * 60 * 60 * 1000))
      const startText = this.formatDateTime(start)
      const endText = this.formatDateTime(now)
      axios.get('/consultations/search', {
        params: {
          page: 1,
          size: 1,
          intentLevel: '高',
          handlingResult: '待跟进',
          startTime: startText,
          endTime: endText,
          rangePreset: 'custom'
        }
      }).then(response => {
        const data = response.data && response.data.data ? response.data.data : {}
        this.highIntentPendingCount = Number(data.total || 0)
      }).catch(() => {
        this.highIntentPendingCount = 0
      })
    },
    loadTodayFollowupCount() {
      const now = new Date()
      const start = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0)
      const end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
      axios.get('/consultations/search', {
        params: {
          page: 1,
          size: 1,
          handlingResult: '待跟进',
          startTime: this.formatDateTime(start),
          endTime: this.formatDateTime(end),
          rangePreset: 'custom'
        }
      }).then(response => {
        const data = response.data && response.data.data ? response.data.data : {}
        this.todayFollowupCount = Number(data.total || 0)
      }).catch(() => {
        this.todayFollowupCount = 0
      })
    },
    applyTodayFollowupFilter() {
      const now = new Date()
      const start = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0)
      const end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
      this.filters.handlingResult = '待跟进'
      this.filters.rangePreset = 'custom'
      this.filters.range = [this.formatDateTime(start), this.formatDateTime(end)]
      this.currentPage = 1
      this.$router.replace({ path: this.$route.path, query: { todayFollowup: '1' } }).catch(() => {})
      this.loadList()
    },
    loadList() {
      this.loading = true
      axios.get('/consultations/search', { params: this.buildQueryParams() }).then(response => {
        const data = response.data && response.data.data ? response.data.data : {}
        this.rows = Array.isArray(data.list) ? data.list : []
        this.totalItems = Number(data.total || 0)
      }).catch(error => {
        console.error('Error loading consultations:', error)
        this.rows = []
        this.totalItems = 0
        showApiError(this, '加载咨询记录', error)
      }).finally(() => {
        this.loading = false
      })
    },
    resetFilters() {
      this.filters = createDefaultFilters()
      this.currentPage = 1
      this.loadList()
    },
    applyQuickHighIntentFilter() {
      const now = new Date()
      const start = new Date(now.getTime() - (7 * 24 * 60 * 60 * 1000))
      this.filters.intentLevel = '高'
      this.filters.handlingResult = '待跟进'
      this.filters.rangePreset = 'custom'
      this.filters.range = [this.formatDateTime(start), this.formatDateTime(now)]
      this.currentPage = 1
      this.$router.replace({ path: this.$route.path, query: { quickHighIntent: '1' } }).catch(() => {})
      this.loadList()
    },
    formatDateTime(date) {
      const d = new Date(date)
      if (Number.isNaN(d.getTime())) return ''
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hour = String(d.getHours()).padStart(2, '0')
      const minute = String(d.getMinutes()).padStart(2, '0')
      const second = String(d.getSeconds()).padStart(2, '0')
      return `${y}-${m}-${day} ${hour}:${minute}:${second}`
    },
    openCreateDialog() {
      this.dialogMode = 'create'
      this.activeRecord = {}
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.dialogMode = 'edit'
      this.activeRecord = Object.assign({}, row)
      this.dialogVisible = true
    },
    openDetailDialog(row) {
      this.dialogMode = 'detail'
      this.activeRecord = Object.assign({}, row)
      this.dialogVisible = true
    },
    handleDialogSaved() {
      this.loadQuickPendingCount()
      this.loadList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadList()
    },
    handleCurrentChange(page) {
      this.currentPage = page
      this.loadList()
    },
    async exportExcel() {
      try {
        const response = await axios.get('/consultations/search', {
          params: this.buildQueryParams(1, 2000)
        })
        const data = response.data && response.data.data ? response.data.data : {}
        const rows = Array.isArray(data.list) ? data.list : []
        const headers = ['咨询时间', '渠道', '姓名/昵称', '联系方式', '主诉项目', '意向强度', '处理结果', '是否成交', '累计成交金额', '预计金额', '下次跟进', '跟进次数', 'AI评分', '录入人']
        const body = rows.map(item => [
          item.consultation_time || '',
          item.consultation_channel || '',
          item.contact_name || item.patient_name || '',
          item.contact_phone || '',
          item.chief_project || '',
          item.intent_level || '',
          item.handling_result || '',
          item.deal_at ? '已成交' : '未成交',
          this.formatMoney(item.total_deal_amount),
          this.formatMoney(item.estimated_amount),
          item.next_followup_time || '',
          item.followup_count || 0,
          item.ai_analysis_score || '',
          item.created_by_name || ''
        ])
        const worksheet = XLSX.utils.aoa_to_sheet([headers, ...body])
        const workbook = XLSX.utils.book_new()
        XLSX.utils.book_append_sheet(workbook, worksheet, '咨询记录')
        XLSX.writeFile(workbook, 'consultation-records.xlsx')
      } catch (error) {
        this.$message.error('导出失败')
      }
    }
  }
}
</script>

<style scoped>
.consultation-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.clickable {
  cursor: pointer;
}

.mini-stat {
  min-width: 156px;
  padding: 10px 14px;
  border-radius: 14px;
  background: #f8fafc;
}

.mini-stat.warn {
  background: #fff7ed;
}

.mini-stat.info {
  background: #eff6ff;
}

.mini-num {
  color: #0f172a;
  font-size: 22px;
  font-weight: 700;
}

.mini-label {
  color: #64748b;
  font-size: 12px;
}

.status-filter-row {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.status-filter-label {
  color: #475569;
  font-size: 13px;
  font-weight: 600;
}

.status-filter-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.status-filter-chip {
  border: 1px solid #dbe4f0;
  background: #f8fafc;
  color: #475569;
  border-radius: 999px;
  padding: 8px 16px;
  font-size: 13px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s ease;
}

.status-filter-chip:hover {
  border-color: #93c5fd;
  color: #2563eb;
}

.status-filter-chip.is-active {
  background: #2563eb;
  border-color: #2563eb;
  color: #fff;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.18);
}

.query-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.second-row {
  margin-top: 12px;
}

.query-input {
  width: 260px;
}

.query-select {
  width: 150px;
}

.range-picker {
  width: 360px;
}

.deal-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.consultation-table {
  width: 100%;
}

.consultation-table.el-table--border {
  border-color: #ebeef5;
}

.consultation-table.el-table--border th,
.consultation-table.el-table--border td {
  border-color: #ebeef5;
}

.deal-text {
  color: #0f766e;
  font-weight: 600;
}

.overdue-text {
  color: #dc2626;
  font-weight: 600;
}

.deal-sub {
  color: #64748b;
  font-size: 12px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .status-filter-row {
    align-items: flex-start;
  }

  .query-input,
  .query-select,
  .range-picker {
    width: 100%;
  }
}
</style>
