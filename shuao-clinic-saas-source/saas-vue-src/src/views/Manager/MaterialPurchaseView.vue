<template>
  <div class="material-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">耗材采购</div>
        <h2>采购记录</h2>
        <p>录入采购单后自动增加库存，并同步写入耗材采购支出。</p>
      </div>
      <div class="hero-actions">
        <el-button v-if="canCreate" type="primary" plain @click="openDialog()">新建采购单</el-button>
        <el-button @click="loadList">刷新</el-button>
      </div>
    </div>

    <el-card shadow="never" class="query-card">
      <div class="query-row">
        <el-input
          v-model="filters.supplierKeyword"
          class="query-input"
          clearable
          placeholder="按供应商搜索"
          @keyup.enter.native="loadList"
        />
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          value-format="yyyy-MM-dd"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="range-picker"
        />
        <el-select v-model="filters.status" clearable class="query-select" placeholder="状态">
          <el-option v-for="item in MATERIAL_PURCHASE_STATUS_OPTIONS" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="loadList">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        :data="rows"
        stripe
        v-loading="loading"
        :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }"
      >
        <el-table-column prop="purchase_date" label="采购日期" width="110">
          <template slot-scope="scope">{{ formatDate(scope.row.purchase_date) }}</template>
        </el-table-column>
        <el-table-column prop="supplier_name" label="供应商" min-width="150" />
        <el-table-column label="总金额" width="110">
          <template slot-scope="scope">¥{{ formatMaterialMoney(scope.row.total_amount) }}</template>
        </el-table-column>
        <el-table-column prop="payment_method" label="付款方式" width="100" />
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="purchaseStatusTagType(scope.row.status)">{{ scope.row.status || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_by_name" label="录入人" width="100" />
        <el-table-column prop="created_at" label="创建时间" min-width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.created_at) }}</template>
        </el-table-column>
        <el-table-column label="明细数" width="80" align="right">
          <template slot-scope="scope">{{ Array.isArray(scope.row.items) ? scope.row.items.length : 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="290">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="openDetail(scope.row)">详情</el-button>
            <el-button v-if="canCreate && canEditPurchase(scope.row)" type="text" size="mini" @click="openDialog(scope.row)">编辑</el-button>
            <el-button
              v-if="canVoid && scope.row.status === '有效'"
              type="text"
              size="mini"
              style="color:#ef4444"
              @click="voidPurchase(scope.row)"
            >作废</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !rows.length" description="暂无采购单"></el-empty>
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

    <MaterialPurchaseDialog
      :visible.sync="dialogVisible"
      :purchase="activePurchase"
      :categories="categories"
      :current-user="currentUser"
      :readonly="dialogReadonly"
      :invoice-locked="dialogInvoiceLocked"
      @saved="loadList"
    />
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'
import MaterialPurchaseDialog from '@/components/MaterialPurchaseDialog.vue'
import { MATERIAL_PURCHASE_STATUS_OPTIONS, canCreateMaterialPurchases, canVoidMaterialPurchases, formatMaterialMoney, normalizeMaterialRole, purchaseStatusTagType } from '@/utils/materialConstants'
import { showApiError } from '@/utils/errorMessage'
export default {
  name: 'MaterialPurchaseView',
  components: { MaterialPurchaseDialog },
  data() {
    return {
      MATERIAL_PURCHASE_STATUS_OPTIONS,
      currentUser: getAdminSession() || {},
      categories: [],
      rows: [],
      loading: false,
      totalItems: 0,
      currentPage: 1,
      pageSize: 10,
      filters: {
        supplierKeyword: '',
        dateRange: [],
        status: ''
      },
      dialogVisible: false,
      dialogReadonly: false,
      dialogInvoiceLocked: false,
      activePurchase: {}
    }
  },
  computed: {
    canCreate() {
      return canCreateMaterialPurchases(normalizeMaterialRole(this.currentUser && this.currentUser.role))
    },
    canVoid() {
      return canVoidMaterialPurchases(normalizeMaterialRole(this.currentUser && this.currentUser.role))
    }
  },
  mounted() {
    this.loadCategories()
    this.loadList()
  },
  methods: {
    formatMaterialMoney,
    purchaseStatusTagType,
    formatDate(value) {
      return value ? String(value).slice(0, 10) : ''
    },
    formatDateTime(value) {
      return value ? String(value).slice(0, 19).replace('T', ' ') : ''
    },
    async loadCategories() {
      try {
        const res = await axios.get('/material-categories/tree', { params: { includeDisabled: false } })
        this.categories = Array.isArray(res.data.data) ? res.data.data : []
      } catch (error) {
        this.categories = []
      }
    },
    async loadList() {
      this.loading = true
      const range = Array.isArray(this.filters.dateRange) ? this.filters.dateRange : []
      try {
        const res = await axios.get('/material-purchases/search', {
          params: {
            supplierKeyword: this.filters.supplierKeyword || undefined,
            startDate: range[0] || undefined,
            endDate: range[1] || undefined,
            status: this.filters.status || undefined,
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
        showApiError(this, '获取采购记录', error)
      } finally {
        this.loading = false
      }
    },
    resetFilters() {
      this.filters = { supplierKeyword: '', dateRange: [], status: '' }
      this.currentPage = 1
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
    openDialog(row) {
      this.dialogReadonly = false
      this.dialogInvoiceLocked = !!(row && !this.canEditPurchase(row))
      this.activePurchase = row ? Object.assign({}, row) : {}
      this.dialogVisible = true
    },
    openDetail(row) {
      this.$router.push({ path: `/material-purchases/${row.id}` }).catch(() => {})
    },
    canEditPurchase(row) {
      if (!row || row.status !== '有效' || !this.canCreate) return false
      const createdDate = row.created_at ? String(row.created_at).slice(0, 10) : ''
      const today = new Date()
      const y = today.getFullYear()
      const m = String(today.getMonth() + 1).padStart(2, '0')
      const d = String(today.getDate()).padStart(2, '0')
      return createdDate === `${y}-${m}-${d}`
    },
    voidPurchase(row) {
      this.$confirm(`确认作废采购单”${row.supplier_name || '未填写供应商'} / ${this.formatDate(row.purchase_date)}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.post(`/material-purchases/void/${row.id}`, {
          voided_by: this.currentUser && this.currentUser.id ? Number(this.currentUser.id) : null,
          voided_by_name: this.currentUser && this.currentUser.name ? this.currentUser.name : '',
          remark: '前端手动作废'
        })
        if (res.data.code === '200') {
          this.$message.success('采购单已作废')
          this.loadList()
        } else {
          this.$message.error(res.data.msg || '作废失败')
        }
      }).catch(() => {})
    },
  }
}
</script>

<style scoped>
.material-page { display:flex; flex-direction:column; gap:14px; }
.hero-card { display:flex; justify-content:space-between; align-items:flex-start; gap:16px; padding:18px; border-radius:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.hero-actions { display:flex; gap:10px; flex-wrap:wrap; }
.query-card,.table-card { border-radius:18px; }
.query-row { display:flex; gap:12px; flex-wrap:wrap; }
.query-input { width:240px; }
.query-select { width:160px; }
.range-picker { width:260px; }
.pagination-row { display:flex; justify-content:flex-end; padding-top:16px; }
@media (max-width: 768px) {
  .hero-card { flex-direction:column; }
  .query-input,.query-select,.range-picker { width:100%; }
}
</style>
