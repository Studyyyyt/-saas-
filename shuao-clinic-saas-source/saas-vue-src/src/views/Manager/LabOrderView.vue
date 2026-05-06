<template>
  <div class="lab-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">义齿加工</div>
        <h2>加工订单</h2>
        <p>统一记录义齿下单、加工、收货与对账状态，支撑后续账单核对。</p>
      </div>
      <div class="hero-actions">
        <el-button v-if="canEditOrders" type="primary" plain @click="openCreateDialog">新建订单</el-button>
        <el-button v-if="canEditOrders" @click="batchDialogVisible = true" :disabled="!selectedRows.length">批量改状态</el-button>
        <el-button @click="loadList">刷新</el-button>
      </div>
    </div>

    <el-alert
      v-if="showPendingLabTip"
      title="当前从“待登记加工”入口进入。新建订单时请优先选择“关联病历操作”，口径基于病历操作记录。"
      type="warning"
      :closable="false"
      show-icon
    />

    <el-card shadow="never" class="query-card">
      <div class="query-row">
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
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          value-format="yyyy-MM-dd"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="range-picker"
        />
      </div>
      <div class="query-row">
        <el-input
          v-model="filters.keyword"
          class="query-input"
          clearable
          placeholder="搜索患者姓名 / 手机号"
          @keyup.enter.native="loadList"
        />
        <el-button type="primary" icon="el-icon-search" @click="loadList">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        :data="rows"
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
        :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }"
      >
        <el-table-column v-if="canEditOrders" type="selection" width="48" />
        <el-table-column prop="factory_name" label="加工厂" min-width="140" />
        <el-table-column prop="patient_name" label="患者" min-width="120" />
        <el-table-column label="关联操作" min-width="180" show-overflow-tooltip>
          <template slot-scope="scope">
            {{ [scope.row.project_name, scope.row.operation_name, scope.row.tooth_positions ? `牙位:${scope.row.tooth_positions}` : ''].filter(Boolean).join('｜') || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="product_name" label="产品" min-width="140" />
        <el-table-column prop="product_spec" label="规格" min-width="120" />
        <el-table-column label="单价" width="90">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.unit_price) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="总金额" width="100">
          <template slot-scope="scope">¥{{ formatMoney(scope.row.total_amount) }}</template>
        </el-table-column>
        <el-table-column prop="order_date" label="下单日期" width="110">
          <template slot-scope="scope">{{ formatDate(scope.row.order_date) }}</template>
        </el-table-column>
        <el-table-column prop="expected_delivery_date" label="预计完成" width="110">
          <template slot-scope="scope">{{ formatDate(scope.row.expected_delivery_date) || '-' }}</template>
        </el-table-column>
        <el-table-column prop="actual_delivery_date" label="实际收货" width="110">
          <template slot-scope="scope">{{ formatDate(scope.row.actual_delivery_date) || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="orderStatusTagType(scope.row.status)">{{ scope.row.status || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="220">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="openDetailDialog(scope.row)">详情</el-button>
            <el-button v-if="canEditOrders" type="text" size="mini" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button v-if="canEditOrders && scope.row.status !== '已对账'" type="text" size="mini" style="color:#ef4444" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog title="批量更新状态" :visible.sync="batchDialogVisible" width="420px">
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
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'
import LabOrderDialog from '@/components/LabOrderDialog.vue'
import { showApiError } from '@/utils/errorMessage'
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
        dateRange: []
      },
      dialogVisible: false,
      dialogMode: 'create',
      activeOrder: {},
      selectedRows: [],
      batchDialogVisible: false,
      batchForm: defaultBatchForm(),
      batchSaving: false
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
    }
  },
  mounted() {
    this.initializePage()
  },
  methods: {
    async initializePage() {
      this.applyRoutePrefill()
      await this.loadFactories()
      await this.loadList()
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
    formatMoney,
    orderStatusTagType,
    formatDate(value) {
      return value ? String(value).slice(0, 10) : ''
    },
    formatPatientLabel(item) {
      const phone = item && item.phone ? ` / ${item.phone}` : ''
      return `${item.name || '未命名患者'}${phone}`
    },
    async loadFactories() {
      try {
        const res = await axios.get('/lab-factories/selectEnabled')
        this.factories = Array.isArray(res.data.data) ? res.data.data : []
      } catch (error) {
        this.factories = []
      }
    },
    async loadList() {
      this.loading = true
      const range = Array.isArray(this.filters.dateRange) ? this.filters.dateRange : []
      try {
        const res = await axios.get('/lab-orders/search', {
          params: {
            factoryId: this.filters.factoryId || undefined,
            status: this.filters.status || undefined,
            patientId: this.filters.patientId || undefined,
            keyword: this.filters.keyword || undefined,
            startDate: range[0] || undefined,
            endDate: range[1] || undefined,
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
        showApiError(this, '获取技工订单', error)
      } finally {
        this.loading = false
      }
    },
    resetFilters() {
      this.filters = { factoryId: '', status: '', patientId: '', keyword: '', dateRange: [] }
      this.currentPage = 1
      this.patientOptions = []
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
      this.loadList()
    },
    handleDelete(row) {
      this.$confirm(`确认删除订单“${row.patient_name} / ${row.product_name}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.delete(`/lab-orders/delete/${row.id}`)
        if (res.data.code === '200') {
          this.$message.success('删除成功')
          this.loadList()
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
          this.loadList()
        } else {
          this.$message.error(res.data.msg || '批量更新失败')
        }
      } catch (error) {
        this.$message.error('批量更新失败')
      } finally {
        this.batchSaving = false
      }
    }
  }
}
</script>

<style scoped>
.lab-page { display:flex; flex-direction:column; gap:14px; }
.hero-card { display:flex; justify-content:space-between; align-items:center; gap:16px; padding:18px; border-radius:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.hero-actions { display:flex; gap:10px; flex-wrap:wrap; }
.query-card,.table-card { border-radius:18px; }
.query-row { display:flex; gap:12px; flex-wrap:wrap; }
.query-input { width:260px; }
.query-select { width:180px; }
.query-select.wide { width:260px; }
.range-picker { width:280px; }
.pagination-row { display:flex; justify-content:flex-end; padding-top:16px; }
@media (max-width: 768px) {
  .hero-card { flex-direction:column; align-items:flex-start; }
  .query-input,.query-select,.query-select.wide,.range-picker { width:100%; }
  .pagination-row { justify-content:center; }
}
</style>
